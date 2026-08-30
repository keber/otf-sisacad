package cl.keber.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;
import cl.keber.infrastructure.persistence.mapper.TrainingProgramPersistenceMapper;
import cl.keber.infrastructure.persistence.repository.SpringDataTrainingProgramRepository;

/**
 * JPA implementation of the domain repository port.
 *
 * <p>This is the only place where a domain {@link TrainingProgram} is translated to and
 * from its JPA row: it delegates storage to {@link SpringDataTrainingProgramRepository}
 * and translation to {@link TrainingProgramPersistenceMapper}, so callers see domain
 * types in and domain types out.
 */
@Repository
public class JpaTrainingProgramRepositoryAdapter implements TrainingProgramRepository {

    private final SpringDataTrainingProgramRepository springDataRepository;

    public JpaTrainingProgramRepositoryAdapter(SpringDataTrainingProgramRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TrainingProgram save(TrainingProgram program) {
        return TrainingProgramPersistenceMapper.toDomain(
            springDataRepository.save(TrainingProgramPersistenceMapper.toJpaEntity(program)));
    }

    @Override
    public Optional<TrainingProgram> findById(Long id) {
        return springDataRepository.findById(id).map(TrainingProgramPersistenceMapper::toDomain);
    }

    @Override
    public List<TrainingProgram> findAll() {
        return springDataRepository.findAll().stream()
            .map(TrainingProgramPersistenceMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
