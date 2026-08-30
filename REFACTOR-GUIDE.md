Plan for an **incremental refactoring toward Clean Architecture + tactical DDD**.

The advantage is that the project already has a good base: an entity with validations, a CRUD service tested with TDD, a repository, a DTO/mapper, `@DataJpaTest`/`@WebMvcTest` tests and a defined API.

There is also an explicit architectural contradiction that is worth resolving: the original `TrainingProgram` task required the class itself to be an `@Entity`, and tests were even written to verify the JPA annotations; that was correct for the original design, but it **directly clashes with the current rubric**, which requires a domain free of technological contamination.

# Layer 1 — Strategic plan

I would organize the whole process into **7 stages**, keeping the application working after each one.

| Stage | Strategic objective | Main criterion affected |
|---|---|---|
| **0. Establish a baseline** | Freeze current behavior with tests before moving architecture | Safety of the refactoring |
| **1. Create the architectural boundaries** | Reorganize the project into `domain`, `application` and `infrastructure` | Layer separation |
| **2. Purify and strengthen the domain** | Remove Spring/JPA dependencies and create Value Objects with invariants | Tactical patterns |
| **3. Introduce ports and use cases** | Make Application depend only on its own contracts | Repository / contracts |
| **4. Encapsulate JPA in Infrastructure** | Turn JPA into a replaceable detail via an Adapter | Separation + Repository |
| **5. Decouple the REST API from the domain** | Make controllers work with DTOs/use cases, never with JPA/repositories | Layer separation |
| **6. Lock the architecture down with tests** | Automatically prevent architectural regressions | All three criteria |
| **7. Cleanup and validation against the rubric** | Remove legacy code, update documentation and check 4/4/4 | Closure |

The conceptual transformation would be:

```text
CURRENT

Controller
    ↓
Service
    ↓
TrainingProgramRepository extends JpaRepository
    ↓
TrainingProgram @Entity
    ↓
JPA / PostgreSQL
```

toward:

```text
TARGET

                 ┌─────────────────────────┐
                 │        DOMAIN           │
                 │                         │
                 │ TrainingProgram         │
                 │ Value Objects           │
                 │ Repository interface    │
                 └───────────▲─────────────┘
                             │
                 ┌───────────┴─────────────┐
                 │      APPLICATION        │
                 │                         │
                 │ Use Cases               │
                 │ Commands / Results      │
                 └───────────▲─────────────┘
                             │
                dependency inversion
                             │
        ┌────────────────────┴────────────────────┐
        │              INFRASTRUCTURE             │
        │                                         │
        │ REST Controller       JPA Adapter       │
        │ DTO / Mapper          JpaEntity         │
        │                       SpringData Repo    │
        │                                         │
        └─────────────────────────────────────────┘
```

The fundamental rule becomes:

> **Dependencies point inward. Domain does not know that Spring, HTTP, PostgreSQL, Hibernate or JPA exist.**

I see no need to turn this into a multi-module Maven project to satisfy the rubric. **Separation by packages is enough** and drastically reduces the cost of the refactoring.

---

# Layer 2 — Tactical/technical plan

## Stage 0 — Build a safety net before moving code

The project has an important advantage: it was developed with TDD and there are already tests for the domain, service, repository, mapper and controller. The service currently covers `save`, `findAll`, `findById`, `update` and `deleteById`; the controller exposes POST/GET/PUT/DELETE under `/programs`.

Before refactoring, I would run:

```bash
mvn clean verify
```

and record:

- total tests;
- failing tests;
- coverage;
- existing endpoints;
- current request/response;
- database schema;
- error behavior;
- HTTP codes.

The goal is not to preserve the implementation, but to **preserve the observable behavior**.

It is especially worth creating characterization tests for:

```text
POST   /programs
GET    /programs
PUT    /programs/{id}
DELETE /programs/{id}
```

and making sure they survive the entire migration. Those endpoints are already part of the current contract.

### Stage outcome

