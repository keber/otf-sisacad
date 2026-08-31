package cl.keber.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import cl.keber.application.command.CreateTrainingProgramCommand;
import cl.keber.application.command.UpdateTrainingProgramCommand;
import cl.keber.application.query.GetTrainingProgramQuery;
import cl.keber.application.usecase.CreateTrainingProgramUseCase;
import cl.keber.application.usecase.DeleteTrainingProgramUseCase;
import cl.keber.application.usecase.GetTrainingProgramUseCase;
import cl.keber.application.usecase.ListTrainingProgramsUseCase;
import cl.keber.application.usecase.UpdateTrainingProgramUseCase;
import cl.keber.domain.exception.TrainingProgramNotFoundException;
import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.repository.TrainingProgramRepository;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;

/**
 * The single implementation of the five training program use cases.
 *
 * <p>Plain Java: it carries no framework import and no annotation, and it depends on the
 * domain repository port only, never on a storage technology. Wiring is somebody else's
 * job - {@code cl.keber.infrastructure.config.TrainingProgramConfiguration} declares this
 * class as a single bean that satisfies all five use case interfaces.
 */
public class TrainingProgramApplicationService
    implements CreateTrainingProgramUseCase,
        GetTrainingProgramUseCase,
        ListTrainingProgramsUseCase,
        UpdateTrainingProgramUseCase,
        DeleteTrainingProgramUseCase {

    private static final String ID_MISMATCH = "program ID does not match the provided ID";

    private final TrainingProgramRepository repository;

    public TrainingProgramApplicationService(TrainingProgramRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public TrainingProgram execute(CreateTrainingProgramCommand command) {
        return repository.save(TrainingProgram.create(
            new TrainingProgramCode(command.code()),
            new TrainingProgramName(command.name()),
            new TrainingPeriod(command.startDate(), command.endDate()),
            new TrainingProgramStatus(command.status())));
    }

    @Override
    public Optional<TrainingProgram> execute(GetTrainingProgramQuery query) {
        return repository.findById(query.id());
    }

    @Override
    public List<TrainingProgram> execute() {
        return repository.findAll();
    }

    /**
     * The addressed program must exist; a payload id that contradicts the addressed id is
     * rejected; and a payload with no id at all is saved as a new program, which is the
     * behaviour exposed defect 2 describes. All three rules are carried over from the
     * legacy service unchanged - see {@link UpdateTrainingProgramUseCase}.
     */
    @Override
    public TrainingProgram execute(Long id, UpdateTrainingProgramCommand command) {
        if (repository.findById(id).isEmpty()) {
            throw new TrainingProgramNotFoundException(id);
        }
        if (command.id() != null && !command.id().equals(id)) {
            throw new IllegalArgumentException(ID_MISMATCH);
        }

        TrainingProgramCode code = new TrainingProgramCode(command.code());
        TrainingProgramName name = new TrainingProgramName(command.name());
        TrainingPeriod period = new TrainingPeriod(command.startDate(), command.endDate());
        TrainingProgramStatus status = new TrainingProgramStatus(command.status());

        TrainingProgram toSave = command.id() == null
            ? TrainingProgram.create(code, name, period, status)
            : TrainingProgram.restore(command.id(), code, name, period, status);

        return repository.save(toSave);
    }

    @Override
    public void execute(Long id) {
        repository.deleteById(id);
    }
}
