package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Розбирає записи, обчислює показники та формує текст звіту.
 */
public final class CatalogService {

    /* Забороняє створення екземплярів службового класу. */
    private CatalogService() {
    }

    /**
     * Перетворює рядки CSV на фільми; помилку кожного рядка записує з його номером.
     *
     * @param lines рядки вхідного файла
     * @param errors список, до якого додаються повідомлення про хибні рядки
     * @return список коректних фільмів у порядку вхідних даних
     */
    public static List<Movie> parse(List<String> lines, List<String> errors) {
        List<Movie> movies = new ArrayList<>();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            // Як і в ЛР № 1, порожні рядки мовчки пропускаються.
            if (line.isBlank()) {
                continue;
            }
            // Хибний запис не потрапляє ні до списку, ні до статистики.
            try {
                movies.add(Movie.fromCsv(line));
            } catch (IllegalArgumentException exception) {
                errors.add("Рядок %d: %s".formatted(lineNumber, exception.getMessage()));
            }
        }
        return movies;
    }

    /**
     * Обчислює кількість, середній рейтинг, найдовший фільм і найстаріший рік.
     *
     * @param movies коректні фільми
     * @return підсумок; для порожнього списку всі показники нульові
     */
    public static YearRating summarize(List<Movie> movies) {
        if (movies.isEmpty()) {
            return new YearRating(0, 0.0, 0, 0);
        }
        double ratingSum = 0.0;
        int longest = 0;
        int oldest = Integer.MAX_VALUE;
        for (Movie movie : movies) {
            ratingSum += movie.getRating();
            longest = Math.max(longest, movie.getMinutes());
            oldest = Math.min(oldest, movie.getYear());
        }
        // Явне приведення не допускає цілочислового ділення.
        return new YearRating(movies.size(), ratingSum / movies.size(), longest, oldest);
    }

    /**
     * Формує єдиний текст звіту для консолі та файла (формат лабораторної № 1).
     *
     * @param summary підсумкові показники
     * @param errors повідомлення про хибні рядки
     * @return текст звіту з роздільниками рядків поточної ОС
     */
    public static String buildReport(YearRating summary, List<String> errors) {
        StringBuilder report = new StringBuilder(String.format(Locale.ROOT,
                "КАТАЛОГ ФІЛЬМІВ%n%n"
                        + "Кількість коректних записів: %d%n"
                        + "Середній рейтинг: %.2f%n"
                        + "Найдовший фільм: %d хв%n"
                        + "Найстаріший рік: %d%n%n"
                        + "Помилки:%n",
                summary.count(), summary.averageRating(),
                summary.longestMinutes(), summary.oldestYear()));
        for (String error : errors) {
            report.append(error).append(System.lineSeparator());
        }
        return report.toString();
    }

    /**
     * Формує додатковий розділ із рекомендаційними балами (лише за ключем --scores).
     *
     * <p>Бал кожного фільму обчислює його підтип; цикл не перевіряє тип об'єкта.</p>
     *
     * @param movies коректні фільми різних підтипів
     * @return текст розділу
     */
    public static String buildScoreSection(List<Movie> movies) {
        StringBuilder section = new StringBuilder(String.format("%nРекомендаційні бали:%n"));
        double best = 0.0;
        for (Movie movie : movies) {
            double score = movie.recommendationScore();
            best = Math.max(best, score);
            section.append(String.format(Locale.ROOT, "%s (%s): %.2f%n",
                    movie.getTitle(), movie.getKind().label(), score));
        }
        // Рівні за equals/hashCode фільми в множині не дублюються.
        Set<Movie> unique = new HashSet<>(movies);
        section.append(String.format(Locale.ROOT, "Унікальних фільмів: %d%nНайвищий бал: %.2f%n",
                unique.size(), best));
        return section.toString();
    }
}
