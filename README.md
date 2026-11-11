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
| 1 -> 2 | Tag `stage-2`. Files added and changed, with reasons, in [Stage 1 to Stage 2](#stage-1-to-stage-2) below. |
| 2 -> 3 | Pending; retain Stage 2 before adding Stage 3 features. |

## Stage 1 to Stage 2

Compare: [`stage-1...stage-2`](https://github.com/Ziru815/SC2002-RestaurantRush/compare/8d7fa09ddae3053f97f9a7ebe9738f3335740133...93fc50663056681469000208941ace992ccbb5a0) (tag commits `8d7fa09` to `93fc506`).

Stage 2 adds VIP and Critic customers. Each new type is a subclass of `Customer` that overrides the behaviour it changes, so the controller never checks a customer's type.

New production files:

| File | Purpose |
| --- | --- |
| `VIPCustomer.java` | Pays 90% of the menu price and loses 5 satisfaction per waiting turn. |
| `CriticCustomer.java` | Always chooses Pasta, pays full price and loses 12 satisfaction per waiting turn. |
| `Stage2Arrivals.java` | Builds the six arrivals for Stage 2: ID 2 is VIP, ID 4 is Critic, the others are Regular. |

Existing files changed:

| File | Change | Reason |
| --- | --- | --- |
| `Customer.java` | Added `priceFor(item)` and `getTypeLabel()`. | Gives each customer type a method to override for its price, and a label for the screen only. Regular behaviour is unchanged. |
| `Order.java` | The bill is now `customer.priceFor(item)`, saved once when the order is created. | Moves the discount to the customer object and locks the price at order time, as the brief requires. Stage 1 bills stay the same because Regular customers pay the menu price. |
| `Cashier.java` | Comments only. | Records that payment uses the locked bill and never applies a discount a second time. |
| `Waiter.java`, `Chef.java` | Extra checks that the customer still holds the table and still owns the order. | Stops an order for a customer who left or moved from being taken, cooked or served. Rejected actions still do not use the staff member's turn. |
| `GameController.java` | Takes an arrival factory (`IntFunction<Customer>`); the old two-argument constructor still creates Regular customers. | Lets each stage choose who arrives without the controller knowing the customer types. |
| `ConsoleUI.java` | Takes a stage label for the title and shows each customer's type outside Stage 1. | Display only; the screen makes no game decisions from the type. |
| `Main.java` | Added `--stage2`. With no option the game is Stage 1. | Keeps Stage 1 playable on its own, as the brief requires. |
| `scripts/test.sh` | Runs the five new test suites. `scripts/test-stage2.sh` is new and calls `test.sh`. | One command runs every check for both stages. |

Tests added: `CustomerExtensionTests`, `Stage2CustomerTests`, `Stage2OrderTests`, `Stage2StaffTests`, `Stage2ControllerTests`, and the replay inputs `stage2-victory.input.txt` and `stage2-defeat.input.txt`.

Deleted: no production or test files. Internal planning notes under `docs/superpowers/` were removed because they were working notes, not part of the submission.

Stage 1 still works: `sh scripts/run.sh` with no option plays the Stage 1 game, and its four original test suites still pass in `sh scripts/test.sh`.

The old README at the immutable Stage 1 tag still says skeleton; these current notes correct that stale description without moving the tag. A final report and accurate diagrams remain separate deliverables.

Scaffold and verification were prepared with AI assistance at Ziru's request. Each member must understand, test and explain their own changes, record actual assistance, retain full required AI histories and complete the course declarations. The scaffold is not four members' completed implementations.
