# Stage 1 Skeleton Implementation Plan

> For agentic workers: implement inline using superpowers:executing-plans. Steps use checkbox syntax for tracking.

**Goal:** Supply a compiling, integrated Java scaffold with four clear ownership boundaries.

**Architecture:** CLI delegates to GameController, which owns Restaurant. Shared domain signatures follow the approved class diagram; unfinished game transitions fail explicitly.

**Tech Stack:** Java 17, POSIX shell, no third-party dependencies.

**Spec:** docs/superpowers/specs/2026-10-29-stage1-skeleton-design.md

## Global Constraints
- Preserve the base settings and Stage 1 scope from the student brief.
- No unfinished simulation is presented as a complete Stage 1 run.
- No stage-1 tag for this scaffold.
- Keep UI free of domain rules and monetary state in integer cents.

## Review Focus
- EOF and invalid input do not hang the interactive loop.
- Calling start on this scaffold leaves pre-game state untouched.
- Exposed collections cannot mutate internal restaurant state.
- Pending actions fail explicitly, without marking staff as having acted.
- ZIP and GitHub branch contain the same source files, without build output.

## Task 1: Integrated scaffold and smoke test
**Files:** src/main/java/restaurantrush/*.java; src/test/java/restaurantrush/SkeletonSmokeTest.java; scripts/*.sh.

**Interfaces:** ConsoleUI(InputStream, PrintStream); GameController(Restaurant, ConsoleUI).run(); Restaurant(); Menu.find(String); Staff.skipTurn()/resetTurn().

- [ ] Write a runtime smoke test for visible preview state, input retry/EOF, immutable collections, staff skip/reset, and unimplemented start leaving turn 0 unchanged.
- [ ] Run it and observe missing scaffold failure.
- [ ] Implement only preview behavior and documented domain stubs.
- [ ] Run scripts/test.sh; expect all smoke scenarios pass and clean Java compilation.

## Task 2: Handoff and publication
**Files:** README.md, docs/TEAM_GUIDE.md, docs/STAGE1_ACCEPTANCE.md, docs/class-diagram-stage1.mmd, docs/IMPLEMENTATION_STATUS.md.

- [ ] Record exact module ownership, signatures, invariants, integration order and build commands.
- [ ] Record pending victory/defeat evidence and Stage 2/3 transitions honestly.
- [ ] Review scaffold against spec, run full smoke suite, and verify compile-only mode.
- [ ] Publish a feature branch and draft PR using the existing remote main commit as parent.
- [ ] Compare remote tree to local file hashes and make matching ZIP available.
