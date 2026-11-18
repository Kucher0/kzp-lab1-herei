
package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldExportMoviesWithCorrectHeader() throws Exception {
        Path file = tempDir.resolve("movies.csv");

        MovieRecord movie = new MovieRecord(
                "Inception",
                "Christopher Nolan",
                2010,
                148,
                8.8
        );

        CsvExporter.write(file, MovieRecord.class, List.of(movie));

        String content = Files.readString(file);
        List<List<String>> rows = CsvParser.parseDocument(content);

        assertEquals(
                List.of("Режисер", "Тривалість", "Оцінка", "Назва", "Рік"),
                rows.get(0)
        );

        assertEquals(2, rows.size());
    }

    @Test
    void shouldExportCommasQuotesAndMultilineText() throws Exception {
        Path file = tempDir.resolve("special.csv");

        MovieRecord movie = new MovieRecord(
                "Film, \"The Best\"\nSecond line",
                "Test Director",
                2020,
                120,
                9.0
        );

        CsvExporter.write(file, MovieRecord.class, List.of(movie));

        String content = Files.readString(file);
        List<List<String>> rows = CsvParser.parseDocument(content);

        assertEquals(
                "Film, \"The Best\"\nSecond line",
                rows.get(1).get(3)
        );
    }

    @Test
    void shouldExportHeaderForEmptyList() throws Exception {
        Path file = tempDir.resolve("empty.csv");

        CsvExporter.write(file, MovieRecord.class, List.of());

        String content = Files.readString(file);
        List<List<String>> rows = CsvParser.parseDocument(content);

        assertEquals(1, rows.size());
        assertEquals(
                List.of("Режисер", "Тривалість", "Оцінка", "Назва", "Рік"),
                rows.get(0)
        );
    }

    @Test
    void shouldWrapFileErrorsInDataStorageException() {
        MovieRecord movie = new MovieRecord(
                "Inception",
                "Christopher Nolan",
                2010,
                148,
                8.8
        );

        assertThrows(
                DataStorageException.class,
                () -> CsvExporter.write(tempDir, MovieRecord.class, List.of(movie))
        );
    }
}
