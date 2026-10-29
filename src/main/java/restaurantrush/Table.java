package restaurantrush;

/** Owner B. Current table/customer association is optional at both ends. */
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
        // TODO[B]: Require a free table and QUEUED customer with no table; set occupant.
        // This updates only the table side. Waiter also calls Customer.seat(this).
        throw new UnsupportedOperationException("TODO[B] Table.seat");
    }

    public void release() {
        // TODO[B]: Permit release only after occupant is PAID or LEFT; clear occupant.
        // A free table may be released as a no-op. Do not free merely on service.
        throw new UnsupportedOperationException("TODO[B] Table.release");
    }
}
