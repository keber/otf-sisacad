package cl.keber.mapper;

import cl.keber.dto.ProgramaFormativoDTO;
import cl.keber.model.ProgramaFormativo;

public class ProgramaFormativoMapper {

    private ProgramaFormativoMapper() {
        // Previene la instanciación
    }

    public static ProgramaFormativoDTO toDTO(ProgramaFormativo entity) {
        if (entity == null) return null;
        return new ProgramaFormativoDTO(
            entity.getCodigo(),
            entity.getNombre(),
            entity.getFechaInicio(),
            entity.getFechaFin(),
            entity.getEstado()
        );
    }

    public static ProgramaFormativo toEntity(ProgramaFormativoDTO dto) {
        if (dto == null) return null;
        return new ProgramaFormativo(
            dto.getCodigo(),
            dto.getNombre(),
            dto.getFechaInicio(),
            dto.getFechaFin(),
            dto.getEstado()
        );
    }
}
