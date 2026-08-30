package cl.keber.infrastructure.web.controller;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.application.service.TrainingProgramService;
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

    @Test
    @DisplayName("POST /programs should create a new training program")
    void shouldCreateProgram() throws Exception {
        TrainingProgram created = new TrainingProgram("PF001", "Test Course", LocalDate.now(), LocalDate.now().plusDays(5), "Activo");

        Mockito.when(service.save(any())).thenReturn(created);

        mockMvc.perform(post("/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(created)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("PF001"));
    }

    @Test
    @DisplayName("GET /programs should return all programs")
    void shouldListPrograms() throws Exception {
        List<TrainingProgram> list = List.of(
                new TrainingProgram("PF001", "Course 1", LocalDate.now(), LocalDate.now().plusDays(1), "Activo"),
                new TrainingProgram("PF002", "Course 2", LocalDate.now(), LocalDate.now().plusDays(2), "Inactivo")
        );

        Mockito.when(service.findAll()).thenReturn(list);

        mockMvc.perform(get("/programs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("PUT /programs/{id} should update an existing program")
    void shouldUpdateProgram() throws Exception {
        TrainingProgram updated = new TrainingProgram("PF001", "Updated Course", LocalDate.now(), LocalDate.now().plusDays(3), "Actualizado");

        Mockito.when(service.update(Mockito.eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/programs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Course"));
    }

    @Test
    @DisplayName("DELETE /programs/{id} should delete an existing program")
    void shouldDeleteProgram() throws Exception {
        mockMvc.perform(delete("/programs/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(service).deleteById(1L);
    }
}
