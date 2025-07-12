package cl.keber.controller;

import cl.keber.model.ProgramaFormativo;
import cl.keber.service.ProgramaFormativoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programas")
public class ProgramaFormativoController {

    private final ProgramaFormativoService service;

    public ProgramaFormativoController(ProgramaFormativoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProgramaFormativo> crearPrograma(@RequestBody ProgramaFormativo programa) {
        return new ResponseEntity<>(service.guardarPrograma(programa), HttpStatus.OK);
    }

    @GetMapping
    public List<ProgramaFormativo> listarProgramas() {
        return service.listarTodos();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramaFormativo> actualizarPrograma(
            @PathVariable("id") Long id,
            @RequestBody ProgramaFormativo programaActualizado) {
        return new ResponseEntity<>(service.actualizarPrograma(id, programaActualizado), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPrograma(@PathVariable("id") Long id) {
        service.eliminarPrograma(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
