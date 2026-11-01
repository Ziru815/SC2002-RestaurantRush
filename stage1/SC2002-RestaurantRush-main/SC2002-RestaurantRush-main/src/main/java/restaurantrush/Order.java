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
        this.lockedPriceCents = item.getPriceCents();
    }

    public Customer getCustomer() { return customer; }
    public MenuItem getItem() { return item; }
    public int getLockedPriceCents() { return lockedPriceCents; }
    public int getUnitsDone() { return unitsDone; }
    public OrderStatus getStatus() { return status; }
    public int getReadyTurn() { return readyTurn; }

    public void advance(int turn) {
        // TODO[B]: PLACED/PREPARING only; add exactly one unit, READY at prepUnits.
        // Set readyTurn only when the last unit completes.
        throw new UnsupportedOperationException("TODO[B] Order.advance");
    }

    public boolean isServable(int turn) {
        // TODO[B]: status == READY and turn > readyTurn.
        throw new UnsupportedOperationException("TODO[B] Order.isServable");
    }

    public void markServed() {
        // TODO[B]: READY -> SERVED. Waiter checks isServable(turn) before calling.
        throw new UnsupportedOperationException("TODO[B] Order.markServed");
    }

    public void markPaid() {
        // TODO[B]: SERVED -> PAID. Cashier checks customer.canPay(turn) first.
        throw new UnsupportedOperationException("TODO[B] Order.markPaid");
    }

    public void cancel() {
        // TODO[B]: PLACED/PREPARING/READY -> CANCELLED; reject served or paid orders.
        throw new UnsupportedOperationException("TODO[B] Order.cancel");
    }
}
