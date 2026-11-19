# Звіт до лабораторної роботи № 5

Варіант: 8 — Каталог фільмів

Операційна система: Windows.

Виконав: ст. КІ-304 Герей Владислав

Прийняв: Грицько Т.Л.
## 1. Тема, номер і варіант
Параметризоване сховище, винятки й CSV-експорт у Java-проєкті. Предметна область: Каталог фільмів. Варіант: 8 (незмінний).
Клас запису: `MovieRecord` — `title`, `director`, `year`, `durationMinutes`, `rating`; CSV-стовпці: назва, режисер, рік, тривалість, оцінка;
ключ round-trip: `title` + `year`. ОС: Windows <версія>.

## 2. Мета роботи
Завершити збереження даних продукту: `Repository<T>` замість прямого списку, власний виняток із `cause`, анотація `@CsvColumn`,
універсальний рефлексивний CSV-експортер, читання назад і доведення round-trip. Версія 1.4.0 (MINOR), тег `v1.4.0`.

## 3. Стан до і після цієї роботи
| Було (v1.3.0) | Стало (v1.4.0) |
|---|---|
| дані фільмів у `List` у пам'яті, звіт лише в `.txt` | `Repository<Movie>` і `Repository<MovieRecord>` |
| помилки лише в повідомленнях розбору рядків | `DataStorageException` із `cause` |
| експорту не було | `CsvExporter` (рефлексія + `@CsvColumn`), `CsvParser`, ключ `--csv` |
| — | round-trip у `Main` і в тестах; перевірка кодування й роздільників рядків у тестах і CI |
Усі запити Stream API, рекомендаційні бали та формат звіту збережено.

## 4. Repository<T>
`Repository<T>` — параметр типу `T`, raw types не застосовуються. `add` відхиляє `null`; `all()` повертає `List.copyOf` (незмінний знімок,
пізніші додавання його не змінюють); `find(Predicate<? super T>)` фільтрує потоком і повертає незмінний список.
`? super T` дозволяє передати умову над супертипом (тест `shouldAcceptPredicateOfSupertype`). Використано як
`Repository<Movie>` і `Repository<MovieRecord>`.

## 5. Модель даних
| Поле | Тип | CSV-стовпець | Інваріант |
|---|---|---|---|
| `title` | `String` | Назва | не null, не порожня |
| `director` | `String` | Режисер | не null, не порожній |
| `year` | `int` | Рік | > 0 |
| `durationMinutes` | `int` | Тривалість | > 0 |
| `rating` | `double` | Оцінка | скінченне, 0–10 |
Клас `MovieRecord` незмінний (усі поля `private final`); `equals`/`hashCode` — за всіма п'ятьма полями (`Double.compare`).
Ключ round-trip: назва + рік. Вид фільму (`MovieKind`) не входить до полів варіанта, тому не експортується.

## 6. @CsvColumn
`@Retention(RUNTIME)` — анотацію видно рефлексії під час виконання; `@Target(FIELD)` — лише поля; один елемент `String value()`.
Анотація лише описує контракт і сама нічого не експортує. Усі п'ять полів `MovieRecord` позначено.
Порядок стовпців задає не анотація, а сортування Java-імен полів: `director`, `durationMinutes`, `rating`, `title`, `year`.

## 7. Рефлексивний експортер
`annotatedFields` проходить клас і суперкласи до `Object`, залишає нестатичні поля з `@CsvColumn`, сортує за іменем, викликає `trySetAccessible()`
(помилка → `DataStorageException`). Один і той самий список полів будує заголовок і всі рядки. Значення — `String.valueOf`, `null` → порожнє поле.
Експортер не містить `if` за класом чи підтипом; це доведено тестом на іншому класі (`UniversalExporterTest`: успадковане поле, `null`, клас без анотацій).
Запис: `Files.writeString(path, text, UTF_8, CREATE, TRUNCATE_EXISTING, WRITE)`; батьківський каталог створюється через `Files.createDirectories`.

