package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Набір чистих потокових запитів до списку фільмів.
 *
 * <p>Запити працюють зі спільним типом {@link Movie}, не змінюють вхідний список
 * і нічого не друкують: форматування виконується окремо в {@link CatalogService}.</p>
 */
public final class MovieQueries {
    /** Поріг високої оцінки для запиту відбору (межа включається). */
    public static final double HIGH_RATING_THRESHOLD = 8.0;
    /** Кількість елементів у рейтингу top-N. */
    public static final int TOP_N = 5;

    /* Забороняє створення екземплярів класу статичних запитів. */
    private MovieQueries() {
    }

    /**
     * Запит 1 (filter): фільми з оцінкою не нижче порога.
     *
     * @param movies фільми
     * @return нові фільми у вхідному порядку
     */
    public static List<Movie> highlyRated(List<Movie> movies) {
        return streamOf(movies)
                .filter(movie -> movie.getRating() >= HIGH_RATING_THRESHOLD)
                .toList();
    }

    /**
     * Запит 2 (map): назви фільмів у вхідному порядку.
     *
     * @param movies фільми
     * @return назви
     */
    public static List<String> titles(List<Movie> movies) {
        return streamOf(movies)
                .map(Movie::getTitle)
                .toList();
    }

    /**
     * Запит 3 (groupingBy): середня оцінка за режисером, ключі впорядковано за абеткою.
     *
     * @param movies фільми
     * @return карта «режисер → середня оцінка»
     */
    public static Map<String, Double> averageRatingByDirector(List<Movie> movies) {
        return streamOf(movies)
                .collect(Collectors.groupingBy(
                        Movie::getDirector,
                        TreeMap::new,
                        Collectors.averagingDouble(Movie::getRating)));
    }

    /**
     * Запит 4 (summarizingInt): статистика тривалості в хвилинах.
     *
     * @param movies фільми
     * @return кількість, мінімум, максимум, сума та середнє
     */
    public static IntSummaryStatistics durationStatistics(List<Movie> movies) {
        return streamOf(movies)
                .collect(Collectors.summarizingInt(Movie::getMinutes));
    }

    /**
     * Запит 5 (sorted + limit): найкращі фільми за оцінкою (спадання), потім за назвою (зростання).
     *
     * @param movies фільми
     * @param limit кількість елементів; 0 дає порожній результат, завеликий — усі
     * @return не більше {@code limit} фільмів
     * @throws IllegalArgumentException якщо {@code limit} від'ємний
     */
    public static List<Movie> topByRating(List<Movie> movies, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним");
        }
        // reversed() застосовано до першого критерію до додавання другого.
        Comparator<Movie> byRatingThenTitle = Comparator
                .comparingDouble(Movie::getRating)
                .reversed()
                .thenComparing(Movie::getTitle);
        return streamOf(movies)
                .sorted(byRatingThenTitle)
                .limit(limit)
                .toList();
    }

    /**
     * Пошук (Optional): перший за порядком вхідних даних фільм із заданою назвою.
     *
     * @param movies фільми
     * @param title назва
     * @return знайдений фільм або {@link Optional#empty()}
     */
    public static Optional<Movie> findByTitle(List<Movie> movies, String title) {
        Objects.requireNonNull(title, "Назва не може бути null");
        return streamOf(movies)
                .filter(movie -> movie.getTitle().equals(title))
                .findFirst();
    }

    /**
     * Підсумкові показники звіту (замінює цикл ЛР № 1–3): кількість, середній рейтинг,
     * найдовший фільм, найстаріший рік.
     *
     * @param movies фільми
     * @return підсумок; для порожнього списку всі показники нульові
     */
    public static YearRating summary(List<Movie> movies) {
        DoubleSummaryStatistics rating = streamOf(movies)
                .collect(Collectors.summarizingDouble(Movie::getRating));
        if (rating.getCount() == 0) {
            return new YearRating(0, 0.0, 0, 0);
        }
        int longest = streamOf(movies).mapToInt(Movie::getMinutes).max().orElse(0);
        int oldest = streamOf(movies).mapToInt(Movie::getYear).min().orElse(0);
        return new YearRating((int) rating.getCount(), rating.getAverage(), longest, oldest);
    }

    /* Перевіряє список і повертає новий потік для кожного незалежного запиту. */
    private static Stream<Movie> streamOf(List<Movie> movies) {
        return Objects.requireNonNull(movies, "Список не може бути null").stream();
    }
}
