package cl.keber.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import cl.keber.application.command.CreateTrainingProgramCommand;
import cl.keber.application.service.TrainingProgramApplicationService;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;

/**
 * The use case is exercised through the domain repository port only: the mock speaks
 * domain entities, so nothing here knows that JPA exists.
 */
class CreateTrainingProgramUseCaseTest {

    private final TrainingProgramRepository repository =
        Mockito.mock(TrainingProgramRepository.class);
    private final CreateTrainingProgramUseCase useCase =
        new TrainingProgramApplicationService(repository);

    @Test
    @DisplayName("create hands the repository a domain program built from the command")
    void createPersistsADomainProgramBuiltFromTheCommand() {
        TrainingProgram saved = UseCaseFixtures.stored(
            7L, "PF001", "Occupational Health", "2025-01-01", "2025-02-01", "VIGENTE");
        Mockito.when(repository.save(any())).thenReturn(saved);

        TrainingProgram result = useCase.execute(new CreateTrainingProgramCommand(
            "PF001",
            "Occupational Health",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 2, 1),
            "VIGENTE"));

        assertSame(saved, result, "the stored program comes back from the repository");

        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        Mockito.verify(repository).save(captor.capture());
        TrainingProgram handed = captor.getValue();
        assertNull(handed.getId(), "a created program has no identity until it is stored");
        assertEquals("PF001", handed.getCode().value());
        assertEquals("Occupational Health", handed.getName().value());
        assertEquals(LocalDate.of(2025, 1, 1), handed.getPeriod().startDate());
        assertEquals(LocalDate.of(2025, 2, 1), handed.getPeriod().endDate());
        assertEquals("VIGENTE", handed.getStatus().value());
    }

    @Test
    @DisplayName("create rejects a blank code and never reaches the repository")
    void createRejectsABlankCode() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
            new CreateTrainingProgramCommand(
                "  ",
                "Blank Code",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1),
                "VIGENTE")));

        Mockito.verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("create rejects an end date that is not after the start date")
    void createRejectsAnInvertedPeriod() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
            new CreateTrainingProgramCommand(
                "PF002",
                "Inverted",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 1, 1),
                "VIGENTE")));

        Mockito.verifyNoInteractions(repository);
    }
}
