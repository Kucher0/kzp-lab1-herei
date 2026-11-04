package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний тип фільму каталогу.
 *
 * <p>Спільні поля й інваріанти зберігаються тут; відмінна поведінка
 * (рекомендаційний бал) реалізується в підтипах.</p>
 */
public abstract class Movie {
    /** Мінімальний допустимий рейтинг. */
    public static final double MIN_RATING = 0.0;
    /** Максимальний допустимий рейтинг. */
    public static final double MAX_RATING = 10.0;

    private final String title;
    private final String director;
    private final int year;
    private final int minutes;
    private final double rating;
    private final MovieKind kind;

    /**
     * Створює фільм із перевіреними спільними полями.
     *
     * @param title назва фільму
     * @param director режисер
     * @param year рік випуску
     * @param minutes тривалість у хвилинах
     * @param rating рейтинг від 0 до 10
     * @param kind вид фільму
     * @throws NullPointerException якщо назва, режисер або вид дорівнюють null
     * @throws IllegalArgumentException якщо порушено інваріант
     */
    protected Movie(String title, String director, int year, int minutes,
                    double rating, MovieKind kind) {
        this(validateTitle(title), validateDirector(director), validateYear(year),
                validateMinutes(minutes), validateRating(rating), validateKind(kind), true);
    }

    /* Присвоює вже перевірений стан під час внутрішнього делегування. */
    private Movie(String title, String director, int year, int minutes,
                  double rating, MovieKind kind, boolean validated) {
        this.title = title;
        this.director = director;
        this.year = year;
        this.minutes = minutes;
        this.rating = rating;
        this.kind = kind;
    }

    /* Перевіряє назву. */
    private static String validateTitle(String title) {
        Objects.requireNonNull(title, "Назва не може бути null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("порожня назва фільму");
        }
        return title;
    }

    /* Перевіряє режисера. */
    private static String validateDirector(String director) {
        Objects.requireNonNull(director, "Режисер не може бути null");
        if (director.isBlank()) {
            throw new IllegalArgumentException("порожній режисер");
        }
        return director;
    }

    /* Перевіряє рік. */
    private static int validateYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("рік має бути додатним");
        }
        return year;
    }

    /* Перевіряє тривалість. */
    private static int validateMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("тривалість має бути додатною");
        }
        return minutes;
    }

    /* Перевіряє рейтинг. */
    private static double validateRating(double rating) {
        if (rating < 0) {
            throw new IllegalArgumentException("рейтинг не може бути від'ємним");
        }
        if (!Double.isFinite(rating) || rating > MAX_RATING) {
            throw new IllegalArgumentException("рейтинг має бути в діапазоні від 0 до 10");
        }
        return rating;
    }

    /* Перевіряє вид фільму. */
    private static MovieKind validateKind(MovieKind kind) {
        return Objects.requireNonNull(kind, "Вид не може бути null");
    }

    /**
     * Створює фільм потрібного підтипу з CSV-рядка.
     *
     * <p>Формат: назва;режисер;рік;хвилини;рейтинг[;вид]. Без шостого поля
     * створюється художній фільм, тому файли попередніх робіт працюють без змін.</p>
     *
     * @param line рядок CSV
     * @return фільм відповідного підтипу
     * @throws IllegalArgumentException якщо структура, число або значення некоректні
     */
    public static Movie fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("Порожній рядок");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 5 && fields.length != 6) {
            throw new IllegalArgumentException("неправильна кількість полів");
        }
        String title = fields[0].trim();
        String director = fields[1].trim();
        int year;
        int minutes;
        double rating;
        try {
            year = Integer.parseInt(fields[2].trim());
            minutes = Integer.parseInt(fields[3].trim());
            rating = Double.parseDouble(fields[4].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("неправильне числове значення", exception);
        }
        MovieKind kind = fields.length == 6 ? MovieKind.fromText(fields[5]) : MovieKind.FEATURE;
        return kind.create(title, director, year, minutes, rating);
    }

    /**
     * Обчислює рекомендаційний бал (0–100); різні підтипи рахують його по-різному.
     *
     * @return рекомендаційний бал
     */
    public abstract double recommendationScore();

    /** Повертає назву фільму. */
    public final String getTitle() {
        return title;
    }

    /** Повертає режисера. */
    public final String getDirector() {
        return director;
    }

    /** Повертає рік випуску. */
    public final int getYear() {
        return year;
    }

    /** Повертає тривалість у хвилинах. */
    public final int getMinutes() {
        return minutes;
    }

    /** Повертає рейтинг. */
    public final double getRating() {
        return rating;
    }

    /** Повертає вид фільму. */
    public final MovieKind getKind() {
        return kind;
    }

    /** Порівнює фільми за підтипом, назвою, режисером і роком. */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Movie movie = (Movie) other;
        return year == movie.year && title.equals(movie.title)
                && director.equals(movie.director);
    }

    /** Повертає хеш-код, узгоджений із {@link #equals(Object)}. */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), title, director, year);
    }

    /** Повертає текстове подання запису для звіту. */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, %d): %d хв, рейтинг %.2f",
                title, director, year, minutes, rating);
    }
}
