package cl.keber.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ProgramaFormativoDTOTest {

    @Test
    void shouldSetAndGetFields() {
        ProgramaFormativoDTO dto = new ProgramaFormativoDTO();
        dto.setCodigo("PF001");
        dto.setNombre("Programa A");

        assertEquals("PF001", dto.getCodigo());
        assertEquals("Programa A", dto.getNombre());
    }
}
