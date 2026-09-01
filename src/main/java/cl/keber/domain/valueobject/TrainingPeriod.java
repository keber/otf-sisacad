package cl.keber.domain.valueobject;

import java.time.LocalDate;

/**
 * The date range a training program runs over.
 *
 * <p>Immutable and self-validating: both dates are mandatory and {@code endDate} must be
 * strictly after {@code startDate}. A zero-length period (equal dates) is invalid, which
 * preserves the rule the previous entity constructor enforced (decision D3).
 */
public record TrainingPeriod(LocalDate startDate, LocalDate endDate) {

    public TrainingPeriod {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("endDate must not be null");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
    }
}
