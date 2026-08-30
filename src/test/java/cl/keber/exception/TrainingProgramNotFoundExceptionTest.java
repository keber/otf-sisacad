package cl.keber.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TrainingProgramNotFoundExceptionTest {

    @Test
    void shouldReturnCustomMessage() {
        Long id = 123L;
        TrainingProgramNotFoundException ex = new TrainingProgramNotFoundException(id);
        assertEquals("Training program not found with ID: 123", ex.getMessage());
    }
}
