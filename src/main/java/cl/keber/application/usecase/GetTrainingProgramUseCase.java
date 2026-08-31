package cl.keber.application.usecase;

import java.util.Optional;

import cl.keber.application.query.GetTrainingProgramQuery;
import cl.keber.domain.model.TrainingProgram;

/**
 * Reads a single training program by its identifier.
 *
 * <p>The argument is a {@link GetTrainingProgramQuery} rather than a plain {@code Long}
 * because {@link DeleteTrainingProgramUseCase} already takes {@code execute(Long)}: one
 * class implements both, and two {@code execute(Long)} methods differing only in return
 * type do not compile.
 *
 * <p>Per decision D5 this use case exists but is not routed; WP7 does not add a
 * {@code GET /programs/{id}} mapping.
 */
public interface GetTrainingProgramUseCase {

    /** Returns the program with the given identifier, or empty when none exists. */
    Optional<TrainingProgram> execute(GetTrainingProgramQuery query);
}
