package cl.keber.mapper;

import cl.keber.dto.ProgramaFormativoDTO;
import cl.keber.model.ProgramaFormativo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProgramaFormativoMapperTest {

    @Test
    @DisplayName("Debe convertir de Entidad a DTO correctamente")
    void debeConvertirA_DTO() {
        ProgramaFormativo entidad = new ProgramaFormativo("PF001", "Curso Seguridad", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), "Activo");

        ProgramaFormativoDTO dto = ProgramaFormativoMapper.toDTO(entidad);

        assertEquals("PF001", dto.getCodigo());
        assertEquals("Curso Seguridad", dto.getNombre());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getFechaInicio());
        assertEquals(LocalDate.of(2024, 1, 10), dto.getFechaFin());
        assertEquals("Activo", dto.getEstado());
    }

    @Test
    @DisplayName("Debe convertir de DTO a Entidad correctamente")
    void debeConvertirA_Entidad() {
        ProgramaFormativoDTO dto = new ProgramaFormativoDTO(
            "PF002", "Curso Primeros Auxilios", LocalDate.of(2024, 3, 15), LocalDate.of(2024, 3, 25), "Inactivo"
        );

        ProgramaFormativo entidad = ProgramaFormativoMapper.toEntity(dto);

        assertEquals("PF002", entidad.getCodigo());
        assertEquals("Curso Primeros Auxilios", entidad.getNombre());
        assertEquals(LocalDate.of(2024, 3, 15), entidad.getFechaInicio());
        assertEquals(LocalDate.of(2024, 3, 25), entidad.getFechaFin());
        assertEquals("Inactivo", entidad.getEstado());
    }
}
