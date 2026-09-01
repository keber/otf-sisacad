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
│   ├── query
│   │   └── GetTrainingProgramQuery             record wrapping the id
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
    │   ├── RestExceptionHandler                @RestControllerAdvice 400 / 404
    │   ├── controller
    │   │   └── TrainingProgramController       @RestController /programs
    │   ├── dto
    │   │   └── TrainingProgramDto              JSON shape
    │   └── mapper
    │       └── TrainingProgramMapper           DTO <-> domain
    └── config
        ├── WebConfig                           CORS
        └── TrainingProgramConfiguration        @Bean wiring for the app service
```

The test tree mirrors this layout under `src/test/java/cl/keber`, plus
`cl.keber.architecture` for the ArchUnit rules.

`OtfSisacadApplication` stays at the `cl.keber` root on purpose, so
`@SpringBootApplication` component scanning still covers all three sub-trees.

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

Documentation drifts; tests do not. Every rule above is executable, as ArchUnit
tests in `src/test/java/cl/keber/architecture/ArchitectureTest.java`:

```java
@AnalyzeClasses(packages = "cl.keber",
                importOptions = ImportOption.DoNotIncludeTests.class)
```

They run as part of `mvn clean verify`, so a violation fails the build and the
CI pipeline.

Nine rules, named as they appear in the source:

| Rule | Constraint |
|---|---|
| `domainMustNotDependOnSpring` | no `..domain..` -> `org.springframework..` |
| `domainMustNotDependOnJpaOrHibernate` | no `..domain..` -> `jakarta.persistence..` / `org.hibernate..` |
| `domainMustNotDependOnJackson` | no `..domain..` -> `com.fasterxml.jackson..` |
| `applicationMustNotDependOnInfrastructure` | no `..application..` -> `..infrastructure..` |
| `applicationMustNotDependOnSpring` | no `..application..` -> `org.springframework..` |
| `applicationMustNotDependOnJpaOrSpringData` | no `..application..` -> `jakarta.persistence..` / `org.springframework.data..` |
| `controllersMustNotDependOnPersistenceOrTheRepositoryPort` | no `..web.controller..` -> `..persistence..` / `..domain.repository..` |
| `domainAndApplicationMustNotDependOnWeb` | no `..domain..` / `..application..` -> `..web..` |
| `layersAreRespected` | `layeredArchitecture().consideringAllDependencies()` — infrastructure accessed by nobody, application only by infrastructure, domain only by application and infrastructure |

There is no freeze store and no allowance list. If a rule cannot pass, the fix is
to change the code, not to weaken the rule.

### Two details that matter more than they look

**Only production classes are analysed** (`ImportOption.DoNotIncludeTests`).
Test classes cross layers by design — `@WebMvcTest` mocks use cases,
`@DataJpaTest` drives the adapter — so including them would force per-test
exceptions into the rules and blunt them.

**ArchUnit is pinned at 1.4.1, and the version is not incidental.** Earlier 1.x
releases bundle an ASM that cannot read Java 25 bytecode: they report
`Unsupported class file major version 69`, import **zero** classes, and every
rule then fails as "failed to check any classes". That is the dangerous failure
mode for a rule suite — one that scans nothing can look green forever — so it is
worth knowing that a sudden mass failure of all nine rules after a JDK bump
usually means the analyser cannot read the bytecode, not that the architecture
broke.

The rules were verified adversarially rather than assumed: adding a real
`@Component` annotation to `TrainingProgram` failed `domainMustNotDependOnSpring`
and turned the build red. (Adding only an unused `import` did *not* fail it, and
should not — ArchUnit reads bytecode, and an unused import leaves no reference.)

This is what makes the architecture a *verifiable property of the code* rather
than a picture in a README. If someone puts `@Entity` back on
`domain.model.TrainingProgram`, or injects
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
