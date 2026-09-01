package cl.keber.domain.model;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;

/**
 * Pure JUnit: no Spring, no JPA, no reflection over persistence annotations. The domain
 * entity no longer carries any, and the JPA mapping is verified in infrastructure.
 */
class TrainingProgramTest {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = LocalDate.of(2025, 12, 31);

    @Test
    @DisplayName("create yields a program with no id yet")
    void createYieldsProgramWithoutId() {
        TrainingProgram program = new TrainingProgramBuilder().build();

        assertNull(program.getId(), "a program that was never persisted has no id");
        assertEquals("PF001", program.getCode().value());
        assertEquals("Valid name", program.getName().value());
        assertEquals(START, program.getPeriod().startDate());
        assertEquals(END, program.getPeriod().endDate());
        assertEquals("active", program.getStatus().value());
    }

    @Test
    @DisplayName("restore keeps the id it is rehydrated with")
    void restoreKeepsTheId() {
        TrainingProgram program = TrainingProgram.restore(
            42L,
            new TrainingProgramCode("PF001"),
            new TrainingProgramName("Valid name"),
            new TrainingPeriod(START, END),
            new TrainingProgramStatus("active"));

        assertEquals(42L, program.getId());
    }

    @Test
    @DisplayName("Date validation happens in the period value object")
    void shouldFailWhenEndDateIsBeforeStartDate() {
        LocalDate endBeforeStart = LocalDate.of(2024, 12, 31);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new TrainingPeriod(START, endBeforeStart));

        assertEquals("endDate must be after startDate", exception.getMessage());
    }

    @ParameterizedTest(name = "Should fail when {0} is null")
    @MethodSource("nullFieldProvider")
    void shouldFailWhenFieldIsNull(String field, TrainingProgramBuilder expectedBuilder, String expectedMessage) {
        Executable action = expectedBuilder::build;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, action);
        assertEquals(expectedMessage, ex.getMessage());
    }

    static Stream<Arguments> nullFieldProvider() {
        return Stream.of(
            Arguments.of("code", new TrainingProgramBuilder().withCode(null), "code must not be null or blank"),
            Arguments.of("name", new TrainingProgramBuilder().withName(null), "name must not be null or blank"),
            Arguments.of("startDate", new TrainingProgramBuilder().withStartDate(null), "startDate must not be null"),
            Arguments.of("endDate", new TrainingProgramBuilder().withEndDate(null), "endDate must not be null"),
            Arguments.of("status", new TrainingProgramBuilder().withStatus(null), "status must not be null or blank")
        );
    }

    @Test
    @DisplayName("rename changes only the name")
    void renameChangesOnlyTheName() {
        TrainingProgram program = new TrainingProgramBuilder().build();

        program.rename(new TrainingProgramName("New name"));

        assertEquals("New name", program.getName().value());
        assertEquals("PF001", program.getCode().value());
        assertEquals(START, program.getPeriod().startDate());
        assertEquals(END, program.getPeriod().endDate());
        assertEquals("active", program.getStatus().value());
    }

    @Test
    @DisplayName("reschedule moves the program to a new period")
    void rescheduleChangesThePeriod() {
        TrainingProgram program = new TrainingProgramBuilder().build();

        program.reschedule(new TrainingPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)));

        assertEquals(LocalDate.of(2026, 1, 1), program.getPeriod().startDate());
        assertEquals(LocalDate.of(2026, 6, 1), program.getPeriod().endDate());
        assertEquals("Valid name", program.getName().value());
    }

    @Test
    @DisplayName("Rescheduling to an invalid period is impossible: the value object rejects it")
    void rescheduleToInvalidPeriodIsUnconstructable() {
        TrainingProgram program = new TrainingProgramBuilder().build();

        assertThrows(IllegalArgumentException.class, () -> new TrainingPeriod(END, START));

        assertEquals(START, program.getPeriod().startDate(), "the program is left untouched");
        assertEquals(END, program.getPeriod().endDate(), "the program is left untouched");
    }

    @Test
    @DisplayName("changeStatus changes only the status")
    void changeStatusChangesOnlyTheStatus() {
        TrainingProgram program = new TrainingProgramBuilder().build();

        program.changeStatus(new TrainingProgramStatus("CERRADO"));

        assertEquals("CERRADO", program.getStatus().value());
        assertEquals("Valid name", program.getName().value());
    }

    @Test
    @DisplayName("Programs with the same id are the same program; unsaved programs are equal only to themselves")
    void identityIsTheId() {
        TrainingProgram one = new TrainingProgramBuilder().withId(1L).build();
        TrainingProgram sameId = new TrainingProgramBuilder().withId(1L).withName("Different name").build();
        TrainingProgram otherId = new TrainingProgramBuilder().withId(2L).build();
        TrainingProgram unsaved = new TrainingProgramBuilder().build();
        TrainingProgram sameUnsaved = unsaved;
        TrainingProgram anotherUnsaved = new TrainingProgramBuilder().build();

        assertEquals(one, sameId);
        assertEquals(one.hashCode(), sameId.hashCode());
        assertNotEquals(one, otherId);
        assertEquals(unsaved, sameUnsaved, "an unsaved program is equal to itself");
        assertNotEquals(unsaved, anotherUnsaved, "distinct unsaved programs are never equal");
        assertNotEquals(null, one);

        assertEquals(
            System.identityHashCode(unsaved),
            unsaved.hashCode()
        );
    }

    @Test
    @DisplayName("")
    void toStringRepresentsProgramState() {
        TrainingProgram program =
        new TrainingProgramBuilder()
            .withId(42L)
            .build();

        assertEquals(
        "TrainingProgram{id=42, code='PF001', name='Valid name', startDate=2025-01-01, endDate=2025-12-31, status='active'}",
        program.toString()
        );
    }
}

class TrainingProgramBuilder {
    private Long id = null;
    private String code = "PF001";
    private String name = "Valid name";
    private LocalDate startDate = LocalDate.of(2025, 1, 1);
    private LocalDate endDate = LocalDate.of(2025, 12, 31);
    private String status = "active";

    TrainingProgramBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    TrainingProgramBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    TrainingProgramBuilder withName(String name) {
        this.name = name;
        return this;
    }

    TrainingProgramBuilder withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    TrainingProgramBuilder withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    TrainingProgramBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    TrainingProgram build() {
        TrainingProgramCode programCode = new TrainingProgramCode(code);
        TrainingProgramName programName = new TrainingProgramName(name);
        TrainingProgramStatus programStatus = new TrainingProgramStatus(status);
        TrainingPeriod period = new TrainingPeriod(startDate, endDate);

        return id == null
            ? TrainingProgram.create(programCode, programName, period, programStatus)
            : TrainingProgram.restore(id, programCode, programName, period, programStatus);
    }
}
