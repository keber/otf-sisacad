package cl.keber.domain.valueobject;

/**
 * The human readable name of a training program.
 *
 * <p>Immutable and self-validating: a blank or null name cannot be constructed. The value
 * is trimmed, so surrounding whitespace never reaches persistence.
 */
public record TrainingProgramName(String value) {

    public TrainingProgramName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        value = value.trim();
    }
}
