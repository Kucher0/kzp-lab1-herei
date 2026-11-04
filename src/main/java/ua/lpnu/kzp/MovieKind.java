package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Вид фільму: художній або документальний.
 */
public enum MovieKind {
    /** Художній фільм. */
    FEATURE("художній", FeatureFilm::new),
    /** Документальний фільм. */
    DOCUMENTARY("документальний", Documentary::new);

    /** Створює фільм потрібного підтипу з уже розібраних полів. */
    @FunctionalInterface
    private interface Creator {
        Movie create(String title, String director, int year, int minutes, double rating);
    }

    private final String label;
    private final Creator creator;

    /* Конструктор enum викликається JVM один раз для кожної константи. */
    MovieKind(String label, Creator creator) {
        this.label = label;
        this.creator = creator;
    }

    /** Повертає текстове подання виду для звіту. */
    public String label() {
        return label;
    }

    /**
     * Створює фільм підтипу, що відповідає цьому виду (без switch за підтипом).
     *
     * @param title назва
     * @param director режисер
     * @param year рік
     * @param minutes тривалість у хвилинах
     * @param rating рейтинг
     * @return фільм відповідного підтипу
     */
    public Movie create(String title, String director, int year, int minutes, double rating) {
        return creator.create(title, director, year, minutes, rating);
    }

    /**
     * Розпізнає вид за підписом або назвою константи без урахування регістру.
     *
     * @param text текст із CSV, наприклад «документальний» або DOCUMENTARY
     * @return знайдений вид
     * @throws IllegalArgumentException якщо вид невідомий
     */
    public static MovieKind fromText(String text) {
        String value = Objects.requireNonNull(text, "Вид не може бути null").trim();
        for (MovieKind kind : values()) {
            if (kind.label.equalsIgnoreCase(value) || kind.name().equalsIgnoreCase(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("невідомий вид фільму");
    }
}
