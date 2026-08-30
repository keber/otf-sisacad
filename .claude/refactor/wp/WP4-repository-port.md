# WP4 — Pure repository port

**Maps to:** Guide Stage 3 / PR 4
**Worktree branch:** `refactor/wp4-repository-port` from `dev` (after WP3 merged)
**Depends on:** WP3.
**Runs in parallel with:** WP-DOCS.

## Objective

Introduce the domain-owned repository contract and make the application depend
on it, not on Spring Data. Small WP; it unlocks the WP5 ∥ WP6 parallel window.

## Tasks

1. New interface `cl.keber.domain.repository.TrainingProgramRepository`:

   ```java
   public interface TrainingProgramRepository {
       TrainingProgram save(TrainingProgram program);
       Optional<TrainingProgram> findById(Long id);
       List<TrainingProgram> findAll();
       boolean existsById(Long id);
       void deleteById(Long id);
   }
   ```

   No `JpaRepository`, `Pageable`, `@Repository`, `EntityManager`,
   `jakarta.*`, `org.springframework.*`. Pure Java + domain types only.

2. Rename the Spring Data interface from WP3's bridge to
   `cl.keber.infrastructure.persistence.repository.SpringDataTrainingProgramRepository`
   (`extends JpaRepository<TrainingProgramJpaEntity, Long>`).

3. Add `cl.keber.infrastructure.persistence.adapter.JpaTrainingProgramRepositoryAdapter`
   `implements` the domain port, delegating to
   `SpringDataTrainingProgramRepository` + `TrainingProgramPersistenceMapper`
   (both from WP3's bridge). `@Repository` lives **here**, in infrastructure.
   Move the bridge mapping logic out of `TrainingProgramService` and into this
   adapter.

4. `application.service.TrainingProgramService` now depends on
   `cl.keber.domain.repository.TrainingProgramRepository` (the port). Its body
   deals only in domain types. Leave `@Service` on it for now — WP7 removes
   Spring annotations from application and adds the `@Configuration` wiring.

5. Update `TrainingProgramServiceTest` to mock the **port** (it already mocks a
   `TrainingProgramRepository` type — just make sure the import is
   `cl.keber.domain.repository.TrainingProgramRepository`).

6. Add `src/test/java/cl/keber/infrastructure/persistence/JpaTrainingProgramRepositoryAdapterTest.java`
   — `@DataJpaTest` (H2, `OTFSISACAD` schema properties like the existing repo
   test) covering save/find/list/exists/delete round-trips through the adapter
   returning **domain** objects.

## Files in scope

- `src/main/java/cl/keber/domain/repository/**` (new port)
- `src/main/java/cl/keber/infrastructure/persistence/**` (rename Spring Data
  iface, new adapter, move bridge mapping here)
- `src/main/java/cl/keber/application/service/TrainingProgramService.java` (depend on the port)
- `src/test/java/cl/keber/application/service/TrainingProgramServiceTest.java` (import fix)
- `src/test/java/cl/keber/infrastructure/persistence/**` (new adapter test)
- `.claude/refactor/STATE.md` (handoff note)

## Definition of done

- `grep -R "org.springframework.data\|JpaRepository\|jakarta.persistence" src/main/java/cl/keber/application src/main/java/cl/keber/domain` → no hits.
- The application service's constructor parameter type is the domain port.
- Adapter test + all WP1 characterization tests + domain tests pass.
- `mvn clean verify` green.
- Handoff note: exact port method signatures, adapter class name/package,
  confirmation that `application/**` and `infrastructure/persistence/**` are now
  cleanly separable (so WP5 and WP6 can run in parallel).

## Commit plan

- `feat: add TrainingProgramRepository port in the domain`
- `refactor: implement the port with a JPA persistence adapter`
- `refactor: depend application on the repository port`
- `test: cover the JPA repository adapter with DataJpaTest`
