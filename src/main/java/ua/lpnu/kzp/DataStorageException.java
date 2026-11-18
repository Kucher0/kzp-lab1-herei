package ua.lpnu.kzp;

/**
 * Повідомляє про помилки зберігання, читання або обробки даних.
 *
 * <p>Це checked-виняток предметної області: повідомлення пояснює операцію,
 * а {@link #getCause()} зберігає першопричину (наприклад, {@code IOException},
 * {@code NumberFormatException} або помилку рефлексії).</p>
 */
public class DataStorageException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Створює виняток із повідомленням.
     *
     * @param message опис помилки
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток із повідомленням і першопричиною.
     *
     * @param message опис помилки
     * @param cause першопричина помилки
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}