# WP8 — ArchUnit rules, legacy removal, rubric close-out

**Maps to:** Guide Stage 9–10 / PR 8
**Worktree branch:** `refactor/wp8-archunit-cleanup` from `dev` (after WP7 merged)
**Depends on:** WP1–WP7.
**Runs in parallel with:** WP-DOCS finish (docs merge can be folded into this PR or kept separate — orchestrator decides).

## Objective

Make the architecture an executable constraint, delete dead code, and verify the
rubric is 4 / 4 / 4 with evidence.

## Tasks

1. **Add ArchUnit** to `pom.xml` (test scope only):

   ```xml
   <dependency>
     <groupId>com.tngtech.archunit</groupId>
     <artifactId>archunit-junit5</artifactId>
     <version>1.3.0</version>
     <scope>test</scope>
   </dependency>
   ```

   This is the only allowed `pom.xml` change in the whole plan. Do not touch
   `java.version`.

2. **`src/test/java/cl/keber/architecture/ArchitectureTest.java`**
   (`@AnalyzeClasses(packages = "cl.keber")`), rules:
   - `domain` must not depend on `org.springframework..`.
   - `domain` must not depend on `jakarta.persistence..` /
     `jakarta.persistence.*` / Hibernate.
   - `domain` must not depend on `com.fasterxml.jackson..`.
   - `application` must not depend on `..infrastructure..`.
   - `application` must not depend on `org.springframework..` and not on
     `jakarta.persistence..` / `org.springframework.data..`.
   - classes in `..web.controller..` must not depend on
     `..persistence..` or `..domain.repository..` (controllers never see
     repositories).
   - `..domain..` and `..application..` must not depend on `..web..`.
   - layered-architecture check: `domain` accessed by `application` +
     `infrastructure`; `application` accessed by `infrastructure`;
     `infrastructure` accessed by nothing.
   - `freeze`/allowance list: none — the slice should be fully clean by now. If
     a rule cannot pass, stop and report; do not weaken the rule.

3. **Remove legacy code** — only what is now genuinely unused:
   - old `cl.keber.model` / `cl.keber.repository` / `cl.keber.service`
     packages if anything still lingered (should already be gone after WP2–WP7;
     confirm with a grep).
   - any superseded mapper/bridge classes and their tests.
   - dead imports.

4. **Rubric close-out** — walk the checklist at the bottom of
   [`../STATE.md`](../STATE.md), tick each box, and paste the evidence
   (grep output, ArchUnit test names, key class list) under each item.

5. **Docs** — if WP-DOCS is being merged here, confirm
   `docs/architecture/*.md` matches the final code and `README.md` links to it.
   Add the milestone note from the guide's Stage 11:
   > From Milestone 3, the domain model is separated from the JPA
   > representation. `TrainingProgram` is no longer a persistence entity; JPA
   > lives in `infrastructure.persistence`.

## Files in scope

- `pom.xml` (ArchUnit dependency only)
- `src/test/java/cl/keber/architecture/**` (new)
- deletions across `src/main` / `src/test` for confirmed-dead code
- `docs/**`, `README.md` (if folding WP-DOCS in)
- `.claude/refactor/STATE.md` (checklist + handoff note)

## Definition of done

- `mvn clean verify` green with ArchUnit rules active and passing.
- No `cl.keber.model` / `cl.keber.repository` / `cl.keber.service` packages
  remain; grep for `JpaRepository`, `@Entity`, `jakarta.persistence` shows hits
  **only** under `infrastructure.persistence`.
- Rubric checklist fully ticked with evidence.
- All WP1 characterization tests still pass unchanged.

## Commit plan

- `test: add ArchUnit rules enforcing the layer boundaries`
- `chore: remove superseded legacy classes and tests`
- `docs: add the Milestone 3 architecture note` (if docs folded in)
