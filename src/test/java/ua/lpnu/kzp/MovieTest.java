package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MovieTest {

    /* Коректний конструктор зберігає всі поля, включно з українським текстом. */
    @Test
    void constructorStoresAllFields() {
        Movie movie = new Movie("Довбуш", "Олесь Санін", 2023, 112, 7.1);
        assertEquals("Довбуш", movie.getTitle());
        assertEquals("Олесь Санін", movie.getDirector());
        assertEquals(2023, movie.getYear());
        assertEquals(112, movie.getMinutes());
        assertEquals(7.1, movie.getRating(), 0.0001);
    }

    /* Граничні значення: рейтинг 0 і 10, рік 1, тривалість 1. */
    @Test
    void constructorAcceptsBoundaryValues() {
        Movie low = new Movie("A", "B", 1, 1, 0.0);
        Movie high = new Movie("A", "B", 1, 1, 10.0);
        assertEquals(0.0, low.getRating(), 0.0001);
        assertEquals(10.0, high.getRating(), 0.0001);
    }

    @Test
    void constructorRejectsNullTitle() {
        assertThrows(NullPointerException.class, () -> new Movie(null, "B", 2000, 90, 5.0));
    }

    @Test
    void constructorRejectsNullDirector() {
        assertThrows(NullPointerException.class, () -> new Movie("A", null, 2000, 90, 5.0));
    }

    @Test
    void constructorRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> new Movie("  ", "B", 2000, 90, 5.0));
    }

    @Test
    void constructorRejectsBlankDirector() {
        assertThrows(IllegalArgumentException.class, () -> new Movie("A", "", 2000, 90, 5.0));
    }

    @Test
    void constructorRejectsZeroYear() {
        assertThrows(IllegalArgumentException.class, () -> new Movie("A", "B", 0, 90, 5.0));
    }

    @Test
    void constructorRejectsNegativeMinutes() {
        assertThrows(IllegalArgumentException.class, () -> new Movie("A", "B", 2000, -5, 5.0));
    }

    @Test
    void constructorRejectsRatingAboveRange() {
        assertThrows(IllegalArgumentException.class, () -> new Movie("A", "B", 2000, 90, 10.1));
    }

    @Test
    void constructorRejectsNegativeRating() {
        assertThrows(IllegalArgumentException.class, () -> new Movie("A", "B", 2000, 90, -0.1));
    }

    @Test
    void constructorRejectsNaNRating() {
        assertThrows(IllegalArgumentException.class,
                () -> new Movie("A", "B", 2000, 90, Double.NaN));
    }

    @Test
    void constructorRejectsInfiniteRating() {
        assertThrows(IllegalArgumentException.class,
                () -> new Movie("A", "B", 2000, 90, Double.POSITIVE_INFINITY));
    }

    @Test
    void fromCsvCreatesValidMovie() {
        Movie movie = Movie.fromCsv("Тіні забутих предків;Сергій Параджанов;1965;97;8.2");
        assertEquals("Тіні забутих предків", movie.getTitle());
        assertEquals("Сергій Параджанов", movie.getDirector());
        assertEquals(1965, movie.getYear());
        assertEquals(97, movie.getMinutes());
        assertEquals(8.2, movie.getRating(), 0.0001);
    }

    @Test
    void fromCsvTrimsSpaces() {
        Movie movie = Movie.fromCsv(" A ; B ; 2000 ; 90 ; 5.5 ");
        assertEquals("A", movie.getTitle());
        assertEquals(2000, movie.getYear());
    }

    @Test
    void fromCsvRejectsEmptyLine() {
        assertThrows(IllegalArgumentException.class, () -> Movie.fromCsv(""));
    }

    @Test
    void fromCsvRejectsWrongFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> Movie.fromCsv("A;B;2000;90"));
    }

    @Test
    void fromCsvRejectsInvalidNumber() {
        assertThrows(IllegalArgumentException.class, () -> Movie.fromCsv("A;B;рік;90;5.0"));
    }

    @Test
    void fromCsvRejectsInvariantViolation() {
        assertThrows(IllegalArgumentException.class, () -> Movie.fromCsv("A;B;2000;90;11"));
    }

    @Test
    void toStringUsesRootLocale() {
        Movie movie = new Movie("Довбуш", "Олесь Санін", 2023, 112, 7.1);
        assertEquals("Довбуш (Олесь Санін, 2023): 112 хв, рейтинг 7.10", movie.toString());
    }
}
