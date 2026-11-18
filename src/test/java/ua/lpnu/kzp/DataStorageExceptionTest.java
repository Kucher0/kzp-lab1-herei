package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class DataStorageExceptionTest {

    @Test
    void keepsMessageAndCause() {
        IOException cause = new IOException("disk");

        DataStorageException exception =
                new DataStorageException("Не вдалося записати", cause);

        assertEquals("Не вдалося записати", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void worksWithoutCause() {
        DataStorageException exception = new DataStorageException("Помилка формату");

        assertEquals("Помилка формату", exception.getMessage());
        assertNull(exception.getCause());
    }
}