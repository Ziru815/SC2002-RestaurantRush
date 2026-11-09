# Stage 3 interface sheet

Shared implementation contract, published 9 November 2026. These additions are
**not implemented by the setup commit**. Existing Stage 2 code remains runnable.
Package: `restaurantrush`; production files: `src/main/java/restaurantrush/`;
tests: `src/test/java/restaurantrush/`. Keep existing signatures working.

## Ownership and branches

| Part | Branch | New production files | Existing files to edit | New test suite |
| --- | --- | --- | --- | --- |
| A / Ziru | `work/stage3-customer-menu` | `ComboMeal.java` | `Customer.java`, `Menu.java` | `Stage3CustomerMenuTests` |
| B | `work/stage3-pricing-order` | `HappyHourPolicy.java` | `Order.java` | `Stage3PricingTests` |
| C | `work/stage3-host-staff` | `Host.java` | `Waiter.java` | `Stage3HostStaffTests` |
| D | `work/stage3-controller` | No new domain type required | `Restaurant.java`, `GameController.java`, `ConsoleUI.java`, `Main.java`, `scripts/test.sh` | `Stage3ControllerTests` |

A/B can work independently. C can implement Host immediately; its new Waiter
overload needs B. D can prepare the controller/UI changes using the signatures
below, but the complete integration will compile only after dependencies exist.
Do not add competing placeholder versions of someone else's classes. Only D
edits the central test script; each member writes and runs their own test suite.

## A: customer and menu

```java
public final class ComboMeal extends MenuItem {
    public ComboMeal(String name, MenuItem first, MenuItem second);
}

// Menu: retain getItems() and find(String).
public Menu();
public Menu(boolean includeCombo);
public boolean hasCombo();
public ComboMeal getCombo();

// Customer: retain all existing operations.
public void restoreWaitingSatisfaction(int amount);
// Existing chooseMeal(Menu menu) gains the documented ID 5 combo rule.
```

- `ComboMeal` holds two distinct, non-null basic `MenuItem` references. Price is
  90% of their sum (nearest cent, half up); preparation units are summed. Use
  `long` arithmetic and reject values outside `int` range. Do not mutate items.
- `Menu()` and `Menu(false)` retain the three base dishes. `Menu(true)` adds
  `Combo` = Salad + Pasta: 2,430 cents and 3 preparation units. Return an immutable
  list. `getCombo()` throws `IllegalStateException` when unavailable.
- In `Customer.chooseMeal`, ID 5 selects the combo only if `menu.hasCombo()`;
  otherwise keep the existing cycle. `CriticCustomer` still overrides selection
  to Pasta. The manager does not choose a customer's food.
- Recovery rejects negative amounts, accepts zero as a no-op, and affects only
  `QUEUED` / `SEATED`, capped at 100. Served/eating, ready-to-pay, paid and left
  customers are unchanged. Use safe arithmetic even for large amounts.

## B: Happy Hour and immutable bills

```java
public final class HappyHourPolicy {
    public HappyHourPolicy();
    public void activate(int turn);
    public boolean hasActivated();
    public boolean isActive(int turn);
    public int remainingTurns(int turn);
    public int quote(Customer customer, MenuItem item, int turn);
}

// Order: keep the existing two-argument constructor.
public Order(Customer customer, MenuItem item);
public Order(Customer customer, MenuItem item, int lockedPriceCents);
```

| Operation | Required behaviour |
| --- | --- |
| `activate(turn)` | Accept turns 1..18, once per game. Invalid turn: `IllegalArgumentException`; repeated activation: `IllegalStateException`, without changing the original activation. |
| `hasActivated()` | False initially; permanently true after successful activation, including after expiry. |
| `isActive(turn)` | True only for the activation turn and the following turn. Read-only query; allow 0 for the pre-game screen and reject negative turns. |
| `remainingTurns(turn)` | 0 before activation; 2 on activation turn; 1 next turn; 0 after expiry. Also 0 when queried before the activation turn. |
| `quote(customer,item,turn)` | Validate non-null inputs and turn 1..18; call `customer.priceFor(item)` exactly once; reject a negative quote; apply 80% only while active, with half-up rounding in `long` arithmetic. No mutations. |
| Old `Order(customer,item)` | Preserve the existing customer quote-once contract. |
| New `Order(customer,item,lockedPriceCents)` | Validate customer/item and nonnegative cents; save the final amount without calling `priceFor` again. `lockedPriceCents` remains final. |

Pricing order: combo base price, if applicable -> customer/VIP discount ->
Happy Hour -> lock order. VIP Burger: 1,500 -> 1,350 -> 1,080 cents.
Existing orders never change. Cashier never applies discounts again.
Activation on turn 18 is allowed; it does not extend the 18-turn game.

