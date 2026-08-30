package cl.keber.infrastructure.web.mapper;

import cl.keber.infrastructure.web.dto.TrainingProgramDto;
import cl.keber.domain.model.TrainingProgram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingProgramMapperTest {

    @Test
    @DisplayName("Should convert from Entity to DTO correctly")
    void shouldConvertToDto() {
        TrainingProgram entity = new TrainingProgram("PF001", "Safety Course", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), "Activo");

        TrainingProgramDto dto = TrainingProgramMapper.toDto(entity);

        assertEquals("PF001", dto.getCode());
        assertEquals("Safety Course", dto.getName());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2024, 1, 10), dto.getEndDate());
        assertEquals("Activo", dto.getStatus());
    }

    @Test
    @DisplayName("Should convert from DTO to Entity correctly")
    void shouldConvertToEntity() {
        TrainingProgramDto dto = new TrainingProgramDto(
            "PF002", "First Aid Course", LocalDate.of(2024, 3, 15), LocalDate.of(2024, 3, 25), "Inactivo"
        );

        TrainingProgram entity = TrainingProgramMapper.toEntity(dto);

        assertEquals("PF002", entity.getCode());
        assertEquals("First Aid Course", entity.getName());
        assertEquals(LocalDate.of(2024, 3, 15), entity.getStartDate());
        assertEquals(LocalDate.of(2024, 3, 25), entity.getEndDate());
        assertEquals("Inactivo", entity.getStatus());
    }
}
