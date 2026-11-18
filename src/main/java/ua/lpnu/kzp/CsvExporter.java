
package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Універсальний рефлексивний експортер об'єктів у формат CSV.
 */
public final class CsvExporter {

    private CsvExporter() {
        // Utility class.
    }

    /**
     * Записує анотовані поля об'єктів у CSV-файл.
     *
     * @param <T> тип записів
     * @param path шлях до CSV-файла
     * @param type клас записів
     * @param records записи для експорту
     * @throws DataStorageException якщо поля неможливо прочитати або файл записати
     */
    public static <T> void write(
            Path path,
            Class<T> type,
            List<? extends T> records
    ) throws DataStorageException {

        Objects.requireNonNull(path, "Шлях не може бути null");
        Objects.requireNonNull(type, "Тип записів не може бути null");
        Objects.requireNonNull(records, "Список записів не може бути null");

        List<Field> fields = annotatedFields(type);

        if (fields.isEmpty()) {
            throw new DataStorageException(
                    "Клас " + type.getSimpleName()
                            + " не містить полів з @CsvColumn");
        }

        StringBuilder csv = new StringBuilder();

        List<String> headers = fields.stream()
                .map(field -> escape(
                        field.getAnnotation(CsvColumn.class).value()))
                .toList();

        csv.append(String.join(",", headers))
                .append(System.lineSeparator());

        for (T record : records) {
            if (record == null) {
                throw new DataStorageException(
                        "Список містить null замість запису");
            }

            if (!type.isInstance(record)) {
                throw new DataStorageException(
                        "Запис не відповідає типу " + type.getSimpleName());
            }

            List<String> values = new ArrayList<>();

            for (Field field : fields) {
                try {
                    if (!field.trySetAccessible()) {
                        throw new IllegalAccessException(
                                "Неможливо отримати доступ до поля "
                                        + field.getName());
                    }

                    Object value = field.get(record);
                    values.add(escape(value == null ? "" : String.valueOf(value)));

                } catch (IllegalAccessException | SecurityException exception) {
                    throw new DataStorageException(
                            "Не вдалося прочитати поле " + field.getName(),
                            exception);
                }
            }

            csv.append(String.join(",", values))
                    .append(System.lineSeparator());
        }

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    path,
                    csv.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

        } catch (IOException | SecurityException exception) {
            throw new DataStorageException(
                    "Не вдалося записати CSV-файл: " + path,
                    exception);
        }
    }

    /**
     * Отримує анотовані поля класу та його батьківських класів.
     *
     * @param type клас для аналізу
     * @return поля у детермінованому порядку
     */
    private static List<Field> annotatedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        Class<?> current = type;

        while (current != null && current != Object.class) {
            Arrays.stream(current.getDeclaredFields())
                    .filter(field ->
                            field.isAnnotationPresent(CsvColumn.class))
                    .filter(field ->
                            !Modifier.isStatic(field.getModifiers()))
                    .forEach(fields::add);

            current = current.getSuperclass();
        }

        fields.sort(
                Comparator.comparing(Field::getName)
                        .thenComparing(field ->
                                field.getDeclaringClass().getName())
        );

        return List.copyOf(fields);
    }

    /**
     * Екранує значення відповідно до правил CSV.
     *
     * @param value початкове значення
     * @return екрановане значення
     */
    private static String escape(String value) {
        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
