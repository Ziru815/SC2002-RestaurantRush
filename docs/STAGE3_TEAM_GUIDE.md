# Stage 3 team guide
Published 9 November 2026. Implementation contract, not completed Stage 3 code.
Code baseline: `stage-2` at `93fc50663056681469000208941ace992ccbb5a0`. Documentation-only changes may follow this baseline.
Authority: assignment sections 2-4.3 and 5; current Stage 2 Java source.

## SCOPE AND DESIGN CHOICES
Required: independent Host seating; two-item combo (sum preparation, 90% price);
once-only two-turn Happy Hour (20% off new bills, +15 waiting satisfaction);
VIP discount before Happy Hour; locked bills; independently runnable Stage 3.
Proposed choices, not extra assignment requirements: Host acts before Waiter;
one combo named Combo = Salad + Pasta; Regular ID 5 chooses it in Stage 3;
nearest-cent ties round half up at each discount step; activation at 18 allowed.
Recommended approach: extend current domain classes, keep one controller and
backward-compatible constructors. A duplicate Stage 3 engine would duplicate
stable rules; a generic plugin framework adds unnecessary scope for 3 features.
The setup commit contains documentation only. The four development branches start at that same setup commit. No Stage 3 classes are implemented by this setup.

Use [the interface sheet](STAGE3_INTERFACES.md) for exact signatures and [the class diagram](class-diagram-stage3.mmd) for the design.

Team target: tested, reviewed Stage 3 tagged by Wednesday 11 November 2026 (Singapore). Reserve 12-15 November for report, final diagrams and video. This is an internal target, not a statement of the official course deadline.

## A - CUSTOMER AND COMBO (Ziru)
Branch: work/stage3-customer-menu
New: ComboMeal.java, Stage3CustomerMenuTests.java
Edit: Customer.java, Menu.java. MenuItem remains unchanged unless a concrete need
is reviewed; keep its immutable contract. A owns all customer/menu files.
Implement ComboMeal extends MenuItem with two shared basic MenuItem references.
Compute price once: ((first.price + second.price) * 90 + 50) / 100 in long cents.
Prep is component sum; reject null/same component, int overflow and invalid data.
Menu() has 3 items; Menu(true) has 4; add hasCombo() and getCombo() (throws if absent).
Customer.chooseMeal selects Combo only for id==5 and menu.hasCombo(); otherwise
keeps the old cycle. Critic's override still always selects Pasta. No UI menu choice.
Customer.restoreWaitingSatisfaction(int amount): reject negative; zero no-op;
only QUEUED/SEATED change; cap at 100 with safe arithmetic. Other states unchanged.
Tests: 2430 cents / 3 units, rounding boundary, components shared and immutable,
ID5 old/new menu, Critic preference, null/overflow, recovery cap/state restrictions.

## B - PRICING AND ORDER
Branch: work/stage3-pricing-order
New: HappyHourPolicy.java, Stage3PricingTests.java
Edit: Order.java. B owns Table/OrderStatus but should not change their base rules.
HappyHourPolicy owns activationTurn=-1. activate(int turn) accepts 1..18 exactly
once; reject duplicate activation without changing existing state.
hasActivated(); isActive(int turn); remainingTurns(int turn). Queries allow 0
for initial UI; reject negative. For activation t, duration is 2 at t, 1 at t+1,
0 otherwise (including before activation). Turn 18 does not create turn 19.
quote(Customer customer, MenuItem item, int turn): require 1..18 and non-null
arguments; call customer.priceFor(item) exactly once; reject negative quotes;
active -> (quote * 80L + 50) / 100, inactive -> unchanged quote.
Order(Customer,MenuItem,int lockedPriceCents) validates and stores a final quote
without invoking priceFor again. Old 2-argument constructor still quotes once.
Tests: VIP Burger 1080 active / 1350 inactive; 2,1,0 duration; repeat activation;
rounding, null/negative/overflow safety; unchanged old orders; quote count one.
B tests using base items can run independently of A's new ComboMeal.

## C - HOST AND EMPLOYEE INTEGRATION
Branch: work/stage3-host-staff
New: Host.java, Stage3HostStaffTests.java
Edit: Waiter.java. Keep Chef/Cashier/Staff rules unless a tested need is found.
Host extends Staff; seat(Customer,Table) mirrors validated Waiter seating.
Check all preconditions before table/customer changes; mark action only after
success. Host does NOT own Restaurant or remove queue entries. Controller does.
Host and Waiter have independent actions; either can still seat.
Waiter.takeOrder(Customer,Menu,HappyHourPolicy,int turn) validates availability,
customer/table/order and arguments, chooses once, quotes once, constructs Order
with final cents, attaches it, then marks action. Keep old overload working.
Tests: Host seating, Host+Waiter same turn, occupied table and invalid customer
leave all state/task unchanged; repeat action; reset; Stage 2 path; discounted
order; normal Chef preparation and Cashier locked-price payment.
C can start Host independently. Merge B via merge commit before writing the
pricing overload; do not create local placeholder copies of B's classes.

