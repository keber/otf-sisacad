package cl.keber.infrastructure.persistence.mapper;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.persistence.entity.TrainingProgramJpaEntity;

/**
 * Translates between the domain entity and its JPA row representation.
 *
 * <p>Called only by {@code JpaTrainingProgramRepositoryAdapter}, which owns the
 * translation at the repository boundary. No layer above infrastructure sees the JPA
 * entity.
 */
public final class TrainingProgramPersistenceMapper {

    private TrainingProgramPersistenceMapper() {
        // Prevents instantiation
    }

    public static TrainingProgram toDomain(TrainingProgramJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return TrainingProgram.restore(
            entity.getId(),
            new TrainingProgramCode(entity.getCode()),
            new TrainingProgramName(entity.getName()),
            new TrainingPeriod(entity.getStartDate(), entity.getEndDate()),
            new TrainingProgramStatus(entity.getStatus()));
    }

    public static TrainingProgramJpaEntity toJpaEntity(TrainingProgram program) {
        if (program == null) {
            return null;
        }
        TrainingProgramJpaEntity entity = new TrainingProgramJpaEntity();
        entity.setId(program.getId());
        entity.setCode(program.getCode().value());
        entity.setName(program.getName().value());
        entity.setStartDate(program.getPeriod().startDate());
        entity.setEndDate(program.getPeriod().endDate());
        entity.setStatus(program.getStatus().value());
        return entity;
    }
}
