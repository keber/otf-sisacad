package cl.keber.infrastructure.web.controller;

import cl.keber.application.command.CreateTrainingProgramCommand;
import cl.keber.application.command.UpdateTrainingProgramCommand;
import cl.keber.application.usecase.CreateTrainingProgramUseCase;
import cl.keber.application.usecase.DeleteTrainingProgramUseCase;
import cl.keber.application.usecase.ListTrainingProgramsUseCase;
import cl.keber.application.usecase.UpdateTrainingProgramUseCase;
import cl.keber.domain.exception.TrainingProgramNotFoundException;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.web.dto.TrainingProgramDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The controller binds and returns {@link TrainingProgramDto} (decision D7) and depends on
 * the use case interfaces only (decision D8), so those are what this slice mocks. The use
 * cases still speak commands in and the domain entity out.
 */
@WebMvcTest(controllers = TrainingProgramController.class)
@TestPropertySource(properties = {
  "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class TrainingProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateTrainingProgramUseCase createTrainingProgram;

    @MockBean
    private ListTrainingProgramsUseCase listTrainingPrograms;

    @MockBean
    private UpdateTrainingProgramUseCase updateTrainingProgram;

    @MockBean
    private DeleteTrainingProgramUseCase deleteTrainingProgram;

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
        Mockito.when(createTrainingProgram.execute(any()))
            .thenReturn(program(1L, "PF001", "Test Course", "Activo"));

        mockMvc.perform(post("/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "PF001", "Test Course", "Activo"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("PF001"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /programs passes the request body through as a create command")
    void shouldPassCreateCommandFromBody() throws Exception {
        Mockito.when(createTrainingProgram.execute(any()))
            .thenReturn(program(1L, "PF001", "Test Course", "Activo"));

        mockMvc.perform(post("/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "PF001", "Test Course", "Activo"))))
            .andExpect(status().isOk());

        ArgumentCaptor<CreateTrainingProgramCommand> command =
            ArgumentCaptor.forClass(CreateTrainingProgramCommand.class);
        Mockito.verify(createTrainingProgram).execute(command.capture());

        assertEquals("PF001", command.getValue().code());
        assertEquals("Test Course", command.getValue().name());
        assertEquals(LocalDate.of(2025, 1, 1), command.getValue().startDate());
        assertEquals(LocalDate.of(2025, 2, 1), command.getValue().endDate());
        assertEquals("Activo", command.getValue().status());
    }

    @Test
    @DisplayName("GET /programs should return all programs")
    void shouldListPrograms() throws Exception {
        List<TrainingProgram> list = List.of(
            program(1L, "PF001", "Course 1", "Activo"),
            program(2L, "PF002", "Course 2", "Inactivo"));

        Mockito.when(listTrainingPrograms.execute()).thenReturn(list);

        mockMvc.perform(get("/programs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].code").value("PF001"))
            .andExpect(jsonPath("$[1].status").value("Inactivo"));
    }

    @Test
    @DisplayName("PUT /programs/{id} should update an existing program")
    void shouldUpdateProgram() throws Exception {
        Mockito.when(updateTrainingProgram.execute(Mockito.eq(1L), any()))
            .thenReturn(program(1L, "PF001", "Updated Course", "Actualizado"));

        mockMvc.perform(put("/programs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(1L, "PF001", "Updated Course", "Actualizado"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Course"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /programs/{id} keeps the path id and the body id as separate arguments")
    void shouldKeepPathIdAndBodyIdSeparate() throws Exception {
        Mockito.when(updateTrainingProgram.execute(Mockito.eq(1L), any()))
            .thenReturn(program(1L, "PF001", "Updated Course", "Actualizado"));

        mockMvc.perform(put("/programs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "PF001", "Updated Course", "Actualizado"))))
            .andExpect(status().isOk());

        ArgumentCaptor<UpdateTrainingProgramCommand> command =
            ArgumentCaptor.forClass(UpdateTrainingProgramCommand.class);
        Mockito.verify(updateTrainingProgram).execute(Mockito.eq(1L), command.capture());

        assertNull(command.getValue().id(),
            "the body carried no id, so the command carries none either - the path id is not "
                + "copied into it, which is what preserves both the mismatch guard and defect 2");
    }

    @Test
    @DisplayName("DELETE /programs/{id} should delete an existing program")
    void shouldDeleteProgram() throws Exception {
        mockMvc.perform(delete("/programs/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(deleteTrainingProgram).execute(1L);
    }

    @Test
    @DisplayName("An IllegalArgumentException from a use case is answered with 400")
    void shouldAnswerBadRequestOnIllegalArgument() throws Exception {
        Mockito.when(createTrainingProgram.execute(any()))
            .thenThrow(new IllegalArgumentException("code must not be null or blank"));

        mockMvc.perform(post("/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "PF001", "Test Course", "Activo"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("A TrainingProgramNotFoundException from a use case is answered with 404")
    void shouldAnswerNotFoundOnMissingProgram() throws Exception {
        Mockito.when(updateTrainingProgram.execute(Mockito.eq(999L), any()))
            .thenThrow(new TrainingProgramNotFoundException(999L));

        mockMvc.perform(put("/programs/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(999L, "PF001", "Nobody", "Activo"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
