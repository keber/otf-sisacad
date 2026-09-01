# WP-DOCS — Architecture documentation

**Maps to:** Guide Stage 11
**Worktree branch:** `refactor/wp-docs` from `dev`
**Depends on:** nothing to start; final pass needs WP7 merged.
**Runs in parallel with:** everything. Only edits `docs/**` and `README.md`, so
it never conflicts with a code work package.

## Objective

Document the target architecture so the deliberate nature of the change is
visible, without deleting the historical per-task docs.

## Working style

- Start immediately using `REFACTOR-GUIDE.md` as the source of truth for the
  *target* design. Draft everything.
- After each spine WP merges, the orchestrator pings you; reconcile the docs
  with what actually landed (class names, packages, method names).
- Final reconciliation pass after WP7 merges. Then the docs merge in Wave 6
  (standalone PR, or folded into WP8 — orchestrator decides).

## Deliverables

Create `docs/architecture/`:

- **`clean-architecture.md`** — the layering, the dependency rule
  ("dependencies point inward; the domain does not know Spring, HTTP,
  PostgreSQL, Hibernate or JPA exist"), and why package separation (not
  multi-module Maven) is sufficient. Include the target diagram from the guide.
- **`package-dependencies.md`** — the `cl.keber.{domain,application,
  infrastructure}` tree with the final class list per package, and the allowed
  dependency directions. Reference the ArchUnit rules (WP8) that enforce it.
- **`domain-model.md`** — `TrainingProgram` entity (factories `create` /
  `restore`, lifecycle methods `rename` / `reschedule` / `changeStatus`), the
  Value Objects (`TrainingProgramCode`, `TrainingProgramName`,
  `TrainingPeriod`, `TrainingProgramStatus`) and their invariants, and the
  `TrainingProgramRepository` port vs. the `JpaTrainingProgramRepositoryAdapter`.
- **`persistence.md`** (short) — domain `TrainingProgram` vs.
  `TrainingProgramJpaEntity`, the mapper, the adapter, and the fact that the
  `training_program` table/columns are unchanged (no new Flyway migration).

Update:

- **`README.md`** — add an "Architecture" section linking to
  `docs/architecture/*` and stating the Milestone 3 note:
  > From Milestone 3, the domain model is separated from the JPA
  > representation. `TrainingProgram` is no longer a persistence entity; JPA
  > lives in `infrastructure.persistence`. The REST contract under `/programs`
  > is unchanged.
- **`docs/106.md` … `docs/112.md`** — do **not** rewrite history. Add a short
  banner at the top of any doc that describes `TrainingProgram` as a JPA
  entity:
  > **Superseded by Milestone 3.** This task is historically accurate.
  > `TrainingProgram` is no longer a JPA entity — see
  > `docs/architecture/clean-architecture.md`.
- **`docs/diag-class.md`, `docs/diag-er.md`** — update the class diagram to
  show domain vs. JPA entity and the port/adapter; the ER diagram is unchanged
  (schema did not change) — just confirm it still matches.

## Language / attribution

English throughout. No agent attribution in commits (see `CONVENTIONS.md`).

## Files in scope

- `docs/**`
- `README.md`
- `.claude/refactor/STATE.md` (handoff note)

Nothing under `src/`.

## Definition of done

- `docs/architecture/` has the four files, consistent with merged code.
- README links to them and carries the Milestone 3 note.
- Historical task docs carry the "superseded" banner but keep their content.
- Diagrams reflect the domain/JPA split.
- Handoff note in `STATE.md`.

## Commit plan

- `docs: add clean architecture and package dependency documentation`
- `docs: document the TrainingProgram domain model and repository port`
- `docs: mark JPA-entity task docs as superseded by Milestone 3`
- `docs: update class diagram for the domain / JPA entity split`