## C: Host and Waiter

```java
public final class Host extends Staff {
    public Host();
    public void seat(Customer customer, Table table);
    // Inherited: hasActed(), resetTurn(), skipTurn().
}

// Waiter: keep seat(), serve() and the old takeOrder(Customer, Menu).
public Order takeOrder(Customer customer, Menu menu,
                       HappyHourPolicy policy, int turn);
```

- Host has one independent action per turn. Validate staff availability,
  non-null arguments, queued/unseated customer and free table before mutation.
  Update table/customer consistently; call `markActed()` only after success.
- Host does not own the restaurant queue. D removes the customer from the queue
  after a successful seat. Host and Waiter must not seat the same customer twice.
- The new Waiter overload validates before mutation, chooses the item once,
  calls `policy.quote` once, creates the three-argument `Order`, attaches it to
  the customer, and then marks its action. Invalid choices do not use a task.
- Existing Waiter seating remains available. Chef and Cashier use ordinary
  `MenuItem` preparation units and the locked bill, including for combos.

## D: setup and control

```java
// Restaurant: preserve existing getters and operations.
public Restaurant();
public Restaurant(boolean stage3Enabled);
public boolean isStage3Enabled();
public Host getHost();
public HappyHourPolicy getHappyHour();

// GameController: keep both existing constructor signatures.
public GameController(Restaurant restaurant, ConsoleUI ui);
public GameController(Restaurant restaurant, ConsoleUI ui,
                      java.util.function.IntFunction<Customer> arrivalFactory);
// Planned private helpers:
private void processHappyHourChoice();
private void restoreWaitingCustomers();
private void processHostChoice();

// ConsoleUI: retain constructors and stageLabel.
public void showHappyHour(HappyHourPolicy policy, int turn);
```

`Restaurant()` / `Restaurant(false)` retain Stage 1/2 setup. `Restaurant(true)`
owns `Menu(true)`, one Host and one HappyHourPolicy. Outside Stage 3, Host/policy
are absent; their getters throw `IllegalStateException`. Guard with
`isStage3Enabled()` rather than using the UI label as a game rule.

Main adds `--stage3`, mutually exclusive with `--stage2`, and supports
`--stage3 --check` / `--stage3 --help`. Default launch stays Stage 1; `--stage2`
keeps its command sequence. Reuse `Stage2Arrivals`: ID 2 VIP, ID 4 Critic,
other IDs Regular, with the original six arrivals. No customer type checks in
the controller for pricing, meal selection or waiting loss.

## Exact turn order and prompts

1. Advance turn, reset staff, process the scheduled arrival and show state.
2. Show Happy Hour unused/active/expired and remaining duration. While activation
   is unused, accept `Manager> happyhour` or `Manager> wait`.
3. If active, apply `restoreWaitingSatisfaction(15)` once per eligible customer
   and refresh state. Never repeat recovery because of `help`, `status` or a retry.
4. `Host> seat <customerId> <tableNo>` or `wait`.
5. Existing `Waiter>` commands. A Host-seated customer can order this same turn.
6. Existing `Chef>` commands. One preparation unit, including a new order.
7. Existing eating, oldest eligible payment, waiting loss, abandonment, totals.
8. Evaluate victory only after turn 18. No additional turns or early ending.

After activation, skip later Manager choices but keep the Happy Hour display.
`status`/`help` do not consume choices; wrong-context commands retry without
changing state. Recovery includes unserved customers whose food is READY, and
never revives customers who already left. Arrival satisfaction remains capped at 100.

## Start work and review

```sh
git fetch origin
git switch --track origin/work/stage3-customer-menu
```

Replace the branch name with your assigned branch. If you already have a local
branch, switch to it instead of recreating it. Preserve local uncommitted work.

Use ordinary merge commits to pull dependencies into your branch, or wait until
they are merged to main and merge `origin/main`. No rebase/amend/force-push of
published shared branches. Merge the setup PR first, then implementation PRs in
order A -> B -> C -> D. Use **Create a merge commit**; do not merge your own PR.

Until D adds the new suites to the central loop, `sh scripts/test.sh` compiles
all test sources and runs the existing suites. Then run your own class, e.g.:

```sh
java -cp out/main:out/test restaurantrush.Stage3PricingTests
```

See [the team guide](STAGE3_TEAM_GUIDE.md) for each part's test checklist and the
planned victory scenario. Existing 72 checks plus new suites and full replays
must pass on merged main before tagging `stage-3`. Wednesday 11 November is the
team's code target; 12-15 November is reserved for report, diagrams and video.
The setup documentation is not acceptance of any Stage 3 implementation.
