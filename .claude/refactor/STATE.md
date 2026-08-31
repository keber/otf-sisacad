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
| WP3 domain | MERGED | `refactor/wp3-domain` | – | – | Merged 2026-08-30; 4 VOs, pure entity, D7 controller DTO binding, 83/83 |
| WP4 repository port | MERGED | `refactor/wp4-repository-port` | – | – | Merged 2026-08-30; port + adapter, 89/89, WP5/WP6 window verified safe |
| WP5 use cases | MERGED | `refactor/wp5-use-cases` | – | – | Merged 2026-08-30; 5 use cases, D8 delegate, 110/110 |
| WP6 persistence | MERGED | `refactor/wp6-persistence` | – | – | Merged 2026-08-30; explicit @Column mapping, mapper test, 96/96 |
| WP7 web + wiring | MERGED | `refactor/wp7-web` | – | – | Merged 2026-08-31; use-case wiring, 400/404 advice, 108/108 |
| WP8 archunit + cleanup | MERGED | `refactor/wp8-archunit-cleanup` | – | – | Merged 2026-08-31; 9 ArchUnit rules, 117/117 |
| WP-DOCS architecture | MERGED | `refactor/wp-docs` | – | – | Merged 2026-08-31; 11 commits, 43/43 identifiers verified |

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

### D8 — WP5 keeps `TrainingProgramService` as a temporary delegate

Approved by Keber Flores on 2026-08-30.

**Problem.** WP5's commit plan removes the legacy generic
`TrainingProgramService`, but its "Files in scope" forbids touching
`infrastructure/**`. `TrainingProgramController` and
`TrainingProgramControllerTest` both depend on that service, so deleting it
breaks the web layer and WP5 is not allowed to repair it. Same shape as D7.

**Decision.** WP5 does NOT delete `TrainingProgramService`. It reduces it to a
thin delegate that forwards to the new `TrainingProgramApplicationService`,
marked `// temporary delegate: removed in WP7`. The controller is untouched and
WP5 stays inside `application/**`, which preserves the WP5 / WP6 parallel window.

**WP7 then owns**, in one reviewable change: swapping the controller onto the
use-case interfaces, deleting this delegate, adding the `@RestControllerAdvice`
(D1), and adding the `@Configuration` wiring.

Rejected alternative: letting WP5 update the controller. It would not have
collided with WP6, but it would gut WP7 and leave the riskiest wiring change
merged without a wave of its own.

### D9 — WP6 is a verify-and-finish package, not a rebuild

Recorded by the orchestrator on 2026-08-30.

WP4 already delivered `JpaTrainingProgramRepositoryAdapter`,
`SpringDataTrainingProgramRepository`, `TrainingProgramPersistenceMapper` and an
adapter `@DataJpaTest` - that is most of WP6 tasks 2 to 4. WP6's remaining real
work is finalising `TrainingProgramJpaEntity`, adding a plain-JUnit mapper
round-trip test, and migrating then deleting the legacy
`SpringDataTrainingProgramRepositoryTest`.

WP6 must verify what already exists against its brief and report anything
already satisfied, rather than rewriting working code to match the brief's
assumption that it starts from a bridge.

### D10 — Ratified class names for the wiring layer

Recorded by the orchestrator on 2026-08-31.

Two names came from `REFACTOR-GUIDE.md` and had been carried by the docs without
ever being ratified. WP7 is what makes them real, so they are settled here:

- `cl.keber.application.service.TrainingProgramApplicationService` — the
  framework-free implementation of all five use cases. **Already exists** as of
  WP5; the name is confirmed, not proposed.
- `cl.keber.infrastructure.config.TrainingProgramConfiguration` — the
  `@Configuration` class holding the bean wiring. WP7 creates it. It sits beside
  the existing `WebConfig`, which is left alone.

### D11 — Error-contract changes expected in WP7

Recorded by the orchestrator on 2026-08-31. Follows from D1.

Adding the `@RestControllerAdvice` changes more characterization assertions than
the two WP3 already touched. Every one below is an approved consequence of D1,
must carry a `// behaviour change: approved 2026-08-31` note, and must be
justified individually - a blanket edit is not acceptable.

Expected to move from `500` to `400` (`IllegalArgumentException` from a value
object or the id-mismatch guard):

- `POST /programs` with a blank `code`
- `POST /programs` with `endDate` before `startDate`
- `POST /programs` with a null `code`
- `POST /programs` with an empty body `{}`
- `PUT /programs/{id}` where the body id contradicts the path id

Expected to move from `500` to `404` (`TrainingProgramNotFoundException`):

- `PUT /programs/{id}` on an unknown id

Must NOT change:

- every success path (`POST` 200, `GET` 200, `PUT` 200, `DELETE` 204) and the
  exact response JSON field set and order
- `POST` with malformed JSON stays `400` (Jackson parse failure, never reaches
  the advice)
- `DELETE` on an unknown id stays `204` (exposed defect 3, not being fixed)
- `GET /programs/{id}` stays `405` (D5 - the use case exists but is not routed)

If an assertion moves that is not on this list, WP7 must stop and report rather
than edit it.

### D12 — WP-DOCS merges separately, after WP8

Recorded by the orchestrator on 2026-08-31.

WP8 offers to fold the documentation PR into itself. It is kept **separate**:
the branches are disjoint (`pom.xml` + `src/test/architecture/**` versus
`docs/**` + `README.md`), and a 10-commit documentation history is easier to
review on its own than buried inside the ArchUnit change.

Sequencing: WP8 merges first, then WP-DOCS receives a final ping carrying the
**actual** ArchUnit rule names as written, reconciles `package-dependencies.md`
against them, and merges last. The docs reference those rules, so they cannot be
finalised until the rules exist.

### D13 — WP8 deletes nothing; task 3 is a confirmation

Recorded by the orchestrator on 2026-08-31.

WP8 task 3 says to remove legacy `cl.keber.model` / `repository` / `service`
packages. Verified on `dev` at `e5a4ab1`: **they are already gone**, removed by
WP2 in Wave 1, and the WP3-WP7 bridge classes were removed by their own WPs.
`grep -rn "bridge" src --include=*.java` returns nothing.

WP8 must therefore treat task 3 as a grep-and-confirm step and delete nothing.
If it believes it has found genuinely dead code, it must report rather than
delete: at this point every remaining class is either live or deliberately
retained (for example `GetTrainingProgramUseCase`, built but unrouted per D5,
and `TrainingProgramMapper`, now live again after D7).

### D14 — ArchUnit pinned at 1.4.1, not the 1.3.0 in the WP8 brief

Recorded by the orchestrator on 2026-08-31.

WP8's brief pins `archunit-junit5:1.3.0`. That version bundles an ASM that cannot
read Java 25 bytecode: it reports `Unsupported class file major version 69` for
every class, imports nothing, and every rule then fails as "failed to check any
classes". 1.4.1 is the nearest working 1.x. Only the dependency block changed;
`java.version` and `maven.compiler.release` were not touched.

