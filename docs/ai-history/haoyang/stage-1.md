# AI assistance history: Haoyang (Part B), Stage 1

Tool: Codex (OpenAI), as identified in the supplied draft. Formatted 15 Nov 2026.

> Note: Original technical messages were written in Chinese; content below has been translated by AI to natural English without altering technical semantics.

Table and Order responsibilities, lifecycle rules, validation, implementation and focused tests.

[Index](README.md) · [Next: Stage 2](stage-2.md)

### Me (Thu 24 Sep 2026, 14:10 SGT)

I am responsible for Part B of Restaurant Rush. My scope includes the Table and Order lifecycle, order pricing and the related tests.
Please first analyse the Stage 1 requirements and explain exactly what Part B needs to implement. Do not write code yet.

### Codex

*(Codex used tools: GitHub x4)*

Part B should make `Table` and `Order` enforce their own legal states instead of spreading those rules through `Waiter`, `Chef`, `Cashier` or `GameController`.
Your main Stage 1 ownership is:

- `Table`
- `Order`
- `OrderStatus`
- focused `OrderTableTests`
You should not implement the staff or controller workflow. Those classes coordinate the objects, but they should use Part B's public methods rather than modify internal fields.

### Me (Thu 24 Sep 2026, 14:16 SGT)

Before implementation, I want to understand the responsibilities and state transitions.

### Codex

`Table` maintains its current occupant. `seat(customer)` should accept only a non‑null, queued customer who is not already linked to a table, and only when this table is free. `release()` should be a no‑op on a free table; an occupied table may be released only after its occupant is `PAID` or `LEFT`.
The Order lifecycle is:
```text
PLACED -> PREPARING -> READY -> SERVED -> PAID
   \__________________________> CANCELLED
```
Each successful `advance(turn)` completes exactly one preparation unit. When the required units are reached, the Order becomes `READY` and stores that turn in `readyTurn`. It is servable only when `turn > readyTurn`, so food completed this turn cannot be served until a later turn.
`markServed()` permits only `READY -> SERVED`; `markPaid()` permits only `SERVED -> PAID`; and `cancel()` permits `PLACED`, `PREPARING` or `READY` to become `CANCELLED`.

### Me (Thu 24 Sep 2026, 14:22 SGT)

What are the important edge cases and interfaces with the other team members?

### Codex

Invalid arguments should be rejected before mutation, and illegal lifecycle transitions should leave every field unchanged. In particular:

- an occupied table must preserve its original occupant after rejected seating;
- premature release must preserve the occupant;
- an invalid preparation turn must not increment `unitsDone`;
- completed and terminal orders must reject further transitions;
- the construction price must be stored rather than read again later.

Part A supplies the Customer lifecycle. Part C's `Waiter` coordinates both sides of seating, the `Chef` calls `advance()`, and the `Cashier` marks payment and releases the table. Part D's controller chooses when those actions happen. `Table` must not call `Customer.seat()` itself, because that would couple the two domain objects and risk recursive or duplicate updates.

### Me (Thu 24 Sep 2026, 14:30 SGT)

My understanding is that `Table` only maintains its occupant and must not directly modify `Customer`. `Order` owns preparation and its state transitions. Part C's staff classes coordinate multiple objects, which avoids `Table` and `Customer` calling each other.
Is that division reasonable?

### Codex

Yes. It gives each domain class one clear responsibility while leaving cross‑object orchestration to the staff layer.
For `Table`, also account for these cases:

- seating the same customer twice is still invalid;
- the customer must be both `QUEUED` and unlinked from any table;
- failed seating must preserve the old occupant;
- releasing a free table is idempotent;
- only `PAID` and `LEFT` occupants allow release;
- `Table` should not try to repair an inconsistent Customer object.

For `Order`, reject null references, non‑positive preparation turns, work after `READY`, early service, early payment, repeated payment and illegal cancellation. `isServable()` should be a pure query. Cancellation should preserve recorded preparation work and any existing `readyTurn`.

### Me (Thu 24 Sep 2026, 14:38 SGT)

I plan to validate every method before changing state: invalid arguments use `IllegalArgumentException`, and illegal state transitions use `IllegalStateException`. Releasing a free table remains a no‑op.
Why is a general state‑machine class not suitable here?

### Codex

The proposed validation rule is sound, subject to preserving any exception types already required by the skeleton or tests.
A general state machine is unnecessary here for three reasons. First, the number of states and transitions is small. Second, transitions such as `advance()` also update domain data such as `unitsDone` and `readyTurn`, so a generic transition table would still need callbacks and special conditions. Third, no other class needs to reuse this exact transition graph. Explicit methods such as `advance()`, `markServed()` and `markPaid()` express the domain rules more clearly and are easier to test.

### Me (Thu 24 Sep 2026, 14:46 SGT)

I want to fix Part B's behaviour with tests before implementing `Table` and `Order`.
My four groups are: legal Table seating and release; rejected Table operations; normal Order preparation, READY timing, service and payment; and illegal Order transitions, cancellation and locked pricing.

### Codex

*(Codex used tools: GitHub)*

That division is complete. The Table tests should cover:

1. a free table accepts one queued customer without modifying the Customer side;
2. null, occupied‑table, already‑seated and non‑queued cases are rejected without mutation;
3. releasing a free table is a no‑op and premature release preserves the occupant;
4. `PAID` and `LEFT` occupants allow release.

### Me (Thu 24 Sep 2026, 14:52 SGT)

Please turn the Order part into concrete scenarios. Each scenario should state the initial condition, action and expected result, including which fields remain unchanged after failure.

### Codex

Use ten Order scenario groups:

