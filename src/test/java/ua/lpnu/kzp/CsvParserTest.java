package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvParserTest {

    @Test
    void shouldParseSimpleCsv() throws DataStorageException {
        assertEquals(
                List.of(List.of("Name", "Year"), List.of("Film", "2020")),
                CsvParser.parseDocument("Name,Year\nFilm,2020"));
    }

    @Test
    void shouldParseCommasInsideQuotes() throws DataStorageException {
        List<List<String>> result = CsvParser.parseDocument("\"Film, The Best\",2020");

        assertEquals("Film, The Best", result.get(0).get(0));
        assertEquals("2020", result.get(0).get(1));
    }

    @Test
    void shouldParseEscapedQuotes() throws DataStorageException {
        List<List<String>> result =
                CsvParser.parseDocument("\"Film \"\"The Best\"\"\",2020");

        assertEquals("Film \"The Best\"", result.get(0).get(0));
    }

    @Test
    void shouldParseMultilineFields() throws DataStorageException {
        List<List<String>> result =
                CsvParser.parseDocument("\"First line\nSecond line\",2020");

        assertEquals(1, result.size());
        assertEquals("First line\nSecond line", result.get(0).get(0));
    }

    @Test
    void shouldParseWindowsLineSeparators() throws DataStorageException {
        assertEquals(
                List.of(List.of("a", "b"), List.of("c", "d")),
                CsvParser.parseDocument("a,b\r\nc,d\r\n"));
    }

    @Test
    void shouldParseEmptyFields() throws DataStorageException {
        assertEquals(List.of(List.of("a", "", "c")), CsvParser.parseDocument("a,,c"));
        assertEquals(List.of(List.of("a", "b", "")), CsvParser.parseDocument("a,b,"));
        assertEquals(List.of(List.of("", "x")), CsvParser.parseDocument("\"\",x"));
    }

    @Test
    void shouldReturnEmptyListForEmptyDocument() throws DataStorageException {
        assertTrue(CsvParser.parseDocument("").isEmpty());
    }

    @Test
    void shouldRejectUnclosedQuotes() {
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("\"Unclosed field"));
    }

    @Test
    void shouldRejectUnexpectedCharactersAfterQuote() {
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("\"Film\"text,2020"));
    }

    @Test
    void shouldRejectQuoteInsideUnquotedField() {
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("ab\"c,2020"));
    }

    @Test
    void shouldRejectNullDocument() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument(null));
    }

    @Test
    void parseShouldReturnSingleRecord() throws DataStorageException {
        assertEquals(List.of("a", "b"), CsvParser.parse("a,b"));
    }

    @Test
    void parseShouldRejectSeveralRecordsAndEmptyText() {
        assertThrows(DataStorageException.class, () -> CsvParser.parse("a\nb"));
        assertThrows(DataStorageException.class, () -> CsvParser.parse(""));
    }

    @Test
    void splitIsNotEnoughForQuotedCommas() throws DataStorageException {
        String line = "\"Film, The Best\",2020";

        assertEquals(3, line.split(",").length);
        assertEquals(2, CsvParser.parse(line).size());
    }
}