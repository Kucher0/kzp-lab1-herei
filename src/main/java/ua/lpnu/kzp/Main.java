
package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Головний клас програми каталогу фільмів.
 *
 * <p>Програма читає дані, зберігає фільми у Repository,
 * експортує записи у CSV та перевіряє відновлення даних.</p>
 */
public class Main {

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            if (args[0].equals("--version")) {
                System.out.println("kzp-lab1-herei version 1.3.0");
                return;
            }

            if (args[0].equals("--help")) {
                System.out.println("""
                        Каталог фільмів — KZP Lab 1

                        Використання:
                          java -jar kzp-lab1-herei-1.3.0.jar
                          java -jar kzp-lab1-herei-1.3.0.jar --help
                          java -jar kzp-lab1-herei-1.3.0.jar --version
                          java -jar kzp-lab1-herei-1.3.0.jar --input <файл>
                          java -jar kzp-lab1-herei-1.3.0.jar --output <файл>
                          java -jar kzp-lab1-herei-1.3.0.jar --csv <файл>
                          java -jar kzp-lab1-herei-1.3.0.jar --scores
                          java -jar kzp-lab1-herei-1.3.0.jar --queries
                          java -jar kzp-lab1-herei-1.3.0.jar --find <назва>

                        Параметри:
                          --help              показати довідку
                          --version           показати версію
                          --input <файл>      вхідний файл
                          --output <файл>     файл текстового звіту
                          --csv <файл>        файл CSV-експорту
                          --scores            додати рекомендаційні бали
                          --queries           додати потокові запити
                          --find <назва>      знайти фільм за назвою
                        """);
                return;
            }
        }

        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");
        Path csvOutput = Path.of("out", "movies.csv");

        boolean showScores = false;
        boolean showQueries = false;
        String findTitle = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--scores")) {
                showScores = true;

            } else if (args[i].equals("--queries")) {
                showQueries = true;

            } else if (args[i].equals("--find")) {
                if (i + 1 >= args.length) {
                    System.err.println(
                            "Помилка: після --find потрібно вказати назву.");
                    return;
                }
                findTitle = args[++i];

            } else if (args[i].equals("--input")) {
                if (i + 1 >= args.length) {
                    System.err.println(
                            "Помилка: після --input потрібно вказати файл.");
                    return;
                }
                input = Path.of(args[++i]);

            } else if (args[i].equals("--output")) {
                if (i + 1 >= args.length) {
                    System.err.println(
                            "Помилка: після --output потрібно вказати файл.");
                    return;
                }
                output = Path.of(args[++i]);

            } else if (args[i].equals("--csv")) {
                if (i + 1 >= args.length) {
                    System.err.println(
                            "Помилка: після --csv потрібно вказати файл.");
                    return;
                }
                csvOutput = Path.of(args[++i]);
            }
        }

        try {
            List<String> lines = Files.readAllLines(
                    input,
                    StandardCharsets.UTF_8
            );

            List<String> errors = new ArrayList<>();
            List<Movie> parsedMovies = CatalogService.parse(lines, errors);

            Repository<Movie> movieRepository = new Repository<>();

            for (Movie movie : parsedMovies) {
                movieRepository.add(movie);
            }

            List<Movie> movies = movieRepository.all();

            String report = CatalogService.buildReport(
                    CatalogService.summarize(movies),
                    errors
            );

            if (showScores) {
                report += CatalogService.buildScoreSection(movies);
            }

            if (showQueries) {
                report += CatalogService.buildQueriesSection(movies);
            }

            if (findTitle != null) {
                report += CatalogService.buildSearchLine(movies, findTitle);
            }

            Repository<MovieRecord> recordRepository = new Repository<>();

            for (Movie movie : movies) {
                recordRepository.add(new MovieRecord(
                        movie.getTitle(),
                        movie.getDirector(),
                        movie.getYear(),
                        movie.getMinutes(),
                        movie.getRating()
                ));
            }

            List<MovieRecord> originalRecords = recordRepository.all();

            CsvExporter.write(
                    csvOutput,
                    MovieRecord.class,
                    originalRecords
            );

            String csvContent = Files.readString(
                    csvOutput,
                    StandardCharsets.UTF_8
            );

            List<List<String>> rows =
                    CsvParser.parseDocument(csvContent);

            Repository<MovieRecord> restoredRepository =
                    new Repository<>();

            for (int i = 1; i < rows.size(); i++) {
                restoredRepository.add(
                        MovieRecord.fromCsv(rows.get(i))
                );
            }

            List<MovieRecord> restoredRecords =
                    restoredRepository.all();

            boolean roundTripSuccessful =
                    originalRecords.equals(restoredRecords);

            report += "\nРезультати CSV-зберігання:\n";
            report += "Кількість записів: "
                    + originalRecords.size() + "\n";
            report += "Відновлено записів: "
                    + restoredRecords.size() + "\n";
            report += "Round-trip: "
                    + (roundTripSuccessful ? "успішно" : "помилка")
                    + "\n";
            report += "CSV-файл: " + csvOutput + "\n";

            System.out.println(report);

            Path outputParent = output.getParent();

            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }

            Files.writeString(
                    output,
                    report,
                    StandardCharsets.UTF_8
            );

            System.out.println("Звіт записано у: " + output);
            System.out.println("CSV-файл записано у: " + csvOutput);

            if (!roundTripSuccessful) {
                System.err.println(
                        "Помилка: початкові та відновлені записи не збігаються."
                );
            }

        } catch (IOException | DataStorageException exception) {
            System.err.println(
                    "Помилка роботи зі сховищем або файлами: "
                            + exception.getMessage()
            );
        }
    }
}
