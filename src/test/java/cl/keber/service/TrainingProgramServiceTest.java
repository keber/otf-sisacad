package cl.keber.service;

import cl.keber.model.TrainingProgram;
import cl.keber.repository.TrainingProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainingProgramServiceTest {

    private TrainingProgramRepository repository;
    private TrainingProgramService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TrainingProgramRepository.class);
        service = new TrainingProgramService(repository);
    }

    @Test
    void shouldSaveTrainingProgram() {

        TrainingProgram program = new TrainingProgram(
            "PF001", "Occupational Health and Safety", LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 2, 1), "active"
        );

        Mockito.when(repository.save(program)).thenReturn(program);

        TrainingProgram result = service.save(program);

        assertEquals(program, result);
        Mockito.verify(repository).save(program);
    }

    @Test
    void shouldListAllPrograms() {
        List<TrainingProgram> list = Arrays.asList(
            new TrainingProgram("PF001", "Course 1", LocalDate.now(), LocalDate.now().plusDays(2), "Activo"),
            new TrainingProgram("PF002", "Course 2", LocalDate.now(), LocalDate.now().plusDays(3), "Activo")
        );

        Mockito.when(repository.findAll()).thenReturn(list);

        List<TrainingProgram> result = service.findAll();

        assertEquals(2, result.size());
        Mockito.verify(repository).findAll();
    }

    @Test
    void shouldFindProgramById() {
        TrainingProgram program = new TrainingProgram("PF003", "Course X", LocalDate.now(), LocalDate.now().plusDays(1), "Activo");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(program));

        Optional<TrainingProgram> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(program, result.get());
        Mockito.verify(repository).findById(1L);
    }

    @Test
    void shouldDeleteTrainingProgram() {
        Long id = 1L;

        service.deleteById(id);

        Mockito.verify(repository).deleteById(id);
    }

    @Test
    void shouldUpdateTrainingProgram() {
        Long id = 1L;

        TrainingProgram original = new TrainingProgram(
            "PF001", "Original Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"
        );

        TrainingProgram updated = new TrainingProgram(
            "PF001", "Updated Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(original));
        Mockito.when(repository.save(updated)).thenReturn(updated);

        TrainingProgram result = service.update(id, updated);

        assertEquals("Updated Course", result.getName());
        Mockito.verify(repository).findById(id);
        Mockito.verify(repository).save(updated);
    }

}
