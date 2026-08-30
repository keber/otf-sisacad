# Package structure and dependency rules

Companion to [`clean-architecture.md`](clean-architecture.md). This page is the
concrete inventory: which class lives where, and which direction each dependency
is allowed to point.

## Package tree

All production code lives under `src/main/java/cl/keber`.

```text
cl.keber
├── OtfSisacadApplication                       @SpringBootApplication (root)
│
├── domain                                      pure Java - no framework imports
│   ├── model
│   │   └── TrainingProgram                     entity with lifecycle behaviour
│   ├── valueobject
│   │   ├── TrainingProgramCode                 record, non-blank
│   │   ├── TrainingProgramName                 record, non-blank
│   │   ├── TrainingPeriod                      record, endDate after startDate
│   │   └── TrainingProgramStatus               record, non-blank
│   ├── repository
│   │   └── TrainingProgramRepository           the port (plain interface)
│   └── exception
│       └── TrainingProgramNotFoundException
│
├── application                                 depends on domain only
│   ├── usecase
│   │   ├── CreateTrainingProgramUseCase
│   │   ├── GetTrainingProgramUseCase
│   │   ├── ListTrainingProgramsUseCase
│   │   ├── UpdateTrainingProgramUseCase
│   │   └── DeleteTrainingProgramUseCase
│   ├── command
│   │   ├── CreateTrainingProgramCommand        record of raw input
│   │   └── UpdateTrainingProgramCommand        record of raw input
│   └── service
│       └── TrainingProgramApplicationService   implements all five use cases
│
└── infrastructure                              all technology lives here
    ├── persistence
    │   ├── entity
    │   │   └── TrainingProgramJpaEntity        @Entity @Table("training_program")
    │   ├── repository
    │   │   └── SpringDataTrainingProgramRepository   extends JpaRepository
    │   ├── adapter
    │   │   └── JpaTrainingProgramRepositoryAdapter   implements the port
    │   └── mapper
    │       └── TrainingProgramPersistenceMapper      domain <-> JPA entity
    ├── web
    │   ├── controller
    │   │   └── TrainingProgramController       @RestController /programs
    │   ├── dto
    │   │   └── TrainingProgramDto              JSON shape
    │   └── mapper
    │       └── TrainingProgramMapper           DTO <-> command / domain
    └── config
        ├── WebConfig                           CORS
        └── TrainingProgramConfiguration        @Bean wiring for the app service
```

The test tree mirrors this layout under `src/test/java/cl/keber`, plus
`cl.keber.architecture` for the ArchUnit rules.

`OtfSisacadApplication` stays at the `cl.keber` root on purpose, so
`@SpringBootApplication` component scanning still covers all three sub-trees.

### Migration status

The tree above is the finished state. The refactor lands it in waves, and the
packages are created empty ahead of the class that fills them, so the intent is
visible in the source layout from the start.

| Package | Status |
|---|---|
| `domain.model`, `domain.exception` | populated |
| `domain.valueobject` | populated — all four Value Objects |
| `domain.repository` | empty — the port arrives with the repository wave |
| `application.service` | populated (still the legacy generic service) |
| `application.usecase`, `application.command` | empty — filled by the use case wave |
| `infrastructure.persistence.entity`, `.mapper` | populated |
| `infrastructure.persistence.repository` | populated — still `TrainingProgramRepository extends JpaRepository`; renamed to `SpringDataTrainingProgramRepository` when the adapter lands |
| `infrastructure.persistence.adapter` | empty — the adapter arrives with the repository wave |
| `infrastructure.web.controller`, `.dto`, `.mapper` | populated |
| `infrastructure.config` | populated (`WebConfig`; the bean wiring arrives with the use case swap) |

The legacy top-level `model`, `repository`, `service`, `controller`, `dto`,
`mapper`, `exception` and `config` packages no longer exist.

Two consequences of landing this incrementally are worth knowing while it is in
flight:

