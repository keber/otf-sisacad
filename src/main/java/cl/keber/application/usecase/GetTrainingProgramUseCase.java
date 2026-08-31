package cl.keber.application.usecase;

import java.util.Optional;

import cl.keber.domain.model.TrainingProgram;

/**
 * Reads a single training program by its identifier.
 *
 * <p>The identifier is passed as a plain {@code Long} rather than wrapped in a query
 * record: a single scalar carries no invariant a record could protect, and the symmetry
 * would only add a type. Per decision D5 this use case exists but is not routed; WP7 does
 * not add a {@code GET /programs/{id}} mapping.
 */
public interface GetTrainingProgramUseCase {

    /** Returns the program with the given identifier, or empty when none exists. */
    Optional<TrainingProgram> execute(Long id);
}
