package cl.keber.infrastructure.web.controller;

import cl.keber.application.service.TrainingProgramService;
import cl.keber.infrastructure.web.dto.TrainingProgramDto;
import cl.keber.infrastructure.web.mapper.TrainingProgramMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller binds and returns {@link TrainingProgramDto}, never the domain entity
 * (decision D7). The JSON shape is unchanged: the DTO carries the same six fields in the
 * same order the JPA entity used to serialise.
 */
@RestController
@RequestMapping("/programs")
public class TrainingProgramController {

    private final TrainingProgramService service;

    public TrainingProgramController(TrainingProgramService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrainingProgramDto> create(@RequestBody TrainingProgramDto program) {
        return new ResponseEntity<>(
            TrainingProgramMapper.toDto(service.save(TrainingProgramMapper.toDomain(program))),
            HttpStatus.OK);
    }

    @GetMapping
    public List<TrainingProgramDto> list() {
        return service.findAll().stream()
            .map(TrainingProgramMapper::toDto)
            .toList();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingProgramDto> update(
            @PathVariable("id") Long id,
            @RequestBody TrainingProgramDto updatedProgram) {
        return new ResponseEntity<>(
            TrainingProgramMapper.toDto(
                service.update(id, TrainingProgramMapper.toDomain(updatedProgram))),
            HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
