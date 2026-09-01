package cl.keber.application.usecase;

import java.util.List;

import cl.keber.domain.model.TrainingProgram;

/** Lists every stored training program. */
public interface ListTrainingProgramsUseCase {

    /** Returns every stored program, in repository order. */
    List<TrainingProgram> execute();
}