**Orchestrator verification that the rules are not vacuous.** A version that
imports zero classes is the dangerous failure mode here, because a rule suite
that scans nothing can look green forever. I tested this adversarially rather
than trusting the report:

1. Adding an unused `import org.springframework.stereotype.Component` to
   `TrainingProgram` did **not** fail the build - correctly, since an unused
   import leaves no bytecode reference and ArchUnit analyses bytecode.
2. Adding an actual `@Component` annotation to the class **did** fail
   `domainMustNotDependOnSpring` with an explicit Architecture Violation, and
   the build went red.

The planted violation was reverted and the branch re-verified green before
merging. The rules genuinely constrain the code.

Analysis scope is `ImportOption.DoNotIncludeTests` - production classes only.
Test classes cross layers by design (`@WebMvcTest` mocks use cases, `@DataJpaTest`
drives the adapter), so including them would force exceptions into the rules.

## Exposed defects (documented, not fixed - see D2)

| # | Defect | Found by | Disposition |
|---|---|---|---|
| 1 | Domain validation never runs over HTTP: Jackson binds through the no-arg constructor and writes private fields, so a blank `code` or an inverted date range is accepted with `200` and persisted. | WP1 | **Fixed as a side effect** of WP3 + WP7 (VOs validate; D1 maps the failure to `400`). |
| 2 | `PUT /programs/{id}` with no `id` in the request body INSERTS a duplicate row and leaves the addressed program untouched. No unique constraint on `code` prevents it. | WP1 | Out of scope. **Verified still present after WP3** - D7's DTO binding did NOT fix it incidentally: a body without an id maps to `create(...)`, so the service still saves a null-id row. Verified still present after WP3, WP4, WP5 and WP7 - the test passes unchanged at every stage. The refactor does NOT fix it: path id and body id stay distinct arguments and the path id is never copied into the command. Remains open for separate follow-up. |
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

_2026-08-30 - branch `refactor/wp3-domain`, branched from `dev` @ `f6c9bce`._

**Commits.** `f23bb03` feat: add TrainingProgram value objects · `2047086`
refactor: make TrainingProgram a pure domain entity with lifecycle methods.
Two commits, not the four the WP planned: once `TrainingProgram` loses its JPA
annotations, the bridge, the controller binding and the test rewrites all have
to land together or nothing compiles. Same constraint WP2 hit.

**Verification.** `mvn clean verify` BUILD SUCCESS on JDK 25.0.2, no extra
flags. 83 tests, 0 failures, 0 errors, 0 skipped (43 before). The two
Railway-backed tests ran and passed. `grep -R
"jakarta.persistence\|org.springframework\|com.fasterxml"
src/main/java/cl/keber/domain` returns nothing; SLF4J is gone from the domain
too, so `cl.keber.domain` has no third-party dependency at all.

**Value object API** (`cl.keber.domain.valueobject`, all Java `record`s,
immutable, `IllegalArgumentException` on violation):

| Value object | Constructor | Accessor | Rules |
|---|---|---|---|
| `TrainingProgramCode` | `new TrainingProgramCode(String value)` | `String value()` | non-null, non-blank, trimmed. Message: `code must not be null or blank` |
| `TrainingProgramName` | `new TrainingProgramName(String value)` | `String value()` | non-null, non-blank, trimmed. Message: `name must not be null or blank` |
| `TrainingProgramStatus` | `new TrainingProgramStatus(String value)` | `String value()` | non-null, non-blank, trimmed. Message: `status must not be null or blank` |
| `TrainingPeriod` | `new TrainingPeriod(LocalDate startDate, LocalDate endDate)` | `LocalDate startDate()`, `LocalDate endDate()` | both non-null (`startDate must not be null`, `endDate must not be null`); `endDate` strictly after `startDate`, equal dates invalid (`endDate must be after startDate`) |

Records give `equals`/`hashCode`/`toString` by value.

**`TrainingProgram` API** (`cl.keber.domain.model`, `final class`, private
all-args constructor):

```java
static TrainingProgram create(TrainingProgramCode code, TrainingProgramName name,
                              TrainingPeriod period, TrainingProgramStatus status);   // id == null
static TrainingProgram restore(Long id, TrainingProgramCode code, TrainingProgramName name,
                               TrainingPeriod period, TrainingProgramStatus status);

void rename(TrainingProgramName newName);
void reschedule(TrainingPeriod newPeriod);
void changeStatus(TrainingProgramStatus newStatus);

Long getId();
TrainingProgramCode getCode();
TrainingProgramName getName();
TrainingPeriod getPeriod();
TrainingProgramStatus getStatus();
```

Note for WP5/WP6: there is **no** `getStartDate()` / `getEndDate()`; the dates
live behind `getPeriod().startDate()` / `.endDate()`. `id` and `code` are final;
`name`, `period` and `status` are mutable through the three behaviour methods
only, so the entity is mutable in place rather than copy-on-write. Equality is
by non-null `id`; an unsaved program (`id == null`) is equal only to itself, and
its `hashCode` is its identity hash. Do not put unsaved programs in a hash set
expecting value semantics.

**The temporary bridge - exactly what WP6 removes.** Four pieces, each carrying
a `// bridge: replaced by the adapter in WP6` comment:

1. `infrastructure/persistence/entity/TrainingProgramJpaEntity.java` (new).
   Anemic `@Entity @Table(name = "training_program")` with
   `id, code, name, startDate, endDate, status`, getters/setters, no-arg
   constructor. Same mapping the domain entity used to carry, so the stored
   shape is unchanged. The four orphan columns stay unmapped. **WP6 keeps and
   owns this file** - it is the only bridge piece that survives.
2. `infrastructure/persistence/repository/TrainingProgramRepository.java` now
   extends `JpaRepository<TrainingProgramJpaEntity, Long>` instead of
   `JpaRepository<TrainingProgram, Long>`. WP6 renames it to
   `SpringDataTrainingProgramRepository` behind the adapter.
3. `infrastructure/persistence/mapper/TrainingProgramPersistenceMapper.java`
   (new). Static `toDomain(TrainingProgramJpaEntity)` /
   `toJpaEntity(TrainingProgram)`, both null-safe. WP6 moves these two methods
   into `JpaTrainingProgramRepositoryAdapter`.
4. `application/service/TrainingProgramService.java` calls that mapper on both
   sides of every repository call. Its five public signatures are **unchanged**
   (domain in, domain out). WP6 deletes every
   `TrainingProgramPersistenceMapper` call here and swaps the field for the
   domain port; nothing else in the class needs to move.

The bridge is two new files plus two small edits, well inside the "~1 file of
glue" budget, so there was no need to pull WP6 forward.

