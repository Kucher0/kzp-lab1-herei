package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Універсальний рефлексивний експортер об'єктів у формат CSV (UTF-8).
 *
 * <p>Експортер не знає жодного предметного класу: стовпці визначаються
 * лише полями з {@link CsvColumn}, а їхній порядок — сортуванням Java-імен.</p>
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

        List<String> headers = new ArrayList<>();
        for (Field field : fields) {
            headers.add(escape(field.getAnnotation(CsvColumn.class).value()));
        }

        StringBuilder csv = new StringBuilder(String.join(",", headers))
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
                    values.add(escape(field.get(record)));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException(
                            "Не вдалося прочитати поле " + field.getName(),
                            exception);
                }
            }

            csv.append(String.join(",", values))
                    .append(System.lineSeparator());
        }

        writeFile(path, csv.toString());
    }

    /* Записує текст через Path і Files з явним UTF-8. */
    private static void writeFile(Path path, String text)
            throws DataStorageException {
        try {
            Path parent = path.toAbsolutePath().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    path,
                    text,
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
     * Отримує анотовані поля класу та його батьківських класів і перевіряє доступ.
     *
     * @param type клас для аналізу
     * @return поля у детермінованому порядку (за Java-іменем)
     * @throws DataStorageException якщо полів немає або доступ заборонено
     */
    private static List<Field> annotatedFields(Class<?> type)
            throws DataStorageException {
        List<Field> fields = new ArrayList<>();

        for (Class<?> current = type;
                current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)
                        && !Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }

        if (fields.isEmpty()) {
            throw new DataStorageException(
                    "Клас " + type.getSimpleName()
                            + " не містить полів з @CsvColumn");
        }

        fields.sort(Comparator.comparing(Field::getName)
                .thenComparing(field -> field.getDeclaringClass().getName()));

        for (Field field : fields) {
            try {
                if (!field.trySetAccessible()) {
                    throw new DataStorageException(
                            "Немає доступу до поля " + field.getName());
                }
            } catch (SecurityException exception) {
                throw new DataStorageException(
                        "Немає доступу до поля " + field.getName(), exception);
            }
        }

        return List.copyOf(fields);
    }

    /**
     * Екранує значення відповідно до правил CSV.
     *
     * @param value значення поля (null дає порожнє поле)
     * @return екрановане текстове подання
     */
    private static String escape(Object value) {
        String text = value == null ? "" : String.valueOf(value);

        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")
                || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}