## 8. Формат CSV
Заголовок: `Режисер,Тривалість,Оцінка,Назва,Рік`. Кодування UTF-8 без BOM, роздільник рядків — `System.lineSeparator()`
(парсер приймає `\n`, `\r\n`, `\r`). Правила екранування: поле з комою, лапкою або переносом рядка беруть у лапки, лапка всередині подвоюється.

Повний CSV, створений із `data/input_lab05.csv` (файл `docs/expected_csv_lab05.csv`):
```text
Режисер,Тривалість,Оцінка,Назва,Рік
Крістофер Нолан,169,8.7,Інтерстеллар,2014
Сергій Параджанов,97,8.2,Тіні забутих предків,1965
Алістер Фотергілл,50,9.3,Наша планета,2019
Люк Жаке,80,7.7,Мандрівка пінгвінів,2005
Ахтем Сеітаблаєв,113,7.1,Захар Беркут,2019
Тестовий Режисер,120,9.0,"Фільм, ""Особливий"" випадок",2020
```
Екранований рядок (кома й лапки в назві):
```text
Тестовий Режисер,120,9.0,"Фільм, ""Особливий"" випадок",2020
```
`split(",")` не підходить, бо розріже поле в лапках на три частини (тест `splitIsNotEnoughForQuotedCommas`).

## 9. Імпорт і round-trip
Алгоритм: `Files.readString(UTF_8)` → `CsvParser.parseDocument` (машина станів із `insideQuotes` / `afterClosingQuote`) → пропуск заголовка →
`MovieRecord.fromCsv` для кожного запису → порівняння списків через `equals`.

Вхід `data/input_lab05.csv`: 7 рядків, із них 6 коректних і 1 хибний (`Аватар;Джеймс Кемерон;2009;162;помилка` — «Рядок 7: неправильне числове значення»).

Доказ round-trip — фактичний вивід програми
(`java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab05.csv --csv out\movies.csv`):
```text
КАТАЛОГ ФІЛЬМІВ

Кількість коректних записів: 6
Середній рейтинг: 8.33
Найдовший фільм: 169 хв
Найстаріший рік: 1965

Помилки:
Рядок 7: неправильне числове значення

Результати CSV-зберігання:
Кількість записів: 6
CSV-рядків разом із заголовком: 7
Відновлено записів: 6
Round-trip: успішно
```
Порівняння початкового й відновленого списків:

| Показник | Початковий список | Відновлений список |
|---|---|---|
| кількість записів | 6 | 6 |
| `originalRecords.equals(restoredRecords)` | — | `true` |
| запис із комою й лапками | `Фільм, "Особливий" випадок` | `Фільм, "Особливий" випадок` |
| ключі (назва + рік) | збігаються за порядком | збігаються за порядком |

Додатково: порожній набір (`data/empty_lab05.csv`) дає CSV лише із заголовком, `Відновлено записів: 0`, `Round-trip: успішно` (тест `emptyInputGivesHeaderOnlyCsvAndSuccessfulRoundTrip`).

## 10. Помилки
| Тип | Повідомлення | cause | Перевірка |
|---|---|---|---|
| Модель | `IllegalArgumentException` / `NullPointerException` у конструкторі | — | `constructorShouldValidateModel` |
| Формат: кількість полів | «Некоректний CSV-запис: очікується рівно 5 полів» | немає | `shouldRejectWrongFieldCount` |
| Формат: лапки | «CSV містить незакриті подвійні лапки», «Неочікуваний символ після закривальної лапки…», «Лапка всередині неекранованого поля…» | немає | `CsvParserTest` |
| Значення: число | «Некоректне числове значення у CSV-записі» | `NumberFormatException` | `shouldKeepNumberFormatExceptionAsCause` |
| Значення: діапазон | «Некоректні дані фільму у CSV-записі» | `IllegalArgumentException` | `shouldKeepDomainValidationErrorAsCause` |
| Файл | «Не вдалося записати CSV-файл: <шлях>» | `IOException` | `shouldWrapFileErrorWhenPathIsDirectory`, `shouldWrapFileErrorWhenParentIsFile` |
| Рефлексія | «Клас X не містить полів з @CsvColumn», «Немає доступу до поля …» | `SecurityException` / `IllegalAccessException` | `rejectsClassWithoutAnnotatedFields` |
| Дані | «Список містить null замість запису» | немає | `shouldRejectNullRecord` |
| Вхідний файл відсутній | `Помилка роботи зі сховищем або файлами: NoSuchFileException` | `IOException` | `missingInputFileIsReportedWithoutException` |