**D7, done.** `TrainingProgramDto` gained `Long id` as its **first declared
field** plus getter/setter, and the all-args constructor is now six-arg:
`(Long id, String code, String name, LocalDate startDate, LocalDate endDate,
String status)`. The declaration order **is** the Jackson response order - do
not reorder it. `TrainingProgramController` binds and returns
`TrainingProgramDto` on every endpoint. `infrastructure.web.mapper
.TrainingProgramMapper.toEntity` was **renamed to `toDomain`** and now returns a
domain entity built via `restore(...)` when the DTO carries an id and
`create(...)` when it does not; `toDto` reads the value-object accessors. That
id-from-the-body rule is what preserves today's PUT semantics.

**Characterization tests: two assertions changed, line by line.** Both marked
`// behaviour change: approved 2026-08-30`. The other 13 tests are untouched.

| Test | Was | Now | Why |
|---|---|---|---|
| `createWithBlankCodeIsAccepted` → renamed `createWithBlankCodeIsRejected` | `assertEquals(200, ...)`; `assertEquals("", created.get("code").asText())`; `assertNotNull(findById(...), "the invalid program is persisted and listed")` | `assertEquals(500, ...)`; `assertErrorBody(response, 500, "Internal Server Error")`; `assertNull(findByName(list(), "Blank Code"), "the invalid program is not persisted")` | Direct consequence of the domain validating: `TrainingProgramCode` rejects the blank value that Jackson used to write straight into a private field. Exposed defect 1 closed. |
| `createWithEndDateBeforeStartDateIsAccepted` → renamed `createWithEndDateBeforeStartDateIsRejected` | `assertEquals(200, ...)`; `assertEquals("2025-07-15", startDate)`; `assertEquals("2025-07-01", endDate)`; `assertNotNull(findById(...), "the invalid program is persisted and listed")` | `assertEquals(500, ...)`; `assertErrorBody(response, 500, "Internal Server Error")`; `assertNull(findByCode(list(), "CH-INVERTED"), "the invalid program is not persisted")` | Same cause: the strict range rule now lives in `TrainingPeriod`, which the request body must pass through. Exposed defect 1 closed. |

One helper was **added** (not changed): `findByName(JsonNode array, String
name)`, needed because a blank-code program cannot be located by code. No test
was weakened, deleted or skipped.

Both now answer **500, not 400**, because `IllegalArgumentException` is still
unhandled - the `@RestControllerAdvice` is WP7 (D1). WP7 flips these two to 400
and updates the `assertErrorBody` calls with them.

**PUT without an id in the body: unchanged, verified not assumed.** D7 flagged
that routing PUT through the path id might incidentally fix exposed defect 2. It
does not, because the controller still passes the body's id through:
`TrainingProgramMapper.toDomain` maps a body without an id to
`TrainingProgram.create(...)`, so `getId()` is null, and
`TrainingProgramService.update` saves a row with a null id exactly as before.
Verified by `updateWithoutIdInBodyInsertsAnotherProgram` passing **unchanged**:
still 200, still a newly generated id in the response, the addressed program
still untouched, still two rows with the same code. Defect 2 stands,
deliberately - WP5's update use case is where it gets fixed. Defects 3, 4 and 5
untouched.

**Other tests updated (forced by the in-scope production changes, not by
choice).**

- `TrainingProgramTest` rewritten as pure JUnit: the four JPA-annotation
  reflection assertions deleted, coverage added for `create`/`restore`,
  `rename`, `reschedule` (including that an invalid reschedule cannot be
  expressed), `changeStatus` and id-based equality. 3 tests → 13.
- `TrainingProgramServiceTest`: the repository mock now returns
  `TrainingProgramJpaEntity`, and the saved row is inspected with an
  `ArgumentCaptor`. Same 5 tests, same behaviour asserted.
- `TrainingProgramRepositoryTest`: saves a `TrainingProgramJpaEntity`.
- `TrainingProgramControllerTest`: posts and expects DTO JSON, and now also
  asserts `id` is on the wire. Still 4 tests.
- `TrainingProgramDtoTest` (1 → 2) and `TrainingProgramMapperTest` (2 → 5) cover
  the new `id` field, `toDomain`, and rejection of an invalid body.
- New: `TrainingProgramCodeTest` (8), `TrainingProgramNameTest` (8),
  `TrainingProgramStatusTest` (9), `TrainingPeriodTest` (6) - pure JUnit, no
  Spring anywhere under `src/test/java/cl/keber/domain`.

**Scope note for the orchestrator.** Beyond the WP file's list and D7, three
groups of test files outside `src/test/java/cl/keber/domain/**` had to change:
`application/service/TrainingProgramServiceTest`,
`infrastructure/persistence/repository/TrainingProgramRepositoryTest` and the
web tests. Each is an unavoidable consequence of a change the WP explicitly
authorises (the repository now stores the JPA entity; the controller now binds
the DTO). No production file outside scope was touched, and `pom.xml`, the
Flyway migrations and `application.properties` are untouched.

**What WP7 no longer has to do.** The DTO and its wiring exist. WP7 is now: add
the `@RestControllerAdvice` (D1), flip the two 500 assertions above to 400, and
swap the controller's `TrainingProgramService` dependency for the use-case
interfaces.

### WP4

_2026-08-30 - branch `refactor/wp4-repository-port`, branched from `dev` @ `ff7e67e`._

**Commits.**

| SHA | Subject |
|---|---|
| `3912e4b` | `feat: add TrainingProgramRepository port in the domain` |
| `420a0a2` | `refactor: implement the port with a JPA persistence adapter` |
| `374c195` | `refactor: depend application on the repository port` |
| `541c33a` | `test: cover the JPA repository adapter with DataJpaTest` |

**The port.** `cl.keber.domain.repository.TrainingProgramRepository`, exactly five
methods, domain types only, no annotations:

```java
TrainingProgram save(TrainingProgram program);
Optional<TrainingProgram> findById(Long id);
List<TrainingProgram> findAll();
boolean existsById(Long id);
void deleteById(Long id);
```

`existsById` has no caller yet; it is part of the contract the WP file specifies and
is covered by the adapter test.

**The adapter.** `cl.keber.infrastructure.persistence.adapter.JpaTrainingProgramRepositoryAdapter`,
annotated `@Repository`, constructor-injected with `SpringDataTrainingProgramRepository`.
It is the single implementation of the port and the single caller of
`TrainingProgramPersistenceMapper`.

**Renames.** `infrastructure.persistence.repository.TrainingProgramRepository` ->
`SpringDataTrainingProgramRepository` (via `git mv`, history follows), and its test
to `SpringDataTrainingProgramRepositoryTest`. The domain port now owns the plain
`TrainingProgramRepository` name, so an unqualified reference in later WPs means the
port.

**Bridge markers.** All three WP3 `// bridge: replaced by the adapter in WP6` markers
are gone (service body, persistence mapper Javadoc, Spring Data interface), along with
the one in `TrainingProgramServiceTest`'s Javadoc. `grep -rn "bridge" src --include=*.java`
returns nothing. WP6 inherits no bridge to dismantle: it inherits a finished adapter.

