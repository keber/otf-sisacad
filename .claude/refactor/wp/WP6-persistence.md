# WP6 — JPA entity, Spring Data repo, persistence adapter (finalised)

**Maps to:** Guide Stage 5 / PR 6
**Worktree branch:** `refactor/wp6-persistence` from `dev` (after WP4 merged)
**Depends on:** WP4.
**Runs in parallel with:** WP5 (disjoint packages — `infrastructure/persistence/**` only here).

> Coordination: same post-WP4 base as WP5. Orchestrator rebases whichever merges
> second; re-run `mvn clean verify` after the rebase.

## Objective

Turn the temporary persistence bridge from WP3/WP4 into the real, isolated
infrastructure implementation. JPA becomes a fully replaceable detail behind the
domain port.

## Tasks

1. **`TrainingProgramJpaEntity`** (`cl.keber.infrastructure.persistence.entity`)
   — finalise the anemic entity:
   - `@Entity @Table(name = "training_program")`.
   - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id`.
   - columns `code`, `name`, `start_date`, `end_date`, `status` — map to the
     **existing** table (Flyway is at `V5`, which already renamed to English and
     added `start_date` / `end_date`). **No new migration.**
   - protected/public no-arg ctor for JPA, plain getters/setters. Anemia is
     fine and expected here — this is not the domain.
2. **`SpringDataTrainingProgramRepository`**
   (`...persistence.repository`) — `extends JpaRepository<TrainingProgramJpaEntity, Long>`.
   Nothing else unless a derived query is genuinely needed.
3. **`TrainingProgramPersistenceMapper`** (`...persistence.mapper`) —
   `toDomain(TrainingProgramJpaEntity)` via `TrainingProgram.restore(...)` and
   the VO constructors; `toJpaEntity(TrainingProgram)` copying VO `value()`s and
   the id. Null-safe.
4. **`JpaTrainingProgramRepositoryAdapter`** (`...persistence.adapter`) —
   `@Repository`, `implements cl.keber.domain.repository.TrainingProgramRepository`,
   holds the Spring Data repo + mapper via constructor injection. Every method
   maps at the boundary and returns domain types / `Optional` / `List`.
5. Delete any leftover bridge glue and comments from WP3/WP4. There must be
   exactly one path: port → adapter → Spring Data → JPA → PostgreSQL/H2.

## Tests (`src/test/java/cl/keber/infrastructure/persistence/**`)

- `@DataJpaTest` with the schema properties used by the existing repo test
  (`spring.flyway.default-schema=OTFSISACAD` etc.) so it runs on H2 and never
  the shared DB.
- Cover: save assigns an id; findById returns a domain `TrainingProgram` with
  correct VO values; findAll; existsById true/false; deleteById.
- A mapper unit test (plain JUnit) for `toDomain` / `toJpaEntity` round-trip.
- Migrate the old `TrainingProgramRepositoryTest` content into the adapter test
  and delete the old class (note in commit body).
- This is now the place that verifies JPA mapping — replacing the annotation
  reflection checks deleted from the domain test in WP3.

## Files in scope

- `src/main/java/cl/keber/infrastructure/persistence/**`
- `src/test/java/cl/keber/infrastructure/persistence/**`
- `.claude/refactor/STATE.md` (handoff note)

Do **not** touch `application/**`, `domain/**`, `infrastructure/web/**`, or
Flyway files.

## Definition of done

- `TrainingProgram` (domain) appears nowhere with a JPA annotation; all JPA
  lives on `TrainingProgramJpaEntity`.
- Adapter implements the domain port; `@Repository` only in
  `infrastructure.persistence`.
- No schema migration added; `DatabaseMigrationTest` unaffected.
- Persistence + mapper tests + WP1 characterization tests green.
- `mvn clean verify` green.
- Handoff note: final class names/packages, column mapping, confirmation the
  bridge is fully gone.

## Commit plan

- `refactor: finalise the standalone TrainingProgram JPA entity`
- `refactor: isolate Spring Data behind the persistence adapter`
- `test: cover the persistence adapter and mapper`
- `chore: remove the temporary persistence bridge`
