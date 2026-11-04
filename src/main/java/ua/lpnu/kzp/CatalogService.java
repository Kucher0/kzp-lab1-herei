package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * <p>Із ЛР № 4 обчислення виконує потоковий запит {@link MovieQueries#summary(List)}.</p>
     *
     * @param movies коректні фільми
     * @return підсумок; для порожнього списку всі показники нульові
     */
    public static YearRating summarize(List<Movie> movies) {
        return MovieQueries.summary(movies);
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

    /**
     * Формує розділ із результатами потокових запитів (лише за ключем --queries).
     *
     * <p>Тут лише форматування: самі запити виконує {@link MovieQueries}.</p>
     *
     * @param movies коректні фільми
     * @return текст розділу
     */
    public static String buildQueriesSection(List<Movie> movies) {
        StringBuilder section = new StringBuilder(String.format("%nЗапити Stream API:%n"));

        List<Movie> highlyRated = MovieQueries.highlyRated(movies);
        section.append(String.format(Locale.ROOT, "Оцінка не нижче %.1f: %d%n",
                MovieQueries.HIGH_RATING_THRESHOLD, highlyRated.size()));
        section.append("Назви: ").append(MovieQueries.titles(highlyRated).stream()
                .collect(Collectors.joining(", "))).append(System.lineSeparator());

        section.append(String.format("Середня оцінка за режисером:%n"));
        Map<String, Double> byDirector = MovieQueries.averageRatingByDirector(movies);
        if (byDirector.isEmpty()) {
            section.append(String.format("  немає даних%n"));
        }
        byDirector.forEach((director, average) -> section.append(
                String.format(Locale.ROOT, "  %s: %.2f%n", director, average)));

        IntSummaryStatistics duration = MovieQueries.durationStatistics(movies);
        if (duration.getCount() == 0) {
            section.append(String.format("Тривалість: немає даних%n"));
        } else {
            section.append(String.format(Locale.ROOT,
                    "Тривалість: кількість=%d, мінімум=%d хв, максимум=%d хв, середня=%.2f хв%n",
                    duration.getCount(), duration.getMin(), duration.getMax(),
                    duration.getAverage()));
        }

        section.append(String.format("Топ-%d за оцінкою:%n", MovieQueries.TOP_N));
        List<Movie> top = MovieQueries.topByRating(movies, MovieQueries.TOP_N);
        for (int index = 0; index < top.size(); index++) {
            Movie movie = top.get(index);
            section.append(String.format(Locale.ROOT, "  %d. %s (%.2f)%n",
                    index + 1, movie.getTitle(), movie.getRating()));
        }
        return section.toString();
    }

    /**
     * Формує рядок результату пошуку за назвою (ключ --find).
     *
     * @param movies коректні фільми
     * @param title шукана назва
     * @return рядок із фільмом або повідомленням про відсутність
     */
    public static String buildSearchLine(List<Movie> movies, String title) {
        String result = MovieQueries.findByTitle(movies, title)
                .map(Movie::toString)
                .orElse("не знайдено");
        return String.format("Пошук «%s»: %s%n", title, result);
    }
}