**Application layer.** `TrainingProgramService` takes the domain port and its body is
domain-in / domain-out with no mapping at all; the five methods are otherwise
unchanged. `@Service` stays (WP7 removes it). `TrainingProgramServiceTest` mocks the
port and asserts over domain entities; the JPA-row assertions it lost are covered by
the new adapter test.

**Behaviour.** No observable change. The 15 characterization tests are byte-identical
(`git diff ff7e67e -- src/test/java/cl/keber/characterization/` is empty) and all 15
pass. D2 was not invoked. Exposed defect 2 is confirmed **still present and untouched**:
`update` still calls `repository.save(updated)` with whatever id the program carries, so
a body without an id still saves a null-id program and inserts a duplicate row. WP5 owns
it. Defects 3-5 likewise untouched.

**Verification.** `mvn clean verify` -> BUILD SUCCESS, 89 tests, 0 failures / 0 errors /
0 skipped (83 before WP4, +6 from the new adapter test). JDK 25.0.2, no extra flags.
`grep -R "org.springframework.data\|JpaRepository\|jakarta.persistence"` over
`src/main/java/cl/keber/application` and `src/main/java/cl/keber/domain` -> no hits.

**Separation for the WP5 / WP6 parallel window.** `application/**` and
`infrastructure/persistence/**` share **no file**. Their only relationship is that both
compile against `domain/**`: `application` imports `cl.keber.domain.*` and nothing else;
`infrastructure/persistence` imports `cl.keber.domain.*` and never `cl.keber.application.*`.
WP5 and WP6 can run simultaneously without collision provided neither edits
`domain/repository/TrainingProgramRepository.java` — a change to the port signature is
the one file that would force them to coordinate.

**New adapter test.** `src/test/java/cl/keber/infrastructure/persistence/JpaTrainingProgramRepositoryAdapterTest.java`,
`@DataJpaTest` + `@Import(JpaTrainingProgramRepositoryAdapter.class)` on H2 with the
`OTFSISACAD` Flyway properties copied from the existing repository test. Six tests:
save, findById present/empty, findAll, existsById, deleteById, and save-as-update.

### WP5

_2026-08-30 - branch `refactor/wp5-use-cases`, branched from `dev` @ `57404a6`._

**What changed.** Five use case interfaces, two command records, one query record and
one framework-free implementation, all under `application/**`. Nothing outside
`src/main/java/cl/keber/application/**` and `src/test/java/cl/keber/application/**`
was touched, so the WP5 / WP6 parallel window held.

**Signatures WP7 codes against.**

| Type | Signature |
|---|---|
| `application.usecase.CreateTrainingProgramUseCase` | `TrainingProgram execute(CreateTrainingProgramCommand command)` |
| `application.usecase.GetTrainingProgramUseCase` | `Optional<TrainingProgram> execute(GetTrainingProgramQuery query)` |
| `application.usecase.ListTrainingProgramsUseCase` | `List<TrainingProgram> execute()` |
| `application.usecase.UpdateTrainingProgramUseCase` | `TrainingProgram execute(Long id, UpdateTrainingProgramCommand command)` |
| `application.usecase.DeleteTrainingProgramUseCase` | `void execute(Long id)` |
| `application.command.CreateTrainingProgramCommand` | `(String code, String name, LocalDate startDate, LocalDate endDate, String status)` |
| `application.command.UpdateTrainingProgramCommand` | `(Long id, String code, String name, LocalDate startDate, LocalDate endDate, String status)` |
| `application.query.GetTrainingProgramQuery` | `(Long id)` |

Two deviations from the WP text, both forced rather than preferred:

- **Get takes a query record, not a `Long`.** Delete already takes `execute(Long)`.
  One class implements both, and two `execute(Long)` methods differing only in
  return type do not compile. The WP allowed the query record as a style option;
  here it is the only option.
- **Update takes two arguments.** The addressed id (from the path) and the id the
  caller put in the payload are different things today: an absent payload id
  inserts a duplicate (exposed defect 2) while a contradicting one is rejected. A
  single id field cannot express both, and collapsing them would have silently
  changed observable behaviour. `UpdateTrainingProgramCommand.id()` is the
  **payload** id and is nullable.

**Implementation.** `application.service.TrainingProgramApplicationService` implements
all five, takes `cl.keber.domain.repository.TrainingProgramRepository` in its
constructor, and has **zero framework imports and zero annotations**. Update keeps the
legacy order exactly: existence check first (`TrainingProgramNotFoundException`), then
the mismatch guard with the unchanged message `program ID does not match the provided
ID`, then `create`/`restore` + `save`. Delete is a bare `deleteById`.

**`TrainingProgramService` is now a thin delegate** (decision D8), marked
`// temporary delegate: removed in WP7`. Its five public methods keep their exact
signatures (`save`, `findAll`, `findById`, `deleteById`, `update`), so
`TrainingProgramController` and `TrainingProgramControllerTest` compile untouched. Its
constructor still takes the repository port; it builds the application service itself
with `new TrainingProgramApplicationService(repository)`.

**Spring annotations remaining in `application/**` - what WP7 strips.** Exactly one:
`@Service` on `TrainingProgramService`, plus its `org.springframework.stereotype.Service`
import. That is the whole of `grep -R "org.springframework" src/main/java/cl/keber/application`.
Keeping the wiring on the doomed delegate rather than on the new service is deliberate:
the application service stays annotation-free today, so WP7 only has to delete the
delegate and declare a `@Configuration` `@Bean`, never to strip anything from code that
survives.

**Exposed defect 2 - verified empirically, not assumed.** Ran
`TrainingProgramApiCharacterizationTest#updateWithoutIdInBodyInsertsAnotherProgram` on
its own after the rewrite: **passes, unchanged**. `PUT /programs/{id}` with no id in the
body still answers 200 with a newly generated id, still leaves the addressed program
untouched, and still leaves two rows with that code. The defect is unchanged by WP5.
Update the disposition to "still present after WP5"; it is now WP7's to re-check when
the controller moves onto the use case interfaces.

**One knowingly accepted divergence, on a path no test covers.** `POST /programs` with
an `id` in the body used to reach `repository.save` carrying that id (a merge); the
create use case now always inserts. No characterization test exercises it, and WP7's
controller would behave the same way once it calls the create use case directly.

**Tests.** 13 new plain-JUnit tests in `cl.keber.application.usecase` over a mocked
domain port, no Spring context: `CreateTrainingProgramUseCaseTest` (3),
`UpdateTrainingProgramUseCaseTest` (5), `ReadTrainingProgramUseCasesTest` (3),
`DeleteTrainingProgramUseCaseTest` (2), plus a shared `UseCaseFixtures`.

`TrainingProgramServiceTest` was **kept**, against the WP's commit plan, because D8 kept
the class it covers: the delegate is still production code on the live request path and
its entity-to-command translation is not covered by the use case tests. Its five
existing tests are unchanged - that is the evidence the delegate's behaviour did not
move - and one test was added for the id-less update path. **WP7 deletes the delegate
and this test together.**

