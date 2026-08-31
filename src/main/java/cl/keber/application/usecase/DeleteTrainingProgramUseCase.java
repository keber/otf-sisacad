package cl.keber.application.usecase;

/**
 * Removes a training program.
 *
 * <p>Deleting an unknown id is a no-op, matching the behaviour the characterization tests
 * pin (exposed defect 3: delete is silently idempotent and never reports not-found).
 */
public interface DeleteTrainingProgramUseCase {

    /** Removes the program with the given identifier; a no-op when none exists. */
    void execute(Long id);
}
