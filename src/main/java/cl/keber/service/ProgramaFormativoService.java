package cl.keber.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.keber.exception.ProgramaNoEncontradoException;
import cl.keber.model.ProgramaFormativo;
import cl.keber.repository.ProgramaFormativoRepository;

@Service
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

    public void eliminarPrograma(Long id) {
        repository.deleteById(id);
    }

    public ProgramaFormativo actualizarPrograma(Long id, ProgramaFormativo actualizado) {
        repository.findById(id)
            .orElseThrow(() -> new ProgramaNoEncontradoException(id));

        if (actualizado.getId() != null && !actualizado.getId().equals(id)) {
            throw new IllegalArgumentException("El ID del programa no coincide con el ID proporcionado");
        }

        return repository.save(actualizado);
    }
}