- `TrainingProgramRepository` is temporarily an **ambiguous simple name** — the
  JPA-extending one in `infrastructure.persistence.repository` and the port in
  `domain.repository` coexist until the former is renamed. Expected, and
  resolved by the rename.
- Technological contamination is removed in stages. The domain is already pure —
  `TrainingProgram` carries no `@Entity` and the JPA mapping has moved to
  `TrainingProgramJpaEntity` — but `TrainingProgramService` still carries
  `@Service` and still maps domain to JPA entity inline, because the port and
  adapter that will take that job over have not landed yet. Purifying and moving
  in one commit would make the diff unreviewable.
- Until the adapter exists, `TrainingProgramService` maps at the repository
  boundary itself. That inline mapping is scaffolding, not the design: it moves
  into `JpaTrainingProgramRepositoryAdapter`, and the service loses its
  `@Service` annotation in favour of explicit bean wiring.

## Allowed dependency directions

```text
     domain   <-----   application   <-----   infrastructure
        ^                                          |
        +------------------------------------------+
```

| From | May depend on | Must never depend on |
|---|---|---|
| `domain` | the Java standard library | `application`, `infrastructure`, `org.springframework..`, `jakarta.persistence..`, Hibernate, `com.fasterxml.jackson..` |
| `application` | `domain` | `infrastructure`, `org.springframework..`, `jakarta.persistence..`, `org.springframework.data..`, any `..web..` type |
| `infrastructure` | `application`, `domain`, any framework | — (nothing depends on it) |

Two consequences worth calling out:

- **Controllers never see repositories.** `infrastructure.web.controller` may
  not reach `infrastructure.persistence` or `domain.repository`. It talks to
  use case interfaces only.
- **The application service is not a Spring bean by annotation.**
  `TrainingProgramApplicationService` is plain Java; `TrainingProgramConfiguration`
  in `infrastructure.config` constructs it with the port. That is what keeps
  `application` free of `org.springframework`.

## How the rules are enforced

Documentation drifts. These rules are executable, as ArchUnit tests in
`src/test/java/cl/keber/architecture/ArchitectureTest.java`, annotated
`@AnalyzeClasses(packages = "cl.keber")`. They run as part of
`mvn clean verify`, so a violation fails the build and the CI pipeline.

The rule set:

| # | Rule |
|---|---|
| 1 | `domain` must not depend on `org.springframework..` |
| 2 | `domain` must not depend on `jakarta.persistence..` or Hibernate |
| 3 | `domain` must not depend on `com.fasterxml.jackson..` |
| 4 | `application` must not depend on `..infrastructure..` |
| 5 | `application` must not depend on `org.springframework..`, `org.springframework.data..` or `jakarta.persistence..` |
| 6 | classes in `..web.controller..` must not depend on `..persistence..` or `..domain.repository..` |
| 7 | `..domain..` and `..application..` must not depend on `..web..` |
| 8 | layered check: `domain` may be accessed by `application` and `infrastructure`; `application` may be accessed by `infrastructure`; `infrastructure` may be accessed by nothing |

There is no freeze store and no allowance list — the slice is expected to be
clean. If a rule cannot pass, the correct response is to fix the code, not to
weaken the rule.

This is what makes the architecture a *verifiable property of the code* rather
than a picture in a README. If someone writes `@Entity` on
`domain.model.TrainingProgram` again, or injects
`SpringDataTrainingProgramRepository` into a use case, the build goes red.

## Quick manual checks

```bash
# domain must be framework-free
grep -R "jakarta.persistence\|org.springframework\|com.fasterxml" \
     src/main/java/cl/keber/domain

# application must not know about JPA or infrastructure
grep -R "JpaRepository\|infrastructure" src/main/java/cl/keber/application

# JPA must exist only in the persistence adapter
grep -Rl "@Entity\|JpaRepository\|jakarta.persistence" src/main/java
```

The first two should print nothing; the third should list files only under
`cl/keber/infrastructure/persistence`.
