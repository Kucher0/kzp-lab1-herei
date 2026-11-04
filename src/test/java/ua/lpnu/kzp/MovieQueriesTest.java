package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MovieQueriesTest {

    /* Шість і більше записів: дубльована назва, межа 8.0, рівні оцінки для другого критерію. */
    private final List<Movie> movies = List.of(
            new FeatureFilm("Інтерстеллар", "Крістофер Нолан", 2014, 169, 8.7),
            new FeatureFilm("Тіні забутих предків", "Сергій Параджанов", 1965, 97, 8.2),
            new Documentary("Наша планета", "Алістер Фотергілл", 2019, 50, 9.3),
            new Documentary("Мандрівка пінгвінів", "Люк Жаке", 2005, 80, 7.7),
            new FeatureFilm("Атлантида", "Валентин Васянович", 2019, 108, 8.0),
            new FeatureFilm("Довбуш", "Олесь Санін", 2023, 112, 8.0),
            new FeatureFilm("Захар Беркут", "Ахтем Сеітаблаєв", 2019, 113, 7.1),
            new FeatureFilm("Інтерстеллар", "Крістофер Нолан", 2015, 120, 6.0));

    private static List<String> titlesOf(List<Movie> list) {
        return list.stream().map(Movie::getTitle).toList();
    }

    /* ---------- запит 1: filter ---------- */

    @Test
    void filterKeepsRatingsFromThresholdIncludingBoundary() {
        assertEquals(List.of("Інтерстеллар", "Тіні забутих предків", "Наша планета",
                "Атлантида", "Довбуш"), titlesOf(MovieQueries.highlyRated(movies)));
    }

    @Test
    void filterExcludesJustBelowThreshold() {
        List<Movie> list = List.of(new FeatureFilm("A", "B", 2000, 90, 7.99));
        assertTrue(MovieQueries.highlyRated(list).isEmpty());
    }

    /* ---------- запит 2: map ---------- */

    @Test
    void mapReturnsTitlesInInputOrderWithDuplicates() {
        List<String> titles = MovieQueries.titles(movies);
        assertEquals(8, titles.size());
        assertEquals("Інтерстеллар", titles.get(0));
        assertEquals("Інтерстеллар", titles.get(7));
    }

    /* ---------- запит 3: groupingBy ---------- */

    @Test
    void groupingAveragesRatingByDirectorWithSortedKeys() {
        Map<String, Double> result = MovieQueries.averageRatingByDirector(movies);
        assertEquals(7, result.size());
        assertEquals("Алістер Фотергілл", result.keySet().iterator().next());
        assertEquals(7.35, result.get("Крістофер Нолан"), 0.0001);
        assertEquals(9.3, result.get("Алістер Фотергілл"), 0.0001);
    }

    /* ---------- запит 4: statistics ---------- */

    @Test
    void statisticsOfDuration() {
        IntSummaryStatistics stats = MovieQueries.durationStatistics(movies);
        assertEquals(8, stats.getCount());
        assertEquals(50, stats.getMin());
        assertEquals(169, stats.getMax());
        assertEquals(849, stats.getSum());
        assertEquals(106.125, stats.getAverage(), 0.0001);
    }

    @Test
    void statisticsOfEmptyListHasZeroCount() {
        IntSummaryStatistics stats = MovieQueries.durationStatistics(List.of());
        assertEquals(0, stats.getCount());
        assertEquals(0, stats.getSum());
    }

    /* ---------- запит 5: top-N зі складеним компаратором ---------- */

    @Test
    void topFiveOrdersByRatingThenTitle() {
        assertEquals(List.of("Наша планета", "Інтерстеллар", "Тіні забутих предків",
                "Атлантида", "Довбуш"), titlesOf(MovieQueries.topByRating(movies, 5)));
    }

    /* Рівна оцінка 8.0: за другим критерієм «Атлантида» стоїть перед «Довбуш». */
    @Test
    void secondCriterionOrdersEqualRatingsByTitle() {
        List<String> top = titlesOf(MovieQueries.topByRating(movies, 8));
        assertTrue(top.indexOf("Атлантида") < top.indexOf("Довбуш"));
    }

    @Test
    void sixthMovieIsTruncatedByLimit() {
        List<Movie> top = MovieQueries.topByRating(movies, MovieQueries.TOP_N);
        assertEquals(5, top.size());
        assertTrue(titlesOf(top).stream().noneMatch("Мандрівка пінгвінів"::equals));
    }

    @Test
    void zeroLimitGivesEmptyAndHugeLimitGivesAll() {
        assertTrue(MovieQueries.topByRating(movies, 0).isEmpty());
        assertEquals(8, MovieQueries.topByRating(movies, 100).size());
    }

    @Test
    void negativeLimitIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MovieQueries.topByRating(movies, -1));
    }

    /* ---------- Optional-пошук ---------- */

    @Test
    void findReturnsFirstMatchInOriginalOrderForDuplicateTitle() {
        Optional<Movie> found = MovieQueries.findByTitle(movies, "Інтерстеллар");
        assertTrue(found.isPresent());
        assertEquals(2014, found.get().getYear());
    }

    @Test
    void findReturnsEmptyOptionalForMissingTitle() {
        assertTrue(MovieQueries.findByTitle(movies, "Невідомий").isEmpty());
    }

    @Test
    void findRejectsNullTitle() {
        assertThrows(NullPointerException.class, () -> MovieQueries.findByTitle(movies, null));
    }

    /* ---------- порожній вхід ---------- */

    @Test
    void emptyInputGivesEmptyResultsEverywhere() {
        List<Movie> empty = List.of();
        assertTrue(MovieQueries.highlyRated(empty).isEmpty());
        assertTrue(MovieQueries.titles(empty).isEmpty());
        assertTrue(MovieQueries.averageRatingByDirector(empty).isEmpty());
        assertTrue(MovieQueries.topByRating(empty, 5).isEmpty());
        assertTrue(MovieQueries.findByTitle(empty, "A").isEmpty());
        assertEquals(new YearRating(0, 0.0, 0, 0), MovieQueries.summary(empty));
    }

    @Test
    void nullListIsRejected() {
        assertThrows(NullPointerException.class, () -> MovieQueries.titles(null));
    }

    /* ---------- еквівалентність циклу й потоку ---------- */

    @Test
    void streamSummaryMatchesLegacyLoop() {
        double ratingSum = 0.0;
        int longest = 0;
        int oldest = Integer.MAX_VALUE;
        for (Movie movie : movies) {
            ratingSum += movie.getRating();
            longest = Math.max(longest, movie.getMinutes());
            oldest = Math.min(oldest, movie.getYear());
        }
        YearRating summary = MovieQueries.summary(movies);
        assertEquals(movies.size(), summary.count());
        assertEquals(ratingSum / movies.size(), summary.averageRating(), 0.0001);
        assertEquals(longest, summary.longestMinutes());
        assertEquals(oldest, summary.oldestYear());
    }

    @Test
    void streamStatisticsMatchLegacyLoopForRating() {
        DoubleSummaryStatistics stats = movies.stream()
                .collect(java.util.stream.Collectors.summarizingDouble(Movie::getRating));
        double max = 0.0;
        for (Movie movie : movies) {
            max = Math.max(max, movie.getRating());
        }
        assertEquals(max, stats.getMax(), 0.0001);
    }

    /* ---------- відсутність мутації та поліморфізм ---------- */

    @Test
    void queriesDoNotMutateInput() {
        List<Movie> before = new ArrayList<>(movies);
        MovieQueries.highlyRated(movies);
        MovieQueries.titles(movies);
        MovieQueries.averageRatingByDirector(movies);
        MovieQueries.durationStatistics(movies);
        MovieQueries.topByRating(movies, 3);
        MovieQueries.findByTitle(movies, "Довбуш");
        assertEquals(before, movies);
        assertEquals("Інтерстеллар", movies.get(0).getTitle());
    }

    @Test
    void collectionContainsDifferentSubtypesProcessedThroughBaseType() {
        assertTrue(movies.stream().anyMatch(movie -> movie.getKind() == MovieKind.DOCUMENTARY));
        assertTrue(movies.stream().anyMatch(movie -> movie.getKind() == MovieKind.FEATURE));
        assertEquals(8, MovieQueries.summary(movies).count());
    }
}
