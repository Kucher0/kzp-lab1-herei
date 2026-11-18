package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvExporterTest {

    private static final List<String> HEADER =
            List.of("Режисер", "Тривалість", "Оцінка", "Назва", "Рік");

    @TempDir
    Path tempDir;

    private static MovieRecord sample(String title) {
        return new MovieRecord(title, "Режисер", 2020, 100, 8.0);
    }

    @Test
    void shouldExportMoviesWithCorrectHeader() throws Exception {
        Path file = tempDir.resolve("movies.csv");

        CsvExporter.write(file, MovieRecord.class, List.of(
                new MovieRecord("Inception", "Christopher Nolan", 2010, 148, 8.8)));

        List<List<String>> rows = CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8));

        assertEquals(HEADER, rows.get(0));
        assertEquals(2, rows.size());
    }

    @Test
    void shouldExportCommasQuotesAndMultilineText() throws Exception {
        Path file = tempDir.resolve("special.csv");

        CsvExporter.write(file, MovieRecord.class, List.of(
                sample("Film, \"The Best\"\nSecond line")));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(content);

        assertTrue(content.contains("\"Film, \"\"The Best\"\"\nSecond line\""));
        assertEquals("Film, \"The Best\"\nSecond line", rows.get(1).get(3));
    }

    @Test
    void shouldExportHeaderForEmptyList() throws Exception {
        Path file = tempDir.resolve("empty.csv");

        CsvExporter.write(file, MovieRecord.class, List.of());

        List<List<String>> rows = CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8));

        assertEquals(List.of(HEADER), rows);
    }

    @Test
    void shouldWriteUtf8WithoutBom() throws Exception {
        Path file = tempDir.resolve("utf8.csv");

        CsvExporter.write(file, MovieRecord.class, List.of(sample("Тіні забутих предків")));

        byte[] bytes = Files.readAllBytes(file);
        String text = new String(bytes, StandardCharsets.UTF_8);

        assertNotEquals((byte) 0xEF, bytes[0]);
        assertTrue(text.startsWith("Режисер,Тривалість,Оцінка,Назва,Рік"));
        assertTrue(text.contains("Тіні забутих предків"));
    }

    @Test
    void shouldTruncateExistingFile() throws Exception {
        Path file = tempDir.resolve("truncate.csv");

        CsvExporter.write(file, MovieRecord.class,
                List.of(sample("Один"), sample("Два"), sample("Три")));
        CsvExporter.write(file, MovieRecord.class, List.of(sample("Єдиний")));

        List<List<String>> rows = CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8));

        assertEquals(2, rows.size());
        assertEquals("Єдиний", rows.get(1).get(3));
    }

    @Test
    void shouldWrapFileErrorWhenPathIsDirectory() {
        DataStorageException exception = assertThrows(
                DataStorageException.class,
                () -> CsvExporter.write(tempDir, MovieRecord.class, List.of(sample("A"))));

        assertInstanceOf(IOException.class, exception.getCause());
        assertTrue(exception.getMessage().contains("Не вдалося записати CSV-файл"));
    }

    @Test
    void shouldWrapFileErrorWhenParentIsFile() throws Exception {
        Path plain = tempDir.resolve("plain.txt");
        Files.writeString(plain, "x", StandardCharsets.UTF_8);
        Path target = plain.resolve("out.csv");

        DataStorageException exception = assertThrows(
                DataStorageException.class,
                () -> CsvExporter.write(target, MovieRecord.class, List.of()));

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void shouldRejectNullRecord() {
        List<MovieRecord> records = new ArrayList<>();
        records.add(null);

        assertThrows(DataStorageException.class,
                () -> CsvExporter.write(tempDir.resolve("null.csv"), MovieRecord.class, records));
    }

    @Test
    void shouldRejectNullArguments() {
        Path file = tempDir.resolve("args.csv");

        assertThrows(NullPointerException.class,
                () -> CsvExporter.write(null, MovieRecord.class, List.of()));
        assertThrows(NullPointerException.class,
                () -> CsvExporter.write(file, null, List.of()));
        assertThrows(NullPointerException.class,
                () -> CsvExporter.write(file, MovieRecord.class, null));
    }
}