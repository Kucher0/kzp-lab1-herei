package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovieHierarchyTest {

    /* ---------- спільний стан у базовому типі ---------- */

    @Test
    void baseRejectsBlankTitleForEverySubtype() {
        assertThrows(IllegalArgumentException.class,
                () -> new FeatureFilm(" ", "Режисер", 2000, 90, 5.0));
        assertThrows(IllegalArgumentException.class,
                () -> new Documentary(" ", "Режисер", 2000, 90, 5.0));
    }

    @Test
    void baseRejectsInvalidRatingForEverySubtype() {
        assertThrows(IllegalArgumentException.class,
                () -> new FeatureFilm("A", "B", 2000, 90, 10.5));
        assertThrows(IllegalArgumentException.class,
                () -> new Documentary("A", "B", 2000, 90, -1.0));
    }

    @Test
    void subtypesStoreKind() {
        assertEquals(MovieKind.FEATURE, new FeatureFilm("A", "B", 2000, 90, 5.0).getKind());
        assertEquals(MovieKind.DOCUMENTARY, new Documentary("A", "B", 2000, 90, 5.0).getKind());
    }

    /* ---------- поліморфний метод ---------- */

    @Test
    void featureScoreIsRatingTimesTen() {
        Movie film = new FeatureFilm("A", "B", 2000, 90, 8.0);
        assertEquals(80.0, film.recommendationScore(), 0.0001);
    }

    @Test
    void documentaryScoreAddsEducationalBonus() {
        Movie film = new Documentary("A", "B", 2000, 90, 8.0);
        assertEquals(84.0, film.recommendationScore(), 0.0001);
    }

    @Test
    void subtypesGiveDifferentScoreForSameData() {
        Movie feature = new FeatureFilm("A", "B", 2000, 90, 8.0);
        Movie documentary = new Documentary("A", "B", 2000, 90, 8.0);
        assertNotEquals(feature.recommendationScore(), documentary.recommendationScore());
    }

    /* Граничні рейтинги 0 і 10 для обох підтипів. */
    @Test
    void scoreBoundaries() {
        assertEquals(0.0, new FeatureFilm("A", "B", 1, 1, 0.0).recommendationScore(), 0.0001);
        assertEquals(100.0, new FeatureFilm("A", "B", 1, 1, 10.0).recommendationScore(), 0.0001);
        assertEquals(20.0, new Documentary("A", "B", 1, 1, 0.0).recommendationScore(), 0.0001);
        assertEquals(100.0, new Documentary("A", "B", 1, 1, 10.0).recommendationScore(), 0.0001);
    }

    @Test
    void polymorphicListUsesSubtypeImplementation() {
        List<Movie> movies = List.of(
                new FeatureFilm("A", "X", 2000, 90, 8.0),
                new Documentary("B", "Y", 2001, 80, 8.0));
        double total = 0.0;
        for (Movie movie : movies) {
            total += movie.recommendationScore();
        }
        assertEquals(164.0, total, 0.0001);
    }

    /* ---------- enum ---------- */

    @Test
    void enumLabels() {
        assertEquals("художній", MovieKind.FEATURE.label());
        assertEquals("документальний", MovieKind.DOCUMENTARY.label());
    }

    @Test
    void enumParsesLabelAndNameIgnoringCase() {
        assertEquals(MovieKind.DOCUMENTARY, MovieKind.fromText(" Документальний "));
        assertEquals(MovieKind.DOCUMENTARY, MovieKind.fromText("documentary"));
        assertEquals(MovieKind.FEATURE, MovieKind.fromText("ХУДОЖНІЙ"));
    }

    @Test
    void enumRejectsUnknownText() {
        assertThrows(IllegalArgumentException.class, () -> MovieKind.fromText("мюзикл"));
    }

    @Test
    void enumCreatesCorrectSubtype() {
        assertInstanceOf(FeatureFilm.class, MovieKind.FEATURE.create("A", "B", 2000, 90, 5.0));
        assertInstanceOf(Documentary.class, MovieKind.DOCUMENTARY.create("A", "B", 2000, 90, 5.0));
    }

    /* ---------- fromCsv створює підтип ---------- */

    @Test
    void fromCsvWithFiveFieldsCreatesFeatureFilm() {
        assertInstanceOf(FeatureFilm.class, Movie.fromCsv("A;B;2000;90;5.0"));
    }

    @Test
    void fromCsvWithKindCreatesDocumentary() {
        Movie movie = Movie.fromCsv("Наша планета;Алістер Фотергілл;2019;50;9.3;документальний");
        assertInstanceOf(Documentary.class, movie);
        assertEquals(MovieKind.DOCUMENTARY, movie.getKind());
    }

    @Test
    void fromCsvRejectsUnknownKind() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Movie.fromCsv("A;B;2000;90;5.0;мюзикл"));
        assertEquals("невідомий вид фільму", error.getMessage());
    }

    @Test
    void fromCsvRejectsSevenFields() {
        assertThrows(IllegalArgumentException.class, () -> Movie.fromCsv("A;B;2000;90;5.0;x;y"));
    }

    /* ---------- equals і hashCode ---------- */

    @Test
    void equalMoviesHaveEqualHashCodes() {
        Movie first = new FeatureFilm("Java", "Автор", 2020, 100, 7.0);
        Movie second = new FeatureFilm("Java", "Автор", 2020, 100, 9.0);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashSetDoesNotDuplicateEqualMovies() {
        Set<Movie> set = new HashSet<>();
        set.add(new FeatureFilm("Java", "Автор", 2020, 100, 7.0));
        set.add(new FeatureFilm("Java", "Автор", 2020, 100, 7.0));
        assertEquals(1, set.size());
    }

    @Test
    void hashMapFindsEqualKey() {
        Map<Movie, String> map = new HashMap<>();
        map.put(new Documentary("Планета", "Автор", 2019, 50, 9.0), "знайдено");
        assertEquals("знайдено", map.get(new Documentary("Планета", "Автор", 2019, 50, 9.0)));
    }

    @Test
    void differentSubtypesAreNotEqualEvenWithSameData() {
        Movie feature = new FeatureFilm("A", "B", 2000, 90, 5.0);
        Movie documentary = new Documentary("A", "B", 2000, 90, 5.0);
        assertNotEquals(feature, documentary);
        Set<Movie> set = new HashSet<>(List.of(feature, documentary));
        assertEquals(2, set.size());
    }

    @Test
    void differentDirectorOrYearMakesMoviesDifferent() {
        Movie base = new FeatureFilm("A", "B", 2000, 90, 5.0);
        assertNotEquals(base, new FeatureFilm("A", "C", 2000, 90, 5.0));
        assertNotEquals(base, new FeatureFilm("A", "B", 2001, 90, 5.0));
    }

    @Test
    void equalsHandlesNullAndOtherTypes() {
        Movie movie = new FeatureFilm("A", "B", 2000, 90, 5.0);
        assertFalse(movie.equals(null));
        assertFalse(movie.equals("A"));
        assertTrue(movie.equals(movie));
    }
}