```text
✓ main green
✓ mvn clean verify green
✓ known HTTP behavior
✓ coverage baseline recorded
```

---

# Stage 1 — Create the architectural boundaries

The application is currently built following technical layers such as `model`, `repository`, `service`, `controller`, `dto`, etc. The key change is to organize it first by **architectural responsibility**.

I propose this structure:

```text
src/main/java/cl/keber/

├── domain/
│   ├── model/
│   ├── valueobject/
│   ├── repository/
│   └── exception/
│
├── application/
│   ├── usecase/
│   ├── command/
│   └── service/
│
└── infrastructure/
    ├── persistence/
    │   ├── entity/
    │   ├── repository/
    │   ├── adapter/
    │   └── mapper/
    │
    ├── web/
    │   ├── controller/
    │   ├── dto/
    │   └── mapper/
    │
    └── config/
```

I would not move everything blindly. The classification must answer one question:

### Domain

Contains what would still make sense even if tomorrow we removed:

- Spring;
- JPA;
- REST;
- PostgreSQL;
- Jackson.

### Application

Contains:

- which operations the system can perform;
- domain coordination;
- logical transactions;
- use cases.

For example:

```text
CreateTrainingProgram
FindTrainingProgram
ListTrainingPrograms
UpdateTrainingProgram
DeleteTrainingProgram
```

### Infrastructure

Contains absolutely everything technological:

```text
@RestController
@Repository
@Configuration
@Entity
JpaRepository
Jackson
HTTP
SQL
Flyway
Spring
```

### Dependency rule

It must end up as:

```text
domain          -> Java
application     -> domain
infrastructure  -> application + domain + frameworks
```

and never:

```text
domain -> infrastructure
domain -> Spring
domain -> JPA

application -> JpaRepository
application -> controller
application -> Hibernate
```

### Outcome

This organization alone already makes the architectural intent visible, but it is not enough yet: afterwards we must remove the technological dependencies that today live inside the domain.

---

# Stage 2 — Purify and enrich the domain

This is probably the most important stage with respect to the rubric.

Currently `TrainingProgram` already has something valuable: it validates nulls and date consistency on construction. That must be kept. In fact, the existing tests were created specifically for those invariants.

What must be removed is:

```java
@Entity
@Table(...)
@Id
@GeneratedValue(...)
```

and any similar dependency.

The future domain entity should be **pure Java**.

## 2.1 Create Value Objects

Instead of representing everything as:

```java
String code;
String name;
LocalDate startDate;
LocalDate endDate;
```

I would create, at a minimum:

```text
TrainingProgramCode
TrainingProgramName
TrainingPeriod
```

Optionally:

```text
TrainingProgramId
```

### Example: code

```java
public record TrainingProgramCode(String value) {

    public TrainingProgramCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Training program code cannot be blank"
            );
        }

        value = value.trim();
    }
}
```

This object is:

- immutable;
- self-consistent;
- impossible to construct in an invalid state;
- independent of Spring;
- independent of JPA.

That is exactly what the rubric is looking for with **immutable, self-validating Value Objects**.

### Date range

The rule currently lives inside `TrainingProgram`.

Conceptually it belongs better in:

```java
public record TrainingPeriod(
    LocalDate startDate,
    LocalDate endDate
) {

    public TrainingPeriod {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                "Training period dates cannot be null"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "End date cannot be before start date"
            );
        }
    }
}
```

Now this condition:

```text
endDate >= startDate
```

cannot be violated anywhere in the system.

There is no need to remember to validate dates in:

- controller;
- service;
- mapper;
- repository.

The type itself guarantees the rule.

---

# 2.2 Turn `TrainingProgram` into a real domain entity

For example:

