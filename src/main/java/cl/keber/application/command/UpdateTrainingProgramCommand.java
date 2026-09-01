package cl.keber.application.command;

import java.time.LocalDate;

/**
 * Raw input for updating a training program.
 *
 * <p>{@code id} is the identifier the caller supplied <em>in the payload</em>, not the
 * identifier of the program being addressed; the addressed id is the separate first
 * argument of
 * {@link cl.keber.application.usecase.UpdateTrainingProgramUseCase#execute(Long,
 * UpdateTrainingProgramCommand)}. The two are kept apart on purpose: the current REST
 * contract accepts a payload with no id at all, and the use case has to tell that apart
 * from a payload whose id matches the addressed program, because the two behave
 * differently today. See the use case for the exact rules.
 */
public record UpdateTrainingProgramCommand(
    Long id,
    String code,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    String status) {
}
