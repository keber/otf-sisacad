package cl.keber.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.keber.infrastructure.persistence.entity.TrainingProgramJpaEntity;

/**
 * Spring Data access to the {@code training_program} table.
 *
 * <p>It speaks the JPA entity only. Callers outside this package go through
 * {@code JpaTrainingProgramRepositoryAdapter}, which implements the domain port
 * {@code cl.keber.domain.repository.TrainingProgramRepository} on top of this interface.
 */
public interface SpringDataTrainingProgramRepository extends JpaRepository<TrainingProgramJpaEntity, Long> {
}