**Verification.** `mvn clean verify` (plain, JDK 25.0.2) - BUILD SUCCESS,
**103 tests, 0 failures, 0 errors, 0 skipped** (89 before WP5). All 15 characterization
tests pass and `git diff dev..HEAD -- src/test/java/cl/keber/characterization/` is
**empty**.

### WP6

_2026-08-30 - branch `refactor/wp6-persistence`, branched from `dev` @ `57404a6`._

**Shape of the package.** Per D9 this was verify-and-finish, not a rebuild. WP4
had already delivered four of the five tasks; only the entity, the mapper unit
test and the legacy test deletion were outstanding. Nothing working was
rewritten.

| WP6 task | Outcome |
|---|---|
| 1 `TrainingProgramJpaEntity` | **Changed.** Already `@Entity @Table(name = "training_program")` with `@Id @GeneratedValue(IDENTITY)`, a public no-arg constructor and plain getters/setters. Added explicit `@Column(name = ...)` on all six mapped fields; the mapping had been relying on the implicit naming strategy. |
| 2 `SpringDataTrainingProgramRepository` | **Already satisfied by WP4.** Extends `JpaRepository<TrainingProgramJpaEntity, Long>`, no derived queries. Unchanged. |
| 3 `TrainingProgramPersistenceMapper` | **Already satisfied by WP4.** Static, final, private constructor, null-safe both ways, restores through `TrainingProgram.restore(...)`. Unchanged. |
| 4 `JpaTrainingProgramRepositoryAdapter` | **Already satisfied by WP4.** `@Repository`, implements the domain port, constructor-injected, maps at every boundary. Unchanged. |
| 5 Delete bridge glue | **Already satisfied by WP4.** `grep -rn "bridge" src --include=*.java` returns nothing. Nothing to remove. |

**Final column mapping of `TrainingProgramJpaEntity`** (table `training_program`,
schema at `V5`):

| Field | Column | Notes |
|---|---|---|
| `Long id` | `id` | `@Id @GeneratedValue(strategy = IDENTITY)` |
| `String code` | `code` | |
| `String name` | `name` | |
| `LocalDate startDate` | `start_date` | added by `V5` |
| `LocalDate endDate` | `end_date` | added by `V5` |
| `String status` | `status` | |

`description`, `revision`, `valid_from` and `valid_to` remain **unmapped** per
exposed defect 5. `TrainingProgramPersistenceMapperTest` now pins this with a
reflection check on the declared fields, so a future WP cannot map them by
accident.

No `length` or `nullable` attributes were declared. Adding them would change what
`ddl-auto=update` does to the shared schema, which is exposed defect 4 and out of
scope here.

**Tests.** `src/test/java/cl/keber/infrastructure/persistence/`:

- `JpaTrainingProgramRepositoryAdapterTest` - `@DataJpaTest` on H2 with the
  `OTFSISACAD` Flyway properties, never the shared database. Now 7 tests: the
  six from WP4 plus `shouldPersistEveryMappedColumn`.
- `mapper/TrainingProgramPersistenceMapperTest` - **new**, 7 tests, plain JUnit
  with no Spring context: both directions field by field, both round trips,
  null-safety, unsaved-program null id, and the unmapped-column guard.
- `repository/SpringDataTrainingProgramRepositoryTest` - **deleted**. Its
  save-and-retrieve assertions were already covered by the adapter test at the
  domain level; what was unique - exercising the JPA entity directly against the
  Flyway schema - moved into `shouldPersistEveryMappedColumn`. Noted in the
  commit body.

Persistence is now the single place that verifies JPA mapping, replacing the
annotation reflection checks removed from the domain test in WP3.

**Verification.** `mvn clean verify` BUILD SUCCESS, 96 tests, 0 failures / 0
errors / 0 skipped (89 before WP6: +7 mapper tests, +1 migrated adapter test, -1
deleted legacy test). All 15 characterization tests pass **unchanged** -
`git diff` for `src/test/java/cl/keber/characterization/` is empty. `DatabaseMigrationTest`
4/4 green and unaffected.

**No Flyway file was added or changed.** The schema stays at `V5`.

**What WP7 and WP8 must know.**

- The only path to the database is port -> `JpaTrainingProgramRepositoryAdapter`
  -> `SpringDataTrainingProgramRepository` -> `TrainingProgramJpaEntity`. The
  bridge is fully gone.
- `TrainingProgramJpaEntity` never leaves `infrastructure.persistence`. WP8's
  ArchUnit rules can assert that, and that `@Entity` / `@Repository` /
  `JpaRepository` appear only under `infrastructure.persistence`.
- The adapter is a `@Repository`, so component scanning already wires it; WP7's
  `@Configuration` needs no bean definition for it.
- Files touched: only `src/main/java/cl/keber/infrastructure/persistence/**`,
  `src/test/java/cl/keber/infrastructure/persistence/**` and this note. The
  WP5 parallel window was respected: nothing under `application/**`,
  `domain/**`, `infrastructure/web/**` or `db/migration/**` was touched.

### WP7

Branch `refactor/wp7-web`, branched from `dev` @ `400df17`. One commit: `8746a9f`
"refactor: wire the web layer onto the use cases (WP7)". Wave 5, 2026-08-31.

**What changed.**

- `TrainingProgramController` now takes the four use case interfaces it actually
  calls by constructor: `CreateTrainingProgramUseCase`,
  `ListTrainingProgramsUseCase`, `UpdateTrainingProgramUseCase`,
  `DeleteTrainingProgramUseCase`. No service class, no repository, no JPA type.
  Each endpoint builds its command from the bound DTO; responses still map
  through `TrainingProgramMapper`. D7's DTO binding and field order untouched.
- `GetTrainingProgramUseCase` is deliberately not injected and
  `GET /programs/{id}` is still unrouted (D5). It stays `405`.
- `cl.keber.application.service.TrainingProgramService` (the D8 delegate) and
  `TrainingProgramServiceTest` deleted together. That was the last Spring import
  in `application/**`.
- New `cl.keber.infrastructure.web.RestExceptionHandler`, a
  `@RestControllerAdvice`: `IllegalArgumentException` -> `400`,
  `TrainingProgramNotFoundException` -> `404` (D1). Its body keeps the shape of
  Spring Boot's default error body, so `assertErrorBody` still applies. It does
  not handle deserialization failures, so malformed JSON keeps Jackson's `400`.
- New `cl.keber.infrastructure.config.TrainingProgramConfiguration` (D10). One
  `@Bean` returns `new TrainingProgramApplicationService(repository)` with the
  concrete class as the declared return type, so the single bean satisfies all
  five use case interfaces by type. `WebConfig` untouched.
- `TrainingProgramControllerTest` is a `@WebMvcTest` mocking the four use cases;
  existing JSON assertions kept, four tests added (command construction, path id
  vs body id, and the two advice mappings).

