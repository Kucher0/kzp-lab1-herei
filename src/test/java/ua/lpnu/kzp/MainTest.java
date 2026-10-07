package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void programShouldProcessValidRecords() throws Exception {
        Path dataDir = Path.of("data");
        Path inputFile = dataDir.resolve("input.csv");

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
                result.contains("version 1.0.0"),
                "Опція --version повинна повертати версію програми"
        );
    }
}