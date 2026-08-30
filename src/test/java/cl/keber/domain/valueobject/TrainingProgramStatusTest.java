package cl.keber.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingProgramStatusTest {

    @Test
    @DisplayName("A non-blank status is accepted")
    void shouldAcceptNonBlankValue() {
        assertEquals("VIGENTE", new TrainingProgramStatus("VIGENTE").value());
    }

    @Test
    @DisplayName("Any non-blank string is a legal status: the values are not an enum")
    void shouldAcceptArbitraryStringValues() {
        assertEquals("CERRADO", new TrainingProgramStatus("CERRADO").value());
        assertEquals("active", new TrainingProgramStatus("active").value());
    }

    @Test
    @DisplayName("A null status is rejected")
    void shouldRejectNull() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingProgramStatus(null));
        assertEquals("status must not be null or blank", ex.getMessage());
    }

    @ParameterizedTest(name = "A blank status [{0}] is rejected")
    @ValueSource(strings = {"", " ", "   ", "\t"})
    void shouldRejectBlank(String value) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingProgramStatus(value));
        assertEquals("status must not be null or blank", ex.getMessage());
    }

    @Test
    @DisplayName("Surrounding whitespace is trimmed")
    void shouldTrimValue() {
        assertEquals("VIGENTE", new TrainingProgramStatus("  VIGENTE  ").value());
    }

    @Test
    @DisplayName("Statuses compare by value")
    void shouldCompareByValue() {
        assertEquals(new TrainingProgramStatus("VIGENTE"), new TrainingProgramStatus("VIGENTE"));
        assertNotEquals(new TrainingProgramStatus("VIGENTE"), new TrainingProgramStatus("CERRADO"));
    }
}
