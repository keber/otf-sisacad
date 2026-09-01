package cl.keber.application.query;

/**
 * Raw input for reading a single training program.
 *
 * <p>This record exists for a compiler reason, not a stylistic one. One class implements
 * all five use cases, so no two of them may share a method signature. Reading and
 * deleting both address a program by its identifier, so if both declared
 * {@code execute(Long)} the two methods would collide - same name, same erasure,
 * different return types - and the implementation would not compile. Wrapping the read
 * argument separates them. The WP allows this shape explicitly.
 */
public record GetTrainingProgramQuery(Long id) {
}
