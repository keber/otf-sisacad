package cl.keber.domain.model;

import java.util.Objects;

import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;

/**
 * A training program: the aggregate root of this slice.
 *
 * <p>Pure Java. It carries no persistence, framework or serialisation annotation, and its
 * state is expressed entirely in value objects, so an invalid program cannot be
 * constructed by any route. State changes go through intention-revealing behaviour
 * ({@link #rename}, {@link #reschedule}, {@link #changeStatus}) rather than setters.
 *
 * <p>Instances are created either by {@link #create} (a brand new program, no id yet) or
 * by {@link #restore} (rehydration of an already persisted program).
 */
public final class TrainingProgram {

    private final Long id;
    private final TrainingProgramCode code;
    private TrainingProgramName name;
    private TrainingPeriod period;
    private TrainingProgramStatus status;

    private TrainingProgram(
            Long id,
            TrainingProgramCode code,
            TrainingProgramName name,
            TrainingPeriod period,
            TrainingProgramStatus status) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code must not be null or blank");
        this.name = Objects.requireNonNull(name, "name must not be null or blank");
        this.period = Objects.requireNonNull(period, "period must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null or blank");
    }

    /**
     * Creates a new program that has never been persisted. Its {@code id} is {@code null}
     * until the persistence layer assigns one.
     */
    public static TrainingProgram create(
            TrainingProgramCode code,
            TrainingProgramName name,
            TrainingPeriod period,
            TrainingProgramStatus status) {
        return new TrainingProgram(null, code, name, period, status);
    }

    /**
     * Rehydrates an already persisted program, keeping its identity.
     */
    public static TrainingProgram restore(
            Long id,
            TrainingProgramCode code,
            TrainingProgramName name,
            TrainingPeriod period,
            TrainingProgramStatus status) {
        return new TrainingProgram(id, code, name, period, status);
    }

    /** Gives the program a new name. */
    public void rename(TrainingProgramName newName) {
        this.name = Objects.requireNonNull(newName, "name must not be null or blank");
    }

    /**
     * Moves the program to a new period. The period value object guarantees the new range
     * is valid, so an invalid reschedule is impossible to express.
     */
    public void reschedule(TrainingPeriod newPeriod) {
        this.period = Objects.requireNonNull(newPeriod, "period must not be null");
    }

    /** Moves the program to a new lifecycle status. */
    public void changeStatus(TrainingProgramStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "status must not be null or blank");
    }

    public Long getId() {
        return id;
    }

    public TrainingProgramCode getCode() {
        return code;
    }

    public TrainingProgramName getName() {
        return name;
    }

    public TrainingPeriod getPeriod() {
        return period;
    }

    public TrainingProgramStatus getStatus() {
        return status;
    }

    /**
     * Identity is the persistent id: two programs are the same program when they carry the
     * same non-null id, whatever their current attribute values. A program that has not
     * been persisted yet (id == null) has no identity to compare, so it is only equal to
     * itself. This is the standard entity contract and deliberately differs from the
     * value objects, which compare by value.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TrainingProgram that)) {
            return false;
        }
        return this.id != null && this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "TrainingProgram{"
            + "id=" + id
            + ", code='" + code.value() + '\''
            + ", name='" + name.value() + '\''
            + ", startDate=" + period.startDate()
            + ", endDate=" + period.endDate()
            + ", status='" + status.value() + '\''
            + '}';
    }
}