```java
public final class TrainingProgram {

    private Long id;

    private TrainingProgramCode code;
    private TrainingProgramName name;
    private TrainingPeriod period;

    private TrainingProgram(
            Long id,
            TrainingProgramCode code,
            TrainingProgramName name,
            TrainingPeriod period) {

        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.period = Objects.requireNonNull(period);
    }

    public static TrainingProgram create(
            TrainingProgramCode code,
            TrainingProgramName name,
            TrainingPeriod period) {

        return new TrainingProgram(null, code, name, period);
    }

    public static TrainingProgram restore(
            Long id,
            TrainingProgramCode code,
            TrainingProgramName name,
            TrainingPeriod period) {

        return new TrainingProgram(id, code, name, period);
    }
}
```

It would not have:

```java
setCodigo(...)
setFechaInicio(...)
setFechaFin(...)
```

but rather semantic behavior:

```java
rename(...)
reschedule(...)
```

For example:

```java
public void reschedule(TrainingPeriod newPeriod) {
    this.period = Objects.requireNonNull(newPeriod);
}
```

This way the entity has a **life cycle**, while the Value Objects that make it up are immutable.

That fits much better with the "excellent" criterion:

> Entities with a life cycle + immutable Value Objects that protect the business rules.

---

# Stage 3 — Create the pure Repository contract

This change must be done **before touching JPA**.

Today `TrainingProgramRepository` extends `JpaRepository<TrainingProgram, Long>` directly.

That contract must disappear from Domain/Application.

I would create:

```text
domain/repository/TrainingProgramRepository.java
```

with something like:

```java
public interface TrainingProgramRepository {

    TrainingProgram save(TrainingProgram program);

    Optional<TrainingProgram> findById(Long id);

    List<TrainingProgram> findAll();

    boolean existsById(Long id);

    void deleteById(Long id);
}
```

Notice what **does not appear**:

```java
JpaRepository
Pageable
JpaSpecificationExecutor
EntityManager
JdbcTemplate
@Repository
```

It is simply Java.

To match the rubric literally, I would use the English contract name:

```text
TrainingProgramRepository
```

even though externally the API can keep talking about `programs`.

## Dependency inversion

The relationship stops being:

```text
Application
      ↓
Spring Data JPA
```

and becomes:

```text
Application
      ↓
TrainingProgramRepository
      ▲
      │ implements
      │
JpaTrainingProgramRepositoryAdapter
```

That triangle is probably **the architectural change that most clearly demonstrates the 4 points of the Repository criterion**.

---

# Stage 4 — Turn the Service into use cases

The existing `TrainingProgramService` performs CRUD and uses `TrainingProgramRepository`; that gives an excellent migration point.

Instead of the controller knowing a "generic service", I would make the use cases explicit.

This could be done with interfaces:

```text
application/usecase/

CreateTrainingProgramUseCase
GetTrainingProgramUseCase
ListTrainingProgramsUseCase
UpdateTrainingProgramUseCase
DeleteTrainingProgramUseCase
```

and a single implementation:

```text
TrainingProgramApplicationService
```

There is no need to create five distinct classes if that only adds ceremony.

For example:

```java
public interface CreateTrainingProgramUseCase {

    TrainingProgram execute(CreateTrainingProgramCommand command);
}
```

The command:

```java
public record CreateTrainingProgramCommand(
    String code,
    String name,
    LocalDate startDate,
    LocalDate endDate
) {}
```

And the implementation:

```java
public class TrainingProgramApplicationService
        implements CreateTrainingProgramUseCase {

    private final TrainingProgramRepository repository;

    public TrainingProgramApplicationService(
            TrainingProgramRepository repository) {

        this.repository = repository;
    }

    @Override
    public TrainingProgram execute(
            CreateTrainingProgramCommand command) {

        var program = TrainingProgram.create(
            new TrainingProgramCode(command.code()),
            new TrainingProgramName(command.name()),
            new TrainingPeriod(
                command.startDate(),
                command.endDate()
            )
        );

        return repository.save(program);
    }
}
```

What matters for the rubric is here:

```java
private final TrainingProgramRepository repository;
```

and here:

```java
public TrainingProgramApplicationService(
    TrainingProgramRepository repository)
```

### Never:

```java
this.repository =
    new JpaTrainingProgramRepository(...);
```

The use case only knows the **abstraction**.

