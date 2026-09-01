package cl.keber.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import cl.keber.application.command.UpdateTrainingProgramCommand;
import cl.keber.application.service.TrainingProgramApplicationService;
import cl.keber.domain.exception.TrainingProgramNotFoundException;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;

class UpdateTrainingProgramUseCaseTest {

    private final TrainingProgramRepository repository =
        Mockito.mock(TrainingProgramRepository.class);
    private final UpdateTrainingProgramUseCase useCase =
        new TrainingProgramApplicationService(repository);

    private static UpdateTrainingProgramCommand command(Long id, String name, String status) {
        return new UpdateTrainingProgramCommand(
            id, "PF001", name, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), status);
    }

    @Test
    @DisplayName("update stores the new state under the addressed id")
    void updateAppliesTheCommandToTheAddressedProgram() {
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(
            UseCaseFixtures.stored(1L, "PF001", "Before", "2025-01-01", "2025-02-01", "VIGENTE")));
        Mockito.when(repository.save(any())).thenAnswer(call -> call.getArgument(0));

        TrainingProgram result = useCase.execute(1L, command(1L, "After", "CERRADO"));

        assertEquals(1L, result.getId());
        assertEquals("After", result.getName().value());
        assertEquals("CERRADO", result.getStatus().value());

        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        Mockito.verify(repository).save(captor.capture());
        assertEquals(1L, captor.getValue().getId(), "the saved program carries the addressed id");
        assertEquals("After", captor.getValue().getName().value());
        assertEquals(LocalDate.of(2025, 2, 1), captor.getValue().getPeriod().endDate());
    }

    @Test
    @DisplayName("update on an unknown id throws TrainingProgramNotFoundException")
    void updateOnAMissingProgramThrows() {
        Mockito.when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
            TrainingProgramNotFoundException.class,
            () -> useCase.execute(404L, command(404L, "Nobody", "VIGENTE")));

        Mockito.verify(repository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("update rejects a payload id that contradicts the addressed id")
    void updateRejectsAMismatchedId() {
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(
            UseCaseFixtures.stored(1L, "PF001", "Before", "2025-01-01", "2025-02-01", "VIGENTE")));

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(1L, command(2L, "Mismatch", "VIGENTE")));

        assertEquals("program ID does not match the provided ID", thrown.getMessage());
        Mockito.verify(repository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("update with no id in the payload saves a new program instead of updating")
    void updateWithoutAPayloadIdSavesANewProgram() {
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(
            UseCaseFixtures.stored(1L, "PF001", "Before", "2025-01-01", "2025-02-01", "VIGENTE")));
        Mockito.when(repository.save(any())).thenAnswer(call -> call.getArgument(0));

        useCase.execute(1L, command(null, "Attempted Update", "VIGENTE"));

        // Exposed defect 2, pinned at unit level: the addressed program is looked up only to
        // prove it exists, and what gets saved is an identity-less program, so the store
        // inserts a duplicate rather than updating in place. WP5 preserves this on purpose;
        // fixing it is a separate, approved behaviour change.
        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        Mockito.verify(repository).save(captor.capture());
        assertNull(captor.getValue().getId(), "the saved program carries no id, so it inserts");
        assertEquals("Attempted Update", captor.getValue().getName().value());
    }

    @Test
    @DisplayName("update rejects an invalid field before touching the store")
    void updateRejectsAnInvalidField() {
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(
            UseCaseFixtures.stored(1L, "PF001", "Before", "2025-01-01", "2025-02-01", "VIGENTE")));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L,
            new UpdateTrainingProgramCommand(
                1L, "PF001", "  ", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "VIGENTE")));

        Mockito.verify(repository, Mockito.never()).save(any());
    }
}
