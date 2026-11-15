# AI assistance history: Haoyang (Part B), Stage 2

Tool: Codex (OpenAI), as identified in the supplied draft. Formatted 15 Nov 2026.

> Note: Original technical messages were written in Chinese; content below has been translated by AI to natural English without altering technical semantics.


Customer polymorphism, one pricing call per order, locked bills and regression tests.

[Previous: Stage 1](stage-1.md) · [Index](README.md) · [Next: Stage 3](stage-3.md)

### Me (Tue 14 Oct 2026, 16:20 SGT)

Stage 1's `Table` and `Order` are complete and the focused tests pass. Stage 2 adds Regular, VIP and Critic customers.
My Part B task is to support customer‑specific prices while making sure the bill is calculated once and cannot change before payment.

### Codex

*(Codex used tools: GitHub x3)*

That is the correct scope. Stage 2 should change how an Order obtains its initial price, but it should not create a separate lifecycle for each customer type.
The intended flow is:
```text
Customer.priceFor(item) -> Order.lockedPriceCents -> Cashier payment
```
Regular and Critic customers return the full price. `VIPCustomer` overrides the same method to return the discounted price. Order uses the Customer abstraction and does not need to know the concrete type.

### Me (Tue 14 Oct 2026, 16:26 SGT)

My idea is to use `Customer.priceFor(MenuItem)` polymorphically. The `Order` constructor should call it once and store the result in `lockedPriceCents`, instead of making `Order`, `Cashier` or `GameController` check the customer type.
Does this avoid type checking and repeated discounts?

### Codex

Yes. Java dispatches `priceFor()` to the actual Customer subclass at run time, so the Order contains no `instanceof VIPCustomer` branch. `Cashier` also remains independent of customer type because it collects only the stored bill.

For a Burger:
```text
Regular -> 1500 cents
VIP     -> 1350 cents
Critic  -> 1500 cents
```
The value 1350 is copied into the VIP Order. Cashier must not multiply it by 90% again, so it cannot become 1215 by mistake. The shared Burger also remains priced at 1500; an individual discount must never mutate shared menu data.

### Me (Tue 14 Oct 2026, 16:32 SGT)

Show me the minimum Part B change to `Order.java`. Stage 1 preparation, READY timing, service, payment and cancellation must remain unchanged.

### Codex

Only the constructor's last line changes.
Stage 1:
```java
public Order(Customer customer, MenuItem item) {
    this.customer = Objects.requireNonNull(customer, "customer");
    this.item = Objects.requireNonNull(item, "item");
    this.lockedPriceCents = item.getPriceCents();
}
```
Stage 2:
```java
public Order(Customer customer, MenuItem item) {
    this.customer = Objects.requireNonNull(customer, "customer");
    this.item = Objects.requireNonNull(item, "item");
    // Capture the polymorphic quote once.
    this.lockedPriceCents = this.customer.priceFor(this.item);
}
```
Both references are validated before the pricing method is called. Every other Order method should remain unchanged, which preserves the Stage 1 workflow.

### Me (Tue 14 Oct 2026, 16:38 SGT)

How can I prove that `priceFor()` is called exactly once, rather than only checking the final number?

### Codex

Use a test‑only Customer whose quote can change and whose calls are observable:
```java
private static final class MutableQuoteCustomer extends Customer {
    private int quoteCents;
    private int quoteCalls;
    private MenuItem lastQuotedItem;
    private MutableQuoteCustomer(int id, int quoteCents) {
        super(id);
        this.quoteCents = quoteCents;
    }
    @Override
    public int waitingLoss() {
        return 8;
    }
    @Override
    public int priceFor(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item is required.");
        }
        quoteCalls++;
        lastQuotedItem = item;
        return quoteCents;
    }
}
```
Create the Order, read `getLockedPriceCents()` several times, and verify `quoteCalls == 1`. Also verify `lastQuotedItem` is the same object stored by the Order. Counting the interaction catches an implementation that calls the method repeatedly but happens to receive the same value.

### Me (Tue 14 Oct 2026, 16:45 SGT)

What is the complete Stage 2 Order test plan?

### Codex

Use eight focused scenario groups:

1. Regular, VIP and Critic Burger orders lock 1500, 1350 and 1500 cents respectively.
2. The Order requests exactly one quote for the same MenuItem that it stores.
3. A later change to the Customer's quote does not change an existing bill or trigger another call.
4. Null Customer and MenuItem references are rejected before quoting; in particular, a null item leaves the call count at zero.
5. A VIP order does not mutate the shared MenuItem price.
6. Pasta still needs two preparation units and records the real READY turn; preparation does not change the bill.
7. A READY order still cannot be served until a later turn.
8. Cancellation, terminal‑state and payment guards from Stage 1 remain unchanged and preserve the bill after rejected operations.

### Me (Tue 14 Oct 2026, 16:52 SGT)

How should I verify Stage 2 and prove that Stage 1 behaviour was not broken?

### Codex

Run the new focused suite:
```bash
java -cp out/main:out/test restaurantrush.Stage2OrderTests
```
Expected result:
```text
PASS 8/8 Stage 2 order pricing scenarios
```
Then rerun the original Part B suite:
```bash
java -cp out/main:out/test restaurantrush.OrderTableTests
```
Expected result:
```text
PASS 14/14 Part B table/order scenarios
```
Finally run:
```bash
sh scripts/test.sh
```
After A, B, C and D are integrated, the Stage 1 and Stage 2 total should be 72 passing checks. The design responsibility is now clear: Customer decides the quote, Order decides when it becomes fixed, and Cashier only collects the fixed amount.
