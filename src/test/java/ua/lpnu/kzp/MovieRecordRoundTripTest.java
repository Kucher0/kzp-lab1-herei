package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieRecordRoundTripTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRestoreMovieAfterCsvExportAndParsing() throws Exception {
        Path file = tempDir.resolve("movies.csv");

        List<MovieRecord> original = List.of(
                new MovieRecord("Inception", "Christopher Nolan", 2010, 148, 8.8),
                new MovieRecord("Interstellar", "Christopher Nolan", 2014, 169, 8.7),
                new MovieRecord("The Matrix", "Lana Wachowski", 1999, 136, 8.7)
        );

        CsvExporter.write(file, MovieRecord.class, original);

        String content = Files.readString(file);
        List<List<String>> rows = CsvParser.parseDocument(content);

        List<MovieRecord> restored = rows.stream()
                .skip(1)
                .map(row -> {
                    try {
                        return MovieRecord.fromCsv(row);
                    } catch (DataStorageException exception) {
                        throw new RuntimeException(exception);
                    }
                })
                .toList();

        assertEquals(original, restored);
    }

    @Test
    void shouldPreserveMovieWithSpecialCharacters() throws Exception {
        Path file = tempDir.resolve("special-movie.csv");

        MovieRecord original = new MovieRecord(
                "Film, \"The Best\"\nSecond line",
                "Test Director",
                2020,
                120,
                9.0
        );

        CsvExporter.write(file, MovieRecord.class, List.of(original));

        String content = Files.readString(file);
        List<List<String>> rows = CsvParser.parseDocument(content);

        MovieRecord restored = MovieRecord.fromCsv(rows.get(1));

        assertEquals(original, restored);
    }
}