
package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Парсер CSV-документів із підтримкою лапок і багаторядкових полів.
 */
public final class CsvParser {

    private CsvParser() {
        // Utility class.
    }

    /**
     * Розбирає весь CSV-документ на записи та поля.
     *
     * @param document повний текст CSV-документа
     * @return список записів, кожен із яких містить список полів
     * @throws DataStorageException якщо CSV має неправильний формат
     */
    public static List<List<String>> parseDocument(String document)
            throws DataStorageException {

        if (document == null) {
            throw new DataStorageException(
                    "CSV-документ не може бути null");
        }

        if (document.isEmpty()) {
            return List.of();
        }

        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();

        boolean insideQuotes = false;
        boolean afterClosingQuote = false;

        for (int i = 0; i < document.length(); i++) {
            char current = document.charAt(i);

            if (insideQuotes) {
                if (current == '"') {
                    if (i + 1 < document.length()
                            && document.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        insideQuotes = false;
                        afterClosingQuote = true;
                    }
                } else {
                    field.append(current);
                }

                continue;
            }

            if (afterClosingQuote) {
                if (current == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    afterClosingQuote = false;

                } else if (current == '\n' || current == '\r') {
                    row.add(field.toString());
                    rows.add(List.copyOf(row));

                    row.clear();
                    field.setLength(0);
                    afterClosingQuote = false;

                    if (current == '\r'
                            && i + 1 < document.length()
                            && document.charAt(i + 1) == '\n') {
                        i++;
                    }

                } else {
                    throw new DataStorageException(
                            "Неочікуваний символ після закривальної лапки"
                                    + " у позиції " + i);
                }

                continue;
            }

            if (current == '"') {
                if (!field.isEmpty()) {
                    throw new DataStorageException(
                            "Лапка всередині неекранованого поля"
                                    + " у позиції " + i);
                }

                insideQuotes = true;

            } else if (current == ',') {
                row.add(field.toString());
                field.setLength(0);

            } else if (current == '\n' || current == '\r') {
                row.add(field.toString());
                rows.add(List.copyOf(row));

                row.clear();
                field.setLength(0);

                if (current == '\r'
                        && i + 1 < document.length()
                        && document.charAt(i + 1) == '\n') {
                    i++;
                }

            } else {
                field.append(current);
            }
        }

        if (insideQuotes) {
            throw new DataStorageException(
                    "CSV містить незакриті подвійні лапки");
        }

        boolean hasPendingData = !row.isEmpty()
                || !field.isEmpty()
                || afterClosingQuote
                || document.endsWith(",");

        if (hasPendingData) {
            row.add(field.toString());
            rows.add(List.copyOf(row));
        }

        return List.copyOf(rows);
    }
}
