package ua.lpnu.kzp;

/** Документальний фільм. */
public final class Documentary extends Movie {

    /**
     * Створює документальний фільм.
     *
     * @param title назва
     * @param director режисер
     * @param year рік
     * @param minutes тривалість у хвилинах
     * @param rating рейтинг від 0 до 10
     */
    public Documentary(String title, String director, int year, int minutes, double rating) {
        super(title, director, year, minutes, rating, MovieKind.DOCUMENTARY);
    }

    /** Бал документального фільму: рейтинг × 8 + 20 (освітня цінність). */
    @Override
    public double recommendationScore() {
        return getRating() * 8.0 + 20.0;
    }
}
