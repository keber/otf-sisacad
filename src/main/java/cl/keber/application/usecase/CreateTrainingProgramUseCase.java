package cl.keber.application.usecase;

import cl.keber.application.command.CreateTrainingProgramCommand;
import cl.keber.domain.model.TrainingProgram;

/** Creates a new training program and persists it. */
public interface CreateTrainingProgramUseCase {

    /**
     * Builds a program from the command and stores it.
     *
     * @return the stored program, carrying the identifier the repository assigned
     * @throws IllegalArgumentException when any command field violates a domain rule
     */
    TrainingProgram execute(CreateTrainingProgramCommand command);
}
