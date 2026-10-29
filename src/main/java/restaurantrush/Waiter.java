package restaurantrush;

/** Owner C. Validate everything before mutating; markActed only after success. */
public final class Waiter extends Staff {
    public void seat(Customer customer, Table table) {
        requireAvailable();
        // TODO[C]: Validate both objects. Table.seat(customer), Customer.seat(table).
        // D removes queued customer only after this succeeds.
        throw new UnsupportedOperationException("TODO[C] Waiter.seat");
    }

    public Order takeOrder(Customer customer, Menu menu) {
        requireAvailable();
        // TODO[C]: Require seated/no order, ask customer.chooseMeal(menu), construct Order,
        // attach with customer.placeOrder, markActed, return it. D registers active order.
        throw new UnsupportedOperationException("TODO[C] Waiter.takeOrder");
    }

    public void serve(Order order, int turn) {
        requireAvailable();
        // TODO[C]: Validate customer SEATED and order.isServable(turn), then update order
        // and customer, markActed. D records served count once after successful service.
        throw new UnsupportedOperationException("TODO[C] Waiter.serve");
    }
}
