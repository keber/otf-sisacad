package cl.keber.infrastructure.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrainingProgramDtoTest {

    @Test
    void shouldSetAndGetFields() {
        TrainingProgramDto dto = new TrainingProgramDto();
        dto.setId(3L);
        dto.setCode("PF001");
        dto.setName("Program A");
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 2, 1));
        dto.setStatus("VIGENTE");

        assertEquals(3L, dto.getId());
        assertEquals("PF001", dto.getCode());
        assertEquals("Program A", dto.getName());
        assertEquals(LocalDate.of(2025, 1, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2025, 2, 1), dto.getEndDate());
        assertEquals("VIGENTE", dto.getStatus());
    }

    @Test
    @DisplayName("The all-args constructor carries the id, which is on the wire in both directions")
    void shouldCarryIdThroughTheAllArgsConstructor() {
        TrainingProgramDto dto = new TrainingProgramDto(
            42L, "PF002", "Program B",
            LocalDate.of(2025, 3, 1), LocalDate.of(2025, 4, 1), "CERRADO");

        assertEquals(42L, dto.getId());
        assertEquals("PF002", dto.getCode());
        assertEquals("CERRADO", dto.getStatus());
    }
}
