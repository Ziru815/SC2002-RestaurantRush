# AI assistance history: Haoyang (Part B), Stage 3

Tool: Codex (OpenAI), as identified in the supplied draft. Formatted 15 Nov 2026.

> Note: Original technical messages were written in Chinese; content below has been translated by AI to natural English without altering technical semantics.



Happy Hour, sequential discounts, explicit-price orders, integration tests and final design review.

[Previous: Stage 2](stage-2.md) · [Index](README.md)

### Me (Mon 03 Nov 2026, 15:30 SGT)

Stage 2 passes its tests. Stage 3 adds Happy Hour. It can be activated only once, lasts for two turns, and affects only orders created while it is active. It must obtain the customer‑specific price first and then apply a 20% discount.
An existing Order must not change when the promotion expires.

### Codex

*(Codex used tools: GitHub x4)*

Those rules imply one price pipeline:
```text
MenuItem base price
    -> Customer.priceFor(item)
    -> HappyHourPolicy.quote(...)
    -> Order final locked price
    -> Cashier payment
```
The policy should own game‑wide activation state. An individual Order should not know whether Happy Hour is active; it should only retain the final amount agreed when it was created.

### Me (Mon 03 Nov 2026, 15:36 SGT)

My design is to create a separate `HappyHourPolicy` for activation and final quotes, rather than put promotion state inside `Order`.
`Order` keeps the existing constructor and adds another constructor that accepts a final `lockedPriceCents`. `Waiter` calculates one final quote and then creates the Order. Is this separation correct?

### Codex

Yes. The old constructor preserves the Stage 1 and Stage 2 paths:
```java
new Order(customer, item)
```
It requests the Customer quote itself. The new constructor is for a caller that has already applied every pricing rule:
```java
int finalPrice = policy.quote(customer, item, turn);
new Order(customer, item, finalPrice);
```
The new constructor must not delegate to the old one, because that would invoke `priceFor()` again. It should validate the references and non‑negative final price, then assign the field directly.

### Me (Mon 03 Nov 2026, 15:42 SGT)

Explain the discount order and rounding. I also want to avoid integer overflow.

### Codex

The Customer discount is applied first. A VIP Burger follows:
```text
1500 -> VIP 90% -> 1350 -> Happy Hour 80% -> 1080 cents
```
Do not apply one combined 72% calculation to the original price, because the two stages are rounded separately. Use integer cents and half‑up rounding:
```java
(int) ((cents * 80L + 50L) / 100L)
```
The `L` makes the multiplication use `long`, preventing overflow before division. The same approach is used for the VIP's 90% calculation.

For the 2430‑cent Combo:
```text
Regular + Happy Hour: 2430 -> 1944
VIP + Happy Hour:     2430 -> 2187 -> 1750
```

### Me (Mon 03 Nov 2026, 15:48 SGT)

Give me the necessary `HappyHourPolicy` implementation. It must validate turns, prevent reactivation and expose the remaining active turns.

### Codex

```java
package restaurantrush;
import java.util.Objects;
public final class HappyHourPolicy {
    private static final int DURATION = 2;
    private int activationTurn = -1;
    public void activate(int turn) {
        requirePlayableTurn(turn);
        if (hasActivated()) {
            throw new IllegalStateException(
                    "Happy Hour has already been activated.");
        }
        activationTurn = turn;
    }
    public boolean hasActivated() {
        return activationTurn >= 0;
    }
    public boolean isActive(int turn) {
        return remainingTurns(turn) > 0;
    }
    public int remainingTurns(int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException("Turn cannot be negative.");
        }
        if (!hasActivated() || turn < activationTurn) {
            return 0;
        }
        return Math.max(0, activationTurn + DURATION - turn);
    }
    public int quote(Customer customer, MenuItem item, int turn) {
        requirePlayableTurn(turn);
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(item, "item");
        int cents = customer.priceFor(item);
        if (cents < 0) {
            throw new IllegalArgumentException(
                    "Customer quote cannot be negative.");
        }
        return isActive(turn)
                ? (int) ((cents * 80L + 50L) / 100L)
                : cents;
    }
    private static void requirePlayableTurn(int turn) {
        if (turn < 1 || turn > GameConfig.MAX_TURNS) {
            throw new IllegalArgumentException(
                    "Turn must be between 1 and "
                            + GameConfig.MAX_TURNS + ".");
        }
    }
}
```
For activation at turn 4, `remainingTurns()` returns 2 on turn 4, 1 on turn 5 and 0 on turn 6. A failed repeat activation leaves the original activation turn unchanged.

