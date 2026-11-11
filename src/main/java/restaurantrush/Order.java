package restaurantrush;

import java.util.Objects;

/** Owner B. Lock the bill at creation; preserve READY turn to prevent early serving. */
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
        // Capture the polymorphic quote once. Later pricing changes must not
        // alter an order that has already been placed.
        this.lockedPriceCents = this.customer.priceFor(this.item);
    }

    /** Stage 3: the caller has already obtained the final, policy-adjusted quote. */
    public Order(Customer customer, MenuItem item, int lockedPriceCents) {
        this.customer = Objects.requireNonNull(customer, "customer");
        this.item = Objects.requireNonNull(item, "item");
        if (lockedPriceCents < 0) {
            throw new IllegalArgumentException("Locked price cannot be negative.");
        }
        this.lockedPriceCents = lockedPriceCents;
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
