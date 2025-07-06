package cl.keber.service;

import cl.keber.model.ProgramaFormativo;
import cl.keber.repository.ProgramaFormativoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramaFormativoServiceTest {

    private ProgramaFormativoRepository repository;
    private ProgramaFormativoService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ProgramaFormativoRepository.class);
        service = new ProgramaFormativoService(repository);
    }

    @Test
    void debeGuardarProgramaFormativo() {

        ProgramaFormativo programa = new ProgramaFormativo(
            "PF001", "Seguridad y Salud Ocupacional", LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 2, 1), "vigente"
        );

        Mockito.when(repository.save(programa)).thenReturn(programa);

        ProgramaFormativo resultado = service.guardarPrograma(programa);

        assertEquals(programa, resultado);
        Mockito.verify(repository).save(programa);
    }

    @Test
    void debeListarTodosLosProgramas() {
        List<ProgramaFormativo> lista = Arrays.asList(
            new ProgramaFormativo("PF001", "Curso 1", LocalDate.now(), LocalDate.now().plusDays(2), "Activo"),
            new ProgramaFormativo("PF002", "Curso 2", LocalDate.now(), LocalDate.now().plusDays(3), "Activo")
        );

        Mockito.when(repository.findAll()).thenReturn(lista);

        List<ProgramaFormativo> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
        Mockito.verify(repository).findAll();
    }

    @Test
    void debeBuscarProgramaPorId() {
        ProgramaFormativo pf = new ProgramaFormativo("PF003", "Curso X", LocalDate.now(), LocalDate.now().plusDays(1), "Activo");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(pf));

        Optional<ProgramaFormativo> resultado = service.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(pf, resultado.get());
        Mockito.verify(repository).findById(1L);
    }

    @Test
    void debeEliminarProgramaFormativo() {
        Long id = 1L;
    
        service.eliminarPrograma(id);

        Mockito.verify(repository).deleteById(id);
    }

}
