package cl.keber.infrastructure.persistence.repository;

import cl.keber.infrastructure.persistence.entity.TrainingProgramJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// bridge: replaced by the adapter in WP6. This Spring Data interface now speaks the JPA
// entity instead of the domain type; WP4 introduces the domain port and WP6 renames this
// to SpringDataTrainingProgramRepository behind an adapter.
public interface TrainingProgramRepository extends JpaRepository<TrainingProgramJpaEntity, Long> {
}
