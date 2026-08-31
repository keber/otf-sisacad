package cl.keber.application.service;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Covers the temporary delegate (decision D8), not application logic: that moved to
 * {@link TrainingProgramApplicationService} and is covered by the use case tests in
 * {@code cl.keber.application.usecase}. What is left to prove here is that the delegate
 * still translates the controller's domain-entity calls into commands faithfully, since
 * the controller keeps calling it until WP7.
 *
 * <p>These tests are unchanged from before the use case split, which is the point: the
 * delegate's observable behaviour did not move. The mock speaks the domain repository
 * port end to end; translation to the JPA row lives in the persistence adapter and is
 * covered by {@code JpaTrainingProgramRepositoryAdapterTest}.
 */
class TrainingProgramServiceTest {

    private TrainingProgramRepository repository;
    private TrainingProgramService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TrainingProgramRepository.class);
        service = new TrainingProgramService(repository);
    }

    private static TrainingProgram program(String code, String name, LocalDate start, LocalDate end, String status) {
        return TrainingProgram.create(
            new TrainingProgramCode(code),
            new TrainingProgramName(name),
            new TrainingPeriod(start, end),
            new TrainingProgramStatus(status));
    }

    private static TrainingProgram stored(
            Long id, String code, String name, LocalDate start, LocalDate end, String status) {
        return TrainingProgram.restore(
            id,
            new TrainingProgramCode(code),
            new TrainingProgramName(name),
            new TrainingPeriod(start, end),
            new TrainingProgramStatus(status));
    }

    @Test
    void shouldSaveTrainingProgram() {
        TrainingProgram toSave = program(
            "PF001", "Occupational Health and Safety",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active");

        Mockito.when(repository.save(any())).thenReturn(stored(
            7L, "PF001", "Occupational Health and Safety",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"));

        TrainingProgram result = service.save(toSave);

        assertEquals(7L, result.getId(), "the generated id comes back from the repository");
        assertEquals("PF001", result.getCode().value());
        assertEquals("Occupational Health and Safety", result.getName().value());

        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        Mockito.verify(repository).save(captor.capture());
        assertNull(captor.getValue().getId(), "an unsaved program is handed to the repository without an id");
        assertEquals("PF001", captor.getValue().getCode().value());
        assertEquals(LocalDate.of(2025, 1, 1), captor.getValue().getPeriod().startDate());
    }

    @Test
    void shouldListAllPrograms() {
        List<TrainingProgram> programs = Arrays.asList(
            stored(1L, "PF001", "Course 1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3), "Activo"),
            stored(2L, "PF002", "Course 2", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 4), "Activo"));

        Mockito.when(repository.findAll()).thenReturn(programs);

        List<TrainingProgram> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("PF001", result.get(0).getCode().value());
        assertEquals("PF002", result.get(1).getCode().value());
        Mockito.verify(repository).findAll();
    }

    @Test
    void shouldFindProgramById() {
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(
            stored(1L, "PF003", "Course X", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), "Activo")));

        Optional<TrainingProgram> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Course X", result.get().getName().value());
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

        TrainingProgram original = stored(
            id, "PF001", "Original Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active");

        TrainingProgram updated = stored(
            id, "PF001", "Updated Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active");

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(original));
        Mockito.when(repository.save(any())).thenReturn(stored(
            id, "PF001", "Updated Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"));

        TrainingProgram result = service.update(id, updated);

        assertEquals("Updated Course", result.getName().value());
        assertEquals(id, result.getId());
        Mockito.verify(repository).findById(id);

        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        Mockito.verify(repository).save(captor.capture());
        assertEquals(id, captor.getValue().getId(), "the program saved carries the addressed id");
        assertEquals("Updated Course", captor.getValue().getName().value());
    }

    @Test
    void shouldSaveANewProgramWhenTheUpdatedProgramCarriesNoId() {
        Long id = 1L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(stored(
            id, "PF001", "Original Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active")));
        Mockito.when(repository.save(any())).thenAnswer(call -> call.getArgument(0));

        service.update(id, program(
            "PF001", "Attempted Update",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"));

        // Exposed defect 2, pinned through the delegate: the request body carries no id, so
        // what reaches the store has none either and is inserted as a duplicate instead of
        // updating the addressed program. Preserved by WP5 on purpose, not introduced by it.
        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        Mockito.verify(repository).save(captor.capture());
        assertNull(captor.getValue().getId(), "an id-less body still reaches the store id-less");
        assertEquals("Attempted Update", captor.getValue().getName().value());
    }
}
