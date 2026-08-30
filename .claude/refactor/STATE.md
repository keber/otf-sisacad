# Refactor state board

The orchestrator maintains this file. Workers append their handoff note to their
WP row when they finish. Convert relative dates to absolute (`YYYY-MM-DD`).

## Status legend

`TODO` · `IN PROGRESS` · `IN REVIEW` · `MERGED` · `BLOCKED`

## Work packages

| WP | Status | Branch | PR | Merged on | Notes |
|---|---|---|---|---|---|
| WP1 baseline | MERGED | `refactor/wp1-baseline` | – | – | Merged 2026-08-30; baseline recorded, 15 characterization tests |
| WP2 boundaries | MERGED | `refactor/wp2-boundaries` | – | – | Merged 2026-08-30; move-only, 43/43 green |
| WP3 domain | IN PROGRESS | `refactor/wp3-domain` | – | – | Wave 2, started 2026-08-30; includes D7 scope extension |
| WP4 repository port | TODO | `refactor/wp4-repository-port` | – | – | |
| WP5 use cases | TODO | `refactor/wp5-use-cases` | – | – | |
| WP6 persistence | TODO | `refactor/wp6-persistence` | – | – | |
| WP7 web + wiring | TODO | `refactor/wp7-web` | – | – | |
| WP8 archunit + cleanup | TODO | `refactor/wp8-archunit-cleanup` | – | – | |
| WP-DOCS architecture | IN PROGRESS | `refactor/wp-docs` | – | – | First draft done 2026-08-30; open for reconciliation, merges in Wave 6 |

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

All approved by Keber Flores on 2026-08-30.

### D1 — Error handling: add a `@RestControllerAdvice` in WP7

`IllegalArgumentException` -> `400`, `TrainingProgramNotFoundException` -> `404`.
Implemented in `infrastructure.web` in WP7. The WP1 characterization tests that
pin today's codes are updated **in the same commit**, each with a
`// behaviour change: approved 2026-08-30` note. Do not weaken or delete them.

### D2 — "Observable behaviour preserved" is a motivation, not a hard gate

The pre-refactor code is not correct, so freezing its behaviour is not the goal;
raising product quality is. Where the refactor turns a silently-accepted invalid
request into a failure, that is a **desired** outcome: it surfaces a real defect.

Rules that follow from this:

- A characterization test may change **only** when the change is a deliberate,
  recorded consequence of the refactor. Convenience is never a reason.
- Every such change is called out in its commit body and in the WP handoff note.
- Defects this exposes are **documented, not fixed**, unless the defect blocks
  the current or a later wave. Out-of-scope fixes are logged under "Exposed
  defects" below for separate follow-up.

### D3 — `TrainingPeriod` follows the code, not the guide

The guide rejects only `endDate.isBefore(startDate)` (allowing equal dates); the
current entity rejects `!startDate.isBefore(endDate)` (equal dates invalid). WP3
keeps the **strict** rule. The guide was derived from the code and does not
mirror it exactly; the code wins.

### D4 — `status` stays a Value Object with string values

`REFACTOR-GUIDE.md` omits `status`, but it is a real field, column and DTO
property. WP3's four-VO design is correct. The guide is general; the WPs are
specific and authoritative where they disagree.

### D5 — Build `GetTrainingProgramUseCase`, do not route it

Verified on 2026-08-30: a `GET /programs/{id}` mapping has never existed on any
branch (`git log --all -S`), so it was not lost in the Spanish-to-English rename.
`TrainingProgramService.findById` exists but no controller method calls it - the
endpoint was planned and never wired. WP5 builds the use case; WP7 does **not**
add the route. Adding it would be a new feature, not a refactor.

### D6 — DTO decoupling does not exist yet

The guide states the DTO split is "already a good decision". It is not: the
controller binds and returns the JPA entity on every endpoint, and
`TrainingProgramDto` / `TrainingProgramMapper` are dead code relative to the REST
layer. Confirmed independently by WP1 (black-box HTTP) and WP-DOCS (source read).
WP7 wires a DTO for the **first time** - scope it as new work, not a refinement.

