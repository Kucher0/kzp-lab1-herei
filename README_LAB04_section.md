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
