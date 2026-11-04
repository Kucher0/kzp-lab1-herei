package ua.lpnu.kzp;

/** Художній фільм. */
public final class FeatureFilm extends Movie {

    /**
     * Створює художній фільм.
     *
     * @param title назва
     * @param director режисер
     * @param year рік
     * @param minutes тривалість у хвилинах
     * @param rating рейтинг від 0 до 10
     */
    public FeatureFilm(String title, String director, int year, int minutes, double rating) {
        super(title, director, year, minutes, rating, MovieKind.FEATURE);
    }

    /** Бал художнього фільму: рейтинг × 10. */
    @Override
    public double recommendationScore() {
        return getRating() * 10.0;
    }
}
