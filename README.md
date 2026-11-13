# SC2002 Restaurant Rush

Java 17+ command-line restaurant game. No third-party libraries or network access are needed at runtime.

## Current status

- **Stage 1 runnable baseline:** tag [`stage-1`](https://github.com/Ziru815/SC2002-RestaurantRush/releases/tag/stage-1), commit `8d7fa09ddae3053f97f9a7ebe9738f3335740133`, after PR #7. Four suites passed 40/40; a victory and defeat were replayed for all 18 turns. See [saved evidence](docs/evidence/stage1/README.md).
- **Stage 2 implemented:** run `sh scripts/run.sh --stage2`. VIP/Critic behavior, locked pricing and mixed arrivals are integrated; all 72 checks pass. Default launch keeps Stage 1. [Acceptance and replay evidence](docs/STAGE2_ACCEPTANCE.md).
- **Stage 3 implemented:** tag [`stage-3`](https://github.com/Ziru815/SC2002-RestaurantRush/releases/tag/stage-3), commit `51721224e62392ad4e6ee115d5bbe8b932b9f9e1`, after PR #18. Run `sh scripts/run.sh --stage3`. Adds the Combo meal, Happy Hour and the Host. All 111 checks pass. [Acceptance and replay evidence](docs/STAGE3_ACCEPTANCE.md).

## Build and play

```sh
sh scripts/run.sh
# Enter start, then choose Waiter and Chef actions each turn.
sh scripts/run.sh --check
sh scripts/run.sh --stage2
sh scripts/run.sh --stage2 --check
sh scripts/run.sh --stage3
sh scripts/run.sh --stage3 --check
sh scripts/test.sh
```

`test.sh` compiles all main/test sources with Java 17, `-Xlint:all -Werror`, then runs all 13 suites: 40 Stage 1 checks, 32 checks added in Stage 2 and 39 added in Stage 3 (111 total; the running totals at the three tags are 40, 72 and 111), including complete victory and defeat games for Stage 2 and Stage 3.

```sh
sh scripts/test-stage2.sh
```

`test-stage2.sh` delegates to `test.sh`; suites run once, and any failure returns a nonzero exit status.

For IDE use, set JDK 17+, main source root `src/main/java`, test root `src/test/java`, entry `restaurantrush.Main`. On Windows use an IDE or Git Bash.

## Team starting points

- [Stage 2 four-person guide and exact contracts](docs/STAGE2_TEAM_GUIDE.md)
- [Stage 2 acceptance checklist](docs/STAGE2_ACCEPTANCE.md)
- [Stage 3 four-person guide](docs/STAGE3_TEAM_GUIDE.md) and [interfaces](docs/STAGE3_INTERFACES.md)
- [Stage 3 acceptance checklist](docs/STAGE3_ACCEPTANCE.md)
- [Current implementation status](docs/IMPLEMENTATION_STATUS.md)
- [Submission checklist and responsibilities](docs/SUBMISSION_CHECKLIST.md)

Keep the single normal `src/` tree. Class diagrams for each stage: [Stage 1](docs/class-diagram-stage1.mmd), [Stage 2](docs/class-diagram-stage2.mmd), [Stage 3 PDF](docs/Stage3-Class-Diagram.pdf) ([Mermaid source](docs/class-diagram-stage3.mmd)).

## Rules retained from Stage 1

18 turns; two tables; one Waiter, Chef and Cashier. Arrivals at 1/4/7/10/13/16. Salad 900 cents / 1 unit, Burger 1500 / 1, Pasta 1800 / 2. Regular loss 8. Regular food choice cycles by ID: Salad, Burger, Pasta. All three targets must hold after turn 18: paid >=4, revenue >=4500 cents, mean satisfaction at payment >=60.

## Stage evidence

Repository: https://github.com/Ziru815/SC2002-RestaurantRush

| Stage/transition | Evidence |
| --- | --- |
| Stage 1 | Fixed `stage-1` tag; replay inputs and results in docs/evidence/stage1 |
| 1 -> 2 | Tag `stage-2`. Files added and changed, with reasons, in [Stage 1 to Stage 2](#stage-1-to-stage-2) below. |
| 2 -> 3 | Tag `stage-3`. Files added and changed, with reasons, in [Stage 2 to Stage 3](#stage-2-to-stage-3) below. |

Sequence diagram (Stage 2): [docs/Stage2SequenceDiagram.png](docs/Stage2SequenceDiagram.png). One manager playthrough with `--stage2`, from arrival and VIP order pricing through serving, payment and the final result check.

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
| `Waiter.java`, `Chef.java` | Waiter checks table occupancy when taking/serving orders; Chef checks that the customer is seated and still owns the order. | Rejects invalid actions without using a staff task. |
| `GameController.java` | Takes an arrival factory (`IntFunction<Customer>`); the old two-argument constructor still creates Regular customers. | Lets each stage choose who arrives without the controller knowing the customer types. |
| `ConsoleUI.java` | Takes a stage label for the title and shows each customer's type outside Stage 1. | Display only; the screen makes no game decisions from the type. |
| `Main.java` | Added `--stage2`. With no option the game is Stage 1. | Keeps Stage 1 playable on its own, as the brief requires. |
| `scripts/test.sh` | Runs the five new test suites. `scripts/test-stage2.sh` is new and calls `test.sh`. | One command runs every check for both stages. |

Tests added: `CustomerExtensionTests`, `Stage2CustomerTests`, `Stage2OrderTests`, `Stage2StaffTests`, `Stage2ControllerTests`, and the replay inputs `stage2-victory.input.txt` and `stage2-defeat.input.txt`.

Deleted: no production or test files. Internal planning notes under `docs/superpowers/` were removed because they were working notes, not part of the submission.

Stage 1 still works: `sh scripts/run.sh` with no option plays the Stage 1 game, and its four original test suites still pass in `sh scripts/test.sh`.

## Stage 2 to Stage 3

Compare: [`stage-2...stage-3`](https://github.com/Ziru815/SC2002-RestaurantRush/compare/93fc50663056681469000208941ace992ccbb5a0...51721224e62392ad4e6ee115d5bbe8b932b9f9e1) (tag commits `93fc506` to `5172122`).

Stage 3 adds a Combo meal, a once-per-game Happy Hour and a Host who seats customers. The Stage 2 customers (VIP ID 2, Critic ID 4) and arrival turns are reused unchanged. Stage 3 is switched on by one flag, `Restaurant(true)`, so the same controller runs all three stages.

New production files:

| File | Purpose |
| --- | --- |
| `ComboMeal.java` | A `MenuItem` made from Salad and Pasta: 2430 cents (10% off 2700) and 3 preparation units. Built from its parts, so the Waiter, Chef and Cashier treat it like any other dish. |
| `HappyHourPolicy.java` | Can be started once per game and lasts that turn and the next. While active, new orders cost 80% of the customer's own price, so a VIP pays 90% first and then 80% (Burger 1500 to 1350 to 1080 cents). |
| `Host.java` | A new `Staff` member with one task per turn: seat a queued customer. The Waiter can still seat, so two customers can be seated in one turn. |

Existing files changed:

| File | Change | Reason |
| --- | --- | --- |
| `Menu.java` | New `Menu(boolean includeCombo)`, `hasCombo()` and `getCombo()`. `Menu()` still gives the three dishes. | Only the Stage 3 menu offers the combo, so Stages 1 and 2 are unchanged. |
| `Customer.java` | Added `restoreWaitingSatisfaction(amount)`, capped at 100 and only for QUEUED or SEATED customers. Regular customer ID 5 picks the combo when the menu has one. | Happy Hour helps customers who are still waiting, through the customer's own method instead of the controller changing satisfaction directly. |
| `Order.java` | New constructor that takes an already final price, `Order(customer, item, lockedPriceCents)`. | The Happy Hour price is worked out once when the order is taken and locked, so later turns cannot change the bill. |
| `Waiter.java` | New `takeOrder(customer, menu, policy, turn)`. Both versions share one private `chooseOrderItem` check. | Stage 3 orders are priced through the policy; the Stage 1 and 2 path stays the same. |
| `Restaurant.java` | New `Restaurant(boolean stage3Enabled)` that creates `Menu(true)`, one Host and one Happy Hour policy. `getHost()` and `getHappyHour()` refuse outside Stage 3. | Stage 3 parts exist only in Stage 3, and asking for them in an earlier stage is an error instead of a silent null. |
| `GameController.java` | In Stage 3 each turn asks the Manager (`happyhour` or `wait`) until Happy Hour is used, gives +15 to waiting customers on each active turn, then asks the Host before the Waiter. Seating by Host and Waiter shares one helper. | Matches the Stage 3 turn order in the team guide. The controller checks `isStage3Enabled()`, never the screen label. |
| `ConsoleUI.java` | In Stage 3 shows the Host line, the Happy Hour status and the Manager and Host commands in help. | Display only. |
| `Main.java` | Added `--stage3`; `--stage2` and `--stage3` together are rejected. | Each stage can still be launched on its own. |
| `scripts/test.sh` | Runs the four new test suites. | One command runs every check for all three stages. |

Tests added: `Stage3CustomerMenuTests` (7), `Stage3PricingTests` (7), `Stage3HostStaffTests` (13), `Stage3ControllerTests` (12), and the replay inputs `stage3-victory.input.txt` and `stage3-defeat.input.txt`.

Deleted: no files.

Stages 1 and 2 still work: `sh scripts/run.sh` and `sh scripts/run.sh --stage2` play the earlier games, their test suites still pass, and the Stage 2 victory and defeat replays give the same results as at `stage-2`.

The old README at the immutable Stage 1 tag still says skeleton; these current notes correct that stale description without moving the tag. A final report and accurate diagrams remain separate deliverables.

Scaffold and verification were prepared with AI assistance at Ziru's request. Each member must understand, test and explain their own changes, record actual assistance, retain full required AI histories and complete the course declarations. The scaffold is not four members' completed implementations.
