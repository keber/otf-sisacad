package cl.keber.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.keber.application.command.CreateTrainingProgramCommand;
import cl.keber.application.command.UpdateTrainingProgramCommand;
import cl.keber.application.query.GetTrainingProgramQuery;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;

/**
 * temporary delegate: removed in WP7.
 *
 * <p>All behaviour now lives in {@link TrainingProgramApplicationService}, which speaks
 * commands and is free of any framework. This class only survives because
 * {@code TrainingProgramController} and its test still depend on it and live in
 * {@code infrastructure.web}, which WP5 may not touch (decision D8). Its public method
 * signatures are therefore unchanged, so the controller compiles untouched.
 *
 * <p>It also carries the wiring for now: Spring builds this bean from the repository port
 * and it constructs the application service itself, which keeps the application service
 * annotation-free. WP7 swaps the controller onto the use case interfaces, declares the
 * application service as a {@code @Configuration} bean, and deletes this class.
 */
@Service
public class TrainingProgramService {

    private final TrainingProgramApplicationService applicationService;

    public TrainingProgramService(TrainingProgramRepository repository) {
        this.applicationService = new TrainingProgramApplicationService(repository);
    }

    /**
     * Creates a program. A caller-supplied id is ignored: creation always inserts. The
     * only caller is {@code POST /programs}, whose payloads carry no id.
     */
    public TrainingProgram save(TrainingProgram program) {
        return applicationService.execute(new CreateTrainingProgramCommand(
            program.getCode().value(),
            program.getName().value(),
            program.getPeriod().startDate(),
            program.getPeriod().endDate(),
            program.getStatus().value()));
    }

    public List<TrainingProgram> findAll() {
        return applicationService.execute();
    }

    public Optional<TrainingProgram> findById(Long id) {
        return applicationService.execute(new GetTrainingProgramQuery(id));
    }

    public void deleteById(Long id) {
        applicationService.execute(id);
    }

    /**
     * Updates the program addressed by {@code id} with the state of {@code updated}. The
     * id carried by {@code updated} is the one the caller put in the request body, and it
     * stays distinct from the addressed id, which is what preserves both the mismatch
     * guard and exposed defect 2.
     */
    public TrainingProgram update(Long id, TrainingProgram updated) {
        return applicationService.execute(id, new UpdateTrainingProgramCommand(
            updated.getId(),
            updated.getCode().value(),
            updated.getName().value(),
            updated.getPeriod().startDate(),
            updated.getPeriod().endDate(),
            updated.getStatus().value()));
    }
}
