package cl.keber.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import cl.keber.application.service.TrainingProgramApplicationService;
import cl.keber.domain.repository.TrainingProgramRepository;

class DeleteTrainingProgramUseCaseTest {

    private final TrainingProgramRepository repository =
        Mockito.mock(TrainingProgramRepository.class);
    private final DeleteTrainingProgramUseCase useCase =
        new TrainingProgramApplicationService(repository);

    @Test
    @DisplayName("delete removes the addressed program")
    void deleteDelegatesToThePort() {
        useCase.execute(1L);

        Mockito.verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete on an unknown id is a silent no-op, it does not check existence first")
    void deleteOnAnUnknownIdDoesNotCheckExistence() {
        useCase.execute(404L);

        // Exposed defect 3, pinned: nothing distinguishes "deleted" from "was never there".
        Mockito.verify(repository).deleteById(404L);
        Mockito.verify(repository, Mockito.never()).existsById(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(repository);
    }
}
