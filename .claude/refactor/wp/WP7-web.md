# WP7 — REST decoupling + Spring wiring

**Maps to:** Guide Stage 6–7 / PR 7
**Worktree branch:** `refactor/wp7-web` from `dev` (after WP5 **and** WP6 merged)
**Depends on:** WP5, WP6.
**Runs in parallel with:** WP-DOCS.

## Objective

Make the controller depend only on use-case interfaces, keep the `/programs`
contract byte-for-byte, and move all dependency wiring into an
`infrastructure.config` `@Configuration` so `application` and `domain` carry
zero Spring annotations.

## Tasks

1. **Controller** (`cl.keber.infrastructure.web.controller.TrainingProgramController`):
   - Constructor takes the use-case interfaces it needs
     (`CreateTrainingProgramUseCase`, `ListTrainingProgramsUseCase`,
     `GetTrainingProgramUseCase`, `UpdateTrainingProgramUseCase`,
     `DeleteTrainingProgramUseCase`).
   - No import of any repository, JPA type, or `TrainingProgramApplicationService`
     concrete class.
   - Endpoints unchanged: `POST /programs` (200), `GET /programs` (200),
     `PUT /programs/{id}` (200), `DELETE /programs/{id}` (204) — or whatever
     WP1 pinned. Response bodies keep fields `code, name, startDate, endDate,
     status`.
2. **DTOs + web mapper** (`infrastructure.web.dto`, `infrastructure.web.mapper`):
   - Request DTO → application `Command` (in the mapper or the controller).
   - Domain `TrainingProgram` → response DTO by reading VO `value()`s.
   - Keep a single `TrainingProgramDto` if the current shape works for both
     directions, or split into request/response DTOs — note the choice.
3. **Wiring** (`cl.keber.infrastructure.config.TrainingProgramConfiguration`):
   - `@Configuration` with a `@Bean TrainingProgramApplicationService
     trainingProgramService(TrainingProgramRepository repository)` returning
     `new TrainingProgramApplicationService(repository)`.
   - Expose it as each use-case interface if Spring needs distinct bean types
     (a single bean implementing all five interfaces is injectable by any of
     them — verify at runtime via the characterization test).
   - **Remove** the temporary `@Service`/`@Component` WP5 may have left on the
     application service.
4. **Error handling:** if WP1 pinned validation/not-found as `500`, leave it.
   If the orchestrator has recorded a decision to add a
   `@RestControllerAdvice` (e.g. `IllegalArgumentException` → 400,
   `TrainingProgramNotFoundException` → 404), do it here in
   `infrastructure.web` and update the characterization tests **in the same
   commit** with a `// behaviour change: approved <date>` note. Default: no
   change.
5. **Web tests** (`src/test/java/cl/keber/infrastructure/web/**`):
   - `@WebMvcTest(TrainingProgramController.class)`, `@MockBean` the **use-case
     interfaces** instead of the old service.
   - Keep the JSON assertions from the old controller test.

## Files in scope

- `src/main/java/cl/keber/infrastructure/web/**`
- `src/main/java/cl/keber/infrastructure/config/**` (new configuration)
- `src/main/java/cl/keber/application/service/TrainingProgramApplicationService.java`
  (remove the temporary Spring annotation only)
- `src/test/java/cl/keber/infrastructure/web/**`
- `src/test/java/cl/keber/characterization/**` (only if an approved behaviour change)
- `.claude/refactor/STATE.md` (handoff note)

## Definition of done

- `grep -R "org.springframework" src/main/java/cl/keber/application src/main/java/cl/keber/domain` → **no hits**.
- Controller references only use-case interfaces + web DTO/mapper.
- App boots; characterization tests pass (unchanged, unless an approved,
  documented behaviour change).
- `mvn clean verify` green.
- Handoff note: bean wiring summary, DTO shape decision, any error-handling
  decision.

## Commit plan

- `refactor: drive the controller through use case interfaces`
- `refactor: map REST DTOs to application commands`
- `feat: wire the application service via infrastructure configuration`
- `refactor: remove Spring annotations from the application layer`
- `test: mock use cases in the controller web test`