**Error contract (D11).** Exactly the six listed assertions moved, and no
others - confirmed empirically: the first build after the controller swap failed
on precisely those six. Five `500` -> `400` (blank code, inverted dates, null
code, empty body, PUT id mismatch) and one `500` -> `404` (PUT on an unknown
id). Each carries a `// behaviour change: approved 2026-08-31` note and an
individual justification in the commit body. Every success path, its JSON field
set and order, malformed JSON `400`, DELETE-unknown `204` and
`GET /programs/{id}` `405` are unchanged.

**Exposed defect 2 still open.**
`updateWithoutIdInBodyInsertsAnotherProgram` was re-run on its own after the
rewrite and passes unchanged: a PUT with no body id still inserts a duplicate.
The rewrite did not incidentally fix it, because the body id is still what the
command carries and the path id is never copied into it.

**Verification.** `grep -R "org.springframework" src/main/java/cl/keber/application
src/main/java/cl/keber/domain` returns nothing. `mvn clean verify` is BUILD
SUCCESS with 108 tests, 0 failures, 0 errors, 0 skipped (up from 110 in WP5
because the 174-line `TrainingProgramServiceTest` was deleted and 4 controller
tests were added).

**For WP8.** The layering is now clean enough for ArchUnit to assert it:
`application/**` and `domain/**` have no `org.springframework` import, and
`infrastructure.web.controller` references only `application.usecase`,
`application.command` and its own DTO/mapper. `RestExceptionHandler` is the only
place mapping domain exceptions to status codes.

### WP8

Branch `refactor/wp8-archunit-cleanup`, branched from `dev` @ `c5be84d`. Wave 6,
2026-08-31. Two commits: `001217a` "test: add ArchUnit rules enforcing the layer
boundaries" and this note.

**What changed.** Two paths only: `pom.xml` (the ArchUnit dependency, nothing
else) and the new `src/test/java/cl/keber/architecture/ArchitectureTest.java`.
No production file and no existing test was modified - `git diff` against `dev`
for `src/main/**` and for `src/test/java/cl/keber/characterization/` is empty.

**ArchUnit version.** `com.tngtech.archunit:archunit-junit5:1.4.1`, test scope,
via a new `${archunit.version}` property. **Not** the 1.3.0 the WP file pinned:
1.3.0 bundles an ASM that cannot read this project's Java 25 bytecode and logs
`java.lang.IllegalArgumentException: Unsupported class file major version 69`
for every class, importing nothing. That surfaces as nine failures rather than a
false pass, because ArchUnit's default `archRule.failOnEmptyShould` treats a
rule that matched no class as a failure and the layered rule reports an empty
layer as a violation. 1.4.1 is the nearest working 1.x. `java.version`, the
compiler release and every other dependency are untouched.

**The nine rules, all passing unweakened.** No `freeze`, no allowance list, no
`@ArchIgnore`, no ignored violation, no relaxed rule. `@AnalyzeClasses(packages
= "cl.keber", importOptions = ImportOption.DoNotIncludeTests.class)`.

| Rule | Constraint |
|---|---|
| `domainMustNotDependOnSpring` | no `..domain..` class depends on `org.springframework..` |
| `domainMustNotDependOnJpaOrHibernate` | no `..domain..` class depends on `jakarta.persistence..` / `jakarta.persistence.*` / `org.hibernate..` |
| `domainMustNotDependOnJackson` | no `..domain..` class depends on `com.fasterxml.jackson..` |
| `applicationMustNotDependOnInfrastructure` | no `..application..` class depends on `..infrastructure..` |
| `applicationMustNotDependOnSpring` | no `..application..` class depends on `org.springframework..` |
| `applicationMustNotDependOnJpaOrSpringData` | no `..application..` class depends on `jakarta.persistence..` / `jakarta.persistence.*` / `org.springframework.data..` |
| `controllersMustNotDependOnPersistenceOrTheRepositoryPort` | no `..web.controller..` class depends on `..persistence..` or `..domain.repository..` |
| `domainAndApplicationMustNotDependOnWeb` | no `..domain..` or `..application..` class depends on `..web..` |
| `layersAreRespected` | `layeredArchitecture().consideringAllDependencies()`: Infrastructure may not be accessed by any layer; Application only by Infrastructure; Domain only by Application and Infrastructure |

Only production classes are analysed. Test classes cross layers by design - the
`@WebMvcTest` mocks use cases, the `@DataJpaTest` drives the JPA adapter, and
the characterization suite boots the whole application - so importing them would
assert something other than the production dependency graph. This narrows what
is scanned, not what is required of production code.

`cl.keber.OtfSisacadApplication` carries `@SpringBootApplication` and sits at the
root, outside all three layers. It imports only `org.springframework.boot..` and
depends on no layer, so `layersAreRespected` passes without any exception for it.

**Task 3 (legacy removal): nothing deleted, per D13 - confirmed by grep.**

- `find src -type d \( -path "*cl/keber/model*" -o -path "*cl/keber/repository*"
  -o -path "*cl/keber/service*" \)` - no output. The legacy packages are gone
  (WP2, Wave 1).
- `grep -rn "cl\.keber\.\(model\|repository\|service\)\b" src --include=*.java` -
  one hit, and it is prose: a Javadoc line in `TrainingProgramJpaEntity` reading
  "The column mapping is exactly what `cl.keber.model`...". No code reference.
- `grep -rni "bridge" src --include=*.java` - no output. Every WP3-WP7 bridge
  class was removed by its own WP.
- `grep -rln "jakarta.persistence\|@Entity\|JpaRepository" src --include=*.java` -
  exactly two files, both under `infrastructure.persistence`:
  `entity/TrainingProgramJpaEntity.java` and
  `repository/SpringDataTrainingProgramRepository.java`.

No genuinely dead code was found to report. `GetTrainingProgramUseCase` is built
but unrouted by D5 and `TrainingProgramMapper` is live again after D7; both are
deliberately retained, not dead.

**Task 5 (docs): skipped by D12.** `docs/**` and `README.md` are WP-DOCS's, on
its own branch, merging after this one. Nothing under `docs/` was touched.

**Verification.** Plain `mvn clean verify`, local Microsoft OpenJDK 25.0.2:
BUILD SUCCESS, **117 tests, 0 failures, 0 errors, 0 skipped** (108 from WP7 plus
the 9 new architecture rules). The 15 characterization tests pass unchanged.

**For WP-DOCS.** The nine rule names above are final as written; reconcile
`package-dependencies.md` against that table. Two things the docs should say:
the analysed scope is production classes only, and the pinned ArchUnit version
is 1.4.1 for Java 25 bytecode support, not the 1.3.0 quoted in the WP file.


### WP-DOCS

**FINAL - reconciled through WP8. Ready to merge. 2026-08-31.**

Branch `refactor/wp-docs`, rebased onto `dev` @ `40d83d3` (post-WP8), no
conflicts. Not merged, not pushed - the orchestrator merges. Eleven commits,
listed by subject since rebasing shifts hashes
(`git log --oneline dev..refactor/wp-docs`):

