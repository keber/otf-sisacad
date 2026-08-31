package cl.keber.infrastructure.persistence.mapper;

import cl.keber.domain.model.TrainingProgram;
import cl.keber.domain.valueobject.TrainingPeriod;
import cl.keber.domain.valueobject.TrainingProgramCode;
import cl.keber.domain.valueobject.TrainingProgramName;
import cl.keber.domain.valueobject.TrainingProgramStatus;
import cl.keber.infrastructure.persistence.entity.TrainingProgramJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Plain JUnit cover for the persistence mapper: no Spring context, no database. It pins
 * the field-by-field translation in both directions and the null-safety contract the
 * adapter relies on.
 */
class TrainingProgramPersistenceMapperTest {

    private static final LocalDate START = LocalDate.of(2025, 7, 1);
    private static final LocalDate END = LocalDate.of(2025, 7, 15);

    private static TrainingProgramJpaEntity jpaEntity() {
        TrainingProgramJpaEntity entity = new TrainingProgramJpaEntity();
        entity.setId(42L);
        entity.setCode("PF001");
        entity.setName("Occupational Health and Safety");
        entity.setStartDate(START);
        entity.setEndDate(END);
        entity.setStatus("VIGENTE");
        return entity;
    }

    private static TrainingProgram domainProgram() {
        return TrainingProgram.restore(
            42L,
            new TrainingProgramCode("PF001"),
            new TrainingProgramName("Occupational Health and Safety"),
            new TrainingPeriod(START, END),
            new TrainingProgramStatus("VIGENTE"));
    }

    @Test
    @DisplayName("toDomain copies every mapped column into the matching value object")
    void shouldMapEntityToDomain() {
        TrainingProgram program = TrainingProgramPersistenceMapper.toDomain(jpaEntity());

        assertEquals(42L, program.getId());
        assertEquals("PF001", program.getCode().value());
        assertEquals("Occupational Health and Safety", program.getName().value());
        assertEquals(START, program.getPeriod().startDate());
        assertEquals(END, program.getPeriod().endDate());
        assertEquals("VIGENTE", program.getStatus().value());
    }

    @Test
    @DisplayName("toJpaEntity unwraps every value object into its column")
    void shouldMapDomainToEntity() {
        TrainingProgramJpaEntity entity = TrainingProgramPersistenceMapper.toJpaEntity(domainProgram());

        assertEquals(42L, entity.getId());
        assertEquals("PF001", entity.getCode());
        assertEquals("Occupational Health and Safety", entity.getName());
        assertEquals(START, entity.getStartDate());
        assertEquals(END, entity.getEndDate());
        assertEquals("VIGENTE", entity.getStatus());
    }

    @Test
    @DisplayName("a domain program survives a round trip through the JPA entity unchanged")
    void shouldRoundTripDomainProgram() {
        TrainingProgram original = domainProgram();

        TrainingProgram roundTripped =
            TrainingProgramPersistenceMapper.toDomain(TrainingProgramPersistenceMapper.toJpaEntity(original));

        assertEquals(original.getId(), roundTripped.getId());
        assertEquals(original.getCode(), roundTripped.getCode());
        assertEquals(original.getName(), roundTripped.getName());
        assertEquals(original.getPeriod(), roundTripped.getPeriod());
        assertEquals(original.getStatus(), roundTripped.getStatus());
    }

    @Test
    @DisplayName("a JPA entity survives a round trip through the domain program unchanged")
    void shouldRoundTripJpaEntity() {
        TrainingProgramJpaEntity original = jpaEntity();

        TrainingProgramJpaEntity roundTripped =
            TrainingProgramPersistenceMapper.toJpaEntity(TrainingProgramPersistenceMapper.toDomain(original));

        assertEquals(original.getId(), roundTripped.getId());
        assertEquals(original.getCode(), roundTripped.getCode());
        assertEquals(original.getName(), roundTripped.getName());
        assertEquals(original.getStartDate(), roundTripped.getStartDate());
        assertEquals(original.getEndDate(), roundTripped.getEndDate());
        assertEquals(original.getStatus(), roundTripped.getStatus());
    }

    @Test
    @DisplayName("a new program with no id maps to an entity with a null id")
    void shouldMapUnsavedProgramWithoutId() {
        TrainingProgram unsaved = TrainingProgram.create(
            new TrainingProgramCode("PF002"),
            new TrainingProgramName("First Aid"),
            new TrainingPeriod(START, END),
            new TrainingProgramStatus("VIGENTE"));

        TrainingProgramJpaEntity entity = TrainingProgramPersistenceMapper.toJpaEntity(unsaved);

        assertNull(entity.getId(), "the database assigns the id, not the mapper");
        assertEquals("PF002", entity.getCode());
    }

    @Test
    @DisplayName("both directions are null-safe")
    void shouldMapNullToNull() {
        assertNull(TrainingProgramPersistenceMapper.toDomain(null));
        assertNull(TrainingProgramPersistenceMapper.toJpaEntity(null));
    }

    @Test
    @DisplayName("the entity maps exactly the six columns the mapper knows about")
    void shouldMapOnlyTheSixKnownColumns() {
        List<String> persistentFields = Arrays.stream(TrainingProgramJpaEntity.class.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(Field::getName)
            .sorted()
            .toList();

        // description, revision, valid_from and valid_to exist in the table and stay
        // deliberately unmapped: nothing in the application reads or writes them.
        assertEquals(
            List.of("code", "endDate", "id", "name", "startDate", "status"),
            persistentFields,
            "the orphan table columns must not gain fields on the entity");
    }
}
