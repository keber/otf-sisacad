# Clean Architecture in otf-sisacad

> **Scope.** Milestone 3 turns `TrainingProgram` into a complete vertical slice
> of Clean Architecture + tactical DDD. The rest of the future domain (course
> editions, clients, facilitators, sections, students, enrolments) is *not*
> rewritten; this slice is the template the next aggregates copy.

## Why this change

The system was originally built in technical layers — `model`, `repository`,
`service`, `controller`, `dto`, `mapper`. That is a perfectly reasonable
starting point, and it was delivered with TDD, `@DataJpaTest` and `@WebMvcTest`
coverage.

It carried one architectural contradiction, though. Task AB#106 deliberately
required `TrainingProgram` itself to be a JPA `@Entity`, and the tests even
asserted on the JPA annotations. That was correct for the original design, but
it means the business model cannot be reasoned about — or tested — without
Hibernate. The domain model and the database representation were the same
object.

Milestone 3 separates the two.

## The dependency rule

One rule governs everything:

> **Dependencies point inward. The domain does not know that Spring, HTTP,
> PostgreSQL, Hibernate or JPA exist.**

Concretely:

```text
domain          ->  Java standard library only
application     ->  domain
infrastructure  ->  application + domain + frameworks
```

and never:

```text
domain         -X->  infrastructure
domain         -X->  org.springframework
domain         -X->  jakarta.persistence
application    -X->  JpaRepository
application    -X->  infrastructure
application    -X->  controllers
```

The arrow that makes this possible is **dependency inversion**. The application
layer needs persistence, but it must not depend on it, so the *contract* lives
in the domain and the *implementation* lives in infrastructure:

```text
        TrainingProgramRepository        (port - cl.keber.domain.repository)
                   ^
                   | implements
                   |
   JpaTrainingProgramRepositoryAdapter   (adapter - infrastructure.persistence)
```

The compile-time dependency points inward; the runtime dependency points
outward. That inversion is the point of the refactor.

## Target layering

```text
                 +-------------------------+
                 |         DOMAIN          |
                 |                         |
                 | TrainingProgram         |
                 | Value Objects           |
                 | Repository port         |
                 +------------^------------+
                              |
                 +------------+------------+
                 |       APPLICATION       |
                 |                         |
                 | Use cases               |
                 | Commands                |
                 +------------^------------+
                              |
                 dependency inversion
                              |
        +---------------------+-------------------+
        |             INFRASTRUCTURE              |
        |                                         |
        | REST controller       JPA adapter       |
        | DTO / web mapper      JPA entity        |
        | CORS config           Spring Data repo  |
        |                       Bean wiring       |
        +-----------------------------------------+
```

Split by adapter, the final picture is:

```text
                       +-----------------------+
                       |        DOMAIN         |
                       | TrainingProgram       |
                       | TrainingProgramCode   |
                       | TrainingProgramName   |
                       | TrainingPeriod        |
                       | TrainingProgramStatus |
                       | Repository port       |
                       +----------^------------+
                                  |
                       +----------+------------+
                       |      APPLICATION      |
                       | Create / Get / List   |
                       | Update / Delete       |
                       +----------^------------+
                                  |
            +---------------------+---------------------+
            |                                           |
+-----------+------------+                +-------------+------------+
|     REST ADAPTER       |                |   PERSISTENCE ADAPTER    |
| TrainingProgramCtrl    |                | RepositoryAdapter        |
| TrainingProgramDto     |                | TrainingProgramJpaEntity |
| Web mapper             |                | SpringData repository    |
| Jackson / HTTP         |                | Hibernate / PostgreSQL   |
+------------------------+                +--------------------------+
             INFRASTRUCTURE / OUTSIDE WORLD
```

## What each layer owns

### Domain

Everything that would still make sense if Spring, JPA, REST, PostgreSQL and
Jackson were deleted tomorrow: the `TrainingProgram` entity, its Value Objects,
the repository port, and `TrainingProgramNotFoundException`.

Pure Java. No annotations from any framework.

### Application

*What the system can do*: the use cases, the commands that carry raw input into
them, and the coordination of domain objects. It depends on the repository
**port**, received through the constructor — never on Spring Data, never on a
concrete adapter, never via `new Jpa...`.

The application service is left as plain Java (no `@Service`); Spring builds it
from a `@Configuration` class in infrastructure. That keeps the framework
annotation count in `domain` and `application` at zero.

### Infrastructure

Everything technological: `@RestController`, `@Entity`, `JpaRepository`,
`@Repository`, `@Configuration`, Jackson, HTTP, SQL, Flyway, CORS.

Infrastructure is where the framework is allowed to win.

## Why packages, not Maven modules

