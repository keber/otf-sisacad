package cl.keber.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TrainingProgramDtoTest {

    @Test
    void shouldSetAndGetFields() {
        TrainingProgramDto dto = new TrainingProgramDto();
        dto.setCode("PF001");
        dto.setName("Program A");

        assertEquals("PF001", dto.getCode());
        assertEquals("Program A", dto.getName());
    }
}
