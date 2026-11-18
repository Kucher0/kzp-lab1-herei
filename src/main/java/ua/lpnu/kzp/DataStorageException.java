
package ua.lpnu.kzp;

/**
 * Повідомляє про помилки зберігання, читання або обробки даних.
 */
public class DataStorageException extends Exception {

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
