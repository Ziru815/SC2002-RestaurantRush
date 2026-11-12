package restaurantrush;

/** Owner C. Seats one customer per turn independently of the Waiter. */
public final class Host extends Staff {
    public Host() { }

    /** The controller removes the customer from its queue after successful seating. */
    public void seat(Customer customer, Table table) {
        requireAvailable();
        if (customer == null || table == null) {
            throw new IllegalArgumentException("Customer and table are required.");
        }
        if (customer.getState() != CustomerState.QUEUED || customer.getTable() != null) {
            throw new IllegalStateException("Customer is not available for seating.");
        }
        if (!table.isFree()) {
            throw new IllegalStateException("Table is occupied.");
        }

        table.seat(customer);
        customer.seat(table);
        markActed();
    }
}
