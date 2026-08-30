package cl.keber.infrastructure.persistence;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.persistence.adapter.JpaTrainingProgramRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round trips through the persistence adapter, which is the only implementation of the
 * domain repository port. Every value crossing the adapter is a domain type: the JPA
 * entity never leaves this package.
 */
@DataJpaTest(properties = {
    "spring.flyway.default-schema=OTFSISACAD",
    "spring.flyway.schemas=OTFSISACAD",
    "spring.jpa.properties.hibernate.default_schema=OTFSISACAD"
})
@Import(JpaTrainingProgramRepositoryAdapter.class)
class JpaTrainingProgramRepositoryAdapterTest {

    @Autowired
    private JpaTrainingProgramRepositoryAdapter adapter;

    private static TrainingProgram newProgram(String code, String name) {
        return TrainingProgram.create(
            new TrainingProgramCode(code),
            new TrainingProgramName(name),
            new TrainingPeriod(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 15)),
            new TrainingProgramStatus("VIGENTE"));
    }

    @Test
    @DisplayName("save assigns an id and returns a domain program")
    void shouldSaveAndReturnDomainProgram() {
        TrainingProgram saved = adapter.save(newProgram("PF001", "Occupational Health and Safety"));

        assertNotNull(saved.getId(), "the database generates the id");
        assertEquals("PF001", saved.getCode().value());
        assertEquals("Occupational Health and Safety", saved.getName().value());
        assertEquals(LocalDate.of(2025, 7, 1), saved.getPeriod().startDate());
        assertEquals(LocalDate.of(2025, 7, 15), saved.getPeriod().endDate());
        assertEquals("VIGENTE", saved.getStatus().value());
    }

    @Test
    @DisplayName("findById returns the stored program, or empty for an unknown id")
    void shouldFindById() {
        TrainingProgram saved = adapter.save(newProgram("PF002", "First Aid"));

        Optional<TrainingProgram> found = adapter.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("PF002", found.get().getCode().value());
        assertEquals("First Aid", found.get().getName().value());

        assertTrue(adapter.findById(999_999L).isEmpty(), "an unknown id yields empty");
    }

    @Test
    @DisplayName("findAll returns every stored program as a domain object")
    void shouldListAllStoredPrograms() {
        adapter.save(newProgram("PF003", "Course 3"));
        adapter.save(newProgram("PF004", "Course 4"));

        List<TrainingProgram> all = adapter.findAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().allMatch(program -> program.getId() != null));
        assertTrue(all.stream().anyMatch(program -> "PF003".equals(program.getCode().value())));
        assertTrue(all.stream().anyMatch(program -> "PF004".equals(program.getCode().value())));
    }

    @Test
    @DisplayName("existsById reflects whether the row is there")
    void shouldReportExistence() {
        TrainingProgram saved = adapter.save(newProgram("PF005", "Course 5"));

        assertTrue(adapter.existsById(saved.getId()));
        assertFalse(adapter.existsById(999_999L));
    }

    @Test
    @DisplayName("deleteById removes the stored program")
    void shouldDeleteById() {
        TrainingProgram saved = adapter.save(newProgram("PF006", "Course 6"));

        adapter.deleteById(saved.getId());

        assertFalse(adapter.existsById(saved.getId()));
        assertTrue(adapter.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("saving a program that already has an id updates it in place")
    void shouldUpdateExistingProgram() {
        TrainingProgram saved = adapter.save(newProgram("PF007", "Original Course"));

        TrainingProgram updated = adapter.save(TrainingProgram.restore(
            saved.getId(),
            new TrainingProgramCode("PF007"),
            new TrainingProgramName("Updated Course"),
            new TrainingPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 20)),
            new TrainingProgramStatus("VIGENTE")));

        assertEquals(saved.getId(), updated.getId());
        assertEquals("Updated Course", updated.getName().value());
        assertEquals(1, adapter.findAll().size(), "the update replaces the row instead of adding one");
    }
}
