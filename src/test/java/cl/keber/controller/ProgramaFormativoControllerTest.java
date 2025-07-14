package cl.keber.controller;

import cl.keber.model.ProgramaFormativo;
import cl.keber.service.ProgramaFormativoService;
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

@WebMvcTest(controllers = ProgramaFormativoController.class)
@TestPropertySource(properties = {
  "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class ProgramaFormativoControllerTest { 

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgramaFormativoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /programas debe crear un nuevo programa formativo")
    void debeCrearPrograma() throws Exception {
        ProgramaFormativo nuevo = new ProgramaFormativo("PF001", "Curso Test", LocalDate.now(), LocalDate.now().plusDays(5), "Activo");

        Mockito.when(service.guardarPrograma(any())).thenReturn(nuevo);

        mockMvc.perform(post("/programas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("PF001"));
    }

    @Test
    @DisplayName("GET /programas debe retornar todos los programas")
    void debeListarProgramas() throws Exception {
        List<ProgramaFormativo> lista = List.of(
                new ProgramaFormativo("PF001", "Curso 1", LocalDate.now(), LocalDate.now().plusDays(1), "Activo"),
                new ProgramaFormativo("PF002", "Curso 2", LocalDate.now(), LocalDate.now().plusDays(2), "Inactivo")
        );

        Mockito.when(service.listarTodos()).thenReturn(lista);

        mockMvc.perform(get("/programas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("PUT /programas/{id} debe actualizar un programa existente")
    void debeActualizarPrograma() throws Exception {
        ProgramaFormativo actualizado = new ProgramaFormativo("PF001", "Curso Actualizado", LocalDate.now(), LocalDate.now().plusDays(3), "Actualizado");

        Mockito.when(service.actualizarPrograma(Mockito.eq(1L), any())).thenReturn(actualizado);

        mockMvc.perform(put("/programas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Curso Actualizado"));
    }

    @Test
    @DisplayName("DELETE /programas/{id} debe eliminar un programa existente")
    void debeEliminarPrograma() throws Exception {
        mockMvc.perform(delete("/programas/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(service).eliminarPrograma(1L);
    }
}