### Me (Mon 03 Nov 2026, 15:54 SGT)

Show the required `Order` change and explain the double‑quoting risk.

### Codex

Keep the original constructor unchanged for the earlier stages, and add:
```java
public Order(Customer customer, MenuItem item, int lockedPriceCents) {
    this.customer = Objects.requireNonNull(customer, "customer");
    this.item = Objects.requireNonNull(item, "item");
    if (lockedPriceCents < 0) {
        throw new IllegalArgumentException(
                "Locked price cannot be negative.");
    }
    this.lockedPriceCents = lockedPriceCents;
}
```
This is wrong:
```java
int finalPrice = policy.quote(customer, item, turn);
Order order = new Order(customer, item);
```
`policy.quote()` has already called `priceFor()`, and the two‑argument constructor calls it again while discarding `finalPrice`.

This is correct:
```java
int finalPrice = policy.quote(customer, item, turn);
Order order = new Order(customer, item, finalPrice);
```
The explicit‑price constructor performs no quote, so each order has exactly one customer‑pricing call.

### Me (Mon 03 Nov 2026, 16:00 SGT)

What seven focused pricing scenarios should I test?

### Codex

Use these groups:

1. **Activation window:** unused, activation turn, second turn, expiry, no retroactive effect and no reactivation.
2. **Invalid and last turns:** reject turns 0 and 19 without consuming the opportunity; allow activation on turn 18.
3. **Discount order:** VIP Burger is 1350 outside the promotion and 1080 during it; Critic receives only Happy Hour; small‑cent examples prove separate rounding.
4. **Monetary boundaries:** verify 0, 2, 3 and `Integer.MAX_VALUE` cents, including the long intermediate.
5. **Quote once and lock:** use a counting Customer to prove one call per order and no call from the explicit‑price constructor; later Customer changes and expiry do not alter old bills.
6. **Invalid quotes and orders:** reject invalid turns, null references, negative customer quotes and negative explicit bills before inappropriate side effects; zero is valid.
7. **Checkout after expiry:** create a VIP Burger Order for 1080 during Happy Hour, complete service and eating, then collect 1080 after the policy has expired.

### Me (Mon 03 Nov 2026, 16:07 SGT)

How should I verify the final integration?

### Codex

Run the focused suite:
```bash
java -cp out/main:out/test restaurantrush.Stage3PricingTests
```
Expected:
```text
PASS 7/7 Stage 3 pricing scenarios
```
Then run:
```bash
sh scripts/test.sh
```
The final integration should pass 13 suites and 111 checks: the four Stage 1 suites, five Stage 2 suites and four Stage 3 suites. Keeping all earlier suites in the run is the regression evidence that the overloads did not replace or break the earlier paths.

### Me (Mon 03 Nov 2026, 16:14 SGT)

I ran `sh scripts/test.sh` on the final version. All 13 suites and 111 checks passed. I also ran `Stage3PricingTests` separately and got 7/7.
Please do a strict review rather than assuming the code is correct only because the tests pass. Start with the `Table` and `Order` invariants.

### Codex

*(Codex used tools: GitHub x8)*

I checked the current `main` implementations of `Table`, `Order`, `HappyHourPolicy`, `VIPCustomer`, `Waiter`, `Cashier`, `MenuItem` and `GameConfig`. I found no blocking issue.

