package cl.keber.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.keber.domain.exception.TrainingProgramNotFoundException;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.infrastructure.persistence.repository.TrainingProgramRepository;

@Service
public class TrainingProgramService {

    private final TrainingProgramRepository repository;

    public TrainingProgramService(TrainingProgramRepository repository) {
        this.repository = repository;
    }

    public TrainingProgram save(TrainingProgram program) {
        return repository.save(program);
    }

    public List<TrainingProgram> findAll() {
        return repository.findAll();
    }

    public Optional<TrainingProgram> findById(Long id) {
        return repository.findById(id);
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

        return repository.save(updated);
    }
}
