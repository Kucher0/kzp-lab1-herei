package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {

    /* Дані з data/input.csv лабораторної № 1. */
    private static final List<String> LAB01_DATA = List.of(
            "Інтерстеллар;Крістофер Нолан;2014;169;8.7",
            "Титанік;Джеймс Кемерон;1997;194;7.9",
            "Матриця;Лана Вачовскі;1999;136;8.7",
            "Захар Беркут;Ахтем Сеітаблаєв;2019;113;7.1",
            "Дюна;Дені Вільньов;2021;155;8.0",
            "Аватар;Джеймс Кемерон;2009;162;помилка",
            "Порожній режисер;;2020;120;6.5");

    /* Звіт має збігатися з виводом ЛР № 1 (докази збереження поведінки). */
    @Test
    void reportMatchesLab01Output() {
        List<String> errors = new ArrayList<>();
        YearRating summary = CatalogService.summarize(CatalogService.parse(LAB01_DATA, errors));
        String expected = String.join(System.lineSeparator(),
                "КАТАЛОГ ФІЛЬМІВ",
                "",
                "Кількість коректних записів: 5",
                "Середній рейтинг: 8.08",
                "Найдовший фільм: 194 хв",
                "Найстаріший рік: 1997",
                "",
                "Помилки:",
                "Рядок 6: неправильне числове значення",
                "Рядок 7: порожній режисер") + System.lineSeparator();
        assertEquals(expected, CatalogService.buildReport(summary, errors));
    }

    /* Хибні рядки не потрапляють у список і зберігають номери рядків. */
    @Test
    void parseKeepsLineNumbersAndMessages() {
        List<String> errors = new ArrayList<>();
        List<Movie> movies = CatalogService.parse(List.of(
                "Тіні забутих предків;Сергій Параджанов;1965;97;8.2",
                ";Хтось;2000;100;7.0",
                "Матриця;Вачовскі;1999;сто;8.7",
                "Нуль;Хтось;0;90;5.0",
                "Нуль;Хтось;2000;0;5.0",
                "Нуль;Хтось;2000;90;-1",
                "Лише;три;поля"), errors);
        assertEquals(1, movies.size());
        assertEquals(List.of(
                "Рядок 2: порожня назва фільму",
                "Рядок 3: неправильне числове значення",
                "Рядок 4: рік має бути додатним",
                "Рядок 5: тривалість має бути додатною",
                "Рядок 6: рейтинг не може бути від'ємним",
                "Рядок 7: неправильна кількість полів"), errors);
    }

    /* Як у ЛР № 1, порожні рядки пропускаються без помилки. */
    @Test
    void parseSkipsBlankLinesSilently() {
        List<String> errors = new ArrayList<>();
        List<Movie> movies = CatalogService.parse(List.of("", "   "), errors);
        assertTrue(movies.isEmpty());
        assertTrue(errors.isEmpty());
    }

    /* Середнє має бути дробовим, а не цілочисловим. */
    @Test
    void summarizeComputesAllMetrics() {
        List<Movie> movies = List.of(
                new Movie("A", "X", 1965, 97, 8.0),
                new Movie("B", "Y", 1994, 142, 9.0),
                new Movie("C", "Z", 2020, 100, 6.0));
        YearRating summary = CatalogService.summarize(movies);
        assertEquals(3, summary.count());
        assertEquals(7.6667, summary.averageRating(), 0.0001);
        assertEquals(142, summary.longestMinutes());
        assertEquals(1965, summary.oldestYear());
    }

    @Test
    void summarizeOfEmptyListIsZero() {
        assertEquals(new YearRating(0, 0.0, 0, 0), CatalogService.summarize(List.of()));
    }

    /* Файл без жодного коректного запису не ділить на нуль. */
    @Test
    void reportForNoValidRecordsHasZeros() {
        List<String> errors = new ArrayList<>();
        List<Movie> movies = CatalogService.parse(List.of("сміття"), errors);
        String report = CatalogService.buildReport(CatalogService.summarize(movies), errors);
        assertTrue(report.contains("Кількість коректних записів: 0"));
        assertTrue(report.contains("Середній рейтинг: 0.00"));
        assertTrue(report.contains("Рядок 1: неправильна кількість полів"));
    }
}
