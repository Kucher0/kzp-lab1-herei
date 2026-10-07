package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {

        if (args.length > 0 && args[0].equals("--version")) {
            System.out.println("kzp-lab1-herei version 1.0.0");
            return;
        }

        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");

        try {
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);

            List<String> validRecords = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            int longestMinutes = 0;
            int oldestYear = Integer.MAX_VALUE;
            double ratingSum = 0.0;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(";", -1);

                if (fields.length != 5) {
                    errors.add("Рядок " + (i + 1)
                            + ": неправильна кількість полів");
                    continue;
                }

                String title = fields[0].trim();
                String director = fields[1].trim();

                if (title.isEmpty()) {
                    errors.add("Рядок " + (i + 1)
                            + ": порожня назва фільму");
                    continue;
                }

                if (director.isEmpty()) {
                    errors.add("Рядок " + (i + 1)
                            + ": порожній режисер");
                    continue;
                }

                try {
                    int year = Integer.parseInt(fields[2].trim());
                    int minutes = Integer.parseInt(fields[3].trim());
                    double rating = Double.parseDouble(fields[4].trim());

                    if (year <= 0) {
                        errors.add("Рядок " + (i + 1)
                                + ": рік має бути додатним");
                        continue;
                    }

                    if (minutes <= 0) {
                        errors.add("Рядок " + (i + 1)
                                + ": тривалість має бути додатною");
                        continue;
                    }

                    if (rating < 0) {
                        errors.add("Рядок " + (i + 1)
                                + ": рейтинг не може бути від'ємним");
                        continue;
                    }

                    validRecords.add(title);
                    ratingSum += rating;

                    if (minutes > longestMinutes) {
                        longestMinutes = minutes;
                    }

                    if (year < oldestYear) {
                        oldestYear = year;
                    }

                } catch (NumberFormatException e) {
                    errors.add("Рядок " + (i + 1)
                            + ": неправильне числове значення");
                }
            }

            int validCount = validRecords.size();

            double averageRating = validCount == 0
                    ? 0.0
                    : ratingSum / validCount;

            String report = String.format(
                    Locale.ROOT,
                    "КАТАЛОГ ФІЛЬМІВ%n%n"
                    + "Кількість коректних записів: %d%n"
                    + "Середній рейтинг: %.2f%n"
                    + "Найдовший фільм: %d хв%n"
                    + "Найстаріший рік: %d%n%n"
                    + "Помилки:%n",
                    validCount,
                    averageRating,
                    longestMinutes,
                    validCount == 0 ? 0 : oldestYear
            );

            for (String error : errors) {
                report += error + System.lineSeparator();
            }

            System.out.println(report);

            Path outputParent = output.getParent();

            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }

            Files.writeString(output, report, StandardCharsets.UTF_8);

            System.out.println("Звіт записано у: " + output);

        } catch (IOException e) {
            System.err.println("Помилка роботи з файлами: "
                    + e.getMessage());
        }
    }
}