package cl.keber.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingProgramCodeTest {

    @Test
    @DisplayName("A non-blank code is accepted")
    void shouldAcceptNonBlankValue() {
        assertEquals("PF001", new TrainingProgramCode("PF001").value());
    }

    @Test
    @DisplayName("A null code is rejected")
    void shouldRejectNull() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingProgramCode(null));
        assertEquals("code must not be null or blank", ex.getMessage());
    }

    @ParameterizedTest(name = "A blank code [{0}] is rejected")
    @ValueSource(strings = {"", " ", "   ", "\t"})
    void shouldRejectBlank(String value) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingProgramCode(value));
        assertEquals("code must not be null or blank", ex.getMessage());
    }

    @Test
    @DisplayName("Surrounding whitespace is trimmed")
    void shouldTrimValue() {
        assertEquals("PF001", new TrainingProgramCode("  PF001  ").value());
    }

    @Test
    @DisplayName("Codes compare by value")
    void shouldCompareByValue() {
        assertEquals(new TrainingProgramCode("PF001"), new TrainingProgramCode("PF001"));
        assertEquals(new TrainingProgramCode("PF001").hashCode(), new TrainingProgramCode("PF001").hashCode());
        assertNotEquals(new TrainingProgramCode("PF001"), new TrainingProgramCode("PF002"));
    }
}
