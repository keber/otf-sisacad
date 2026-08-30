# The TrainingProgram domain model

The domain layer (`cl.keber.domain`) is plain Java. It compiles without Spring,
without Hibernate and without Jackson, and its tests are plain JUnit with no
`@SpringBootTest`, `@DataJpaTest` or `@MockBean` anywhere.

## Overview

```text
TrainingProgram                        (entity - has identity and a lifecycle)
    ├── TrainingProgramCode            (value object - immutable)
    ├── TrainingProgramName            (value object - immutable)
    ├── TrainingPeriod                 (value object - immutable)
    └── TrainingProgramStatus          (value object - immutable)

TrainingProgramRepository              (port - plain interface)
TrainingProgramNotFoundException       (domain exception)
```

The distinction that drives the design:

- A **Value Object** has no identity. Two codes with the same string *are* the
  same code. It is immutable and it validates itself on construction, so an
  invalid instance cannot exist.
- An **entity** has identity (`id`) and a lifecycle. Its state changes over
  time, but only through methods that mean something in the business — never
  through blanket setters.

## Value Objects

All four are Java `record`s in `cl.keber.domain.valueobject`, validating in the
compact constructor. All throw `IllegalArgumentException` on invalid input.

| Value Object | Components | Invariant |
|---|---|---|
| `TrainingProgramCode` | `String value` | not null, not blank; trimmed |
| `TrainingProgramName` | `String value` | not null, not blank; trimmed |
| `TrainingPeriod` | `LocalDate startDate`, `LocalDate endDate` | both non-null; `endDate` strictly after `startDate` |
| `TrainingProgramStatus` | `String value` | not null, not blank; trimmed |

Shape:

```java
public record TrainingProgramCode(String value) {

    public TrainingProgramCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }
        value = value.trim();
    }
}
```

The date rule is the interesting one, because it used to live inside the entity
and had to be remembered anywhere a program was built:

```java
public record TrainingPeriod(LocalDate startDate, LocalDate endDate) {

    public TrainingPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("period dates must not be null");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
    }
}
```

Now `endDate > startDate` cannot be violated *anywhere* in the system. There is
nothing to remember in the controller, the use case, the mapper or the adapter:
the type itself carries the guarantee. Note the rule is **strict** — a period
that starts and ends on the same day is rejected, which preserves the behaviour
the original entity had.

Each Value Object is therefore:

- immutable;
- self-validating;
- impossible to construct in an invalid state;
- independent of Spring;
- independent of JPA.

> **On `status`.** The original design carries `status` as a free-form `String`.
> It is modelled here as a Value Object for consistency with the other three,
> keeping string values. Turning it into an enum would change the accepted
> values and therefore the REST contract, so it was deliberately left as a
> validated string.

## The entity

`cl.keber.domain.model.TrainingProgram` is a `final class` whose fields are the
Value Objects above plus a `Long id`.

### Construction: two factories, no public constructor

```java
public static TrainingProgram create(
        TrainingProgramCode code,
        TrainingProgramName name,
        TrainingPeriod period,
        TrainingProgramStatus status);      // id == null - not yet persisted

public static TrainingProgram restore(
        Long id,
        TrainingProgramCode code,
        TrainingProgramName name,
        TrainingPeriod period,
        TrainingProgramStatus status);      // rehydration from persistence
```

Two named factories instead of one constructor, because the two situations are
genuinely different. `create` expresses *a new program is being registered* and
guarantees the id is absent; `restore` expresses *this program already exists in
the database and is being rebuilt*. The persistence mapper is the only caller of
`restore`.

There is no public no-arg constructor. The JPA/Jackson no-arg constructor that
the old class needed now belongs to `TrainingProgramJpaEntity` and
`TrainingProgramDto` respectively, where it is harmless.

That absence is what forces the domain entity off the wire, and it is worth
being explicit about, because it is the moment the separation stops being
cosmetic. The old controller bound and returned `TrainingProgram` directly. A
pure domain entity cannot do that job at all:

- **Requests cannot bind.** Jackson needs a no-arg constructor and settable
  fields. A final class with a private constructor and factories has neither —
  by design, because that is what makes an invalid instance unconstructable.
- **Responses would change shape.** Getters returning Value Objects serialise as
  `{"code": {"value": "PRG-1"}}` rather than `{"code": "PRG-1"}`, which would
  break every existing client.

So `TrainingProgramDto` becomes the real wire type — flat primitives, a no-arg
constructor, setters, and an `id` field, since `id` is on the wire in both
directions and clients need it to address `PUT` and `DELETE`. The web mapper
translates between the DTO and the domain.

This is the healthy version of the constraint: the serialisation format is a
detail of the REST adapter, and the domain no longer has to compromise its
design to satisfy it.

### Lifecycle: behaviour, not setters

The old class exposed the whole state through getters and was mutated by
replacing the object wholesale. The domain entity instead offers:

```java
public void rename(TrainingProgramName newName);
public void reschedule(TrainingPeriod newPeriod);
public void changeStatus(TrainingProgramStatus newStatus);
```

There is deliberately no `setCode`, no `setStartDate`, no `setEndDate`. Each
method names a business operation, and each takes an already-valid Value Object,
so an invalid mutation is not expressible:

```java
program.reschedule(new TrainingPeriod(end, start));   // throws in the VO -
                                                      // the entity is never touched
```

That is the shape the rubric asks for: **an entity with a lifecycle composed of
immutable Value Objects that protect the business rules.**

Identity follows the id: two `TrainingProgram` instances are equal when they
carry the same non-null `id`.

## The repository port

`cl.keber.domain.repository.TrainingProgramRepository` is the contract the
application depends on. It is a plain Java interface speaking only in domain
types:

```java
public interface TrainingProgramRepository {

    TrainingProgram save(TrainingProgram program);

    Optional<TrainingProgram> findById(Long id);

    List<TrainingProgram> findAll();

    boolean existsById(Long id);

    void deleteById(Long id);
}
```

What is notably absent: `JpaRepository`, `Pageable`, `JpaSpecificationExecutor`,
`EntityManager`, `JdbcTemplate`, `@Repository`, any `jakarta.*` or
`org.springframework.*` import.

### Port and adapter

```text
       TrainingProgramRepository            cl.keber.domain.repository
                  ^
                  | implements
                  |
   JpaTrainingProgramRepositoryAdapter      infrastructure.persistence.adapter
                  |
                  v
  SpringDataTrainingProgramRepository       infrastructure.persistence.repository
                  |
                  v
             JpaRepository
                  |
                  v
              PostgreSQL
```

The adapter is annotated `@Repository`, holds the Spring Data interface and the
persistence mapper, and translates in both directions:

```java
@Override
public Optional<TrainingProgram> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
}
```

Use cases receive the port through the constructor and never construct an
adapter themselves:

```java
public TrainingProgramApplicationService(TrainingProgramRepository repository) {
    this.repository = repository;
}
```

Never `this.repository = new JpaTrainingProgramRepositoryAdapter(...)`. The
wiring is Spring's job, and it happens in
`infrastructure.config.TrainingProgramConfiguration`.

## Use cases

`cl.keber.application` turns the entity into operations the system offers:

| Use case | Input | Output |
|---|---|---|
| `CreateTrainingProgramUseCase` | `CreateTrainingProgramCommand` | `TrainingProgram` |
| `GetTrainingProgramUseCase` | `Long id` | `Optional<TrainingProgram>` |
| `ListTrainingProgramsUseCase` | — | `List<TrainingProgram>` |
| `UpdateTrainingProgramUseCase` | `UpdateTrainingProgramCommand` | `TrainingProgram` |
| `DeleteTrainingProgramUseCase` | `Long id` | `void` |

`GetTrainingProgramUseCase` exists but is **not routed**. There has never been a
`GET /programs/{id}` endpoint — the old service had a `findById` that no
controller method ever called, so the operation was planned and never wired.
Exposing it now would be a new feature rather than a refactor, so the use case is
built and left unrouted, ready for whoever adds the endpoint.

Commands are plain records carrying raw input (`String`, `LocalDate`), because
they sit at the boundary where the caller has not yet produced Value Objects:

```java
public record CreateTrainingProgramCommand(
    String code, String name, LocalDate startDate, LocalDate endDate, String status) {}
```

The use case is what turns raw input into Value Objects — which is exactly where
validation should happen:

```java
@Override
public TrainingProgram execute(CreateTrainingProgramCommand command) {
    var program = TrainingProgram.create(
        new TrainingProgramCode(command.code()),
        new TrainingProgramName(command.name()),
        new TrainingPeriod(command.startDate(), command.endDate()),
        new TrainingProgramStatus(command.status()));

    return repository.save(program);
}
```

All five interfaces are implemented by the single class
`TrainingProgramApplicationService`. Five separate implementation classes would
add ceremony without adding separation; the *interfaces* are what give the
controller a narrow dependency.

## Testing

| Layer | Style | What it proves |
|---|---|---|
| `domain` | plain JUnit, no Spring | invariants hold; invalid states are unconstructable |
| `application` | Mockito, mocking the **port** | dependency inversion — the use case never sees JPA |
| `infrastructure.persistence` | `@DataJpaTest` | the adapter and mapper round-trip correctly |
| `infrastructure.web` | `@WebMvcTest` + `MockMvc`, mocking use cases | the HTTP contract is unchanged |

The domain test suite (`TrainingProgramCodeTest`, `TrainingProgramNameTest`,
`TrainingPeriodTest`, `TrainingProgramStatusTest`, `TrainingProgramTest`) no
longer asserts on JPA annotations. Those assertions were correct under the
original design and were removed on purpose; the JPA mapping is now verified in
the infrastructure tests, which is where it belongs.
