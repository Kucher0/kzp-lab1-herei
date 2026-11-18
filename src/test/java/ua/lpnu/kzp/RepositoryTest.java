package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    @Test
    void shouldStoreAndReturnAllItems() {
        Repository<String> repository = new Repository<>();

        repository.add("Film 1");
        repository.add("Film 2");

        assertEquals(List.of("Film 1", "Film 2"), repository.all());
    }

    @Test
    void shouldFindItemsByPredicate() {
        Repository<Integer> repository = new Repository<>();

        repository.add(10);
        repository.add(20);
        repository.add(30);

        assertEquals(List.of(20, 30), repository.find(number -> number >= 20));
    }

    @Test
    void shouldReturnImmutableList() {
        Repository<String> repository = new Repository<>();
        repository.add("Film");

        assertThrows(
                UnsupportedOperationException.class,
                () -> repository.all().add("Another film")
        );
    }

    @Test
    void shouldRejectNull() {
        Repository<String> repository = new Repository<>();

        assertThrows(NullPointerException.class, () -> repository.add(null));
    }
}