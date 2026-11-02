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

    /** Stage 2 extension contract: quote in cents; Order locks it when created. */
    public int priceFor(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item is required.");
        }
        return item.getPriceCents();
    }

    /** Display metadata only. Game rules must dispatch through behavior methods. */
    public String getTypeLabel() { return "Regular"; }

    public void loseWaitingSatisfaction() {
        if (state == CustomerState.QUEUED || state == CustomerState.SEATED) {
            satisfaction = Math.max(0, satisfaction - waitingLoss());
        }
    }

    public MenuItem chooseMeal(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu is required.");
        }
        String itemName = switch ((id - 1) % 3) {
            case 0 -> "Salad";
            case 1 -> "Burger";
            default -> "Pasta";
        };
        return menu.find(itemName);
    }

    public void seat(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table is required.");
        }
        if (state != CustomerState.QUEUED || this.table != null) {
            throw new IllegalStateException("Customer " + id + " is not waiting to be seated.");
        }
        this.table = table;
        state = CustomerState.SEATED;
    }

    public void placeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }
        if (state != CustomerState.SEATED) {
            throw new IllegalStateException("Customer " + id + " is not seated.");
        }
        if (this.order != null) {
            throw new IllegalStateException("Customer " + id + " already has an order.");
        }
        if (order.getCustomer() != this) {
            throw new IllegalArgumentException("The order belongs to another customer.");
        }
        this.order = order;
    }

    public void markServed(int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Service turn must be positive.");
        }
        if (state != CustomerState.SEATED || order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("Customer " + id + " does not have a served order.");
        }
        state = CustomerState.SERVED;
        servedTurn = turn;
    }

    public void finishEating(int turn) {
        if (state == CustomerState.SERVED && turn > servedTurn) {
            state = CustomerState.READY_TO_PAY;
            readyToPayTurn = turn;
        }
    }

    public boolean canPay(int turn) {
        return state == CustomerState.READY_TO_PAY && turn > readyToPayTurn;
    }

    public void markPaid() {
        if (state != CustomerState.READY_TO_PAY || order == null || order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Customer " + id + " is not ready with a paid order.");
        }
        state = CustomerState.PAID;
        table = null;
    }

    public void leave() {
        if ((state != CustomerState.QUEUED && state != CustomerState.SEATED) || satisfaction != 0) {
            throw new IllegalStateException("Only an unserved waiting customer at satisfaction 0 may leave.");
        }
        state = CustomerState.LEFT;
        table = null;
    }
}