1. `docs: add clean architecture and package dependency documentation`
2. `docs: document the TrainingProgram domain model and repository port`
3. `docs: mark JPA-entity task docs as superseded by Milestone 3`
4. `docs: link the architecture documentation from the README`
5. `docs: update class diagram for the domain / JPA entity split`
6. `docs: record the WP-DOCS first-draft handoff note`
7. `docs: reconcile architecture docs with the WP1 baseline`
8. `docs: reconcile architecture docs with WP2 and decisions D1-D7`
9. `docs: reconcile domain docs with the WP3 value objects and entity`
10. `docs: list WP-DOCS commits by subject rather than hash`
11. `docs: finalise architecture docs against the completed refactor`

**Deliverables.** `docs/architecture/` holds the four required documents:
`clean-architecture.md` (layering, dependency rule, packages-not-modules,
behaviour changes), `package-dependencies.md` (tree, class list, directions, the
nine ArchUnit rules), `domain-model.md` (entity, VOs, port, use cases),
`persistence.md` (domain vs JPA entity, mapper, adapter, schema untouched).
`README.md` carries the Milestone 3 note and links all four. `docs/106.md` -
`docs/110.md` carry the superseded banner with their content intact.
`docs/diag-class.md` keeps the conceptual diagram and adds the implementation
slice. `docs/diag-er.md` is unchanged - re-derived from `V1` + `V5`, matches
column for column.

Only `docs/**`, `README.md` and this section were ever touched. Nothing under
`src/`, `pom.xml` or the frontend.

**Final pass (WP8) - what changed.**

- **Nine ArchUnit rules cited by their real method names**, replacing the
  abstract numbered list. Added `ImportOption.DoNotIncludeTests` and why test
  classes are excluded (they cross layers by design).
- **ArchUnit 1.4.1, not 1.3.0** (D14). Documented *why* the version matters:
  earlier 1.x bundles an ASM that cannot read Java 25 bytecode, imports zero
  classes and fails every rule as "failed to check any classes". Recorded that
  a mass failure of all nine rules after a JDK bump means the analyser cannot
  read bytecode, not that the architecture broke, and noted the adversarial
  `@Component` verification.
- **Error contract documented as shipped, not planned.** All six moved
  assertions are now in a table with before/after codes, plus the Spring-default
  error body shape, plus the explicit list of what deliberately did not move.
- **`application.query` package added to the tree** - `GetTrainingProgramQuery`.
  I had predicted the query would live in `command`; it does not. Also added
  `infrastructure.web.RestExceptionHandler`, which sits at the `web` root rather
  than in a sub-package.
- **Corrected two use-case signatures I had wrong.** `UpdateTrainingProgramUseCase.execute`
  takes `(Long id, UpdateTrainingProgramCommand)`, not one argument;
  `GetTrainingProgramUseCase.execute` takes a `GetTrainingProgramQuery`, not a
  `Long`. Documented the reason for each rather than just the shape - the second
  exists because one class cannot declare two `execute(Long)` methods differing
  only in return type.
- **Controller injects four use cases, not five.** Corrected in the prose and in
  the diagram, which now draws the four interfaces and their realisations.
- **Bean wiring documented as built**: `TrainingProgramConfiguration`, one
  `@Bean` returning the concrete `TrainingProgramApplicationService`, which
  satisfies all five interface injection points by type. `WebConfig` separate.
- **Migration status table deleted** from `package-dependencies.md`, as flagged
  during the WP2 pass. Everything has landed, so it was noise.
- **README milestone note qualified.** The required sentence says the REST
  contract is unchanged; that is true of paths, JSON and success codes but not
  of error codes, which moved to 400/404. Kept the required wording and added
  the error-contract sentence beside it with a link, rather than leaving a
  statement the characterization diffs contradict.

**Defect 2 - no doc implied it was fixed.** `clean-architecture.md` has said
"still inserts a duplicate row" since the WP2 pass. Strengthened rather than
corrected: it now records that this was re-verified at every stage, that the
incidental-fix hypothesis was tested and disproved, and why (path id and body id
stay distinct arguments; the path id is never copied into the command).

**The `final id` trap I flagged after WP3 was avoided.**
`JpaTrainingProgramRepositoryAdapter.save` maps the *saved* entity back through
`toDomain`, so generated ids are returned rather than silently dropped. Verified
in source, not assumed.

**Verification.**

- **Identifier verification script: 43 documented identifiers checked against
  `src/main/java`, `src/test/java` and `pom.xml` - 0 mismatches.** Plus negative
  assertions: the entity exposes neither `getStartDate()` nor `getEndDate()`;
  `domain` + `application` contain no `org.springframework`,
  `jakarta.persistence`, `com.fasterxml.jackson` or `org.hibernate`; and the
  legacy `model` / `repository` / `service` packages and `TrainingProgramService`
  are gone. This pass it caught the two wrong use-case signatures and the missing
  `query` package.
- `mvn test -Dtest=ArchitectureTest`: **9 tests, 0 failures, 0 errors.** Run
  because the docs assert these rules pass; per D14 a vacuous import fails rather
  than passes, so a green run also confirms the rules actually scanned classes.
- All three mermaid blocks in `docs/diag-class.md` and `docs/diag-er.md` parse
  under mermaid 11.
- All relative markdown links resolve; in-page anchors match their heading slugs.
- `git diff` confirms `106`-`110` remain pure insertions and nothing outside
  `docs/**`, `README.md` and this section is touched.

No full `mvn clean verify` was run: this WP changes no code, and CONVENTIONS
discourages casually running the two tests that reach the shared Railway
database. The ArchUnit run above was scoped deliberately.

**Nothing is left inconsistent between the docs and `dev`.** Both names I had
carried unratified are now real in code (D10). Nothing remains to reconcile;
this branch is final and ready to merge.

## Rubric checklist (closed out in WP8)

Closed out on 2026-08-31 on `refactor/wp8-archunit-cleanup` @ `001217a`. Every
box below is evidenced from that tree; nothing is ticked on assertion alone.

- [x] **Layer separation 4/4** — `domain` / `application` / `infrastructure`
      packages exist; ArchUnit proves domain has no Spring/JPA, application has
      no JPA and no infrastructure import, controllers do not touch repositories.

  The three packages exist under `src/main/java/cl/keber/`, plus
  `OtfSisacadApplication` at the root, outside all three. Eight boundary rules
  and one whole-layering rule in
  `src/test/java/cl/keber/architecture/ArchitectureTest.java`, all green:
  `domainMustNotDependOnSpring`, `domainMustNotDependOnJpaOrHibernate`,
  `domainMustNotDependOnJackson`, `applicationMustNotDependOnInfrastructure`,
  `applicationMustNotDependOnSpring`,
  `applicationMustNotDependOnJpaOrSpringData`,
  `controllersMustNotDependOnPersistenceOrTheRepositoryPort`,
  `domainAndApplicationMustNotDependOnWeb`, `layersAreRespected`.
  No `freeze`, no allowance list, no `@ArchIgnore`. Independently confirmed by
  grep: `grep -rn "org\.springframework\|jakarta\.persistence\|JpaRepository\|new
  Jpa" src/main/java/cl/keber/application src/main/java/cl/keber/domain` returns
  no output at all.

