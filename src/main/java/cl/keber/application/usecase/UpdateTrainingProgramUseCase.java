package cl.keber.application.usecase;

import cl.keber.application.command.UpdateTrainingProgramCommand;
import cl.keber.domain.exception.TrainingProgramNotFoundException;
import cl.keber.domain.model.TrainingProgram;

/**
 * Replaces the state of an addressed training program with the state in the command.
 *
 * <p>The addressed id and the id inside the command are separate arguments because the
 * current contract treats them differently, and WP5 is a behaviour-preserving refactor:
 *
 * <ul>
 *   <li>the addressed program must exist, otherwise
 *       {@link TrainingProgramNotFoundException} is thrown;
 *   <li>a command id that is present and different from the addressed id is rejected with
 *       {@link IllegalArgumentException};
 *   <li>a command id that is absent means the caller supplied no identity, and the saved
 *       program is a <em>new</em> one. That is exposed defect 2 (a PUT with no id in the
 *       body inserts a duplicate instead of updating in place). It is preserved here, not
 *       introduced here, and it is not WP5's to fix.
 * </ul>
 */
public interface UpdateTrainingProgramUseCase {

    /**
     * @param id the identifier of the program being addressed, never {@code null}
     * @param command the new state, including the identifier the caller supplied, if any
     * @return the stored program
     * @throws TrainingProgramNotFoundException when no program has the addressed id
     * @throws IllegalArgumentException when the command id contradicts the addressed id,
     *     or when any command field violates a domain rule
     */
    TrainingProgram execute(Long id, UpdateTrainingProgramCommand command);
}
