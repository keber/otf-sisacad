package cl.keber.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingProgramNameTest {

    @Test
    @DisplayName("A non-blank name is accepted")
    void shouldAcceptNonBlankValue() {
        assertEquals("Occupational Health", new TrainingProgramName("Occupational Health").value());
    }

    @Test
    @DisplayName("A null name is rejected")
    void shouldRejectNull() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingProgramName(null));
        assertEquals("name must not be null or blank", ex.getMessage());
    }

    @ParameterizedTest(name = "A blank name [{0}] is rejected")
    @ValueSource(strings = {"", " ", "   ", "\t"})
    void shouldRejectBlank(String value) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingProgramName(value));
        assertEquals("name must not be null or blank", ex.getMessage());
    }

    @Test
    @DisplayName("Surrounding whitespace is trimmed")
    void shouldTrimValue() {
        assertEquals("First Aid", new TrainingProgramName("  First Aid  ").value());
    }

    @Test
    @DisplayName("Names compare by value")
    void shouldCompareByValue() {
        assertEquals(new TrainingProgramName("First Aid"), new TrainingProgramName("First Aid"));
        assertNotEquals(new TrainingProgramName("First Aid"), new TrainingProgramName("Fire Safety"));
    }
}
