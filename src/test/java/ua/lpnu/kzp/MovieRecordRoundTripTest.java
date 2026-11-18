package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MovieRecordRoundTripTest {

    @TempDir
    Path tempDir;

    private static List<MovieRecord> restore(Path file) throws Exception {
        List<List<String>> rows = CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8));
        List<MovieRecord> restored = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            restored.add(MovieRecord.fromCsv(rows.get(i)));
        }
        return restored;
    }

    @Test
    void shouldRestoreMovieAfterCsvExportAndParsing() throws Exception {
        Path file = tempDir.resolve("movies.csv");

        List<MovieRecord> original = List.of(
                new MovieRecord("Inception", "Christopher Nolan", 2010, 148, 8.8),
                new MovieRecord("Interstellar", "Christopher Nolan", 2014, 169, 8.7),
                new MovieRecord("The Matrix", "Lana Wachowski", 1999, 136, 8.7),
                new MovieRecord("Тіні забутих предків", "Сергій Параджанов", 1965, 97, 8.2),
                new MovieRecord("Наша планета", "Алістер Фотергілл", 2019, 50, 9.3)
        );

        CsvExporter.write(file, MovieRecord.class, original);
        List<MovieRecord> restored = restore(file);

        assertEquals(original, restored);
        assertEquals(
                original.stream().map(r -> r.getTitle() + "|" + r.getYear()).toList(),
                restored.stream().map(r -> r.getTitle() + "|" + r.getYear()).toList());
    }

    @Test
    void shouldPreserveMovieWithSpecialCharacters() throws Exception {
        Path file = tempDir.resolve("special-movie.csv");

        MovieRecord original = new MovieRecord(
                "Film, \"The Best\"\nSecond line", "Test Director", 2020, 120, 9.0);

        CsvExporter.write(file, MovieRecord.class, List.of(original));

        assertEquals(List.of(original), restore(file));
    }

    @Test
    void shouldRestoreEmptyExportAsHeaderOnly() throws Exception {
        Path file = tempDir.resolve("empty.csv");

        CsvExporter.write(file, MovieRecord.class, List.of());

        assertTrue(restore(file).isEmpty());
    }

    @Test
    void zeroLengthFileHasNoRecords() throws Exception {
        Path file = tempDir.resolve("zero.csv");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        assertTrue(CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8)).isEmpty());
    }

    @Test
    void fromCsvLineShouldParseQuotedTitle() throws Exception {
        MovieRecord restored =
                MovieRecord.fromCsvLine("Режисер,100,8.5,\"Назва, з комою\",1999");

        assertEquals(new MovieRecord("Назва, з комою", "Режисер", 1999, 100, 8.5), restored);
    }

    @Test
    void shouldRejectWrongFieldCount() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> MovieRecord.fromCsv(List.of("a", "b")));

        assertTrue(exception.getMessage().contains("5 полів"));
        assertNull(exception.getCause());
    }

    @Test
    void shouldRejectNullFields() {
        assertThrows(DataStorageException.class, () -> MovieRecord.fromCsv(null));
    }

    @Test
    void shouldKeepNumberFormatExceptionAsCause() {
        DataStorageException badInt = assertThrows(DataStorageException.class,
                () -> MovieRecord.fromCsv(List.of("Режисер", "abc", "8.0", "Назва", "2000")));
        DataStorageException badDouble = assertThrows(DataStorageException.class,
                () -> MovieRecord.fromCsv(List.of("Режисер", "100", "x", "Назва", "2000")));

        assertInstanceOf(NumberFormatException.class, badInt.getCause());
        assertInstanceOf(NumberFormatException.class, badDouble.getCause());
    }

    @Test
    void shouldKeepDomainValidationErrorAsCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> MovieRecord.fromCsv(List.of("Режисер", "100", "11.0", "Назва", "2000")));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void shouldRejectBrokenLine() {
        assertThrows(DataStorageException.class,
                () -> MovieRecord.fromCsvLine("Режисер,100,8.5"));
    }

    @Test
    void constructorShouldValidateModel() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovieRecord(" ", "Режисер", 2000, 100, 5.0));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieRecord("Назва", "Режисер", 0, 100, 5.0));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieRecord("Назва", "Режисер", 2000, 0, 5.0));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieRecord("Назва", "Режисер", 2000, 100, Double.NaN));
        assertThrows(NullPointerException.class,
                () -> new MovieRecord(null, "Режисер", 2000, 100, 5.0));
    }

    @Test
    void equalRecordsHaveEqualHashCodes() {
        MovieRecord first = new MovieRecord("Назва", "Режисер", 2000, 100, 5.0);
        MovieRecord second = new MovieRecord("Назва", "Режисер", 2000, 100, 5.0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}