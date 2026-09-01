package cl.keber.domain.valueobject;

/**
 * The business identifier a training program is known by, for example {@code PF001}.
 *
 * <p>Immutable and self-validating: a blank or null code cannot be constructed. The value
 * is trimmed, so surrounding whitespace never reaches persistence.
 */
public record TrainingProgramCode(String value) {

    public TrainingProgramCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }
        value = value.trim();
    }
}
