package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Перевіряє кодування та роздільники рядків CSV. Тест виконується в CI
 * на Ubuntu, Windows і macOS, тому доводить однакову поведінку на різних ОС.
 */
class CsvPortabilityTest {

    @TempDir
    Path tempDir;

    private static List<MovieRecord> sample() {
        return List.of(
                new MovieRecord("Інтерстеллар", "Крістофер Нолан", 2014, 169, 8.7),
                new MovieRecord("Тіні забутих предків", "Сергій Параджанов", 1965, 97, 8.2),
                new MovieRecord("Наша планета", "Алістер Фотергілл", 2019, 50, 9.3),
                new MovieRecord("Фільм, \"Особливий\" випадок", "Тестовий Режисер", 2020, 120, 9.0));
    }

    private static List<MovieRecord> restore(String csv) throws Exception {
        List<List<String>> rows = CsvParser.parseDocument(csv);
        List<MovieRecord> restored = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            restored.add(MovieRecord.fromCsv(rows.get(i)));
        }
        return restored;
    }

    @Test
    void exportedBytesAreStrictUtf8WithoutBom() throws Exception {
        Path file = tempDir.resolve("utf8.csv");

        CsvExporter.write(file, MovieRecord.class, sample());
        byte[] bytes = Files.readAllBytes(file);

        // Суворе декодування: будь-яка не-UTF-8 послідовність дасть виняток.
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        String text = decoder.decode(ByteBuffer.wrap(bytes)).toString();

        boolean hasBom = bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF;
        assertFalse(hasBom, "UTF-8 BOM не повинен записуватися");
        // 'Р' у UTF-8 — байти D0 A0 (у windows-1251 це був би один байт D0).
        assertEquals((byte) 0xD0, bytes[0]);
        assertEquals((byte) 0xA0, bytes[1]);
        assertTrue(text.startsWith("Режисер,Тривалість,Оцінка,Назва,Рік"));
    }

    @Test
    void rowTerminatorsEqualPlatformLineSeparator() throws Exception {
        Path file = tempDir.resolve("separators.csv");

        CsvExporter.write(file, MovieRecord.class, sample());
        String content = Files.readString(file, StandardCharsets.UTF_8);
        String separator = System.lineSeparator();

        assertTrue(content.endsWith(separator));
        // Після видалення очікуваних роздільників не має лишитися жодного CR/LF.
        String withoutSeparators = content.replace(separator, "");
        assertFalse(withoutSeparators.contains("\r"));
        assertFalse(withoutSeparators.contains("\n"));
        // Один заголовок + по одному роздільнику на кожен запис.
        int separatorCount =
                (content.length() - withoutSeparators.length()) / separator.length();
        assertEquals(sample().size() + 1, separatorCount);
    }

    @Test
    void parserRestoresSameRecordsFromLfAndCrLfFiles() throws Exception {
        Path file = tempDir.resolve("eol.csv");

        CsvExporter.write(file, MovieRecord.class, sample());
        String unix = Files.readString(file, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        String windows = unix.replace("\n", "\r\n");

        assertEquals(sample(), restore(unix));
        assertEquals(sample(), restore(windows));
        assertEquals(CsvParser.parseDocument(unix), CsvParser.parseDocument(windows));
    }

    @Test
    void multilineFieldSurvivesRoundTripOnAnyPlatform() throws Exception {
        Path file = tempDir.resolve("multiline.csv");
        List<MovieRecord> original = List.of(
                new MovieRecord("Перший рядок\nДругий рядок", "Режисер", 2001, 90, 7.0));

        CsvExporter.write(file, MovieRecord.class, original);

        assertEquals(original,
                restore(Files.readString(file, StandardCharsets.UTF_8)));
    }

    @Test
    void exportOfInputLab05MatchesDocumentedExample() throws Exception {
        List<String> errors = new ArrayList<>();
        List<Movie> movies = CatalogService.parse(
                Files.readAllLines(Path.of("data", "input_lab05.csv"),
                        StandardCharsets.UTF_8),
                errors);
        List<MovieRecord> records = new ArrayList<>();
        for (Movie movie : movies) {
            records.add(new MovieRecord(movie.getTitle(), movie.getDirector(),
                    movie.getYear(), movie.getMinutes(), movie.getRating()));
        }
        Path file = tempDir.resolve("lab05.csv");

        CsvExporter.write(file, MovieRecord.class, records);

        String actual = Files.readString(file, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        String expected = Files.readString(
                        Path.of("docs", "expected_csv_lab05.csv"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        assertEquals(1, errors.size());
        assertEquals(expected, actual);
    }
}