Приклад помилки файла у виводі `Main` (`java -jar ... --input data\does-not-exist.csv`):
```text
Помилка роботи зі сховищем або файлами: NoSuchFileException: data/does-not-exist.csv
```
Приклад помилки значення в тесті (`shouldKeepNumberFormatExceptionAsCause`): `MovieRecord.fromCsv(List.of("Режисер", "abc", "8.0", "Назва", "2000"))`
кидає `DataStorageException("Некоректне числове значення у CSV-записі")`, а `getCause()` — `NumberFormatException`.
`Main` виводить клас винятку, повідомлення й `Причина:`; порожніх `catch` немає, жоден виняток не поглинається. Enum-поля у `MovieRecord` немає;
хибний `MovieKind` перевіряється в `Movie.fromCsv` (тести ЛР № 3).

## 11. Тестування
Нові тести ЛР № 5: `RepositoryTest`, `CsvParserTest`, `CsvExporterTest`, `UniversalExporterTest`, `DataStorageExceptionTest`,
`MovieRecordRoundTripTest`, `CsvPortabilityTest` (кодування, BOM, роздільники рядків, збіг з `docs/expected_csv_lab05.csv`), доповнений `MainTest`.
Покрито:
- позитивні: round-trip 5 записів, кома, лапки, перенос рядка;
- граничні: порожній список, порожній файл, `TRUNCATE_EXISTING`, CRLF, UTF-8 без BOM, порожній вхід `Main`;
- негативні: кількість полів, число, діапазон, лапки, файлова помилка, `null`, клас без анотацій.

Усі тести ЛР № 1–4 збережено й запускаються разом із новими (`MovieTest`, `MovieHierarchyTest`, `MovieQueriesTest`, `YearRatingTest`, `CatalogServiceTest`, `MainTest`).

Кількість: 139 методів `@Test` (133 було до додавання `CsvPortabilityTest` і одного тесту в `MainTest`).
Результат `.\mvnw.cmd test`: <Tests run: 139, Failures: 0, Errors: 0, Skipped: 0>.
Фізичних CSV-рядків (із заголовком) на `input_lab05.csv`: 7, відновлених об'єктів: 6.

## 12. Інфраструктура
| Команда | Результат |
|---|---|
| `.\mvnw.cmd test` | <BUILD SUCCESS, Tests run: 139, Failures: 0> |
| `.\mvnw.cmd verify` (SpotBugs) | <BUILD SUCCESS, BugInstance size is 0> |
| `.\mvnw.cmd package` | <BUILD SUCCESS, target\kzp-lab1-herei-1.4.0.jar> |
| `java -jar target\kzp-lab1-herei-1.4.0.jar --version` | kzp-lab1-herei version 1.4.0 |
| `java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab05.csv --csv out\movies.csv` | Round-trip: успішно |

CI (Ubuntu, Windows, macOS): <посилання на зелений запуск>. Артефакти: JAR `jar-<ОС>-<номер>` і CSV `csv-<ОС>-<номер>`: <посилання>.
`target/`, `out/` і локальні файли не потрапили до репозиторію (`.gitignore`).

