package cl.keber.domain.valueobject;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingPeriodTest {

    private static final LocalDate START = LocalDate.of(2025, 7, 1);
    private static final LocalDate END = LocalDate.of(2025, 7, 15);

    @Test
    @DisplayName("A period whose endDate is after its startDate is accepted")
    void shouldAcceptValidRange() {
        TrainingPeriod period = new TrainingPeriod(START, END);

        assertEquals(START, period.startDate());
        assertEquals(END, period.endDate());
    }

    @Test
    @DisplayName("A null startDate is rejected")
    void shouldRejectNullStartDate() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingPeriod(null, END));
        assertEquals("startDate must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("A null endDate is rejected")
    void shouldRejectNullEndDate() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingPeriod(START, null));
        assertEquals("endDate must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("An endDate before the startDate is rejected")
    void shouldRejectInvertedRange() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingPeriod(END, START));
        assertEquals("endDate must be after startDate", ex.getMessage());
    }

    @Test
    @DisplayName("An endDate equal to the startDate is rejected: the rule is strict (D3)")
    void shouldRejectEqualDates() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> new TrainingPeriod(START, START));
        assertEquals("endDate must be after startDate", ex.getMessage());
    }

    @Test
    @DisplayName("Periods compare by value")
    void shouldCompareByValue() {
        assertEquals(new TrainingPeriod(START, END), new TrainingPeriod(START, END));
    }
}
