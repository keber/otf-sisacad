package cl.keber.domain.valueobject;

/**
 * The lifecycle status of a training program, for example {@code VIGENTE}.
 *
 * <p>Modelled as a value object over a plain string rather than an enum: the persisted
 * values are free-form today and an enum would reject rows that already exist. See
 * decision D4 in the refactor state board.
 *
 * <p>Immutable and self-validating: a blank or null status cannot be constructed. The
 * value is trimmed, so surrounding whitespace never reaches persistence.
 */
public record TrainingProgramStatus(String value) {

    public TrainingProgramStatus {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("status must not be null or blank");
        }
        value = value.trim();
    }
}
