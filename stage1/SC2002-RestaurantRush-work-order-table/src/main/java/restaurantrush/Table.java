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
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is occupied.");
        }
        if (customer.getState() != CustomerState.QUEUED || customer.getTable() != null) {
            throw new IllegalStateException("Customer " + customer.getId() + " is not waiting for a table.");
        }
        occupant = customer;
    }

    public void release() {
        if (isFree()) {
            return;
        }
        CustomerState state = occupant.getState();
        if (state != CustomerState.PAID && state != CustomerState.LEFT) {
            throw new IllegalStateException("Table " + number + " cannot be released before its customer leaves.");
        }
        occupant = null;
    }
}
