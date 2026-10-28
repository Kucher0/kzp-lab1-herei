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
 * <p>Програма читає дані з CSV-файлу, перевіряє їх коректність,
 * обчислює статистичні показники та формує текстовий звіт.</p>
 */
public class Main {

    /**
     * Точка входу в програму.
     *
     * <p>Підтримуються параметри командного рядка:
     * {@code --help}, {@code --version}, {@code --input} та
     * {@code --output}.</p>
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            if (args[0].equals("--version")) {
                System.out.println("kzp-lab1-herei version 1.1.0");
                return;
            }

            if (args[0].equals("--help")) {
                System.out.println("""
                        Каталог фільмів — KZP Lab 1

                        Використання:
                          java -jar kzp-lab1-herei-1.1.0.jar
                          java -jar kzp-lab1-herei-1.1.0.jar --help
                          java -jar kzp-lab1-herei-1.1.0.jar --version
                          java -jar kzp-lab1-herei-1.1.0.jar --input <файл>
                          java -jar kzp-lab1-herei-1.1.0.jar --output <файл>
                          java -jar kzp-lab1-herei-1.1.0.jar --input <файл> --output <файл>

                        Параметри:
                          --help              показати цю довідку
                          --version           показати версію програми
                          --input <файл>      вказати вхідний CSV-файл
                          --output <файл>     вказати файл для збереження звіту
                        """);
                return;
            }
        }

        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--input")) {
                if (i + 1 >= args.length) {
                    System.err.println(
                            "Помилка: після --input потрібно вказати файл."
                    );
                    return;
                }

                input = Path.of(args[++i]);

            } else if (args[i].equals("--output")) {
                if (i + 1 >= args.length) {
                    System.err.println(
                            "Помилка: після --output потрібно вказати файл."
                    );
                    return;
                }

                output = Path.of(args[++i]);
            }
        }

        try {
            List<String> lines = Files.readAllLines(
                    input,
                    StandardCharsets.UTF_8
            );

            List<String> errors = new ArrayList<>();
            List<Movie> movies = CatalogService.parse(lines, errors);
            String report = CatalogService.buildReport(
                    CatalogService.summarize(movies),
                    errors
            );

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

            System.out.println(
                    "Звіт записано у: " + output
            );

        } catch (IOException e) {
            System.err.println(
                    "Помилка роботи з файлами: "
                            + e.getMessage()
            );
        }
    }
}