## D - CONTROLLER, CLI AND FINAL INTEGRATION
Branch: work/stage3-controller
Edit: Restaurant.java, GameController.java, ConsoleUI.java, Main.java,
scripts/test.sh and normal project README/status/acceptance docs.
New: Stage3ControllerTests.java plus reproducible Stage 3 input/result fixtures.
Keep GameConfig and Stage2Arrivals unchanged; reuse ID2 VIP / ID4 Critic.
Restaurant() retains Stage 1/2; Restaurant(true) creates Menu(true), one Host and
one HappyHourPolicy. Add isStage3Enabled(), getHost(), getHappyHour(); getters
reject disabled mode. Host/policy are 0..1 structurally, exactly one in Stage 3.
Keep GameController's current constructors, arrivalFactory and ConsoleUI.stageLabel.
Main adds --stage3; reject combination --stage2 --stage3; support --check/--help.
Default, --stage2 and their old command sequences must remain compatible.

## EXACT STAGE 3 TURN ORDER / CLI CONTRACT
1 nextTurn; reset all staff; scheduled arrival (existing six-customer schedule).
2 Display turn / current state and Happy Hour unused/active/expired + remaining.
3 If not previously activated: Manager> happyhour | wait (status/help do not
  consume this choice). Activation is only accepted here, never mid-turn.
4 If active, apply +15 once to each QUEUED/SEATED customer; refresh state display.
  New arrivals start at 100 and remain capped. Do not recover LEFT/PAID/served.
5 Host> seat <customerId> <tableNo> | wait; on success controller removes queue.
6 Waiter> seat/order/serve/wait; Host-seated customer can be ordered this turn.
7 Chef> cook/wait; one unit, including an order just taken this turn.
8 Existing finishEating / oldest eligible payment / waiting loss / abandonment.
9 End-turn totals; only after turn 18 evaluate the same three victory targets.
After activation skip subsequent Manager choices; continue duration display.
Wrong-prompt commands (e.g. happyhour at Host>) must retry without state change.

## SHARED CONTRACTS / MERGE ORDER
Stage 2 remains frozen. Use the prepared branches below, all based on the same
setup commit on top of main. Merge the setup documentation PR with a merge commit
so that its changes are shared history rather than four duplicate patches. No rebase/amend/force push of published shared branches.
Stage 3 additions use exact method/parameter names in this handoff; do not add
type-label or instanceof chains to the controller for customer behaviour.
Each member writes their tests; D alone owns the central test script so the
four PRs do not all edit it. Until wired, use sh scripts/test.sh to compile main
AND all test sources, then java -cp out/main:out/test restaurantrush.Stage3...Tests
to run the relevant new suite (replace Stage3...Tests with its exact class name).
A and B can develop independently. Merge A, then B, then C, then D using Create
a merge commit and teammate review. C/D pull needed dependencies with merge
commits. D prepares integration tests early, not only after others finish.
Do not merge your own PR. Preserve original authors and stage-1/stage-2 tags.

## ACCEPTANCE / DESIGN SELF-CHECK
All three required features map to concrete classes, methods, CLI and tests.
All 20 Stage 2 principal types are retained; three new types -> 23 total.
Four work areas have unique production-file ownership; B->C->D dependency clear.
Recovery precedes staff actions and happens once per active turn, including when
the UI retries. Unserved READY food still counts as waiting; no revival of LEFT.
Combo price/prep is generic MenuItem data; no special Chef/Cashier branch.
Immutable bill captured once; reductions stack Combo base -> VIP -> Happy Hour.
Stage 1/2 timings, 18-turn result, departure cleanup and paid average preserved.
Required future validation: existing 72 checks + new part suites + full Stage 3
victory and defeat; both launch compatibility and complete stage-1/stage-2 replay
comparisons. Tests are not yet implemented or run for Stage 3.
Class diagram is a reviewed implementation design, not proof of working code.
Before stage-3 tagging, reconcile every new signature/relationship with actual
Java, regenerate final diagrams, and run tests on the merged main.

## PLANNED VICTORY SCENARIO (MANUAL EXPECTATION, NOT EXECUTION EVIDENCE)
Activate Happy Hour at turn 4; before turn 4 choose Manager wait each turn.
Turn: Host / Waiter / Chef
1: seat 1 1 / order 1 / cook 1
2: wait / serve 1 / wait
3: wait / wait / wait
4: seat 2 2 / order 2 / cook 2
5: wait / serve 2 / wait
6: wait / wait / wait
7: seat 3 1 / order 3 / cook 3
8: wait / wait / cook 3
9: wait / serve 3 / wait
10: seat 4 2 / order 4 / cook 4
11: wait / wait / cook 4
12: wait / serve 4 / wait
13: seat 5 1 / order 5 / cook 5
14: wait / wait / cook 5
15: wait / wait / cook 5
16: seat 6 2 / serve 5 / wait
17: wait / order 6 / cook 6
18: wait / wait / cook 6
Expected paid IDs 1..5; bills 900+1080+1800+1800+2430=8010 cents;
recorded satisfaction 92+100+84+76+76=428; average 85.6; VICTORY.
All-wait/no activation should remain a reproducible defeat, with no revenue.
