package restaurantrush;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/** Owner D. Owns state; turn 0 deliberately means this scaffold has not started. */
public final class Restaurant {
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> activeOrders = new ArrayList<>();
    private final Menu menu = new Menu();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private int turn;
    private int servedCount;
    private int unhappyDepartures;

    public List<Table> getTables() { return tables; }
    public List<Customer> getWaitingQueue() { return List.copyOf(waitingQueue); }
    public List<Customer> getCustomers() { return List.copyOf(customers); }
    public List<Order> getActiveOrders() { return List.copyOf(activeOrders); }
    public Menu getMenu() { return menu; }
    public Waiter getWaiter() { return waiter; }
    public Chef getChef() { return chef; }
    public Cashier getCashier() { return cashier; }
    public int getTurn() { return turn; }
    public int getServedCount() { return servedCount; }
    public int getUnhappyDepartures() { return unhappyDepartures; }

    public Table findFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst().orElse(null);
    }

    public Customer findQueuedCustomer(int id) {
        return waitingQueue.stream().filter(customer -> customer.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Customer is not queued: " + id));
    }

    public void nextTurn() {
        // TODO[D]: Increment at turn start only; reject advancing beyond 18.
        throw new UnsupportedOperationException("TODO[D] Restaurant.nextTurn");
    }

    public void addArrival(Customer customer) {
        // TODO[D]: Reject duplicate ID/non-QUEUED customer, add to registry and queue.
        throw new UnsupportedOperationException("TODO[D] Restaurant.addArrival");
    }

    public void removeFromQueue(Customer customer) {
        // TODO[D]: Remove the successfully seated or abandoned customer, not another one.
        throw new UnsupportedOperationException("TODO[D] Restaurant.removeFromQueue");
    }

    public void addOrder(Order order) {
        // TODO[D]: Register once after successful Waiter.takeOrder.
        throw new UnsupportedOperationException("TODO[D] Restaurant.addOrder");
    }

    public void removeOrder(Order order) {
        // TODO[D]: Remove after PAID or CANCELLED; preserve customer's order history.
        throw new UnsupportedOperationException("TODO[D] Restaurant.removeOrder");
    }

    public void recordServed() {
        // TODO[D]: Increment only following successful service, never on preparation.
        throw new UnsupportedOperationException("TODO[D] Restaurant.recordServed");
    }

    public void handleDeparture(Customer customer) {
        // TODO[D]: Validate waiting state/satisfaction 0; retain old table; cancel any
        // unfinished order; customer.leave(); release table/remove queue; increment once.
        throw new UnsupportedOperationException("TODO[D] Restaurant.handleDeparture");
    }
}
