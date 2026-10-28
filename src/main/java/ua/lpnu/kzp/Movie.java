package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Описує коректний незмінний запис про фільм у каталозі.
 */
public final class Movie {
    /** Мінімальний допустимий рейтинг. */
    public static final double MIN_RATING = 0.0;
    /** Максимальний допустимий рейтинг. */
    public static final double MAX_RATING = 10.0;

    private final String title;
    private final String director;
    private final int year;
    private final int minutes;
    private final double rating;

    /**
     * Створює коректний запис про фільм.
     *
     * @param title назва фільму
     * @param director режисер
     * @param year рік випуску
     * @param minutes тривалість у хвилинах
     * @param rating рейтинг від 0 до 10
     * @throws NullPointerException якщо назва або режисер дорівнює null
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    public Movie(String title, String director, int year, int minutes, double rating) {
        this.title = Objects.requireNonNull(title, "Назва не може бути null");
        this.director = Objects.requireNonNull(director, "Режисер не може бути null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("порожня назва фільму");
        }
        if (director.isBlank()) {
            throw new IllegalArgumentException("порожній режисер");
        }
        if (year <= 0) {
            throw new IllegalArgumentException("рік має бути додатним");
        }
        if (minutes <= 0) {
            throw new IllegalArgumentException("тривалість має бути додатною");
        }
        if (rating < 0) {
            throw new IllegalArgumentException("рейтинг не може бути від'ємним");
        }
        if (!Double.isFinite(rating) || rating > MAX_RATING) {
            throw new IllegalArgumentException("рейтинг має бути в діапазоні від 0 до 10");
        }
        this.year = year;
        this.minutes = minutes;
        this.rating = rating;
    }

    /**
     * Створює запис із CSV-рядка.
     *
     * @param line рядок у форматі назва;режисер;рік;хвилини;рейтинг
     * @return створений запис
     * @throws IllegalArgumentException якщо структура, число або значення некоректні
     */
    public static Movie fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("Порожній рядок");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("неправильна кількість полів");
        }
        try {
            return new Movie(
                    fields[0].trim(),
                    fields[1].trim(),
                    Integer.parseInt(fields[2].trim()),
                    Integer.parseInt(fields[3].trim()),
                    Double.parseDouble(fields[4].trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("неправильне числове значення", exception);
        }
    }

    /** Повертає назву фільму. */
    public String getTitle() {
        return title;
    }

    /** Повертає режисера. */
    public String getDirector() {
        return director;
    }

    /** Повертає рік випуску. */
    public int getYear() {
        return year;
    }

    /** Повертає тривалість у хвилинах. */
    public int getMinutes() {
        return minutes;
    }

    /** Повертає рейтинг фільму. */
    public double getRating() {
        return rating;
    }

    /** Повертає текстове подання запису для звіту. */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, %d): %d хв, рейтинг %.2f",
                title, director, year, minutes, rating);
    }
}
