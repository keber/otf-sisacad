package cl.keber.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ProgramaNoEncontradoExceptionTest {

    @Test
    void shouldReturnCustomMessage() {
        Long id = 123L;
        ProgramaNoEncontradoException ex = new ProgramaNoEncontradoException(id);
        assertEquals("No se encontró el programa con ID: 123", ex.getMessage());
    }
}
