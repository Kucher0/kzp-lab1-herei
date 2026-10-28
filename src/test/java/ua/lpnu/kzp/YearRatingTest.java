package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class YearRatingTest {

    @Test
    void recordUsesValueEquality() {
        YearRating first = new YearRating(4, 7.4, 112, 1965);
        YearRating second = new YearRating(4, 7.4, 112, 1965);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void recordExposesComponents() {
        YearRating summary = new YearRating(4, 7.4, 112, 1965);
        assertEquals(4, summary.count());
        assertEquals(7.4, summary.averageRating(), 0.0001);
        assertEquals(112, summary.longestMinutes());
        assertEquals(1965, summary.oldestYear());
    }

    @Test
    void recordAcceptsEmptySummary() {
        assertEquals(0, new YearRating(0, 0.0, 0, 0).count());
    }

    @Test
    void recordRejectsNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> new YearRating(-1, 0.0, 0, 0));
    }

    @Test
    void recordRejectsNaNAverage() {
        assertThrows(IllegalArgumentException.class,
                () -> new YearRating(1, Double.NaN, 90, 2000));
    }
}
