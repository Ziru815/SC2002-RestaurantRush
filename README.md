# SC2002 Restaurant Rush

Java 17+ command-line restaurant game. No third-party libraries or network access are needed at runtime.

## Current status

- **Stage 1 runnable baseline:** tag [`stage-1`](https://github.com/Ziru815/SC2002-RestaurantRush/releases/tag/stage-1), commit `8d7fa09ddae3053f97f9a7ebe9738f3335740133`, after PR #7. Four suites passed 40/40; a victory and defeat were replayed for all 18 turns. See [saved evidence](docs/evidence/stage1/README.md).
- **This branch: Stage 2 scaffold only.** The default game still runs Stage 1. VIP/Critic behavior, pricing integration and mixed arrivals are intentionally TODO; `--stage2` is not enabled yet. Stage 2 tests must report pending/failure until the owners implement them.
- **Stage 3:** not started. Do not mix Host, ComboMeal or Happy Hour into Stage 2.

## Build and play

```sh
sh scripts/run.sh
# Enter start, then choose Waiter and Chef actions each turn.
sh scripts/run.sh --check
sh scripts/test.sh
```

`test.sh` compiles all main/test sources with Java 17, `-Xlint:all -Werror`, and runs the four existing suites plus five scaffold extension checks. A green result certifies those checks, not Stage 2 completion.

```sh
sh scripts/test-stage2.sh
```

This also runs four owner-specific Stage 2 suites. They deliberately fail as pending in the scaffold. Each owner replaces their suite placeholder with meaningful tests. Once Stage 2 is ready, D must include these suites in the normal test command without introducing script recursion.

For IDE use, set JDK 17+, main source root `src/main/java`, test root `src/test/java`, entry `restaurantrush.Main`. On Windows use an IDE or Git Bash.

## Team starting points

- [Stage 2 four-person guide and exact contracts](docs/STAGE2_TEAM_GUIDE.md)
- [Stage 2 acceptance checklist](docs/STAGE2_ACCEPTANCE.md)
- [Current implementation status](docs/IMPLEMENTATION_STATUS.md)
- [Submission checklist and responsibilities](docs/SUBMISSION_CHECKLIST.md)
- [English group message to copy](docs/STAGE2_GROUP_MESSAGE.txt)

Find work with `rg 'TODO\[[ABCD]-S2\]' src` or your IDE's search. Keep the single normal `src/` tree. No nested ZIP uploads, fake authorship or fabricated student contributions.

## Rules retained from Stage 1

18 turns; two tables; one Waiter, Chef and Cashier. Arrivals at 1/4/7/10/13/16. Salad 900 cents / 1 unit, Burger 1500 / 1, Pasta 1800 / 2. Regular loss 8. Regular food choice cycles by ID: Salad, Burger, Pasta. All three targets must hold after turn 18: paid >=4, revenue >=4500 cents, mean satisfaction at payment >=60.

## Stage evidence

Repository: https://github.com/Ziru815/SC2002-RestaurantRush

| Stage/transition | Evidence |
| --- | --- |
| Stage 1 | Fixed `stage-1` tag; replay inputs and results in docs/evidence/stage1 |
| 1 -> 2 | Preparation only. Customer extension methods + subtype/factory stubs + owned test entry points + docs. Order and controller behavior unchanged. [Working comparison](https://github.com/Ziru815/SC2002-RestaurantRush/compare/stage-1...work/stage2-scaffold). D records final added/changed/deleted files and reasons once implemented. |
| 2 -> 3 | Pending; retain Stage 2 before adding Stage 3 features. |

The old README at the immutable Stage 1 tag still says skeleton; these current notes correct that stale description without moving the tag. A final report and accurate diagrams remain separate deliverables.

Scaffold and verification were prepared with AI assistance at Ziru's request. Each member must understand, test and explain their own changes, record actual assistance, retain full required AI histories and complete the course declarations. The scaffold is not four members' completed implementations.
