# WP1 — Baseline + characterization tests

**Maps to:** Guide Stage 0 / PR 1
**Worktree branch:** `refactor/wp1-baseline` from `dev`
**Depends on:** nothing. This blocks the whole spine.
**Runs in parallel with:** WP-DOCS.

## Objective

Freeze the *observable* behaviour of the TrainingProgram slice with tests that
survive every later work package unchanged. Record the baseline in
[`../STATE.md`](../STATE.md).

## Preconditions

- `mvn clean verify` runs (note pass/fail counts even if some DB tests fail).
- Read `CONVENTIONS.md`.

## Tasks

1. **Record the baseline** in `STATE.md` → "Baseline" section:
   - total tests, failing tests, line coverage (from JaCoCo report).
   - the four endpoints and the exact HTTP status codes they return today,
     including error paths (validation failure, not-found). Capture what the app
     *actually* does now — do not "fix" it here.
   - request/response JSON shape for each endpoint.
   - DB schema facts relevant to the slice (`training_program` columns).
2. **Add characterization tests** — a new test class
   `src/test/java/cl/keber/characterization/TrainingProgramApiCharacterizationTest.java`:
   - Full-stack `@SpringBootTest(webEnvironment = RANDOM_PORT)` +
     `@AutoConfigureMockMvc`, or `@WebMvcTest`-plus-stubbed-service if a full
     context is too heavy against the shared DB — prefer an **H2-backed**
     `@SpringBootTest` using the same properties pattern as
     `TrainingProgramRepositoryTest` (`spring.flyway.*` / `default_schema`
     `OTFSISACAD`) so it never touches Railway.
   - Assert, for each of `POST /programs`, `GET /programs`,
     `PUT /programs/{id}`, `DELETE /programs/{id}`:
     - success status code and response body fields (`code`, `name`,
       `startDate`, `endDate`, `status`).
     - round-trip: create then GET returns the created values.
     - update changes the persisted values; delete makes a subsequent
       GET-by-list not contain it.
   - Assert the current error behaviour for: blank `code`, `endDate` before
     `startDate`, `PUT` on a missing id. Pin whatever status/body is produced
     today (likely `500`). Add a `// characterization: pins current behaviour,
     not desired behaviour` comment.
   - Name tests by behaviour, not implementation
     (`createReturnsPersistedProgram`, not `postCallsService`).
3. Do **not** modify production code. If a test is impossible to write without a
   production change, stop and report to the orchestrator.

## Files in scope

- `src/test/java/cl/keber/characterization/**` (new)
- `.claude/refactor/STATE.md` (baseline + handoff note only)

## Definition of done

- New characterization tests pass locally on H2 and add no dependency on the
  shared DB.
- `mvn clean verify` green (JDK caveat allowed).
- Baseline section of `STATE.md` filled with real numbers.
- Handoff note added: test class name, how it is run, any surprising current
  behaviour the later WPs must preserve.

## Commit plan

- `test: add TrainingProgram API characterization tests`
- `docs: record refactor baseline metrics` (the `STATE.md` edit)
