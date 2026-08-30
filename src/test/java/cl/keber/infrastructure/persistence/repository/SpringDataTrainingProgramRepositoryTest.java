package cl.keber.infrastructure.persistence.repository;

import cl.keber.infrastructure.persistence.entity.TrainingProgramJpaEntity;
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
class SpringDataTrainingProgramRepositoryTest {

    @Autowired
    private SpringDataTrainingProgramRepository repository;

    @Test
    @DisplayName("Should save and retrieve a TrainingProgram by ID")
    void shouldSaveAndRetrieveTrainingProgram() {
        // Arrange
        // The repository now stores the JPA entity: the domain entity carries no mapping.
        TrainingProgramJpaEntity program = new TrainingProgramJpaEntity();
        program.setCode("PF001");
        program.setName("Occupational Health and Safety");
        program.setStartDate(LocalDate.of(2025, 7, 1));
        program.setEndDate(LocalDate.of(2025, 7, 15));
        program.setStatus("VIGENTE");

        // Act
        TrainingProgramJpaEntity saved = repository.save(program);
        Optional<TrainingProgramJpaEntity> retrieved = repository.findById(saved.getId());

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("PF001", retrieved.get().getCode());
        assertEquals("Occupational Health and Safety", retrieved.get().getName());
        assertEquals(LocalDate.of(2025, 7, 1), retrieved.get().getStartDate());
        assertEquals(LocalDate.of(2025, 7, 15), retrieved.get().getEndDate());
        assertEquals("VIGENTE", retrieved.get().getStatus());
    }
}
