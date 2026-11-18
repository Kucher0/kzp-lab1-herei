package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище об'єктів у пам'яті.
 *
 * @param <T> тип об'єктів, які зберігаються
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item запис для додавання
     * @throws NullPointerException якщо запис дорівнює null
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Повертає незмінний знімок усіх записів.
     *
     * @return незмінний список записів
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, які задовольняють задану умову.
     *
     * @param condition умова пошуку
     * @return незмінний список знайдених записів
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Умова пошуку не може бути null");
        return items.stream()
                .filter(condition)
                .toList();
    }
}