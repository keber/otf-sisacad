# Refactor state board

The orchestrator maintains this file. Workers append their handoff note to their
WP row when they finish. Convert relative dates to absolute (`YYYY-MM-DD`).

## Status legend

`TODO` · `IN PROGRESS` · `IN REVIEW` · `MERGED` · `BLOCKED`

## Work packages

| WP | Status | Branch | PR | Merged on | Notes |
|---|---|---|---|---|---|
| WP1 baseline | IN REVIEW | `refactor/wp1-baseline` | – | – | Baseline recorded; 15 characterization tests added |
| WP2 boundaries | TODO | `refactor/wp2-boundaries` | – | – | |
| WP3 domain | TODO | `refactor/wp3-domain` | – | – | |
| WP4 repository port | TODO | `refactor/wp4-repository-port` | – | – | |
| WP5 use cases | TODO | `refactor/wp5-use-cases` | – | – | |
| WP6 persistence | TODO | `refactor/wp6-persistence` | – | – | |
| WP7 web + wiring | TODO | `refactor/wp7-web` | – | – | |
| WP8 archunit + cleanup | TODO | `refactor/wp8-archunit-cleanup` | – | – | |
| WP-DOCS architecture | TODO | `refactor/wp-docs` | – | – | |

## Baseline (filled by WP1)

Measured on 2026-08-30 on `refactor/wp1-baseline` (branched from `dev` @ `00480a5`)
with Microsoft OpenJDK 25.0.2 and Maven 3.9.10, via plain `mvn clean verify`.

### Build and test counts

| | Before WP1 | After WP1 |
|---|---|---|
| `mvn clean verify` | BUILD SUCCESS | BUILD SUCCESS |
| Total tests | 28 | 43 |
| Failures / errors / skipped | 0 / 0 / 0 | 0 / 0 / 0 |
| Line coverage (JaCoCo) | 84.69% (83/98) | 90.82% (89/98) |
| Instruction coverage | 75.07% (277/369) | 90.51% (334/369) |
| Branch coverage | 62.50% (10/16) | 81.25% (13/16) |

Per test class before WP1: `DatabaseMigrationTest` 4, `TrainingProgramControllerTest` 4,
`TrainingProgramTest` 8, `TrainingProgramServiceTest` 5, `WebConfigTest` 1,
`TrainingProgramDtoTest` 1, `TrainingProgramNotFoundExceptionTest` 1,
`TrainingProgramMapperTest` 2, `OtfSisacadApplicationTests` 1,
`TrainingProgramRepositoryTest` 1. WP1 adds `TrainingProgramApiCharacterizationTest` (15).

`DatabaseMigrationTest` and `OtfSisacadApplicationTests` do connect to the shared
Railway database and passed. The characterization tests do not.

### Wire format

The controller binds and returns the **JPA entity** `cl.keber.model.TrainingProgram`
directly. `TrainingProgramDto` and `TrainingProgramMapper` exist but are unused by the
REST layer. Request and response bodies are therefore:

```json
{"id": 1, "code": "PF001", "name": "Occupational Health",
 "startDate": "2025-07-01", "endDate": "2025-07-15", "status": "VIGENTE"}
```

Field order in the response is exactly `id, code, name, startDate, endDate, status`.
`GET /programs` returns a bare JSON array of these objects. `id` is accepted on input
and is significant on `PUT` (see below). Dates are ISO-8601 `yyyy-MM-dd`.

### HTTP status codes actually returned today

| Request | Status | Body |
|---|---|---|
| `POST /programs` valid | **200** (not 201) | the created program, with generated `id` |
| `POST /programs` blank `code` (`""`) | **200** | accepted and **persisted** |
| `POST /programs` `endDate` before `startDate` | **200** | accepted and **persisted** |
| `POST /programs` `code: null` | **500** | Spring default error body |
| `POST /programs` `{}` | **500** | Spring default error body |
| `POST /programs` malformed JSON | **400** | Spring default error body |
| `GET /programs` | **200** | JSON array |
| `PUT /programs/{id}` with matching `id` in body | **200** | the updated program |
| `PUT /programs/{id}` **without** `id` in body | **200** | a **newly inserted** program |
| `PUT /programs/{id}` with a different `id` in body | **500** | Spring default error body |
| `PUT /programs/{id}` unknown id | **500** | Spring default error body |
| `DELETE /programs/{id}` existing | **204** | empty |
| `DELETE /programs/{id}` unknown id | **204** | empty |
| `GET /programs/{id}` | **405** (not 404) | Spring default error body |

The 500 error body is Spring Boot's default:
`{"timestamp":"...","status":500,"error":"Internal Server Error","path":"/programs/..."}`.
There is no `@ControllerAdvice` and `TrainingProgramNotFoundException` carries no
`@ResponseStatus`, so every unhandled exception becomes a 500.

### Surprising behaviour the later WPs must preserve (or change deliberately)

1. **Domain validation never runs over HTTP.** `TrainingProgram`'s validating
   constructor rejects a blank `code`/`name`/`status`, a null date, and an `endDate`
   not after `startDate` - but Jackson binds the request through the **no-arg**
   constructor and writes the private fields directly (the properties are discovered
   from the getters). No bean validation is configured. So blank `code` and inverted
   date ranges are accepted with **200** and stored. Only the `NOT NULL` constraints on
   `training_program.code` / `name` from migration `V1` reject anything, and they do it
   as a **500**.
