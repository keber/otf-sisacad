package cl.keber.infrastructure.persistence.repository;

import cl.keber.domain.model.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {
}
