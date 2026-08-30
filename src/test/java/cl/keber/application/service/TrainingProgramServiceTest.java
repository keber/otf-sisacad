package cl.keber.application.service;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.persistence.entity.TrainingProgramJpaEntity;
import cl.keber.infrastructure.persistence.repository.SpringDataTrainingProgramRepository;
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
 * The service now maps between the domain entity and the JPA entity at the repository
 * boundary (the temporary bridge removed in WP6), so the repository mock speaks
 * {@link TrainingProgramJpaEntity} while the service signatures stay domain-in /
 * domain-out.
 */
class TrainingProgramServiceTest {

    private SpringDataTrainingProgramRepository repository;
    private TrainingProgramService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SpringDataTrainingProgramRepository.class);
        service = new TrainingProgramService(repository);
    }

    private static TrainingProgram program(String code, String name, LocalDate start, LocalDate end, String status) {
        return TrainingProgram.create(
            new TrainingProgramCode(code),
            new TrainingProgramName(name),
            new TrainingPeriod(start, end),
            new TrainingProgramStatus(status));
    }

    private static TrainingProgramJpaEntity row(
            Long id, String code, String name, LocalDate start, LocalDate end, String status) {
        TrainingProgramJpaEntity entity = new TrainingProgramJpaEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setName(name);
        entity.setStartDate(start);
        entity.setEndDate(end);
        entity.setStatus(status);
        return entity;
    }

    @Test
    void shouldSaveTrainingProgram() {
        TrainingProgram toSave = program(
            "PF001", "Occupational Health and Safety",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active");

        Mockito.when(repository.save(any())).thenReturn(row(
            7L, "PF001", "Occupational Health and Safety",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"));

        TrainingProgram result = service.save(toSave);

        assertEquals(7L, result.getId(), "the generated id comes back from the repository");
        assertEquals("PF001", result.getCode().value());
        assertEquals("Occupational Health and Safety", result.getName().value());

        ArgumentCaptor<TrainingProgramJpaEntity> captor =
            ArgumentCaptor.forClass(TrainingProgramJpaEntity.class);
        Mockito.verify(repository).save(captor.capture());
        assertNull(captor.getValue().getId(), "an unsaved program is handed to the repository without an id");
        assertEquals("PF001", captor.getValue().getCode());
        assertEquals(LocalDate.of(2025, 1, 1), captor.getValue().getStartDate());
    }

    @Test
    void shouldListAllPrograms() {
        List<TrainingProgramJpaEntity> rows = Arrays.asList(
            row(1L, "PF001", "Course 1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3), "Activo"),
            row(2L, "PF002", "Course 2", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 4), "Activo"));

        Mockito.when(repository.findAll()).thenReturn(rows);

        List<TrainingProgram> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("PF001", result.get(0).getCode().value());
        assertEquals("PF002", result.get(1).getCode().value());
        Mockito.verify(repository).findAll();
    }

    @Test
    void shouldFindProgramById() {
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(
            row(1L, "PF003", "Course X", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), "Activo")));

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

        TrainingProgramJpaEntity original = row(
            id, "PF001", "Original Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active");

        TrainingProgram updated = TrainingProgram.restore(
            id,
            new TrainingProgramCode("PF001"),
            new TrainingProgramName("Updated Course"),
            new TrainingPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1)),
            new TrainingProgramStatus("active"));

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(original));
        Mockito.when(repository.save(any())).thenReturn(row(
            id, "PF001", "Updated Course",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "active"));

        TrainingProgram result = service.update(id, updated);

        assertEquals("Updated Course", result.getName().value());
        assertEquals(id, result.getId());
        Mockito.verify(repository).findById(id);

        ArgumentCaptor<TrainingProgramJpaEntity> captor =
            ArgumentCaptor.forClass(TrainingProgramJpaEntity.class);
        Mockito.verify(repository).save(captor.capture());
        assertEquals(id, captor.getValue().getId(), "the row saved carries the addressed id");
        assertEquals("Updated Course", captor.getValue().getName());
    }
}
