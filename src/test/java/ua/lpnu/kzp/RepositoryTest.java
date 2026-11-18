package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

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
    void shouldAcceptPredicateOfSupertype() {
        Repository<String> repository = new Repository<>();
        repository.add("Film");
        repository.add("Show");

        Predicate<Object> startsWithF = value -> value.toString().startsWith("F");

        assertEquals(List.of("Film"), repository.find(startsWithF));
    }

    @Test
    void shouldReturnImmutableList() {
        Repository<String> repository = new Repository<>();
        repository.add("Film");

        assertThrows(UnsupportedOperationException.class,
                () -> repository.all().add("Another film"));
        assertThrows(UnsupportedOperationException.class,
                () -> repository.find(item -> true).add("Another film"));
    }

    @Test
    void snapshotIsNotAffectedByLaterChanges() {
        Repository<String> repository = new Repository<>();
        repository.add("Film 1");

        List<String> snapshot = repository.all();
        repository.add("Film 2");

        assertEquals(1, snapshot.size());
        assertEquals(2, repository.all().size());
    }

    @Test
    void shouldStoreMovieRecords() {
        Repository<MovieRecord> repository = new Repository<>();
        repository.add(new MovieRecord("Дюна", "Дені Вільньов", 2021, 155, 8.0));
        repository.add(new MovieRecord("Титанік", "Джеймс Кемерон", 1997, 194, 7.9));

        assertEquals(1, repository.find(record -> record.getYear() > 2000).size());
    }

    @Test
    void shouldRejectNullItemAndNullCondition() {
        Repository<String> repository = new Repository<>();

        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
    }
}