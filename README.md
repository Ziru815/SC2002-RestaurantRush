# SC2002 Restaurant Rush

Java 17+ command-line restaurant game. No third-party libraries or network access are needed at runtime.

## Current status

- **Stage 1 runnable baseline:** tag [`stage-1`](https://github.com/Ziru815/SC2002-RestaurantRush/releases/tag/stage-1), commit `8d7fa09ddae3053f97f9a7ebe9738f3335740133`, after PR #7. Four suites passed 40/40; a victory and defeat were replayed for all 18 turns. See [saved evidence](docs/evidence/stage1/README.md).
- **Stage 2 implemented:** run `sh scripts/run.sh --stage2`. VIP/Critic behavior, locked pricing and mixed arrivals are integrated; all 72 checks pass. Default launch keeps Stage 1. [Acceptance and replay evidence](docs/STAGE2_ACCEPTANCE.md).
- **Stage 3:** not started. Do not mix Host, ComboMeal or Happy Hour into Stage 2.

## Build and play

```sh
sh scripts/run.sh
# Enter start, then choose Waiter and Chef actions each turn.
sh scripts/run.sh --check
sh scripts/run.sh --stage2
sh scripts/run.sh --stage2 --check
sh scripts/test.sh
```

`test.sh` compiles all main/test sources with Java 17, `-Xlint:all -Werror`, then runs all nine suites: 45 existing checks plus 27 Stage 2 scenarios (72 total), including complete mixed-customer victory and defeat games.

```sh
sh scripts/test-stage2.sh
```

`test-stage2.sh` delegates to `test.sh`; suites run once, and any failure returns a nonzero exit status.

For IDE use, set JDK 17+, main source root `src/main/java`, test root `src/test/java`, entry `restaurantrush.Main`. On Windows use an IDE or Git Bash.

## Team starting points

- [Stage 2 four-person guide and exact contracts](docs/STAGE2_TEAM_GUIDE.md)
- [Stage 2 acceptance checklist](docs/STAGE2_ACCEPTANCE.md)
- [Current implementation status](docs/IMPLEMENTATION_STATUS.md)
- [Submission checklist and responsibilities](docs/SUBMISSION_CHECKLIST.md)

Keep the single normal `src/` tree. All Stage 2 implementation placeholders are resolved. Use [the Stage 2 class diagram](docs/class-diagram-stage2.mmd) for the implemented interfaces.

## Rules retained from Stage 1

18 turns; two tables; one Waiter, Chef and Cashier. Arrivals at 1/4/7/10/13/16. Salad 900 cents / 1 unit, Burger 1500 / 1, Pasta 1800 / 2. Regular loss 8. Regular food choice cycles by ID: Salad, Burger, Pasta. All three targets must hold after turn 18: paid >=4, revenue >=4500 cents, mean satisfaction at payment >=60.

## Stage evidence

Repository: https://github.com/Ziru815/SC2002-RestaurantRush

| Stage/transition | Evidence |
| --- | --- |
| Stage 1 | Fixed `stage-1` tag; replay inputs and results in docs/evidence/stage1 |
| 1 -> 2 | Added VIPCustomer, CriticCustomer, Stage2Arrivals, customer extension/Stage 2 tests and replay inputs. Changed Customer pricing/type hooks, Order lock-at-placement, Waiter/Chef validation, controller/UI/Main stage setup and test scripts. No Stage 1 production classes deleted. [Immutable code comparison](https://github.com/Ziru815/SC2002-RestaurantRush/compare/8d7fa09ddae3053f97f9a7ebe9738f3335740133...2ece569a68a1ed92d3aaa1b05bc14c60cc851b79). |
| 2 -> 3 | Pending; retain Stage 2 before adding Stage 3 features. |

The old README at the immutable Stage 1 tag still says skeleton; these current notes correct that stale description without moving the tag. A final report and accurate diagrams remain separate deliverables.

Scaffold and verification were prepared with AI assistance at Ziru's request. Each member must understand, test and explain their own changes, record actual assistance, retain full required AI histories and complete the course declarations. The scaffold is not four members' completed implementations.
