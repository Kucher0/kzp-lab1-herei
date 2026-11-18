# Звіт до лабораторної роботи № 5

Варіант: 8 — Каталог фільмів

Операційна система: Windows.

Виконав: ст. КІ-304 Герей Владислав

## 1. Тема, номер і варіант
Параметризоване сховище, винятки й CSV-експорт у Java-проєкті. Предметна область: Каталог фільмів. Варіант: 8 (незмінний). ОС: Windows <версія>.

## 2. Мета роботи
Завершити збереження даних продукту: `Repository<T>` замість прямого списку, власний виняток із `cause`, анотація `@CsvColumn`,
універсальний рефлексивний CSV-експортер, читання назад і доведення round-trip. Версія 1.4.0 (MINOR), тег `v1.4.0`.

## 3. Стан до і після цієї роботи
| Було (v1.3.0) | Стало (v1.4.0) |
|---|---|
| дані фільмів у `List` у пам'яті, звіт лише в `.txt` | `Repository<Movie>` і `Repository<MovieRecord>` |
| помилки лише в повідомленнях розбору рядків | `DataStorageException` із `cause` |
| експорту не було | `CsvExporter` (рефлексія + `@CsvColumn`), `CsvParser`, ключ `--csv` |
| — | round-trip у `Main` і в тестах |
Усі запити Stream API, рекомендаційні бали та формат звіту збережено.

## 4. Repository<T>
`Repository<T>` — параметр типу `T`, raw types не застосовуються. `add` відхиляє `null`; `all()` повертає `List.copyOf` (незмінний знімок,
пізніші додавання його не змінюють); `find(Predicate<? super T>)` фільтрує потоком і повертає незмінний список. Використано як
`Repository<Movie>` і `Repository<MovieRecord>`.

## 5. Модель даних
| Поле | Тип | CSV-стовпець | Інваріант |
|---|---|---|---|
| `title` | `String` | Назва | не null, не порожня |
| `director` | `String` | Режисер | не null, не порожній |
| `year` | `int` | Рік | > 0 |
| `durationMinutes` | `int` | Тривалість | > 0 |
| `rating` | `double` | Оцінка | скінченне, 0–10 |
Клас `MovieRecord` незмінний; `equals`/`hashCode` — за всіма п'ятьма полями (`Double.compare`). Ключ round-trip: назва + рік.
Вид фільму (`MovieKind`) не входить до полів варіанта, тому не експортується.

## 6. @CsvColumn
`@Retention(RUNTIME)` — анотацію видно рефлексії під час виконання; `@Target(FIELD)` — лише поля; один елемент `String value()`.
Порядок стовпців задає не анотація, а сортування Java-імен полів: `director`, `durationMinutes`, `rating`, `title`, `year`.

## 7. Рефлексивний експортер
`annotatedFields` проходить клас і суперкласи до `Object`, залишає нестатичні поля з `@CsvColumn`, сортує за іменем, викликає `trySetAccessible()`
(помилка → `DataStorageException`). Один і той самий список полів будує заголовок і всі рядки. Значення — `String.valueOf`, `null` → порожнє поле.
Експортер не містить `if` за класом чи підтипом; це доведено тестом на іншому класі (`UniversalExporterTest`: успадковане поле, `null`, клас без анотацій).

## 8. Формат CSV
Заголовок: `Режисер,Тривалість,Оцінка,Назва,Рік`. Кодування UTF-8 без BOM, роздільник рядків — `System.lineSeparator()` (парсер приймає `\n`, `\r\n`, `\r`).
Екранований рядок:
```text
Тестовий Режисер,120,9.0,"Фільм, ""Особливий"" випадок",2020
```
Повний приклад — `docs/expected_csv_lab05.csv`. `split(",")` не підходить, бо розріже поле в лапках (тест `splitIsNotEnoughForQuotedCommas`).

## 9. Імпорт і round-trip
`Files.readString(UTF_8)` → `CsvParser.parseDocument` (машина станів із `insideQuotes` / `afterClosingQuote`) → пропуск заголовка →
`MovieRecord.fromCsv` для кожного запису → порівняння списків через `equals`. На `data/input_lab05.csv`:
фізичних CSV-рядків із заголовком <7>, відновлених об'єктів <6>, Round-trip: <успішно>.