`Table` preserves these invariants:

- its positive number is immutable;
- `occupant == null` and `isFree()` always agree;
- only a queued, unlinked Customer can be seated;
- rejected seating cannot overwrite an occupant;
- only `PAID` or `LEFT` occupants can be released;
- validation occurs before mutation.

`Order` begins with zero units, `PLACED` and `readyTurn == -1`. Because MenuItem guarantees positive preparation units, each valid advance moves toward READY. Once READY, further advances are rejected, so work cannot exceed the requirement and `readyTurn` is recorded only once. Terminal transitions are guarded as intended.

### Me (Mon 03 Nov 2026, 16:20 SGT)

Now check the locked‑price design and the two `Order` constructors. Could either path quote or discount twice?

### Codex

The paths are correctly separated:
```text
Stage 1: MenuItem price -> Order
Stage 2: Customer.priceFor(item) -> Order
Stage 3: Customer.priceFor(item) -> HappyHourPolicy -> explicit‑price Order
```
`lockedPriceCents` is final. Cashier reads it without consulting the Customer, MenuItem or policy, so expiry cannot change an existing debt.

The original constructor quotes exactly once. The explicit‑price constructor does not delegate to it and therefore does not quote again. `Waiter.takeOrder(customer, menu, policy, turn)` calculates one final amount and passes it to the correct constructor. There is no double‑discount path in the normal workflow.

The trade‑off is that the explicit‑price constructor trusts its caller to supply the correct non‑negative amount. A more general system might hide this behind a factory or pricing interface, but that abstraction is not needed for one assignment policy.

### Me (Mon 03 Nov 2026, 16:26 SGT)

Check the activation window, discount order, rounding and overflow.

### Codex

Activation at turn 4 gives two active turns:
```text
turn 3 -> 0
turn 4 -> 2
turn 5 -> 1
turn 6 -> 0
```
`activate()` validates before changing state and cannot be used again after expiry. `quote()` accepts only playable turns and validates its references before calling Customer pricing.

The discount order is customer pricing first, then Happy Hour. Integer cents avoid floating‑point error; adding 50 implements half‑up rounding; and the `long` intermediate prevents overflow during multiplication. The VIP and policy calculations both follow this pattern.

One non‑blocking API inconsistency is that `quote()` restricts turns to 1–18 while `remainingTurns()` rejects only negative values. A query for turn 19 is therefore possible even though gameplay and quoting cannot reach it. This does not affect the controller's legal flow.

### Me (Mon 03 Nov 2026, 16:32 SGT)

Does Stage 3 preserve Stage 1 and Stage 2? List any remaining design trade‑offs and what I must be able to explain in the presentation.

### Codex

Backward compatibility is preserved: the original Order constructor and original `Waiter.takeOrder(customer, menu)` remain; Stage 1 creates no policy; Stage 2 continues using Customer polymorphism; and the preparation, READY timing, service, payment and cancellation methods are unchanged. The earlier suites running beside the Stage 3 suites provide regression evidence.

There are no blocking issues. The remaining trade‑offs are:

1. Table and Customer are updated sequentially by Waiter rather than in a transaction.
2. `markServed()` itself has no turn argument; Waiter enforces the timing rule through `isServable(turn)`.
3. The explicit‑price constructor trusts the caller's final quote.
4. The two‑argument constructor assumes official Customer implementations return non‑negative prices.
5. `remainingTurns()` has a wider query range than `quote()`.
6. A concrete policy is used instead of a general pricing‑policy interface.

For the presentation, be able to explain why the bill is locked at order creation; why VIP pricing belongs to Customer while Happy Hour has separate state; why the discounts are sequential; why the explicit constructor must not quote again; how the counting Customer proves one call; why cents and a long intermediate are used; why service requires `turn > readyTurn`; and why Table does not modify Customer directly.