2. **`PUT` without an `id` in the body inserts a duplicate.**
   `TrainingProgramService.update(id, updated)` checks that `{id}` exists, then calls
   `repository.save(updated)` with the request-body entity. If that entity has no `id`,
   JPA persists it as a new row: the addressed program is left untouched and a second
   row appears. The frontend must be sending `id` in the body today.
3. **`DELETE` on an unknown id returns 204**, not 404 - Spring Data's `deleteById` is a
   no-op for an absent id. Delete is idempotent and callers cannot tell "deleted" from
   "never existed".
4. **`GET /programs/{id}` returns 405**, not 404. `TrainingProgramService.findById`
   exists but no controller method exposes it; the path is mapped for `PUT`/`DELETE`
   only.
5. **`POST` returns 200, not 201**, and sets no `Location` header.
6. CORS is open only to `http://localhost:3000` for `GET, POST, PUT, DELETE, OPTIONS`
   on `/**` (`WebConfig`).

### Schema facts for the slice

Table `training_program` after Flyway `V1` + `V5` (renamed from `programa_formativo`):

| Column | Type | Notes |
|---|---|---|
| `id` | `SERIAL PRIMARY KEY` | mapped, `GenerationType.IDENTITY`; `ddl-auto=update` widens it to `bigint` |
| `code` | `VARCHAR(100) NOT NULL` | mapped; `ddl-auto=update` widens it to `varchar(255)` |
| `name` | `VARCHAR(255) NOT NULL` | mapped |
| `description` | `VARCHAR(100)` | **not mapped** by the entity - orphan column |
| `revision` | `INT` | **not mapped** - orphan column (was `version`) |
| `valid_from` | `DATE` | **not mapped** - orphan column |
| `valid_to` | `DATE` | **not mapped** - orphan column |
| `status` | `VARCHAR(50)` | mapped; `ddl-auto=update` widens it to `varchar(255)` |
| `start_date` | `DATE` | mapped; added by `V5` |
| `end_date` | `DATE` | mapped; added by `V5` |

`spring.jpa.hibernate.ddl-auto=update` is active in `application.properties`, so
Hibernate alters column types on every startup on top of the Flyway schema. There are
no unique constraints on `code`: duplicates are legal, which is what makes item 2 above
possible. Schema is `otfsisacad` (`OTFSISACAD` in the H2 tests).

## Decisions

- _None yet._ Record any deviation from `REFACTOR-GUIDE.md` here: what, why, who approved.

## Handoff notes

### WP1

_2026-08-30 - branch `refactor/wp1-baseline`, branched from `dev` @ `00480a5`._

**What changed.** No production code. One new test class:
`src/test/java/cl/keber/characterization/TrainingProgramApiCharacterizationTest.java`
(15 tests). The Baseline section above is filled with the measured numbers.

**How it runs.** `mvn test -Dtest=TrainingProgramApiCharacterizationTest`, or as part
of `mvn clean verify`. It is a `@SpringBootTest(webEnvironment = RANDOM_PORT)` driven by
`TestRestTemplate`, on an in-memory H2 datasource configured explicitly in the
annotation's `properties` (`jdbc:h2:mem:characterization`, Flyway `V1`-`V5` into the
`OTFSISACAD` schema, same pattern as `TrainingProgramRepositoryTest`). It never touches
the shared Railway database and needs no `DB_*` environment variables.

**Why a real port instead of MockMvc.** MockMvc rethrows an unhandled controller
exception to the caller rather than producing a response, which would hide the real
status codes on the error paths. Over a real port the 500s are observable.

**What later WPs must know.**

- These tests are the contract. They must survive WP2-WP8 **unchanged**. If a WP
  deliberately changes one of them (for example adding a `@ControllerAdvice` so
  not-found becomes 404 instead of 500), that is a contract change: record it under
  "Decisions" with sign-off, do not quietly edit the assertion.
- Every assertion that pins behaviour which is arguably wrong carries a
  `// characterization: pins current behaviour, not desired behaviour` comment. See
  "Surprising behaviour" above - in particular that domain validation never runs over
  HTTP, and that `PUT` without an `id` in the body inserts a duplicate row.
- The tests share one Spring context and the H2 database is **not** rolled back between
  methods. Each test uses its own unique `code` and asserts by `code`/`id` rather than
  by list size. Keep that property if you add cases.
- The tests assert the exact response field order `id, code, name, startDate, endDate,
  status`. WP7 must keep the web-layer response shape identical, including `id`.
- `TrainingProgramDto` and `TrainingProgramMapper` are currently **dead code** as far as
  the REST layer is concerned - the controller binds the JPA entity. They are covered
  only by their own unit tests. WP7 will presumably make the DTO the real wire type; the
  characterization tests are what prove the shape did not drift when it does.

**Follow-ups deliberately not done here.** WP1 changes no production code, so the 500s,
the duplicate-inserting `PUT`, the bypassed validation, `ddl-auto=update`, and the four
orphan columns (`description`, `revision`, `valid_from`, `valid_to`) are all left as
found.

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
