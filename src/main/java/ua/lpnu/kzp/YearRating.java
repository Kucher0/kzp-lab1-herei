package ua.lpnu.kzp;

/**
 * Незмінний набір підсумкових показників каталогу фільмів.
 *
 * @param count кількість коректних записів
 * @param averageRating середній рейтинг (0, якщо записів немає)
 * @param longestMinutes тривалість найдовшого фільму (0, якщо записів немає)
 * @param oldestYear найстаріший рік випуску (0, якщо записів немає)
 */
public record YearRating(int count, double averageRating, int longestMinutes, int oldestYear) {

    /** Перевіряє допустимість підсумкових показників. */
    public YearRating {
        if (count < 0 || longestMinutes < 0 || oldestYear < 0
                || !Double.isFinite(averageRating) || averageRating < 0) {
            throw new IllegalArgumentException("Показники не можуть бути від'ємними");
        }
    }
}