---

# Stage 5 — Move JPA entirely to Infrastructure

This is where the technological contamination is removed from the model.

## 5.1 Create an independent JPA entity

For example:

```text
infrastructure/persistence/entity/
    TrainingProgramJpaEntity.java
```

```java
@Entity
@Table(name = "training_program")
public class TrainingProgramJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    // JPA constructor, getters/setters...
}
```

The fact that this class is anemic **does not matter**.

It is not the domain.

It is a persistence object.

This distinction is fundamental:

```text
TrainingProgram
    = business model

TrainingProgramJpaEntity
    = PostgreSQL/Hibernate representation
```

---

# 5.2 Create the internal Spring repository

```java
interface SpringDataTrainingProgramRepository
    extends JpaRepository<TrainingProgramJpaEntity, Long> {
}
```

This repository can use all the technology it wants because it lives in Infrastructure.

---

# 5.3 Create the persistence mapper

```text
TrainingProgram
      ⇅
TrainingProgramPersistenceMapper
      ⇅
TrainingProgramJpaEntity
```

For example:

```java
public TrainingProgram toDomain(
        TrainingProgramJpaEntity entity) {

    return TrainingProgram.restore(
        entity.getId(),
        new TrainingProgramCode(entity.getCode()),
        new TrainingProgramName(entity.getName()),
        new TrainingPeriod(
            entity.getStartDate(),
            entity.getEndDate()
        )
    );
}
```

---

# 5.4 Create the Adapter

Finally:

```java
@Repository
public class JpaTrainingProgramRepositoryAdapter
        implements TrainingProgramRepository {

    private final SpringDataTrainingProgramRepository repository;
    private final TrainingProgramPersistenceMapper mapper;

    ...
}
```

For example:

```java
@Override
public Optional<TrainingProgram> findById(Long id) {

    return repository
        .findById(id)
        .map(mapper::toDomain);
}
```

The full structure becomes:

```text
TrainingProgramRepository
       ▲
       │ implements
       │
JpaTrainingProgramRepositoryAdapter
       │
       ▼
SpringDataTrainingProgramRepository
       │
       ▼
JpaRepository
       │
       ▼
PostgreSQL
```

### Important: it should not require DB changes

Ideally `TrainingProgramJpaEntity` should keep using **the same existing tables and columns**. The project already uses Flyway and tests persistence with `@DataJpaTest`, so there is no architectural reason to introduce a schema migration just to separate domain/JPA.

---

# Stage 6 — Decouple the REST API

Here I would leverage the good decision the project already made when it introduced `TrainingProgramDto` and a mapper specifically to decouple the REST representation from the entity.

But it needs to be taken one level further.

The chain should be:

```text
HTTP JSON
   ↓
DTO
   ↓
Web Mapper
   ↓
Application Command
   ↓
Use Case
   ↓
Domain
```

and on the way back:

```text
Domain
   ↓
Response DTO
   ↓
JSON
```

The controller should not know about:

```text
TrainingProgramRepository
JpaRepository
TrainingProgramJpaEntity
EntityManager
```

Only:

```text
CreateTrainingProgramUseCase
ListTrainingProgramsUseCase
...
```

Example:

```java
@RestController
@RequestMapping("/programs")
public class TrainingProgramController {

    private final CreateTrainingProgramUseCase createProgram;

    public TrainingProgramController(
            CreateTrainingProgramUseCase createProgram) {

        this.createProgram = createProgram;
    }
}
```

This allows the following to be preserved exactly:

```http
POST /programs
GET /programs
PUT /programs/{id}
DELETE /programs/{id}
```

even though everything that happens behind them changes. Those are the endpoints currently documented.

Therefore, the frontend **should not need any changes**.

---

# Stage 7 — Configure the dependency wiring

To maximize even the decoupling of Application from Spring, I would avoid:

```java
@Service
public class TrainingProgramApplicationService
```

and leave that class as pure Java:

```java
public class TrainingProgramApplicationService {
    ...
}
```

