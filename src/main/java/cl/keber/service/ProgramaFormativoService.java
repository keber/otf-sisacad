package cl.keber.service;

import java.util.List;
import java.util.Optional;

import cl.keber.model.ProgramaFormativo;
import cl.keber.repository.ProgramaFormativoRepository;

public class ProgramaFormativoService {

    private final ProgramaFormativoRepository repository;

    public ProgramaFormativoService(ProgramaFormativoRepository repository) {
        this.repository = repository;
    }

    public ProgramaFormativo guardarPrograma(ProgramaFormativo programa) {
        return repository.save(programa);
    }

    public List<ProgramaFormativo> listarTodos() {
        return repository.findAll();
    }

    public Optional<ProgramaFormativo> buscarPorId(Long id) {
        return repository.findById(id);
    }
}
