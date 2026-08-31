package cl.keber.application.command;

import java.time.LocalDate;

/**
 * Raw input for creating a training program.
 *
 * <p>Commands carry primitives, exactly as they arrive from the caller. The use case
 * turns them into value objects, so an invalid field fails with the domain's
 * {@link IllegalArgumentException} rather than being accepted silently.
 *
 * <p>There is deliberately no {@code id}: a created program has no identity until the
 * repository assigns one.
 */
public record CreateTrainingProgramCommand(
    String code,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    String status) {
}
