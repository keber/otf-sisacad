package cl.keber.domain.repository;

import java.util.List;
import java.util.Optional;

import cl.keber.domain.model.TrainingProgram;

/**
 * Repository contract owned by the domain.
 *
 * <p>It is expressed in domain types only: no Spring Data, no JPA, no framework
 * annotations. The persistence adapter in {@code infrastructure.persistence.adapter}
 * implements it, so the application layer depends on this interface and never on the
 * storage technology behind it.
 */
public interface TrainingProgramRepository {

    /**
     * Persists the given program and returns the stored state, including the identifier
     * generated when the program had none.
     */
    TrainingProgram save(TrainingProgram program);

    /** Returns the program with the given identifier, or empty when none exists. */
    Optional<TrainingProgram> findById(Long id);

    /** Returns every stored program. */
    List<TrainingProgram> findAll();

    /** Tells whether a program with the given identifier exists. */
    boolean existsById(Long id);

    /** Removes the program with the given identifier; a no-op when none exists. */
    void deleteById(Long id);
}
