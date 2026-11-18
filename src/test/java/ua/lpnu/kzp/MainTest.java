package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void programShouldProcessValidRecords() throws Exception {
        Path inputFile = Path.of("data").resolve("input.csv");

        assertTrue(Files.exists(inputFile), "Файл data/input.csv повинен існувати");

        String content = Files.readString(inputFile, StandardCharsets.UTF_8);

        assertTrue(content.contains("К"), "Файл повинен містити українські символи");
    }

    @Test
    void programShouldCreateReport() throws Exception {
        Main.main(new String[0]);

        Path reportFile = Path.of("out", "report.txt");

        assertTrue(
                Files.exists(reportFile),
                "Після запуску повинен створюватися out/report.txt"
        );
    }

    @Test
    void versionOptionShouldWork() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        System.setOut(new PrintStream(output));

        try {
            Main.main(new String[]{"--version"});
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString(StandardCharsets.UTF_8);

        assertTrue(
                result.contains("version 1.4.0"),
                "Опція --version повинна повертати версію програми"
        );
    }

    @Test
    void scoresOptionAddsPolymorphicSection() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            Main.main(new String[]{
                "--input", "data/input_lab03.csv",
                "--output", "target/lab03-report.txt",
                "--csv", "target/lab03-movies.csv",
                "--scores"
            });
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Рекомендаційні бали:"));
        assertTrue(result.contains("Наша планета (документальний): 94.40"));
        assertTrue(Files.readString(Path.of("target", "lab03-report.txt"),
                StandardCharsets.UTF_8).contains("Унікальних фільмів: 4"));
    }

    @Test
    void queriesAndFindOptionsAddStreamSection() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            Main.main(new String[]{
                "--input", "data/input_lab04.csv",
                "--output", "target/lab04-report.txt",
                "--csv", "target/lab04-movies.csv",
                "--queries",
                "--find", "Інтерстеллар"
            });
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Запити Stream API:"));
        assertTrue(result.contains("Топ-5 за оцінкою:"));
        assertTrue(result.contains("Пошук «Інтерстеллар»: Інтерстеллар (Крістофер Нолан, 2014)"));
        assertTrue(Files.readString(Path.of("target", "lab04-report.txt"),
                StandardCharsets.UTF_8).contains("Кількість коректних записів: 8"));
    }

    @Test
    void csvOptionWritesFileAndRoundTripSucceeds() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path csv = Path.of("target", "lab05-movies.csv");

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            Main.main(new String[]{
                "--input", "data/input_lab05.csv",
                "--output", "target/lab05-report.txt",
                "--csv", "target/lab05-movies.csv"
            });
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString(StandardCharsets.UTF_8);
        List<List<String>> rows =
                CsvParser.parseDocument(Files.readString(csv, StandardCharsets.UTF_8));

        assertTrue(result.contains("Round-trip: успішно"));
        assertTrue(result.contains("Відновлено записів: 6"));
        assertEquals(
                List.of("Режисер", "Тривалість", "Оцінка", "Назва", "Рік"),
                rows.get(0));
        assertEquals(7, rows.size());
        assertEquals("Фільм, \"Особливий\" випадок", rows.get(6).get(3));
    }

    @Test
    void missingInputFileIsReportedWithoutException() {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        System.setErr(new PrintStream(error, true, StandardCharsets.UTF_8));

        try {
            Main.main(new String[]{"--input", "data/does-not-exist.csv"});
        } finally {
            System.setErr(originalErr);
        }

        String message = error.toString(StandardCharsets.UTF_8);

        assertTrue(message.contains("Помилка роботи зі сховищем або файлами"));
        assertTrue(message.contains("NoSuchFileException"));
    }
}