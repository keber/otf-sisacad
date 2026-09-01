package cl.keber.infrastructure.web.controller;

import cl.keber.application.command.CreateTrainingProgramCommand;
import cl.keber.application.command.UpdateTrainingProgramCommand;
import cl.keber.application.usecase.CreateTrainingProgramUseCase;
import cl.keber.application.usecase.DeleteTrainingProgramUseCase;
import cl.keber.application.usecase.ListTrainingProgramsUseCase;
import cl.keber.application.usecase.UpdateTrainingProgramUseCase;
import cl.keber.infrastructure.web.dto.TrainingProgramDto;
import cl.keber.infrastructure.web.mapper.TrainingProgramMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The web entry point for {@code /programs}.
 *
 * <p>It depends on the application's use case interfaces only - no service class, no
 * repository and no persistence type (decision D8). Each endpoint translates the wire DTO
 * into the command its use case speaks, and maps the returned domain entity back through
 * {@link TrainingProgramMapper}.
 *
 * <p>The DTO binding and the six-field response shape come from decision D7 and are
 * unchanged here.
 *
 * <p>{@code GetTrainingProgramUseCase} is deliberately <em>not</em> injected: no
 * {@code GET /programs/{id}} route has ever existed, and adding one would be a new
 * feature rather than a refactor (decision D5).
 */
@RestController
@RequestMapping("/programs")
public class TrainingProgramController {

    private final CreateTrainingProgramUseCase createTrainingProgram;
    private final ListTrainingProgramsUseCase listTrainingPrograms;
    private final UpdateTrainingProgramUseCase updateTrainingProgram;
    private final DeleteTrainingProgramUseCase deleteTrainingProgram;

    public TrainingProgramController(
            CreateTrainingProgramUseCase createTrainingProgram,
            ListTrainingProgramsUseCase listTrainingPrograms,
            UpdateTrainingProgramUseCase updateTrainingProgram,
            DeleteTrainingProgramUseCase deleteTrainingProgram) {
        this.createTrainingProgram = createTrainingProgram;
        this.listTrainingPrograms = listTrainingPrograms;
        this.updateTrainingProgram = updateTrainingProgram;
        this.deleteTrainingProgram = deleteTrainingProgram;
    }

    /** Creation always inserts; an id in the request body is ignored, as it was before. */
    @PostMapping
    public ResponseEntity<TrainingProgramDto> create(@RequestBody TrainingProgramDto program) {
        return new ResponseEntity<>(
            TrainingProgramMapper.toDto(createTrainingProgram.execute(new CreateTrainingProgramCommand(
                program.getCode(),
                program.getName(),
                program.getStartDate(),
                program.getEndDate(),
                program.getStatus()))),
            HttpStatus.OK);
    }

    @GetMapping
    public List<TrainingProgramDto> list() {
        return listTrainingPrograms.execute().stream()
            .map(TrainingProgramMapper::toDto)
            .toList();
    }

    /**
     * The addressed id from the path and the id inside the body stay distinct arguments.
     * They mean different things, and collapsing them would silently change the contract:
     * the mismatch guard would disappear, and so would exposed defect 2.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrainingProgramDto> update(
            @PathVariable("id") Long id,
            @RequestBody TrainingProgramDto updatedProgram) {
        return new ResponseEntity<>(
            TrainingProgramMapper.toDto(updateTrainingProgram.execute(id, new UpdateTrainingProgramCommand(
                updatedProgram.getId(),
                updatedProgram.getCode(),
                updatedProgram.getName(),
                updatedProgram.getStartDate(),
                updatedProgram.getEndDate(),
                updatedProgram.getStatus()))),
            HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        deleteTrainingProgram.execute(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
