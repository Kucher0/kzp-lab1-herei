package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Доводить, що експортер не прив'язаний до класу MovieRecord. */
class UniversalExporterTest {

    @TempDir
    Path tempDir;

    static class Base {
        @CsvColumn("Ідентифікатор")
        private final int id;

        Base(int id) {
            this.id = id;
        }
    }

    static final class City extends Base {
        @CsvColumn("Місто")
        private final String name;

        @CsvColumn("Населення")
        private final long population;

        // Поле без @CsvColumn: експортер має його проігнорувати.
        @SuppressWarnings("unused")
        private final String ignored = "не експортується";

        City(int id, String name, long population) {
            super(id);
            this.name = name;
            this.population = population;
        }
    }

    static final class Plain {
        // Клас без жодного поля з @CsvColumn.
        @SuppressWarnings("unused")
        private final String value = "x";
    }

    @Test
    void exportsAnyAnnotatedClassIncludingInheritedFields() throws Exception {
        Path file = tempDir.resolve("cities.csv");

        CsvExporter.write(file, City.class, List.of(
                new City(1, "Львів", 720000L),
                new City(2, "Місто, \"Центр\"", 1L)));

        List<List<String>> rows = CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8));

        assertEquals(List.of("Ідентифікатор", "Місто", "Населення"), rows.get(0));
        assertEquals(List.of("1", "Львів", "720000"), rows.get(1));
        assertEquals(List.of("2", "Місто, \"Центр\"", "1"), rows.get(2));
        assertEquals(3, rows.size());
    }

    @Test
    void exportsNullValueAsEmptyField() throws Exception {
        Path file = tempDir.resolve("null-value.csv");

        CsvExporter.write(file, City.class, List.of(new City(3, null, 5L)));

        List<List<String>> rows = CsvParser.parseDocument(
                Files.readString(file, StandardCharsets.UTF_8));

        assertEquals(List.of("3", "", "5"), rows.get(1));
    }

    @Test
    void rejectsClassWithoutAnnotatedFields() {
        Path file = tempDir.resolve("plain.csv");

        DataStorageException exception = assertThrows(
                DataStorageException.class,
                () -> CsvExporter.write(file, Plain.class, List.of()));

        assertTrue(exception.getMessage().contains("Plain"));
    }

    @Test
    void annotationIsReadableAtRuntime() throws NoSuchFieldException {
        Field field = City.class.getDeclaredField("name");

        assertEquals("Місто", field.getAnnotation(CsvColumn.class).value());
    }

    @Test
    void annotationHasRuntimeRetentionAndFieldTarget() {
        Retention retention = CsvColumn.class.getAnnotation(Retention.class);
        Target target = CsvColumn.class.getAnnotation(Target.class);

        assertEquals(RetentionPolicy.RUNTIME, retention.value());
        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target.value());
    }
}