package cl.keber.mapper;

import cl.keber.dto.TrainingProgramDto;
import cl.keber.model.TrainingProgram;

public class TrainingProgramMapper {

    private TrainingProgramMapper() {
        // Prevents instantiation
    }

    public static TrainingProgramDto toDto(TrainingProgram entity) {
        if (entity == null) return null;
        return new TrainingProgramDto(
            entity.getCode(),
            entity.getName(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getStatus()
        );
    }

    public static TrainingProgram toEntity(TrainingProgramDto dto) {
        if (dto == null) return null;
        return new TrainingProgram(
            dto.getCode(),
            dto.getName(),
            dto.getStartDate(),
            dto.getEndDate(),
            dto.getStatus()
        );
    }
}
