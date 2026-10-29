package restaurantrush;

/** Owner A. Implement transitions here, without CLI reads or prints. */
public abstract class Customer {
    private final int id;
    private int satisfaction = 100;
    private CustomerState state = CustomerState.QUEUED;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;

    protected Customer(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.id = id;
    }

    public int getId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerState getState() { return state; }
    public Table getTable() { return table; }
    public Order getOrder() { return order; }
    public int getServedTurn() { return servedTurn; }
    public int getReadyToPayTurn() { return readyToPayTurn; }

    public abstract int waitingLoss();

    public void loseWaitingSatisfaction() {
        // TODO[A]: Only QUEUED/SEATED lose waitingLoss(); clamp to 0. D handles departure.
        throw new UnsupportedOperationException("TODO[A] Customer.loseWaitingSatisfaction");
    }

    public MenuItem chooseMeal(Menu menu) {
        // TODO[A]: Customer chooses using ID cycle 1=Salad, 2=Burger, 3=Pasta, repeat.
        // The manager must never choose the dish. This rule needs no random seed.
        throw new UnsupportedOperationException("TODO[A] Customer.chooseMeal");
    }

    public void seat(Table table) {
        // TODO[A]: Require QUEUED and set the customer-side table reference and SEATED.
        // C validates and links both sides, then D removes this customer from the queue.
        throw new UnsupportedOperationException("TODO[A] Customer.seat");
    }

    public void placeOrder(Order order) {
        // TODO[A]: Require SEATED, no existing order, and order.getCustomer() == this.
        throw new UnsupportedOperationException("TODO[A] Customer.placeOrder");
    }

    public void markServed(int turn) {
        // TODO[A]: Require an attached SERVED order, then record state and servedTurn.
        throw new UnsupportedOperationException("TODO[A] Customer.markServed");
    }

    public void finishEating(int turn) {
        // TODO[A]: If SERVED and turn > servedTurn, become READY_TO_PAY and record turn.
        // For other states or same-turn service this is a no-op.
        throw new UnsupportedOperationException("TODO[A] Customer.finishEating");
    }

    public boolean canPay(int turn) {
        // TODO[A]: state == READY_TO_PAY and turn > readyToPayTurn.
        throw new UnsupportedOperationException("TODO[A] Customer.canPay");
    }

    public void markPaid() {
        // TODO[A]: Require READY_TO_PAY and a PAID order; set PAID and clear table ref.
        // Cashier retains the old Table reference and releases its occupant.
        throw new UnsupportedOperationException("TODO[A] Customer.markPaid");
    }

    public void leave() {
        // TODO[A]: Only an unserved QUEUED/SEATED customer at satisfaction 0 may leave.
        // Set LEFT and clear table reference. D cancels order and releases old table.
        throw new UnsupportedOperationException("TODO[A] Customer.leave");
    }
}
