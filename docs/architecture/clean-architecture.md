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

- The REST contract under `/programs` — same paths, same JSON, same HTTP codes.
  The frontend needs no changes. Characterization tests written before the
  refactor started guard this.
- The database. `TrainingProgramJpaEntity` maps the existing `training_program`
  table and columns, so **no new Flyway migration was introduced** by this
  refactor. See [`persistence.md`](persistence.md).
- The historical per-task documentation (`docs/106.md` ... `docs/112.md`). It is
  preserved as traceability and carries a "superseded" banner where it describes
  the old design.

## Further reading

- [`package-dependencies.md`](package-dependencies.md) — the package tree, the
  class list, and the rules that enforce the direction of dependencies.
- [`domain-model.md`](domain-model.md) — the entity, the Value Objects, their
  invariants, and the repository port.
- [`persistence.md`](persistence.md) — domain entity vs. JPA entity, the mapper
  and the adapter.
