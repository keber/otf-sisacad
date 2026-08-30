package cl.keber.infrastructure.web.mapper;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.web.dto.TrainingProgramDto;

/**
 * Translates between the wire DTO and the domain entity.
 *
 * <p>Building the domain entity runs its value-object validation, so an invalid request
 * body now fails here instead of being persisted silently. Until WP7 adds a
 * {@code @RestControllerAdvice}, the resulting {@link IllegalArgumentException} surfaces
 * as HTTP 500.
 */
public class TrainingProgramMapper {

    private TrainingProgramMapper() {
        // Prevents instantiation
    }

    public static TrainingProgramDto toDto(TrainingProgram program) {
        if (program == null) return null;
        return new TrainingProgramDto(
            program.getId(),
            program.getCode().value(),
            program.getName().value(),
            program.getPeriod().startDate(),
            program.getPeriod().endDate(),
            program.getStatus().value()
        );
    }

    /**
     * Builds the domain entity from a request body. A body carrying an {@code id} is
     * treated as an already persisted program ({@code restore}); one without is a new
     * program ({@code create}). That distinction preserves today's PUT semantics, where
     * the id travels in the body rather than being taken from the path.
     */
    public static TrainingProgram toDomain(TrainingProgramDto dto) {
        if (dto == null) return null;
        TrainingProgramCode code = new TrainingProgramCode(dto.getCode());
        TrainingProgramName name = new TrainingProgramName(dto.getName());
        TrainingProgramStatus status = new TrainingProgramStatus(dto.getStatus());
        TrainingPeriod period = new TrainingPeriod(dto.getStartDate(), dto.getEndDate());

        return dto.getId() == null
            ? TrainingProgram.create(code, name, period, status)
            : TrainingProgram.restore(dto.getId(), code, name, period, status);
    }
}
