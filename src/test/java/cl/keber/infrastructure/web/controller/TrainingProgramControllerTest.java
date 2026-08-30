package cl.keber.infrastructure.web.controller;

import cl.keber.application.service.TrainingProgramService;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.web.dto.TrainingProgramDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The controller binds and returns {@link TrainingProgramDto} (decision D7); the service
 * mock still speaks the domain entity.
 */
@WebMvcTest(controllers = TrainingProgramController.class)
@TestPropertySource(properties = {
  "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class TrainingProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingProgramService service;

    @Autowired
    private ObjectMapper objectMapper;

    private static TrainingProgram program(Long id, String code, String name, String status) {
        return TrainingProgram.restore(
            id,
            new TrainingProgramCode(code),
            new TrainingProgramName(name),
            new TrainingPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1)),
            new TrainingProgramStatus(status));
    }

    private static TrainingProgramDto dto(Long id, String code, String name, String status) {
        return new TrainingProgramDto(
            id, code, name, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), status);
    }

    @Test
    @DisplayName("POST /programs should create a new training program")
    void shouldCreateProgram() throws Exception {
        Mockito.when(service.save(any())).thenReturn(program(1L, "PF001", "Test Course", "Activo"));

        mockMvc.perform(post("/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "PF001", "Test Course", "Activo"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("PF001"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /programs should return all programs")
    void shouldListPrograms() throws Exception {
        List<TrainingProgram> list = List.of(
            program(1L, "PF001", "Course 1", "Activo"),
            program(2L, "PF002", "Course 2", "Inactivo"));

        Mockito.when(service.findAll()).thenReturn(list);

        mockMvc.perform(get("/programs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].code").value("PF001"))
            .andExpect(jsonPath("$[1].status").value("Inactivo"));
    }

    @Test
    @DisplayName("PUT /programs/{id} should update an existing program")
    void shouldUpdateProgram() throws Exception {
        Mockito.when(service.update(Mockito.eq(1L), any()))
            .thenReturn(program(1L, "PF001", "Updated Course", "Actualizado"));

        mockMvc.perform(put("/programs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(1L, "PF001", "Updated Course", "Actualizado"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Course"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /programs/{id} should delete an existing program")
    void shouldDeleteProgram() throws Exception {
        mockMvc.perform(delete("/programs/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(service).deleteById(1L);
    }
}