- [x] **Tactical patterns 4/4** — immutable self-validating Value Objects
      (`TrainingProgramCode`, `TrainingProgramName`, `TrainingPeriod`,
      `TrainingProgramStatus`); entity `TrainingProgram` with lifecycle
      behaviour (`rename`, `reschedule`, …) and no blanket setters; invalid
      states unconstructable; pure-Java domain tests.

  All four VOs are `record`s in `cl.keber.domain.valueobject`, each validating
  in its compact constructor: `public record TrainingProgramCode(String value)`,
  `TrainingProgramName(String value)`, `TrainingProgramStatus(String value)`,
  `TrainingPeriod(LocalDate startDate, LocalDate endDate)`.
  `public final class TrainingProgram` exposes `create(...)` / `restore(...)`
  factories, the lifecycle methods `rename(TrainingProgramName)`,
  `reschedule(TrainingPeriod)`, `changeStatus(TrainingProgramStatus)`, and
  VO-typed getters — no setter of any kind. Invalid states are unconstructable:
  the constructor is private and every field is a validated VO.
  Domain tests are plain JUnit, no Spring context: `TrainingProgramTest` (13),
  `TrainingPeriodTest` (6), `TrainingProgramCodeTest` (8),
  `TrainingProgramNameTest` (8), `TrainingProgramStatusTest` (9),
  `TrainingProgramNotFoundExceptionTest` (1) — 45 tests.

- [x] **Repository + contracts 4/4** — `domain` port `TrainingProgramRepository`;
      `infrastructure` `JpaTrainingProgramRepositoryAdapter implements` it over
      `SpringDataTrainingProgramRepository`; use cases receive the port via
      constructor; no `new Jpa…` and no `JpaRepository` reference in application.

  `cl.keber.domain.repository.TrainingProgramRepository:16: public interface
  TrainingProgramRepository`.
  `cl.keber.infrastructure.persistence.adapter.JpaTrainingProgramRepositoryAdapter:22:
  public class JpaTrainingProgramRepositoryAdapter implements
  TrainingProgramRepository`, delegating to `SpringDataTrainingProgramRepository`.
  `TrainingProgramApplicationService:42: public TrainingProgramApplicationService(TrainingProgramRepository
  repository)` — the port by constructor, wired in
  `infrastructure.config.TrainingProgramConfiguration`.
  The `new Jpa` / `JpaRepository` grep over `application/` and `domain/` (above)
  is empty, and `applicationMustNotDependOnInfrastructure` plus
  `applicationMustNotDependOnJpaOrSpringData` enforce it from now on.

- [x] `mvn clean verify` green on `dev`.

  Plain `mvn clean verify`, Microsoft OpenJDK 25.0.2, no extra flags:
  `Tests run: 117, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`.
  117 = WP7's 108 plus the 9 new architecture rules. Measured on
  `refactor/wp8-archunit-cleanup` @ `001217a`, whose only delta from `dev` is
  the ArchUnit dependency and the new test.

- [x] REST contract under `/programs` unchanged **on every success path**; frontend needs no changes. Error codes changed deliberately - see the qualification below.

  All 15 WP1 characterization tests pass unchanged:
  `Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 -- in
  cl.keber.characterization.TrainingProgramApiCharacterizationTest`, and
  `git diff dev -- src/test/java/cl/keber/characterization/` is empty. WP8 adds
  no production code, so the contract cannot have moved in this WP. The
  deliberate error-code changes remain the six approved under D1/D11 and are
  recorded in the WP7 note.

  **Stated precisely, because "unchanged" alone would overstate it:** paths, JSON
  field set and order, and every success status code (`POST` 200, `GET` 200,
  `PUT` 200, `DELETE` 204) are byte-for-byte unchanged, so the frontend needs no
  changes. Error responses did move, by approval: invalid input `500` -> `400`
  and unknown program `500` -> `404`. A blank code or an inverted date range
  previously returned `200` and was silently persisted; both are now rejected.
  Any client that depended on those error codes - or on invalid data being
  accepted - is affected. `README.md` carries the same qualification.

- [x] Legacy `model` / `repository` / `service` packages removed.

  Already satisfied by **WP2 in Wave 1**; WP8 deleted nothing (D13) and
  confirmed by grep on this branch:
  `find src -type d \( -path "*cl/keber/model*" -o -path "*cl/keber/repository*"
  -o -path "*cl/keber/service*" \)` — no output.
  `grep -rn "cl\.keber\.\(model\|repository\|service\)\b" src --include=*.java` —
  a single hit, and it is prose: the Javadoc line in `TrainingProgramJpaEntity`
  reading "The column mapping is exactly what `cl.keber.model`…".
  `grep -rni "bridge" src --include=*.java` — no output.
  `grep -rln "jakarta.persistence\|@Entity\|JpaRepository" src --include=*.java` —
  exactly `infrastructure/persistence/entity/TrainingProgramJpaEntity.java` and
  `infrastructure/persistence/repository/SpringDataTrainingProgramRepository.java`,
  which is the WP8 definition-of-done condition met.

- [x] `docs/architecture/*` published and README points to it.

  Closed by the orchestrator on 2026-08-31 when `refactor/wp-docs` merged
  (11 commits). Published: `docs/architecture/clean-architecture.md`,
  `package-dependencies.md`, `domain-model.md`, `persistence.md`. `README.md`
  carries an Architecture section linking to all four plus the Milestone 3 note.
  Historical task docs `106`-`110` carry the "superseded" banner with their
  content intact; `docs/diag-class.md` shows the domain / JPA entity split;
  `docs/diag-er.md` is unchanged because the schema did not change.

  Verified by the WP-DOCS identifier script: **43 documented identifiers checked
  against `src/main/java`, `src/test/java` and `pom.xml`, 0 mismatches**, plus
  negative assertions (no `getStartDate()`/`getEndDate()`; no Spring, JPA,
  Jackson or Hibernate in `domain` or `application`; legacy packages and
  `TrainingProgramService` gone). The script caught three real documentation
  errors on this pass: two wrong use-case signatures and an undocumented
  `cl.keber.application.query` package.

## Environment notes (orchestrator)

- Local JDK is Microsoft OpenJDK 25.0.2, matching `pom.xml`. The CONVENTIONS
  JDK caveat (`-Dmaven.compiler.release=21`) does **not** apply; every wave
  verifies with a plain `mvn clean verify`.
- Maven 3.9.10.
- `REFACTOR-GUIDE.md` was committed to `dev` before Wave 0 (`00480a5`) so that
  worker worktrees carry the design authority.
