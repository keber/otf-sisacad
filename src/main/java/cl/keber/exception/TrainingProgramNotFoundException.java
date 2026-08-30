package cl.keber.exception;

public class TrainingProgramNotFoundException extends RuntimeException {
    public TrainingProgramNotFoundException(Long id) {
        super("Training program not found with ID: " + id);
    }
}