### Версія і тег
Версія 1.4.0 (MINOR — нові можливості без порушення сумісності), змінена в `pom.xml`, `Main` (`--version`), `MainTest`, `ci.yml`, README.
Тег: `v1.4.0` — <посилання на тег / реліз>.

### Кодування та роздільники рядків на різних ОС
Експортер завжди пише UTF-8 без BOM; між рядками CSV стоїть `System.lineSeparator()`. Перевірка виконується двічі:
у JUnit (`CsvPortabilityTest`) та в CI-кроці «Check CSV encoding and line separators» (перші байти, відсутність BOM, кількість CR і LF).

| ОС | Перші байти файла | BOM | CR | LF | Round-trip | Джерело |
|---|---|---|---|---|---|---|
| Ubuntu | `d0 a0 d0 b5` («Ре…») | немає | 0 | 7 | успішно | <лог CI> |
| macOS | `d0 a0 d0 b5` | немає | 0 | 7 | успішно | <лог CI> |
| Windows | `d0 a0 d0 b5` | немає | 7 | 7 | успішно | <лог CI> |

Значення в таблиці — очікувані й відтворені локально (Linux; Windows — з `-Dline.separator=CRLF`); після зеленого запуску CI підставте фактичні рядки з логів.
Парсер читає і CRLF, і LF (тест `parserRestoresSameRecordsFromLfAndCrLfFiles`). Кирилиця в консолі Windows залежить від кодової сторінки
(`chcp 65001` або `-Dstdout.encoding=UTF-8`); файли від консолі не залежать. `.gitattributes` фіксує LF для `*.java`, `*.csv`, `*.md`, `*.yml`, `*.xml`, тому еталонні файли однакові на всіх ОС.

## 13. GitHub Issues і Pull Request
| № GitHub Issue | Зміна | Перевірка |
|---|---|---|
| #<N> | `Repository<T>` | `RepositoryTest` |
| #<N> | `@CsvColumn` і рефлексивний експортер | `UniversalExporterTest`, `CsvExporterTest` |
| #<N> | `CsvParser` і екранування | `CsvParserTest` |
| #<N> | `DataStorageException` і помилки | `DataStorageExceptionTest`, `MovieRecordRoundTripTest` |
| #<N> | round-trip у `Main`, дані `input_lab05.csv`, `empty_lab05.csv` | `MainTest` |
| #<N> | кодування та роздільники рядків | `CsvPortabilityTest`, крок CI |
| #<N> | документація й CI | README, REPORT, ci.yml |

Гілка: `lab05-storage-csv`. Pull Request: <посилання> (`Closes #<N>`), містить приклад CSV, результат round-trip, таблицю помилок і посилання на CI.
Зауваження рецензування та їх виправлення: <опишіть або напишіть «зауважень не було»>.

## 14. Порівняння стану
Попередній звіт збережено в `docs/REPORT_LAB04.md` (так само `REPORT_LAB01–03`); `data/input.csv`, `input_lab03.csv`, `input_lab04.csv`,
`docs/expected_scores_lab03.txt`, `docs/expected_queries_lab04.txt` і всі тести ЛР № 1–4 не змінено й проходять разом із новими.
Звіт за замовчуванням доповнено лише розділом «Результати CSV-зберігання». Нові файли: `data/input_lab05.csv`, `data/empty_lab05.csv`, `docs/expected_csv_lab05.csv`.

## 15. Висновки
Продукт зберігає фільми через `Repository<T>`, експортує їх універсальним рефлексивним експортером у UTF-8 CSV і відновлює без втрат
(round-trip доведено тестами та виводом програми). Спеціальні символи (кома, лапки, перенос рядка, кирилиця) обробляються коректно; помилки
розрізняються за рівнями й зберігають `cause`. Створений `out/movies.csv` стане вхідним файлом для наступного програмного блоку.
Версія 1.4.0, тег `v1.4.0`, CI на трьох ОС: <посилання>.