Spring builds the graph from Infrastructure:

```java
@Configuration
public class TrainingProgramConfiguration {

    @Bean
    TrainingProgramApplicationService trainingProgramService(
            TrainingProgramRepository repository) {

        return new TrainingProgramApplicationService(repository);
    }
}
```

So that:

```text
domain
    0 Spring annotations

application
    0 Spring annotations

infrastructure
    @Configuration
    @Bean
    @Repository
    @RestController
    @Entity
```

This makes the rubric's statement especially evident:

> **Zero technological coupling.**

---

# Stage 8 — Restructure the tests

Here I would not remove the existing TDD strategy; I would **adapt it to the new boundaries**.

## A. Domain tests

They must be pure Java/JUnit.

```text
TrainingProgramCodeTest
TrainingProgramNameTest
TrainingPeriodTest
TrainingProgramTest
```

Without:

```java
@SpringBootTest
@DataJpaTest
@MockBean
```

Examples:

```text
✓ null code → invalid
✓ empty code → invalid
✓ empty name → invalid
✓ end date earlier → invalid
✓ valid period → constructs
✓ invalid reschedule → impossible
```

These tests would directly demonstrate the **Tactical patterns** criterion.

---

## B. Application tests

Mock:

```text
TrainingProgramRepository
```

not:

```text
JpaRepository
```

For example:

```java
@Mock
TrainingProgramRepository repository;
```

and test:

```text
CreateTrainingProgramUseCase
UpdateTrainingProgramUseCase
DeleteTrainingProgramUseCase
```

That demonstrates dependency inversion.

---

## C. Infrastructure persistence tests

Here yes:

```java
@DataJpaTest
```

Testing:

```text
TrainingProgram
     ↓
adapter
     ↓
JPA
     ↓
H2/PostgreSQL
```

The project already uses this strategy for the current repository.

---

## D. Web tests

Keep:

```java
@WebMvcTest
MockMvc
```

which are already used today.

But the mock becomes:

```text
CreateTrainingProgramUseCase
```

instead of:

```text
TrainingProgramService
```

---

# Stage 9 — Add architecture tests

This part is not strictly necessary for the code to work, but I strongly recommend it because it turns the rubric into an **executable constraint**.

I would use **ArchUnit**.

For example:

```java
@AnalyzeClasses(packages = "cl.keber")
class ArchitectureTest {
}
```

### Rule 1

Domain cannot depend on Spring:

```text
domain
    X→ org.springframework
```

### Rule 2

Domain cannot depend on JPA:

```text
domain
    X→ jakarta.persistence
```

### Rule 3

Application cannot depend on Infrastructure:

```text
application
    X→ infrastructure
```

### Rule 4

Controllers cannot access repositories:

```text
controller
    X→ repository
```

### Rule 5

Infrastructure may depend inward:

```text
infrastructure
    → application
    → domain
```

That way, if in six months someone writes:

```java
@Entity
public class TrainingProgram
```

the build fails.

Or if someone introduces:

```java
@Autowired
private SpringDataTrainingProgramRepository repository;
```

inside a use case, the pipeline fails.

The architecture stops being just a diagram and becomes a verifiable property of the code.

---

# Stage 10 — Remove legacy code

Only after everything works would I remove:

```text
model/TrainingProgram
repository/TrainingProgramRepository
service/TrainingProgramService
```

and any old mapper that has been replaced.

This sequence avoids a "big bang" refactoring.

During part of the migration the following could temporarily coexist:

```text
old/
new/
```

though ideally through small, short-lived commits.

---

# Stage 11 — Update documentation

This is particularly important in this repo because there is detailed per-task documentation.

For example, today AB#106 explicitly says that:

- `TrainingProgram` is a JPA entity;
- `@Entity` was added;
- the tests verify the JPA annotations.

After the refactoring that would remain historically correct, but architecturally obsolete.

I would not delete those documents; they are part of the traceability.

I would add something like:

```text
docs/
  architecture/
    clean-architecture.md
    package-dependencies.md
    domain-model.md
```

