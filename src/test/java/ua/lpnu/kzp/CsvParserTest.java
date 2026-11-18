
package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    @Test
    void shouldParseSimpleCsv() throws DataStorageException {
        String csv = "Name,Year\nFilm,2020";

        List<List<String>> result = CsvParser.parseDocument(csv);

        assertEquals(
                List.of(
                        List.of("Name", "Year"),
                        List.of("Film", "2020")
                ),
                result
        );
    }

    @Test
    void shouldParseCommasInsideQuotes() throws DataStorageException {
        String csv = "\"Film, The Best\",2020";

        List<List<String>> result = CsvParser.parseDocument(csv);

        assertEquals("Film, The Best", result.get(0).get(0));
        assertEquals("2020", result.get(0).get(1));
    }

    @Test
    void shouldParseEscapedQuotes() throws DataStorageException {
        String csv = "\"Film \"\"The Best\"\"\",2020";

        List<List<String>> result = CsvParser.parseDocument(csv);

        assertEquals("Film \"The Best\"", result.get(0).get(0));
    }

    @Test
    void shouldParseMultilineFields() throws DataStorageException {
        String csv = "\"First line\nSecond line\",2020";

        List<List<String>> result = CsvParser.parseDocument(csv);

        assertEquals("First line\nSecond line", result.get(0).get(0));
    }

    @Test
    void shouldReturnEmptyListForEmptyDocument() throws DataStorageException {
        assertTrue(CsvParser.parseDocument("").isEmpty());
    }

    @Test
    void shouldRejectUnclosedQuotes() {
        assertThrows(
                DataStorageException.class,
                () -> CsvParser.parseDocument("\"Unclosed field")
        );
    }

    @Test
    void shouldRejectUnexpectedCharactersAfterQuote() {
        assertThrows(
                DataStorageException.class,
                () -> CsvParser.parseDocument("\"Film\"text,2020")
        );
    }
}