### D7 — WP3 absorbs the controller's DTO binding (scope extension)

Approved by Keber Flores on 2026-08-30.

**Problem.** `TrainingProgramController` binds and returns the domain type
directly (`@RequestBody TrainingProgram`). WP3 makes `TrainingProgram` a final
class with a private constructor, factories, and VO-typed fields. That breaks the
REST layer twice over:

1. Requests cannot bind - Jackson has no no-arg constructor, so every `POST` and
   `PUT` fails.
2. Responses change shape - VO-returning getters serialise as
   `{"code":{"value":"PRG-1"}}` instead of `{"code":"PRG-1"}`, breaking `GET`
   too. That is a contract break far beyond the validation change in D1/D2.

WP3's declared "Files in scope" excludes `infrastructure/web/controller/`, so
WP3 as written cannot end green. Its bridge assumes a DTO-speaking controller,
which does not exist yet (see D6).

**Decision.** Extend WP3's scope by exactly two things:

- Add an `id` field to `TrainingProgramDto`. It currently has none, but WP1
  proved `id` is on the wire in both directions and the frontend needs it to
  address `PUT` / `DELETE`. Without this the refactor would silently drop `id`
  from every response.
- Switch `TrainingProgramController` to bind and return `TrainingProgramDto`,
  mapping through `infrastructure.web.mapper.TrainingProgramMapper`.

This pulls one slice of WP7 forward. It is the smallest change that keeps `dev`
green while letting the domain go pure. The JSON contract stays byte-identical
apart from the validation behaviour already approved in D1.

**Consequences for later WPs.** WP7 no longer introduces the DTO; it adds the
`@RestControllerAdvice` (D1) and swaps the controller's dependency from
`TrainingProgramService` to the use-case interfaces. Re-scope WP7 accordingly.

**Watch for.** Routing `PUT` through the path id instead of the request body may
incidentally fix exposed defect 2. Verify rather than assume, and record the
outcome.

## Exposed defects (documented, not fixed - see D2)

| # | Defect | Found by | Disposition |
|---|---|---|---|
| 1 | Domain validation never runs over HTTP: Jackson binds through the no-arg constructor and writes private fields, so a blank `code` or an inverted date range is accepted with `200` and persisted. | WP1 | **Fixed as a side effect** of WP3 + WP7 (VOs validate; D1 maps the failure to `400`). |
| 2 | `PUT /programs/{id}` with no `id` in the request body INSERTS a duplicate row and leaves the addressed program untouched. No unique constraint on `code` prevents it. | WP1 | Out of scope. Log only. Re-check after WP5 - the update use case may fix it incidentally. |
| 3 | `DELETE` on an unknown id returns `204`, so delete is unobservable and silently idempotent. | WP1 | Out of scope. Log only. |
| 4 | `spring.jpa.hibernate.ddl-auto=update` runs on top of the Flyway schema and widens `id` / `code` / `status` column types on every startup. | WP1 | Out of scope, but **flag before production**. Not a refactor concern. |
| 5 | The `training_program` table has four columns the entity does not map: `description`, `revision`, `valid_from`, `valid_to`. | WP1 | Out of scope. WP6 must not "helpfully" map them. |

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

_2026-08-30 - branch `refactor/wp2-boundaries`, branched from `dev` @ `669db04`._

**What changed.** Move-only. Every class was relocated from technical-layer
packages into architectural packages. Nothing else: no logic, annotation,
signature or class name changed, and no file outside `src/**` was touched.
`pom.xml`, the Flyway migrations and `application.properties` are untouched.

**Final package of every class** (later WPs must target these exactly):

