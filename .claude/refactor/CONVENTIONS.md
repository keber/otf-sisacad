# Conventions — read before starting any work package

These rules apply to **every** agent (orchestrator and workers). A work package
file may add constraints; it may not relax these.

## Language

- All commit messages, branch descriptions, PR text, code comments, Javadoc,
  test names, and documentation are written in **English**.
- Identifiers are English. The public REST path stays `/programs`; visible
  Spanish UI copy in the frontend is out of scope and untouched.

## Attribution — commits are not attributed to Claude or any agent

- Every worktree must contain `.claude/settings.json` with exactly:

  ```json
  {
    "attribution": {
      "commit": "",
      "pr": "",
      "sessionUrl": false
    }
  }
  ```

  Copy it from [`settings.template.json`](settings.template.json). The
  `setup-worktree` scripts do this for you.
- Do **not** add `Co-authored-by:`, `Generated with…`, `🤖`, or any agent/tool
  trailer or footer to commits or PRs.
- `git config user.name` / `user.email` are inherited from the main checkout
  (currently `Keber Flores`). Do not change them.

## Commit style

- Conventional-commit subject: `feat:`, `refactor:`, `chore:`, `test:`,
  `docs:`, `fix:`. Match the existing history.
- `.gitlint`: subject ≤ 100 chars, body lines ≤ 120 chars.
- Small, focused, reversible commits. Each commit compiles.
- Prefer `git mv` for file moves/renames so history follows the file.
- Example:

  ```text
  refactor: move TrainingProgram into domain package

  Pure move with no behaviour change. Imports updated across the module.
  Characterization tests untouched and still green.
  ```

## Verification gate — every work package ends green

Run before opening the PR and again after any rebase:

```bash
mvn clean verify
```

- All previously passing tests still pass. New tests for the package pass.
- Do not delete or weaken a test to make the build green. If a test encodes
  behaviour that the refactor deliberately changes (e.g. the domain no longer
  carries JPA annotations), **move** the assertion to the layer that now owns
  it and note the move in the commit body.
- JDK note: the project targets Java 25 (`pom.xml`, CI uses Temurin 25). If the
  local JDK is older, build with `-Dmaven.compiler.release=21` locally and say
  so in the PR; never edit `pom.xml`'s `java.version` to work around it. No
  code may use Java 22+ language features.
- Tests named in `DatabaseMigrationTest` / `OtfSisacadApplicationTests` connect
  to the shared Railway database. Run them only when your change could affect
  persistence or app startup; otherwise scope your local run (e.g.
  `mvn test -Dtest='!DatabaseMigrationTest,!OtfSisacadApplicationTests'`) and
  let CI run the full suite. Never point local tests at the shared DB casually.

## Worktree hygiene

- One work package = one worktree = one branch = one PR.
- Branch from the up-to-date base (`dev` unless the WP says otherwise).
- Keep the worktree focused: touch only the paths your WP lists under
  "Files in scope". If you must touch something else, stop and tell the
  orchestrator.
- When the PR is merged, remove the worktree:
  `git worktree remove <path>` and `git branch -d <branch>`.
- Never commit `target/`, `.env`, `node_modules/`, or `.claude/settings.json`
  (the last is intentionally untracked — it stays local to each worktree).

## Handoff

- Each WP ends with a short **handoff note** appended to its section in
  [`STATE.md`](STATE.md): branch, PR link, what changed, anything the next WP
  must know (new package names, moved tests, follow-ups).
- The orchestrator merges; workers do not merge their own PRs unless the
  orchestrator delegates it.

## Definition of done (applies to every WP)

1. `mvn clean verify` green (with the JDK caveat above).
2. REST contract under `/programs` unchanged (verified by the characterization
   tests from WP1).
3. Only in-scope paths changed.
4. Commits follow the style above and carry no agent attribution.
5. Handoff note added to `STATE.md`.
