package cl.keber.exception;

public class ProgramaNoEncontradoException extends RuntimeException {
    public ProgramaNoEncontradoException(Long id) {
        super("No se encontró el programa con ID: " + id);
    }
}