1. one‑unit Salad and Burger orders become `READY` after one advance;
2. Pasta remains `PREPARING` after one unit and becomes `READY` after the second;
3. invalid turns and work after completion are rejected without changing `unitsDone`, `status` or `readyTurn`;
4. an order is not servable on its READY turn but is servable later;
5. `markServed()` rejects a non‑READY order;
6. only a `SERVED` order can be paid, and repeated payment fails;
7. `PLACED`, `PREPARING` and `READY` orders can be cancelled;
8. `SERVED`, `PAID` and already‑cancelled orders reject cancellation;
9. terminal orders reject all further transitions without mutation;
10. changing a test MenuItem's later price does not change the Order's construction price.

The locked‑price test should use a mutable test subclass. Testing only the normal immutable `MenuItem` could pass even if the Order were reading its price dynamically.

### Me (Thu 24 Sep 2026, 14:58 SGT)

I will implement `Table` first. My idea is to keep a table identifier and `occupant`; derive `isFree()` from whether occupant is null; validate null, occupancy, Customer state and the Customer's existing table before seating; and allow release only for `PAID` or `LEFT` occupants.
Please check the order of those checks without changing any public method signatures.

### Codex

*(Codex used tools: GitHub)*

The existing API names the identifier `number`, not `id`, so preserve `number`, `getNumber()` and `Table(int number)`.
The correct order is:

1. validate the constructor's positive table number;
2. reject a null customer;
3. reject an occupied table;
4. reject a customer who is not queued or is already linked to a table;
5. assign `occupant` only after every check succeeds;
6. return immediately when releasing a free table;
7. otherwise verify `PAID` or `LEFT` before clearing the occupant.

### Me (Thu 24 Sep 2026, 15:05 SGT)

Give me the minimal `Table.java`. Do not modify `Customer` or add Part C's staff logic. Also tell me which tests to run.

### Codex

```java
package restaurantrush;
public final class Table {
    private final int number;
    private Customer occupant;
    public Table(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("Table number must be positive.");
        }
        this.number = number;
    }
    public int getNumber() { return number; }
    public Customer getOccupant() { return occupant; }
    public boolean isFree() { return occupant == null; }
    public void seat(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is occupied.");
        }
        if (customer.getState() != CustomerState.QUEUED
                || customer.getTable() != null) {
            throw new IllegalStateException(
                    "Customer " + customer.getId() + " is not waiting for a table.");
        }
        occupant = customer;
    }
    public void release() {
        if (isFree()) {
            return;
        }
        CustomerState state = occupant.getState();
        if (state != CustomerState.PAID && state != CustomerState.LEFT) {
            throw new IllegalStateException(
                    "Table " + number + " cannot be released before its customer leaves.");
        }
        occupant = null;
    }
}
```
Run the focused Table cases in `OrderTableTests`, then `CustomerTests` to ensure Part A still passes, and finally the smoke suite. Do not change Customer merely to make a Table test pass.

### Me (Thu 24 Sep 2026, 15:12 SGT)

The Table design is settled. Now give me a complete but minimal Stage 1 `Order.java` using the state transitions we defined.
It must lock the price at construction, advance exactly one preparation unit per call, record `readyTurn`, prevent same‑turn service, allow only READY to become SERVED and only SERVED to become PAID, and allow cancellation only before service. Illegal operations must not partially modify state.

### Codex

*(Codex used tools: GitHub)*

```java
package restaurantrush;
import java.util.Objects;
public final class Order {
    private final Customer customer;
    private final MenuItem item;
    private final int lockedPriceCents;
    private int unitsDone;
    private OrderStatus status = OrderStatus.PLACED;
    private int readyTurn = -1;
    public Order(Customer customer, MenuItem item) {
        this.customer = Objects.requireNonNull(customer, "customer");
        this.item = Objects.requireNonNull(item, "item");
        this.lockedPriceCents = item.getPriceCents();
    }
    public Customer getCustomer() { return customer; }
    public MenuItem getItem() { return item; }
    public int getLockedPriceCents() { return lockedPriceCents; }
    public int getUnitsDone() { return unitsDone; }
    public OrderStatus getStatus() { return status; }
    public int getReadyTurn() { return readyTurn; }
    public void advance(int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Preparation turn must be positive.");
        }
        if (status != OrderStatus.PLACED && status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Only an unfinished order can be prepared.");
        }
        unitsDone++;
        if (unitsDone == item.getPrepUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
        } else {
            status = OrderStatus.PREPARING;
        }
    }
    public boolean isServable(int turn) {
        return status == OrderStatus.READY && turn > readyTurn;
    }
    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Only a ready order can be served.");
        }
        status = OrderStatus.SERVED;
    }
    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Only a served order can be paid.");
        }
        status = OrderStatus.PAID;
    }
    public void cancel() {
        if (status != OrderStatus.PLACED
                && status != OrderStatus.PREPARING
                && status != OrderStatus.READY) {
            throw new IllegalStateException("Only an unfinished order can be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }
}
```

### Me (Thu 24 Sep 2026, 15:18 SGT)

What are the three most likely mistakes, and how should I verify this stage?

### Codex

The three main risks are:

1. reading the MenuItem price again at payment instead of copying it at construction;
2. using `turn >= readyTurn`, which permits same‑turn service, instead of `turn > readyTurn`;
3. mutating `unitsDone` or status before validation, which corrupts terminal orders after a rejected action.

Verify with:
```bash
sh scripts/build.sh
java -cp out/main:out/test restaurantrush.OrderTableTests
java -cp out/main:out/test restaurantrush.CustomerTests
sh scripts/test.sh
```
The focused expectation is `PASS 14/14 Part B table/order scenarios`. If a controller test still fails while the focused suites pass, first check whether Part C's staff methods remain incomplete rather than weakening Order's domain rules.
