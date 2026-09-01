# Clean Architecture + DDD refactor — agent work plan

This directory turns [`REFACTOR-GUIDE.md`](../../REFACTOR-GUIDE.md) into a set of
self-contained work packages that independent agents can execute, most of them in
their own git worktree, coordinated by an orchestrator.

## Files

| File | Purpose |
|---|---|
| [`orchestrator.md`](orchestrator.md) | Playbook for the coordinating agent. Start here. |
| [`CONVENTIONS.md`](CONVENTIONS.md) | Rules every agent must follow (commits, attribution, verification gate, worktree hygiene). |
| [`STATE.md`](STATE.md) | Living progress board. The orchestrator owns it; workers read it. |
| [`settings.template.json`](settings.template.json) | Drop-in `.claude/settings.json` for each worktree. |
| [`setup-worktree.ps1`](setup-worktree.ps1) / [`setup-worktree.sh`](setup-worktree.sh) | Create a worktree and seed its `.claude/settings.json`. |
| `wp/WP*.md` | One work package per unit of work. Hand exactly one to a fresh agent. |

## Scope

Only the **TrainingProgram vertical slice** is refactored. It becomes the
architectural template for future aggregates. The frontend is already
anglicised and is out of scope; the REST contract under `/programs`
(POST / GET / PUT `/{id}` / DELETE `/{id}`) must not change.

Base branch for every work package: **`dev`**. Package base: `cl.keber`.
Build/verify command: `mvn clean verify` (see CONVENTIONS for the JDK note).

## Work packages

| WP | Title | Maps to guide | Worktree branch |
|---|---|---|---|
| [WP1](wp/WP1-baseline.md) | Baseline + characterization tests | Stage 0 / PR 1 | `refactor/wp1-baseline` |
| [WP2](wp/WP2-boundaries.md) | Create `domain` / `application` / `infrastructure` packages (move-only) | Stage 1 / PR 2 | `refactor/wp2-boundaries` |
| [WP3](wp/WP3-domain.md) | Value Objects + pure domain entity | Stage 2 / PR 3 | `refactor/wp3-domain` |
| [WP4](wp/WP4-repository-port.md) | Pure `TrainingProgramRepository` port | Stage 3 / PR 4 | `refactor/wp4-repository-port` |
| [WP5](wp/WP5-use-cases.md) | Use cases + application service (framework-free) | Stage 4 / PR 5 | `refactor/wp5-use-cases` |
| [WP6](wp/WP6-persistence.md) | JPA entity + Spring Data repo + persistence adapter | Stage 5 / PR 6 | `refactor/wp6-persistence` |
| [WP7](wp/WP7-web.md) | Controller + DTO wired to use cases, Spring wiring config | Stage 6–7 / PR 7 | `refactor/wp7-web` |
| [WP8](wp/WP8-archunit-cleanup.md) | ArchUnit rules, legacy removal, rubric check | Stage 9–10 / PR 8 | `refactor/wp8-archunit-cleanup` |
| [WP-DOCS](wp/WP-DOCS-architecture.md) | Architecture documentation | Stage 11 | `refactor/wp-docs` |

## Dependency graph and parallelism

```text
        ┌─────────────────────────────────────────────── WP-DOCS ──────────────┐
        │  (runs in parallel with WP3..WP7, merges in the final wave)          │
        └────────────────────────────────────────────────────────────────────┘

  WP1 ──▶ WP2 ──▶ WP3 ──▶ WP4 ──┬──▶ WP5 ──┐
                                └──▶ WP6 ──┴──▶ WP7 ──▶ WP8
```

- **Sequential spine:** WP1 → WP2 → WP3 → WP4, then WP7 → WP8.
- **Real parallel window:** after WP4 merges, **WP5 and WP6 run at the same
  time** in separate worktrees. They touch disjoint packages
  (`application/**` vs `infrastructure/persistence/**`) and share only the
  already-merged domain + port.
- **WP-DOCS** runs in its own worktree from the moment WP2's target package
  layout is fixed; it only edits `docs/**` and `README.md`, so it never
  conflicts with code packages. It merges in the last wave.
- WP8 may **draft** its ArchUnit rules early in the WP-DOCS worktree or a
  scratch worktree, but the rules are only wired into the build once every
  spine package has merged.

## Execution waves

| Wave | Runs | Gate to advance |
|---|---|---|
| 0 | WP1, WP-DOCS (start) | WP1 merged to `dev`, green |
| 1 | WP2 | merged, green, no behaviour change |
| 2 | WP3 | merged, green |
| 3 | WP4 | merged, green |
| 4 | WP5 ∥ WP6 | both merged to `dev`, green after each merge |
| 5 | WP7 | merged, green, REST contract unchanged |
| 6 | WP8, WP-DOCS (finish) | merged, green, rubric 4/4/4 checklist complete |
