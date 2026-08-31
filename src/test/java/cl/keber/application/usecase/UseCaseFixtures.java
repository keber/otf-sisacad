package cl.keber.application.usecase;

import java.time.LocalDate;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;

/** Shared builders for the use case tests. */
final class UseCaseFixtures {

    private UseCaseFixtures() {
        // Prevents instantiation
    }

    /** An already persisted program, rehydrated with the given identity. */
    static TrainingProgram stored(
            Long id, String code, String name, String startDate, String endDate, String status) {
        return TrainingProgram.restore(
            id,
            new TrainingProgramCode(code),
            new TrainingProgramName(name),
            new TrainingPeriod(LocalDate.parse(startDate), LocalDate.parse(endDate)),
            new TrainingProgramStatus(status));
    }
}
