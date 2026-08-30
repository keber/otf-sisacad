# WP2 — Architectural boundaries (move-only)

**Maps to:** Guide Stage 1 / PR 2
**Worktree branch:** `refactor/wp2-boundaries` from `dev` (after WP1 merged)
**Depends on:** WP1.
**Runs in parallel with:** WP-DOCS.

## Objective

Reorganise the module from technical layers (`model`, `repository`, `service`,
`controller`, `dto`, `mapper`, `exception`, `config`) into architectural
packages. **No behaviour change, no logic change** — pure moves + import fixes.
This WP deliberately leaves technological contamination in place (the entity
still has `@Entity`, the repo still extends `JpaRepository`). Later WPs purify.

## Target package layout (create the tree; only some leaves are filled now)

```text
cl.keber
├── domain
│   ├── model            <- TrainingProgram (still @Entity for now)
│   ├── valueobject      <- (empty; filled in WP3)
│   ├── repository       <- (empty; port added in WP4)
│   └── exception        <- TrainingProgramNotFoundException
├── application
│   ├── usecase          <- (empty; WP5)
│   ├── command          <- (empty; WP5)
│   └── service          <- TrainingProgramService (still @Service for now)
└── infrastructure
    ├── persistence
    │   ├── repository   <- TrainingProgramRepository (still extends JpaRepository)
    │   ├── entity       <- (empty; WP6)
    │   ├── adapter      <- (empty; WP6)
    │   └── mapper       <- (empty; WP6)
    ├── web
    │   ├── controller   <- TrainingProgramController
    │   ├── dto          <- TrainingProgramDto
    │   └── mapper       <- TrainingProgramMapper
    └── config           <- WebConfig
```

`OtfSisacadApplication` stays at `cl.keber` root. Confirm
`@SpringBootApplication` component scanning still covers all three new
sub-trees (it does — same base package).

## Tasks

1. `git mv` each class to its target package. Update `package` declarations and
   every `import`. Move the matching test class to the mirror test package.
2. Keep class names identical for now (renames happen in later WPs). Exception:
   none in this WP.
3. Adjust `TrainingProgramControllerTest` / `...RepositoryTest` /
   `...ServiceTest` / `...MapperTest` / `...DtoTest` / `WebConfigTest` /
   characterization test imports only.
4. Do not touch `pom.xml`, Flyway files, or `application.properties`.
5. `mvn clean verify` green. Diff should be almost entirely `package`/`import`
   lines plus path changes.

## Files in scope

- `src/main/java/cl/keber/**` (moves + package/import lines)
- `src/test/java/cl/keber/**` (moves + package/import lines)
- `.claude/refactor/STATE.md` (handoff note)

## Definition of done

- Every class lives under `domain`, `application`, or `infrastructure`.
- Zero behaviour change; all WP1 characterization tests pass unchanged.
- `mvn clean verify` green.
- Handoff note lists the final package of every moved class so later WPs have
  exact targets.

## Commit plan

One commit per architectural package is fine, e.g.:

- `refactor: move web layer into infrastructure.web package`
- `refactor: move persistence classes into infrastructure.persistence`
- `refactor: move TrainingProgram and exception into domain package`
- `refactor: move TrainingProgramService into application.service package`
