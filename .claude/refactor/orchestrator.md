# Orchestrator playbook

You coordinate the TrainingProgram Clean Architecture + DDD refactor. You do not
write production code yourself; you sequence work, spin up worker agents in
worktrees, review their PRs against the definition of done, merge in order, and
keep [`STATE.md`](STATE.md) current.

## Inputs

- [`REFACTOR-GUIDE.md`](../../REFACTOR-GUIDE.md) — the design authority.
- [`README.md`](README.md) — dependency graph, waves, WP list.
- [`CONVENTIONS.md`](CONVENTIONS.md) — non-negotiable rules.
- `wp/WP*.md` — the brief for each worker.

## Loop

For each wave in the table in [`README.md`](README.md):

1. **Confirm the gate** for the previous wave is met (PRs merged to `dev`,
   `mvn clean verify` green on `dev`). Update `STATE.md`.
2. **Launch the wave's work packages.** For each one:
   - Create the worktree:
     `.claude/refactor/setup-worktree.ps1 -Branch <wp-branch> -Base <base>`
     (`-Base dev` unless the WP says to stack on another branch).
   - Start a fresh worker agent in that worktree. Give it exactly one
     instruction: *"Execute `.claude/refactor/wp/<file>` to completion. Follow
     `.claude/refactor/CONVENTIONS.md`. Stop and report if you need to touch
     anything outside the WP's 'Files in scope'."*
   - Do not give one worker two packages.
3. **When a worker reports done**, review its branch:
   - Diff is limited to the WP's declared scope.
   - `mvn clean verify` green (apply the JDK caveat from CONVENTIONS).
   - Characterization tests from WP1 still pass unchanged.
   - Commits are English, conventional style, no agent attribution, `.gitlint`
     limits respected.
   - Handoff note present in `STATE.md`.
4. **Merge** to `dev` (squash or merge per repo norm; keep history readable).
   Re-run `mvn clean verify` on `dev`.
5. **Rebase any in-flight sibling branch** onto the new `dev` and tell that
   worker to re-verify. (Relevant in Wave 4: after merging WP5, rebase WP6, and
   vice versa.)
6. **Tear down** merged worktrees: `git worktree remove`, `git branch -d`.

## Wave notes

- **Wave 0:** WP1 is the safety net — nothing else in the spine starts until it
  is merged. WP-DOCS starts now and runs continuously; it only edits `docs/**`
  and `README.md`, so it never blocks and never conflicts with code waves.
- **Wave 4 (WP5 ∥ WP6):** the one true parallel window. Base both on the merged
  WP4 state of `dev`. Expect zero file overlap; if a worker reports overlap,
  something is mis-scoped — investigate before merging.
- **Wave 5 (WP7):** brings the wiring together (`@Configuration` beans,
  controller on use cases). This is where the app is proven end-to-end again.
- **Wave 6 (WP8 + WP-DOCS finish):** enable ArchUnit, delete legacy packages,
  merge docs, walk the rubric checklist in `STATE.md`.

## If something goes wrong

- A wave gate fails to go green on `dev` after a merge → revert that merge,
  reopen the WP with a defect note, do not start the next wave.
- Two workers need the same file → the later one waits; re-scope if the overlap
  is structural.
- A worker proposes a design change vs. the guide → you decide; record the
  decision and rationale in `STATE.md` under "Decisions".

## Done

All WPs merged, `dev` green, and the rubric checklist in `STATE.md` shows
4 / 4 / 4 with evidence (package layout, ArchUnit rules passing, VO + entity
tests, port + adapter, use cases receiving the port by constructor).
