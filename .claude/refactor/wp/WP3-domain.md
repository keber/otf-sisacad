# WP3 — Value Objects + pure domain entity

**Maps to:** Guide Stage 2 / PR 3
**Worktree branch:** `refactor/wp3-domain` from `dev` (after WP2 merged)
**Depends on:** WP2.
**Runs in parallel with:** WP-DOCS.

## Objective

Make `cl.keber.domain` pure Java: no `jakarta.persistence`, no
`org.springframework`, no Jackson. Introduce immutable, self-validating Value
Objects and give `TrainingProgram` lifecycle behaviour instead of setters.

At the end of this WP the domain will **not** compile against JPA anymore, so
persistence must keep working through a temporary bridge until WP6. See
"Keeping the app green" below.

## Value Objects (new, in `cl.keber.domain.valueobject`)

Java `record`s, validate in the compact constructor, trim where sensible:

- `TrainingProgramCode(String value)` — non-blank.
- `TrainingProgramName(String value)` — non-blank.
- `TrainingPeriod(LocalDate startDate, LocalDate endDate)` — both non-null;
  `endDate` strictly after `startDate` (preserve the current rule and message
  intent: `"endDate must be after startDate"`).
- `TrainingProgramStatus(String value)` — non-blank. (The guide omits status;
  the slice has it and the characterization tests exercise it, so model it as a
  VO for consistency. Keep string values; do not introduce an enum unless the
  orchestrator approves it under "Decisions".)

Each VO: immutable, no setters, impossible to construct invalid. Keep exception
type `IllegalArgumentException` and keep messages English and close to today's
wording so WP1 tests still pass.

## Entity `cl.keber.domain.model.TrainingProgram`

- `final class`, private fields typed as the VOs above plus `Long id`.
- Private all-args constructor; two factories:
  - `create(code, name, period, status)` → `id == null`.
  - `restore(id, code, name, period, status)` → rehydration from persistence.
- Behaviour methods, not setters:
  - `rename(TrainingProgramName newName)`
  - `reschedule(TrainingPeriod newPeriod)`
  - `changeStatus(TrainingProgramStatus newStatus)`
- Remove `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, the JPA no-arg
  constructor, and the `import jakarta.persistence.*`.
- Keep an SLF4J debug log on creation if you like (SLF4J is not a framework
  coupling the rubric cares about), or drop it — your call, note it.
- `equals`/`hashCode`: by `id` when present; document the choice in a comment.

## Tests (`src/test/java/cl/keber/domain/**`) — pure JUnit, no Spring

- `TrainingProgramCodeTest`, `TrainingProgramNameTest`, `TrainingPeriodTest`,
  `TrainingProgramStatusTest`: null → invalid, blank → invalid, valid →
  constructs, trimming, `endDate` before/equal `startDate` → invalid.
- `TrainingProgramTest`: rewrite the existing one.
  - **Delete the reflection-based assertions** that check for `@Entity`, `@Id`,
    `@Table`, `@GeneratedValue`. In the commit body, state: *"annotation checks
    removed; JPA mapping is verified in infrastructure from WP6"*. This is the
    architectural contradiction called out in the guide's intro — it is
    intentional.
  - Add: factory `create` yields `id == null`; `restore` keeps the id;
    `reschedule` with an invalid period is impossible (the VO throws);
    `rename` changes only the name.
- No `@SpringBootTest`, `@DataJpaTest`, `@MockBean` anywhere under
  `domain` tests.

## Keeping the app green (temporary bridge — removed in WP6)

`infrastructure.persistence.repository.TrainingProgramRepository` currently is
`JpaRepository<TrainingProgram, Long>` and `TrainingProgramService` /
`TrainingProgramController` / `TrainingProgramMapper` all pass the domain type
around. Once `TrainingProgram` loses its JPA annotations that breaks.

Do the **minimum** to keep `mvn clean verify` green without re-introducing JPA
into the domain:

1. Add `infrastructure/persistence/entity/TrainingProgramJpaEntity.java` — an
   anemic `@Entity @Table(name = "training_program")` class with
   `id, code, name, startDate, endDate, status` + getters/setters + no-arg ctor.
   (WP6 will own this properly; a first cut here is acceptable.)
2. Point `TrainingProgramRepository extends JpaRepository<TrainingProgramJpaEntity, Long>`.
3. Add `infrastructure/persistence/mapper/TrainingProgramPersistenceMapper` with
   `toDomain` / `toJpaEntity`.
4. In `application.service.TrainingProgramService`, map domain ⇄ JPA entity at
   the repository boundary so its public method signatures (domain in, domain
   out) are unchanged.
5. `infrastructure.web.mapper.TrainingProgramMapper` now builds the domain
   entity via `TrainingProgram.create(...)` from the DTO and reads VO accessors
   back out for the response DTO.

Keep this bridge small and clearly commented `// bridge: replaced by the
adapter in WP6`. WP5 and WP6 will supersede items 2–5.

> If the bridge balloons past ~1 file of glue, stop and tell the orchestrator —
> it may be better to pull WP6 forward.

## Files in scope

- `src/main/java/cl/keber/domain/**` (new VOs, rewritten entity)
- `src/main/java/cl/keber/infrastructure/persistence/entity/**` (new, first cut)
- `src/main/java/cl/keber/infrastructure/persistence/mapper/**` (new bridge)
- `src/main/java/cl/keber/infrastructure/persistence/repository/TrainingProgramRepository.java`
- `src/main/java/cl/keber/infrastructure/web/mapper/TrainingProgramMapper.java`
- `src/main/java/cl/keber/application/service/TrainingProgramService.java` (bridge mapping only)
- `src/test/java/cl/keber/domain/**`
- `.claude/refactor/STATE.md` (handoff note; Decisions entry for status VO / log choice)

## Definition of done

- `grep -R "jakarta.persistence\|org.springframework\|com.fasterxml" src/main/java/cl/keber/domain` → no hits.
- Domain tests are pure JUnit and pass.
- All WP1 characterization tests pass unchanged.
- `mvn clean verify` green.
- Handoff note: VO list + constructors, entity factory/behaviour signatures,
  and a precise description of the temporary bridge for WP5/WP6 to remove.

## Commit plan

- `feat: add TrainingProgram value objects`
- `refactor: make TrainingProgram a pure domain entity with lifecycle methods`
- `refactor: bridge persistence to the JPA entity to keep the slice working`
- `test: rewrite domain tests as pure JUnit`
