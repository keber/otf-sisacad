package cl.keber.repository;

import cl.keber.model.TrainingProgram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.flyway.default-schema=OTFSISACAD",
    "spring.flyway.schemas=OTFSISACAD",
    "spring.jpa.properties.hibernate.default_schema=OTFSISACAD"
})
class TrainingProgramRepositoryTest {

    @Autowired
    private TrainingProgramRepository repository;

    @Test
    @DisplayName("Should save and retrieve a TrainingProgram by ID")
    void shouldSaveAndRetrieveTrainingProgram() {
        // Arrange
        TrainingProgram program = new TrainingProgram(
            "PF001", "Occupational Health and Safety",
            LocalDate.of(2025, 7, 1),
            LocalDate.of(2025, 7, 15),
            "VIGENTE"
        );

        // Act
        TrainingProgram saved = repository.save(program);
        Optional<TrainingProgram> retrieved = repository.findById(saved.getId());

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("PF001", retrieved.get().getCode());
        assertEquals("Occupational Health and Safety", retrieved.get().getName());
    }
}
