package cl.keber.infrastructure.web.controller;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.application.service.TrainingProgramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programs")
public class TrainingProgramController {

    private final TrainingProgramService service;

    public TrainingProgramController(TrainingProgramService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrainingProgram> create(@RequestBody TrainingProgram program) {
        return new ResponseEntity<>(service.save(program), HttpStatus.OK);
    }

    @GetMapping
    public List<TrainingProgram> list() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingProgram> update(
            @PathVariable("id") Long id,
            @RequestBody TrainingProgram updatedProgram) {
        return new ResponseEntity<>(service.update(id, updatedProgram), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
