package cl.keber.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import cl.keber.application.query.GetTrainingProgramQuery;
import cl.keber.application.service.TrainingProgramApplicationService;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;

/**
 * The two read use cases, grouped: both are pure delegations to the port and share the
 * same fixture.
 */
class ReadTrainingProgramUseCasesTest {

    private final TrainingProgramRepository repository =
        Mockito.mock(TrainingProgramRepository.class);
    private final TrainingProgramApplicationService service =
        new TrainingProgramApplicationService(repository);

    @Test
    @DisplayName("get returns the stored program for the queried id")
    void getReturnsTheStoredProgram() {
        TrainingProgram stored =
            UseCaseFixtures.stored(1L, "PF003", "Course X", "2025-01-01", "2025-01-02", "VIGENTE");
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(stored));

        GetTrainingProgramUseCase useCase = service;
        Optional<TrainingProgram> result = useCase.execute(new GetTrainingProgramQuery(1L));

        assertTrue(result.isPresent());
        assertSame(stored, result.get());
        Mockito.verify(repository).findById(1L);
    }

    @Test
    @DisplayName("get returns empty when no program has that id")
    void getReturnsEmptyForAnUnknownId() {
        Mockito.when(repository.findById(404L)).thenReturn(Optional.empty());

        GetTrainingProgramUseCase useCase = service;

        assertTrue(useCase.execute(new GetTrainingProgramQuery(404L)).isEmpty());
    }

    @Test
    @DisplayName("list returns every stored program, in repository order")
    void listReturnsEveryStoredProgram() {
        List<TrainingProgram> stored = List.of(
            UseCaseFixtures.stored(1L, "PF001", "Course 1", "2025-01-01", "2025-01-03", "VIGENTE"),
            UseCaseFixtures.stored(2L, "PF002", "Course 2", "2025-01-01", "2025-01-04", "VIGENTE"));
        Mockito.when(repository.findAll()).thenReturn(stored);

        ListTrainingProgramsUseCase useCase = service;
        List<TrainingProgram> result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals("PF001", result.get(0).getCode().value());
        assertEquals("PF002", result.get(1).getCode().value());
        Mockito.verify(repository).findAll();
    }
}
