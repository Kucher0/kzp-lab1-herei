
package ua.lpnu.kzp;

import java.util.List;
import java.util.Objects;

/**
 * Плоский запис фільму для збереження та відновлення з CSV.
 *
 * <p>Ключ запису для демонстрації round-trip: назва та рік.</p>
 */
public final class MovieRecord {

    @CsvColumn("Назва")
    private final String title;

    @CsvColumn("Режисер")
    private final String director;

    @CsvColumn("Рік")
    private final int year;

    @CsvColumn("Тривалість")
    private final int durationMinutes;

    @CsvColumn("Оцінка")
    private final double rating;

    /**
     * Створює запис фільму та перевіряє його поля.
     *
     * @param title назва фільму
     * @param director режисер
     * @param year рік випуску
     * @param durationMinutes тривалість у хвилинах
     * @param rating оцінка від 0 до 10
     * @throws IllegalArgumentException якщо дані некоректні
     * @throws NullPointerException якщо назва або режисер дорівнюють null
     */
    public MovieRecord(String title, String director, int year,
                       int durationMinutes, double rating) {
        this.title = validateText(title, "Назва");
        this.director = validateText(director, "Режисер");

        if (year <= 0) {
            throw new IllegalArgumentException("Рік має бути додатним");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Тривалість має бути додатною");
        }
        if (!Double.isFinite(rating) || rating < 0.0 || rating > 10.0) {
            throw new IllegalArgumentException("Оцінка має бути в діапазоні від 0 до 10");
        }

        this.year = year;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
    }

    private static String validateText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " не може бути null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не може бути порожньою");
        }
        return value;
    }

    /**
     * Відновлює запис фільму з полів CSV.
     *
     * <p>Порядок відповідає сортуванню Java-імен полів:
     * director, durationMinutes, rating, title, year.</p>
     *
     * @param fields значення одного CSV-запису
     * @return відновлений запис фільму
     * @throws DataStorageException якщо кількість або значення полів некоректні
     */
   public static MovieRecord fromCsv(List<String> fields)
        throws DataStorageException {
    if (fields == null || fields.size() != 5) {
        throw new DataStorageException(
                "Некоректний CSV-запис: очікується рівно 5 полів");
    }

    if (fields.stream().anyMatch(Objects::isNull)) {
        throw new DataStorageException(
                "Некоректний CSV-запис: поля не можуть бути null");
    }

    try {
        String director = fields.get(0);
        int durationMinutes = Integer.parseInt(fields.get(1));
        double rating = Double.parseDouble(fields.get(2));
        String title = fields.get(3);
        int year = Integer.parseInt(fields.get(4));

        return new MovieRecord(
                title, director, year, durationMinutes, rating);
    } catch (NumberFormatException exception) {
        throw new DataStorageException(
                "Некоректне числове значення у CSV-записі", exception);
    } catch (IllegalArgumentException exception) {
        throw new DataStorageException(
                "Некоректні дані фільму у CSV-записі", exception);
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
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /** Повертає оцінку фільму. */
    public double getRating() {
        return rating;
    }

    /**
     * Порівнює записи за всіма полями.
     *
     * @param other інший об'єкт
     * @return true, якщо всі поля збігаються
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MovieRecord record)) {
            return false;
        }
        return year == record.year
                && durationMinutes == record.durationMinutes
                && Double.compare(rating, record.rating) == 0
                && title.equals(record.title)
                && director.equals(record.director);
    }

    /**
     * Повертає хеш-код запису.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, director, year, durationMinutes, rating);
    }

    /**
     * Повертає текстове подання запису.
     *
     * @return інформація про фільм
     */
    @Override
    public String toString() {
        return title + " (" + year + "), " + director
                + ", " + durationMinutes + " хв, оцінка " + rating;
    }
}
