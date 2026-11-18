# Каталог фільмів — KZP, варіант 8 (версія 1.4.0)

Java-застосунок наскрізного продукту: читає каталог фільмів, обчислює підсумки, виконує потокові запити,
зберігає записи в параметризованому сховищі та експортує їх у CSV (UTF-8) із перевіркою round-trip.

## Швидкий старт

Потрібні Java 21 та інтернет для першого завантаження Maven Wrapper. Окремо Maven встановлювати не треба.

| Дія | Windows (PowerShell / cmd) | macOS / Ubuntu |
|---|---|---|
| Тести | `.\mvnw.cmd test` | `./mvnw test` |
| Тести + SpotBugs | `.\mvnw.cmd verify` | `./mvnw verify` |
| Виконуваний JAR | `.\mvnw.cmd package` | `./mvnw package` |
| Запуск JAR | `java -jar target\kzp-lab1-herei-1.4.0.jar` | `java -jar target/kzp-lab1-herei-1.4.0.jar` |

Для коректної кирилиці в консолі Windows: `chcp 65001` або `java -Dstdout.encoding=UTF-8 -jar ...`.
Файли (звіт і CSV) завжди записуються в UTF-8 незалежно від налаштувань консолі.

Параметри програми: `--help`, `--version`, `--input <файл>`, `--output <файл>`, `--csv <файл>`, `--scores`, `--queries`, `--find <назва>`.

## Ієрархія фільмів і поліморфізм (лабораторна № 3)
`Movie` — абстрактний базовий клас: спільні поля (назва, режисер, рік, тривалість, рейтинг, вид),
перевірка інваріантів, `fromCsv`, `equals`/`hashCode`. Абстрактний метод `recommendationScore()`
реалізують підтипи:
- `FeatureFilm` (художній): бал = рейтинг × 10;
- `Documentary` (документальний): бал = рейтинг × 8 + 20.

`MovieKind` — enum `FEATURE("художній")`, `DOCUMENTARY("документальний")`; він же створює потрібний підтип.

Формат вхідного CSV: `назва;режисер;рік;хвилини;рейтинг[;вид]`. Без шостого поля фільм вважається художнім,
тому файли попередніх робіт працюють без змін.

Рівність: підтип + назва + режисер + рік (`HashSet` не дублює такі фільми).

```text
java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab03.csv --scores
```

## Потокова обробка (лабораторна № 4)
Запити винесено в клас `MovieQueries` (чисті статичні методи, без друку й без зміни вхідного списку):

| Запит | Метод | Операції Stream API | Результат |
|---|---|---|---|
| відбір | `highlyRated` | `filter` (оцінка ≥ 8.0) | `List<Movie>` |
| перетворення | `titles` | `map` | `List<String>` |
| групування | `averageRatingByDirector` | `groupingBy` + `averagingDouble` (`TreeMap`) | `Map<String, Double>` |
| статистика | `durationStatistics` | `summarizingInt` | `IntSummaryStatistics` |
| top-N | `topByRating` | `sorted` (оцінка за спаданням, потім назва) + `limit` | `List<Movie>` |
| пошук | `findByTitle` | `filter` + `findFirst` | `Optional<Movie>` |

N = 5, поріг оцінки = 8.0.
```text
java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab04.csv --queries
java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab04.csv --find Довбуш
```

## Параметризоване сховище, винятки й CSV-експорт (лабораторна № 5, версія 1.4.0)
Варіант 8 — «Каталог фільмів». Дані з `Movie` переносяться в плоский `MovieRecord` і зберігаються в CSV.

| Компонент | Призначення |
|---|---|
| `Repository<T>` | типобезпечне сховище: `add`, `all` (незмінний знімок), `find(Predicate<? super T>)` |
| `@CsvColumn` | анотація (`RUNTIME`, `FIELD`) з назвою стовпця |
| `CsvExporter` | універсальний рефлексивний експортер, не знає жодного предметного класу |
| `CsvParser` | документний парсер (машина станів), розрізняє коми/переноси всередині лапок |
| `DataStorageException` | checked-виняток із конструктором `(message, cause)` |
| `MovieRecord` | `title`, `director`, `year`, `durationMinutes`, `rating`; `fromCsv`, `fromCsvLine` |

Порядок стовпців — сортування Java-імен полів, тому заголовок такий:
```text
Режисер,Тривалість,Оцінка,Назва,Рік
```
Екранування: поле з комою, лапкою або переносом рядка беруть у `"..."`, лапка всередині подвоюється:
```text
Тестовий Режисер,120,9.0,"Фільм, ""Особливий"" випадок",2020
```
Кодування — завжди UTF-8 без BOM (`Path` + `Files` + `StandardCharsets.UTF_8`), файл перезаписується з `TRUNCATE_EXISTING`.
Роздільник рядків CSV — `System.lineSeparator()` (CRLF у Windows, LF в Ubuntu/macOS); парсер читає `\n`, `\r\n` і `\r`.

Запуск:
```text
.\mvnw.cmd verify
java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab05.csv --csv out\movies.csv
java -jar target\kzp-lab1-herei-1.4.0.jar --input data\empty_lab05.csv --csv out\empty.csv
```
Очікуваний вивід для `input_lab05.csv`: 6 коректних записів, 1 хибний рядок (`Рядок 7`), `CSV-рядків разом із заголовком: 7`,
`Відновлено записів: 6`, `Round-trip: успішно`. Для порожнього `empty_lab05.csv` CSV містить лише заголовок, `Відновлено записів: 0`.

Дані лабораторної № 5: `data/input_lab05.csv` (6 коректних записів, один — із комою й лапками, один хибний рядок),
`data/empty_lab05.csv` (порожній набір). Еталонний CSV: `docs/expected_csv_lab05.csv`. Попередні звіти: `docs/`.

## Версії та теги
| Лабораторна | Версія | Git-тег |
|---|---|---|
| № 1 | 1.0.0 | `v1.0.0` |
| № 2 | 1.1.0 | `v1.1.0` |
| № 3 | 1.2.0 | `v1.2.0` |
| № 4 | 1.3.0 | `v1.3.0` |
| № 5 | 1.4.0 | `v1.4.0` |

## CI
GitHub Actions (`.github/workflows/ci.yml`) на Ubuntu, Windows і macOS виконує `verify` (усі тести + SpotBugs + JAR),
запускає JAR на `data/input_lab05.csv`, перевіряє round-trip, відсутність BOM та роздільники рядків CSV
і публікує JAR та CSV як артефакти.