Multi-module Maven would enforce the boundary at build level, but it is not
required here and it costs a lot: a parent POM, three child modules, split test
configuration, a reworked CI pipeline and a reworked coverage/Sonar setup — all
for a single aggregate.

**Separation by package is sufficient**, provided the boundary is actually
enforced rather than merely documented. It is enforced by ArchUnit tests (see
[`package-dependencies.md`](package-dependencies.md)), which fail the build on
violation. A diagram can drift; a failing test cannot be ignored.

If the system later grows to several bounded contexts, promoting these packages
to modules is a mechanical change, because the dependency graph is already
acyclic and already verified.

## What did *not* change

- **The shape of the REST contract under `/programs`** — same paths, same JSON
  field names in the same order (`id, code, name, startDate, endDate, status`),
  same success status codes (`POST` and `PUT` return `200`, `DELETE` returns
  `204`). The frontend needs no changes. A suite of characterization tests
  written *before* the refactor started guards this.

  Two things about error handling *did* change, deliberately and with sign-off.
  See [Behaviour that changed on purpose](#behaviour-that-changed-on-purpose).
- The database. `TrainingProgramJpaEntity` maps the existing `training_program`
  table and columns, so **no new Flyway migration was introduced** by this
  refactor. See [`persistence.md`](persistence.md).
- The historical per-task documentation (`docs/106.md` ... `docs/112.md`). It is
  preserved as traceability and carries a "superseded" banner where it describes
  the old design.

## Behaviour that changed on purpose

Preserving observable behaviour was the *motivation* for the safety net, not a
hard gate. The pre-refactor code was not correct, and freezing incorrect
behaviour forever is not a goal. Where the refactor turns a silently-accepted
invalid request into an honest failure, that is the desired outcome: it surfaces
a real defect rather than preserving it.

Two changes were approved on that basis. Both are reflected in the
characterization tests, updated in the same commit that causes them — never
weakened or deleted to make a build pass.

### Validation now actually runs

Before the refactor, the validating constructor on `TrainingProgram` **never ran
over HTTP**. Jackson bound requests through the no-arg constructor and wrote the
private fields directly, so a blank `code` or an `endDate` before `startDate`
was accepted with `200` and persisted. The only thing rejecting anything was the
`NOT NULL` constraint from migration `V1` — and it did so as a `500`.

Now requests bind to a DTO and the use case builds Value Objects, so those
inputs fail in the domain, where the rule lives. This is a fix, not a
regression.

### Errors have honest status codes

`cl.keber.infrastructure.web.RestExceptionHandler`, a `@RestControllerAdvice`,
maps domain failures onto the codes they always should have had. Six
characterization assertions moved as a result:

| Request | Before | Now |
|---|---|---|
| `POST` with a blank `code` | `200`, persisted | **`400`** |
| `POST` with `endDate` before `startDate` | `200`, persisted | **`400`** |
| `POST` with a null `code` | `500` | **`400`** |
| `POST` with an empty body `{}` | `500` | **`400`** |
| `PUT /{id}` where the body id contradicts the path id | `500` | **`400`** |
| `PUT /{id}` on an unknown id | `500` | **`404`** |

The response body keeps Spring Boot's default error shape — `timestamp`,
`status`, `error`, `message`, `path` — so clients that already parsed the old
`500` bodies see the same fields, just with an honest code and a real message.

Everything else held still, and was checked rather than assumed: every success
path (`POST` `200`, `GET` `200`, `PUT` `200`, `DELETE` `204`), the exact response
JSON field set and order, malformed JSON staying `400` (Jackson fails before the
advice is reached), `DELETE` on an unknown id staying `204`, and
`GET /programs/{id}` staying `405`.

Those last three are among the oddities **deliberately left alone**, because
fixing them would be new work rather than refactoring: `PUT` without an `id` in
the body still inserts a duplicate row, `DELETE` on an unknown id is still
silently idempotent, `POST` still returns `200` rather than `201`, and there is
still no `GET /programs/{id}` route. They are logged as known defects for
separate follow-up.

> The duplicate-inserting `PUT` in particular was **re-verified at every stage**
> of the refactor and is genuinely unchanged. It was plausible that routing the
> update through the path id would fix it incidentally; it does not, because the
> path id and the body id remain distinct arguments and the path id is never
> copied into the command. A controller test now pins that separation.

## Further reading

- [`package-dependencies.md`](package-dependencies.md) — the package tree, the
  class list, and the rules that enforce the direction of dependencies.
- [`domain-model.md`](domain-model.md) — the entity, the Value Objects, their
  invariants, and the repository port.
- [`persistence.md`](persistence.md) — domain entity vs. JPA entity, the mapper
  and the adapter.