## 10. Помилки
| Тип | Повідомлення | cause | Перевірка |
|---|---|---|---|
| Модель | `IllegalArgumentException` / `NullPointerException` у конструкторі | — | `constructorShouldValidateModel` |
| Формат: кількість полів | «Некоректний CSV-запис: очікується рівно 5 полів» | немає | `shouldRejectWrongFieldCount` |
| Формат: лапки | «CSV містить незакриті подвійні лапки», «Неочікуваний символ після закривальної лапки…», «Лапка всередині неекранованого поля…» | немає | `CsvParserTest` |
| Значення: число | «Некоректне числове значення у CSV-записі» | `NumberFormatException` | `shouldKeepNumberFormatExceptionAsCause` |
| Значення: діапазон | «Некоректні дані фільму у CSV-записі» | `IllegalArgumentException` | `shouldKeepDomainValidationErrorAsCause` |
| Файл | «Не вдалося записати CSV-файл: <шлях>» | `IOException` | `shouldWrapFileErrorWhenPathIsDirectory`, `…ParentIsFile` |
| Рефлексія | «Клас X не містить полів з @CsvColumn», «Немає доступу до поля …» | `SecurityException` / `IllegalAccessException` | `rejectsClassWithoutAnnotatedFields` |
| Дані | «Список містить null замість запису» | немає | `shouldRejectNullRecord` |
`Main` виводить клас винятку, повідомлення й `Причина:`; порожніх `catch` немає. Enum-поля у `MovieRecord` немає; хибний `MovieKind` перевіряється
в `Movie.fromCsv` (тести ЛР № 3).

## 11. Тестування
Нові: `CsvExporterTest`, `UniversalExporterTest`, `DataStorageExceptionTest`, розширені `CsvParserTest`, `MovieRecordRoundTripTest`, `RepositoryTest`, `MainTest`.
Покрито: позитивні (round-trip 5 записів, кома/лапки/перенос рядка), граничні (порожній список, нульовий файл, `TRUNCATE_EXISTING`, CRLF, UTF-8 без BOM),
негативні (кількість полів, число, діапазон, лапки, файл, `null`, клас без анотацій). Усі тести ЛР № 1–4 збережено.
Результат `mvnw.cmd test`: <N tests, 0 failures>. На `input_lab05.csv`: 7 фізичних CSV-рядків, 6 відновлених об'єктів.

## 12. Інфраструктура
| Команда | Результат |
|---|---|
| `.\mvnw.cmd test` | <...> |
| `.\mvnw.cmd verify` (SpotBugs) | <...> |
| `.\mvnw.cmd package` | <...> |
| `java -jar target\kzp-lab1-herei-1.4.0.jar --version` | kzp-lab1-herei version 1.4.0 |
| `java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab05.csv --csv out\movies.csv` | Round-trip: успішно |
CI (Ubuntu, Windows, macOS): <посилання>. Артефакти JAR і CSV: <посилання>. Тег: <v1.4.0>.
Кодування й роздільники: на Windows CSV має CRLF, на Ubuntu/macOS — LF; парсер читає обидва, тест `shouldParseWindowsLineSeparators`; результат на трьох ОС: <...>.

## 13. GitHub Issues і Pull Request
| № Issue | Зміна | Перевірка |
|---|---|---|
| #<N> | `Repository<T>` | `RepositoryTest` |
| #<N> | `@CsvColumn` і рефлексивний експортер | `UniversalExporterTest`, `CsvExporterTest` |
| #<N> | `CsvParser` і екранування | `CsvParserTest` |
| #<N> | `DataStorageException` і помилки | `DataStorageExceptionTest`, `MovieRecordRoundTripTest` |
| #<N> | round-trip у `Main`, дані `input_lab05.csv` | `MainTest` |
| #<N> | документація й CI | README, REPORT, ci.yml |
Pull Request: <посилання> (Closes #<N>).

## 14. Порівняння стану
Попередній звіт збережено в `docs/REPORT_LAB04.md`; `data/input*.csv`, `docs/expected_*` і всі тести ЛР № 1–4 не змінено й проходять разом із новими.
Звіт за замовчуванням доповнено лише розділом «Результати CSV-зберігання».

## 15. Висновки
Продукт зберігає фільми через `Repository<T>`, експортує їх універсальним рефлексивним експортером у UTF-8 CSV і відновлює без втрат
(round-trip доведено тестами). Створений `out/movies.csv` стане вхідним файлом для наступного програмного блоку.