# WP5 — Use cases + application service (framework-free)

**Maps to:** Guide Stage 4 / PR 5
**Worktree branch:** `refactor/wp5-use-cases` from `dev` (after WP4 merged)
**Depends on:** WP4.
**Runs in parallel with:** WP6 (disjoint packages — `application/**` only here).

> Coordination: WP5 and WP6 both branch from the same post-WP4 `dev`. When one
> merges, the orchestrator rebases the other; re-run `mvn clean verify` after
> the rebase.

## Objective

Replace the generic `TrainingProgramService` with explicit use cases and a
single framework-free implementation. The controller (WP7) will depend on these
interfaces, never on a repository.

## Use case interfaces (`cl.keber.application.usecase`)

- `CreateTrainingProgramUseCase` → `TrainingProgram execute(CreateTrainingProgramCommand)`
- `GetTrainingProgramUseCase` → `Optional<TrainingProgram> execute(Long id)` (or a `GetTrainingProgramQuery` if you prefer symmetry — note the choice)
- `ListTrainingProgramsUseCase` → `List<TrainingProgram> execute()`
- `UpdateTrainingProgramUseCase` → `TrainingProgram execute(UpdateTrainingProgramCommand)`
- `DeleteTrainingProgramUseCase` → `void execute(Long id)`

## Commands (`cl.keber.application.command`) — plain records, primitives in

```java
public record CreateTrainingProgramCommand(
    String code, String name, LocalDate startDate, LocalDate endDate, String status) {}

public record UpdateTrainingProgramCommand(
    Long id, String code, String name, LocalDate startDate, LocalDate endDate, String status) {}
```

Commands carry raw input; the use case turns them into VOs / domain entities.
Validation errors surface as the domain's `IllegalArgumentException` (keep WP1
behaviour).

## Implementation

`cl.keber.application.service.TrainingProgramApplicationService` implements all
five interfaces.

- Constructor takes **`cl.keber.domain.repository.TrainingProgramRepository`**
  only. No `new Jpa…`. No Spring annotations — **remove `@Service`** (WP7 adds
  the `@Configuration` `@Bean`). If leaving the app non-wired between WP5 merge
  and WP7 would make `mvn verify` fail, keep a temporary
  `@Component`/`@Service` with a `// wiring moves to infrastructure config in
  WP7` comment and flag it in the handoff.
- `create`: build `TrainingProgram.create(new TrainingProgramCode(cmd.code()),
  …)` then `repository.save`.
- `update`: `repository.findById(id).orElseThrow(() -> new
  TrainingProgramNotFoundException(id))`; apply `rename` / `reschedule` /
  `changeStatus` (or `restore` + save) — preserve the current id-mismatch guard
  behaviour from the old service (`"program ID does not match the provided
  ID"`).
- `delete`: preserve current behaviour (old service just calls
  `deleteById`; keep that unless WP1 pinned a not-found error — match WP1).
- Keep `TrainingProgramNotFoundException` usage; it now lives in
  `cl.keber.domain.exception`.

## Tests (`src/test/java/cl/keber/application/**`) — Mockito, no Spring

- Mock the **domain port**. One test class per use case (or one per behaviour
  group): create persists a built domain entity; update on missing id throws
  `TrainingProgramNotFoundException`; update applies changes; delete delegates;
  list/get delegate. Assert the repository is called with a domain
  `TrainingProgram` whose VOs hold the command values.
- Delete the old `TrainingProgramServiceTest` (its behaviour is now covered by
  the use-case tests) — state this in the commit body.

## Files in scope

- `src/main/java/cl/keber/application/**`
- `src/test/java/cl/keber/application/**`
- `.claude/refactor/STATE.md` (handoff note)

Do **not** touch `infrastructure/**` or `domain/**`. If you think you must,
stop and tell the orchestrator (likely a WP6/WP5 boundary problem).

## Definition of done

- Five use-case interfaces + commands + one implementation, implementation has
  zero framework imports (or a single flagged temporary wiring annotation).
- `grep -R "org.springframework" src/main/java/cl/keber/application` → ideally
  no hits (WP7 guarantees zero).
- Application tests + WP1 characterization tests green.
- `mvn clean verify` green.
- Handoff note: interface + command signatures for WP7; whether a temporary
  wiring annotation was left.

## Commit plan

- `feat: add TrainingProgram use case interfaces and commands`
- `feat: implement use cases in a framework-free application service`
- `test: cover use cases with mocked repository port`
- `refactor: remove the legacy generic TrainingProgramService`
