## Ієрархія фільмів і поліморфізм (лабораторна № 3)
`Movie` — абстрактний базовий клас: спільні поля (назва, режисер, рік, тривалість, рейтинг, вид),
перевірка інваріантів, `fromCsv`, `equals`/`hashCode`. Абстрактний метод `recommendationScore()`
реалізують підтипи:
- `FeatureFilm` (художній): бал = рейтинг × 10;
- `Documentary` (документальний): бал = рейтинг × 8 + 20.

`MovieKind` — enum `FEATURE("художній")`, `DOCUMENTARY("документальний")`; він же створює потрібний підтип.

Формат CSV: `назва;режисер;рік;хвилини;рейтинг[;вид]`. Без шостого поля фільм вважається художнім,
тому файли попередніх робіт працюють без змін, а звіт за замовчуванням не змінився.

Рівність: підтип + назва + режисер + рік (`HashSet` не дублює такі фільми).

Додатковий ключ `--scores` додає до звіту розділ «Рекомендаційні бали»:
```text
java -jar target\kzp-lab1-herei-1.2.0.jar --input data\input_lab03.csv --scores
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

N = 5, поріг оцінки = 8.0. Підсумок звіту (`summary`) тепер теж обчислюється потоками.
Звіт за замовчуванням не змінився. Додаткові ключі:
```text
java -jar target\kzp-lab1-herei-1.3.0.jar --input data\input_lab04.csv --queries
java -jar target\kzp-lab1-herei-1.3.0.jar --input data\input_lab04.csv --find Довбуш
```
