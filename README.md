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
Кодування — завжди UTF-8 (`Path` + `Files` + `StandardCharsets.UTF_8`), файл перезаписується з `TRUNCATE_EXISTING`.

Запуск:
```text
.\mvnw.cmd verify
java -jar target\kzp-lab1-herei-1.4.0.jar --input data\input_lab05.csv --csv out\movies.csv
```
Приклад результату: `docs/expected_csv_lab05.csv`. Попередні звіти: `docs/`.
Для коректної кирилиці в консолі Windows: `chcp 65001`.