and a note:

> Starting from Milestone 3, the domain model is separated from the JPA representation. `TrainingProgram` is no longer a persistence entity and JPA is moved to Infrastructure.

This also shows that the decision was **deliberate**, not accidental.

---

# How I would ship the changes as PRs

I do not recommend doing everything in a single PR.

I would propose roughly:

| PR | Change |
|---|---|
| **PR 1** | Baseline + characterization tests |
| **PR 2** | Create the `domain/application/infrastructure` structure |
| **PR 3** | Create Value Objects and the pure domain |
| **PR 4** | Create the pure `TrainingProgramRepository` |
| **PR 5** | Introduce use cases / Application Service |
| **PR 6** | Create the JPA entity + persistence adapter |
| **PR 7** | Migrate the controller and DTO mapping |
| **PR 8** | ArchUnit + cleanup + documentation |

Each PR should end with:

```bash
mvn clean verify
```

green.

This also preserves the RED-GREEN-REFACTOR philosophy that is already part of the project's history.

---

# Expected final state against the rubric

## 1. Separation into decoupled layers — **4/4**

We could demonstrate:

```text
domain/
application/
infrastructure/
```

with:

```text
Domain → no Spring
Domain → no JPA
Application → no JPA
Application → no Infrastructure
Infrastructure → technological dependencies
```

Expected result:

**4/4.**

---

## 2. Modeling of tactical patterns — **4/4**

We would have:

```text
TrainingProgram
    ├── TrainingProgramCode
    ├── TrainingProgramName
    └── TrainingPeriod
```

with:

- immutable Value Objects;
- invariants in the constructor;
- an entity with behavior;
- absence of indiscriminate setters;
- impossibility of constructing invalid states.

Expected result:

**4/4.**

---

## 3. Repository and contracts — **4/4**

We would have:

```text
TrainingProgramRepository
           ▲
           │
JpaTrainingProgramRepositoryAdapter
           │
SpringDataTrainingProgramRepository
```

and the use cases would receive:

```java
TrainingProgramRepository
```

via the constructor.

Never:

```java
new Jpa...
```

nor:

```java
JpaRepository
```

from Application.

Expected result:

**4/4.**

---

# Final architectural result

The goal should not be simply to "move files", but to reach this situation:

```text
                       ┌───────────────────────┐
                       │        DOMAIN         │
                       │                       │
                       │ TrainingProgram       │
                       │ TrainingProgramCode   │
                       │ TrainingProgramName   │
                       │ TrainingPeriod        │
                       │ Repository Port       │
                       └──────────▲────────────┘
                                  │
                                  │
                       ┌──────────┴────────────┐
                       │     APPLICATION       │
                       │                       │
                       │ Create Program        │
                       │ Update Program        │
                       │ Find Program          │
                       │ Delete Program        │
                       └──────────▲────────────┘
                                  │
            ┌─────────────────────┴─────────────────────┐
            │                                           │
┌───────────┴────────────┐                ┌─────────────┴────────────┐
│    REST ADAPTER        │                │   PERSISTENCE ADAPTER    │
│                        │                │                          │
│ Controller             │                │ RepositoryAdapter        │
│ DTO                    │                │ JpaEntity                │
│ Mapper                 │                │ SpringDataRepository     │
│ Jackson                │                │ Hibernate / PostgreSQL   │
└────────────────────────┘                └──────────────────────────┘
              INFRASTRUCTURE / OUTSIDE WORLD
```

And there is an important point about scope: **I would not try to "DDD-ify" all of Sisacad in one go**. The README shows that the future domain includes programs, versions, editions, client organizations, facilitators, students, sections, attendance and assessments; that could grow considerably. For Milestone 3, I would make **Training Program a flawless vertical slice**, capable of demonstrating 4/4/4, and use that slice as the architectural template for the future aggregates.

That approach significantly reduces the work: **we are not rewriting Sisacad; we are creating a correct pattern with `TrainingProgram` that the following features can replicate.**
