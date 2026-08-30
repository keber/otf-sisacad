package cl.keber.infrastructure.web.mapper;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.web.dto.TrainingProgramDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingProgramMapperTest {

    @Test
    @DisplayName("Should convert from domain entity to DTO correctly")
    void shouldConvertToDto() {
        TrainingProgram entity = TrainingProgram.restore(
            5L,
            new TrainingProgramCode("PF001"),
            new TrainingProgramName("Safety Course"),
            new TrainingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10)),
            new TrainingProgramStatus("Activo"));

        TrainingProgramDto dto = TrainingProgramMapper.toDto(entity);

        assertEquals(5L, dto.getId());
        assertEquals("PF001", dto.getCode());
        assertEquals("Safety Course", dto.getName());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2024, 1, 10), dto.getEndDate());
        assertEquals("Activo", dto.getStatus());
    }

    @Test
    @DisplayName("Should convert from DTO to domain entity correctly")
    void shouldConvertToDomain() {
        TrainingProgramDto dto = new TrainingProgramDto(
            null, "PF002", "First Aid Course",
            LocalDate.of(2024, 3, 15), LocalDate.of(2024, 3, 25), "Inactivo");

        TrainingProgram entity = TrainingProgramMapper.toDomain(dto);

        assertNull(entity.getId(), "a body without an id maps to a new program");
        assertEquals("PF002", entity.getCode().value());
        assertEquals("First Aid Course", entity.getName().value());
        assertEquals(LocalDate.of(2024, 3, 15), entity.getPeriod().startDate());
        assertEquals(LocalDate.of(2024, 3, 25), entity.getPeriod().endDate());
        assertEquals("Inactivo", entity.getStatus().value());
    }

    @Test
    @DisplayName("A DTO carrying an id maps to a restored program that keeps it")
    void shouldKeepIdWhenPresent() {
        TrainingProgramDto dto = new TrainingProgramDto(
            9L, "PF003", "Fire Safety",
            LocalDate.of(2024, 3, 15), LocalDate.of(2024, 3, 25), "Activo");

        assertEquals(9L, TrainingProgramMapper.toDomain(dto).getId());
    }

    @Test
    @DisplayName("An invalid request body is rejected by the domain value objects")
    void shouldRejectInvalidDto() {
        TrainingProgramDto blankCode = new TrainingProgramDto(
            null, "", "Blank Code",
            LocalDate.of(2024, 3, 15), LocalDate.of(2024, 3, 25), "Activo");
        IllegalArgumentException codeError = assertThrows(
            IllegalArgumentException.class, () -> TrainingProgramMapper.toDomain(blankCode));
        assertEquals("code must not be null or blank", codeError.getMessage());

        TrainingProgramDto invertedDates = new TrainingProgramDto(
            null, "PF004", "Inverted Dates",
            LocalDate.of(2024, 3, 25), LocalDate.of(2024, 3, 15), "Activo");
        IllegalArgumentException dateError = assertThrows(
            IllegalArgumentException.class, () -> TrainingProgramMapper.toDomain(invertedDates));
        assertEquals("endDate must be after startDate", dateError.getMessage());
    }

    @Test
    @DisplayName("Null maps to null in both directions")
    void shouldMapNullToNull() {
        assertNull(TrainingProgramMapper.toDto(null));
        assertNull(TrainingProgramMapper.toDomain(null));
    }
}