| Class | Package before | Package now |
|---|---|---|
| `TrainingProgram` | `cl.keber.model` | `cl.keber.domain.model` |
| `TrainingProgramNotFoundException` | `cl.keber.exception` | `cl.keber.domain.exception` |
| `TrainingProgramService` | `cl.keber.service` | `cl.keber.application.service` |
| `TrainingProgramRepository` | `cl.keber.repository` | `cl.keber.infrastructure.persistence.repository` |
| `TrainingProgramController` | `cl.keber.controller` | `cl.keber.infrastructure.web.controller` |
| `TrainingProgramDto` | `cl.keber.dto` | `cl.keber.infrastructure.web.dto` |
| `TrainingProgramMapper` | `cl.keber.mapper` | `cl.keber.infrastructure.web.mapper` |
| `WebConfig` | `cl.keber.config` | `cl.keber.infrastructure.config` |
| `OtfSisacadApplication` | `cl.keber` | `cl.keber` (unmoved, on purpose) |

Test classes moved to the mirror package of the class they cover:
`TrainingProgramTest`, `TrainingProgramNotFoundExceptionTest`,
`TrainingProgramServiceTest`, `TrainingProgramRepositoryTest`,
`TrainingProgramControllerTest`, `TrainingProgramDtoTest`,
`TrainingProgramMapperTest`, `WebConfigTest`.
`DatabaseMigrationTest`, `OtfSisacadApplicationTests` and the
characterization test stayed where they were.

**Empty packages created for later waves.** Each carries a `.gitkeep` so git
tracks the directory: `domain.valueobject` (WP3), `domain.repository` (WP4),
`application.usecase` and `application.command` (WP5), and
`infrastructure.persistence.entity` / `.adapter` / `.mapper` (WP6).

**Verification.** `mvn clean verify` green on JDK 25.0.2 with no extra flags.
43 tests, 0 failures, 0 errors, 0 skipped. All 15 characterization tests pass
**byte-identical** - `git diff dev...HEAD -- src/test/java/cl/keber/characterization/`
is empty, because that test drives the REST API over HTTP and imports no
`cl.keber` type. Excluding renames, the whole branch diff is nothing but
`package` and `import` lines.

**Single commit, deliberately.** The WP suggests one commit per architectural
package, but CONVENTIONS also requires every commit to compile. Moving
`TrainingProgram` invalidates the imports of every class referencing it, so no
smaller commit compiles. The move is therefore one atomic commit.

**What later WPs must know.**

- Class names are unchanged. In particular `TrainingProgramRepository` is still
  called that and still extends `JpaRepository`, now in
  `infrastructure.persistence.repository`. WP4 adds the port in the empty
  `domain.repository`; the simple-name collision that creates is expected and is
  resolved when WP4/WP6 rename this one to `SpringDataTrainingProgramRepository`.
- Technological contamination was left in place on purpose: `TrainingProgram`
  still carries `@Entity`, `TrainingProgramService` still carries `@Service`.
  WP3 and WP6 purify.
- `TrainingProgramService.findById`, `TrainingProgramDto` and
  `TrainingProgramMapper` are still unused by the controller (D5, D6). They were
  moved, not deleted and not wired.
- Component scanning is unaffected: `OtfSisacadApplication` stays at the
  `cl.keber` root, so `@SpringBootApplication` still covers `domain`,
  `application` and `infrastructure`. Verified by the Spring-context tests.
- The legacy `model` / `repository` / `service` package directories are gone, so
  the WP8 rubric line about removing them is already satisfied.

**Not done here.** No renames, no port/adapter, no DTO wiring, no ArchUnit rules,
and none of the exposed defects touched. WP2 changes no behaviour.

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

## Environment notes (orchestrator)

- Local JDK is Microsoft OpenJDK 25.0.2, matching `pom.xml`. The CONVENTIONS
  JDK caveat (`-Dmaven.compiler.release=21`) does **not** apply; every wave
  verifies with a plain `mvn clean verify`.
- Maven 3.9.10.
- `REFACTOR-GUIDE.md` was committed to `dev` before Wave 0 (`00480a5`) so that
  worker worktrees carry the design authority.
