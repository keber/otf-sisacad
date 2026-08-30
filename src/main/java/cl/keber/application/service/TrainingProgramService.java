package cl.keber.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.keber.domain.exception.TrainingProgramNotFoundException;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.infrastructure.persistence.mapper.TrainingProgramPersistenceMapper;
import cl.keber.infrastructure.persistence.repository.TrainingProgramRepository;

@Service
public class TrainingProgramService {

    private final TrainingProgramRepository repository;

    public TrainingProgramService(TrainingProgramRepository repository) {
        this.repository = repository;
    }

    // bridge: replaced by the adapter in WP6. The public signatures below are unchanged
    // (domain in, domain out); only the translation to and from the JPA entity at the
    // repository boundary is new. WP4 gives the domain a repository port and WP6 moves
    // this mapping into the adapter that implements it, at which point every
    // TrainingProgramPersistenceMapper call here disappears.

    public TrainingProgram save(TrainingProgram program) {
        return TrainingProgramPersistenceMapper.toDomain(
            repository.save(TrainingProgramPersistenceMapper.toJpaEntity(program)));
    }

    public List<TrainingProgram> findAll() {
        return repository.findAll().stream()
            .map(TrainingProgramPersistenceMapper::toDomain)
            .toList();
    }

    public Optional<TrainingProgram> findById(Long id) {
        return repository.findById(id).map(TrainingProgramPersistenceMapper::toDomain);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public TrainingProgram update(Long id, TrainingProgram updated) {
        repository.findById(id)
            .orElseThrow(() -> new TrainingProgramNotFoundException(id));

        if (updated.getId() != null && !updated.getId().equals(id)) {
            throw new IllegalArgumentException("program ID does not match the provided ID");
        }

        return TrainingProgramPersistenceMapper.toDomain(
            repository.save(TrainingProgramPersistenceMapper.toJpaEntity(updated)));
    }
}
