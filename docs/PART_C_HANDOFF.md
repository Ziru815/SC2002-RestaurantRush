# Part C: Host and staff handoff

The Stage 3 staff logic for Host and Waiter is complete. This document records
the Part C implementation and validation; it does not indicate that the full
Stage 3 CLI is complete.

## Part C files

- [Host.java](../src/main/java/restaurantrush/Host.java): Extends Staff and provides
  one independent seating action per turn. It checks action availability,
  arguments, customer state and table availability before updating both sides
  of the association, then consumes the action. The controller removes the
  customer from the queue after successful seating.
- [Waiter.java](../src/main/java/restaurantrush/Waiter.java): Retains the existing
  seat, takeOrder and serve methods, and adds
  takeOrder(Customer, Menu, HappyHourPolicy, int).
  Both ordering methods share customer, table and duplicate-order checks.
  The new overload also validates the policy and turn before selecting a meal
  once, calling policy.quote once, locking the price through the three-argument
  Order constructor and attaching the order to the customer.
- [Stage3HostStaffTests.java](../src/test/java/restaurantrush/Stage3HostStaffTests.java):
  Contains 13 scenarios covering independent actions, skipping and resetting,
  duplicate seating, failures that preserve action availability, argument and
  customer/table association validation, retries after selection or pricing
  errors, single-quote pricing, promotion activation and expiry, compatibility
  with the existing API, and the service flow from Chef to Cashier.

Staff, Chef and Cashier require no changes and retain their existing general rules.

## Part B dependency sources

The local input directory is a source copy without .git and was missing the
Part B Stage 3 dependencies, so no local Git merge was performed.
The following file contents were taken directly from the team's
work/stage3-pricing-order branch, rather than implemented separately for Part C:

| File | Source | Git blob SHA |
| --- | --- | --- |
| [HappyHourPolicy.java](../src/main/java/restaurantrush/HappyHourPolicy.java) | [Part B branch](https://github.com/Ziru815/SC2002-RestaurantRush/blob/work/stage3-pricing-order/src/main/java/restaurantrush/HappyHourPolicy.java) | 11fcc7f4b35420a3357170000191f5ecfe4927cc |
| [Order.java](../src/main/java/restaurantrush/Order.java) | [Part B branch](https://github.com/Ziru815/SC2002-RestaurantRush/blob/work/stage3-pricing-order/src/main/java/restaurantrush/Order.java) | 943e7abefcb1275f4ad2409e02edfae986edc29e |

GitHub publication uses a merge commit with the existing Part C branch head and
Part B commit ae89509ad5bbd64d1824e3ec68376b999159c5da as parents. This preserves
Part B's original authorship and history. The subsequent Part C commit contains
only Host, Waiter, the Part C tests and this handoff.

The pull request initially targets work/stage3-pricing-order so its diff contains
only Part C changes. After the setup and prerequisite PRs are merged, retarget
the Part C PR to main before merging it. The imported Stage3PricingTests suite
also comes unchanged from Part B.

## Validation

On 2026-11-12, the code was compiled using the JDK bundled with the local
VS Code Java extension and the project's existing --release 17, -Xlint:all
and -Werror options:

- Existing nine test suites: 72/72 passed.
- Stage3PricingTests from Part B: 7/7 passed during pre-publication validation.
- Stage3HostStaffTests: 13/13 passed.
- VIP Burger: 1500 -> 1350 -> 1080 cents. Payment still uses the locked price
  after the promotion has expired.
- A generic MenuItem with three preparation units verified that Chef and Cashier
  do not require special combo handling. Part A's ComboMeal is not yet present
  in this source copy; integration testing with the actual combo remains pending
  until Part A is merged.

From the project root, run the following in a terminal with JDK 17+ on PATH:

~~~sh
sh scripts/test.sh
java -cp out/main:out/test restaurantrush.Stage3PricingTests
java -cp out/main:out/test restaurantrush.Stage3HostStaffTests
~~~

Changes to the central [scripts/test.sh](../scripts/test.sh) remain assigned to
Part D. The script compiles the new tests, but the two explicit java commands
are currently required to run the Part B and Part C suites separately.

## Integration steps for Part D

1. In Stage 3, Restaurant owns one Host and one HappyHourPolicy. Reset the Host
   at the start of each turn.
2. During the Host phase, call host.seat(customer, table), followed by
   removeFromQueue(customer) only after seating succeeds.
3. In the same turn, Waiter can use the new takeOrder overload for the newly
   seated customer, passing the shared policy and current turn. After success,
   the controller registers the returned Order with Restaurant.
4. Stage 1/2 continue to use takeOrder(Customer, Menu).
5. Add Stage3HostStaffTests to the central test loop.

The --stage3 option, Manager/Host command prompts, and complete Stage 3 victory
and defeat replays remain within Part D's integration scope.
