package cl.keber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;



@DisplayName("Validación de fechas en ProgramaFormativo")
class ProgramaFormativoTest {

    @Test
    void debeFallarSiFechaTerminoEsAntesDeInicio() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2024, 12, 31);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProgramaFormativo("PF001", "Programa de Prueba", inicio, fin, "Descripción");
        });

        assertEquals("La fecha de término debe ser posterior a la de inicio", exception.getMessage());
    }
}
