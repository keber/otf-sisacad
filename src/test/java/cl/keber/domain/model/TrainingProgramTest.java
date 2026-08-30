package cl.keber.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingProgramTest {

    @Test
    @DisplayName("Date validation in TrainingProgram")
    void shouldFailWhenEndDateIsBeforeStartDate() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new TrainingProgram("PF001", "Test Program", start, end, "Description");
        });

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
    void shouldBeAnnotatedWithEntityAndTable() {
        assertTrue(TrainingProgram.class.isAnnotationPresent(Entity.class),
            "The class must be annotated with @Entity");

        assertTrue(TrainingProgram.class.isAnnotationPresent(Table.class),
            "The class must be annotated with @Table");
    }

    @Test
    void shouldHaveIdFieldWithJpaAnnotations() throws NoSuchFieldException {
        Field idField = TrainingProgram.class.getDeclaredField("id");

        assertNotNull(idField, "The 'id' field must exist");
        assertTrue(idField.isAnnotationPresent(Id.class), "The 'id' field must have @Id");
        assertTrue(idField.isAnnotationPresent(GeneratedValue.class), "The 'id' field must have @GeneratedValue");
    }

}

class TrainingProgramBuilder {
    private String code = "PF001";
    private String name = "Valid name";
    private LocalDate startDate = LocalDate.of(2025, 1, 1);
    private LocalDate endDate = LocalDate.of(2025, 12, 31);
    private String status = "active";

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
        return new TrainingProgram(code, name, startDate, endDate, status);
    }
}
