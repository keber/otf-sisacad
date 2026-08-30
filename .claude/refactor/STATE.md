# Refactor state board

The orchestrator maintains this file. Workers append their handoff note to their
WP row when they finish. Convert relative dates to absolute (`YYYY-MM-DD`).

## Status legend

`TODO` · `IN PROGRESS` · `IN REVIEW` · `MERGED` · `BLOCKED`

## Work packages

| WP | Status | Branch | PR | Merged on | Notes |
|---|---|---|---|---|---|
| WP1 baseline | TODO | `refactor/wp1-baseline` | – | – | |
| WP2 boundaries | TODO | `refactor/wp2-boundaries` | – | – | |
| WP3 domain | TODO | `refactor/wp3-domain` | – | – | |
| WP4 repository port | TODO | `refactor/wp4-repository-port` | – | – | |
| WP5 use cases | TODO | `refactor/wp5-use-cases` | – | – | |
| WP6 persistence | TODO | `refactor/wp6-persistence` | – | – | |
| WP7 web + wiring | TODO | `refactor/wp7-web` | – | – | |
| WP8 archunit + cleanup | TODO | `refactor/wp8-archunit-cleanup` | – | – | |
| WP-DOCS architecture | TODO | `refactor/wp-docs` | – | – | |

## Baseline (filled by WP1)

- Total tests: _tbd_
- Failing tests: _tbd_
- Line coverage: _tbd_
- Endpoints: `POST /programs`, `GET /programs`, `PUT /programs/{id}`, `DELETE /programs/{id}`
- HTTP codes today: create `200`, list `200`, update `200`, delete `204`, validation error `500` (`IllegalArgumentException`, uncaught), not-found `500` (`TrainingProgramNotFoundException`, uncaught)
- Schema: table `training_program` (Flyway through `V5`), columns include `code`, `name`, `start_date`, `end_date`, `status`

> The characterization tests freeze whatever the baseline actually is. If error
> responses are currently `500`, they stay `500` through the refactor unless a WP
> explicitly changes them with sign-off recorded under "Decisions".

## Decisions

- _None yet._ Record any deviation from `REFACTOR-GUIDE.md` here: what, why, who approved.

## Handoff notes

### WP1
_pending_

### WP2
_pending_

### WP3
_pending_

### WP4
_pending_

### WP5
_pending_

### WP6
_pending_

### WP7
_pending_

### WP8
_pending_

### WP-DOCS
_pending_

## Rubric checklist (closed out in WP8)

- [ ] **Layer separation 4/4** — `domain` / `application` / `infrastructure`
      packages exist; ArchUnit proves domain has no Spring/JPA, application has
      no JPA and no infrastructure import, controllers do not touch repositories.
- [ ] **Tactical patterns 4/4** — immutable self-validating Value Objects
      (`TrainingProgramCode`, `TrainingProgramName`, `TrainingPeriod`,
      `TrainingProgramStatus`); entity `TrainingProgram` with lifecycle
      behaviour (`rename`, `reschedule`, …) and no blanket setters; invalid
      states unconstructable; pure-Java domain tests.
- [ ] **Repository + contracts 4/4** — `domain` port `TrainingProgramRepository`;
      `infrastructure` `JpaTrainingProgramRepositoryAdapter implements` it over
      `SpringDataTrainingProgramRepository`; use cases receive the port via
      constructor; no `new Jpa…` and no `JpaRepository` reference in application.
- [ ] `mvn clean verify` green on `dev`.
- [ ] REST contract under `/programs` unchanged; frontend needs no changes.
- [ ] Legacy `model` / `repository` / `service` packages removed.
- [ ] `docs/architecture/*` published and README points to it.
