package restaurantrush;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/** Owner D. Owns state; turn 0 deliberately means the game has not started. */
public final class Restaurant {
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> activeOrders = new ArrayList<>();
    private final Menu menu;
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final boolean stage3Enabled;
    private final Host host;
    private final HappyHourPolicy happyHour;
    private int turn;
    private int servedCount;
    private int unhappyDepartures;

    /** Stage 1 and 2 setup: three dishes, no Host, no Happy Hour. */
    public Restaurant() {
        this(false);
    }

    /** Stage 3 setup adds the combo to the menu, one Host and one Happy Hour policy. */
    public Restaurant(boolean stage3Enabled) {
        this.stage3Enabled = stage3Enabled;
        this.menu = new Menu(stage3Enabled);
        this.host = stage3Enabled ? new Host() : null;
        this.happyHour = stage3Enabled ? new HappyHourPolicy() : null;
    }

    public List<Table> getTables() { return tables; }
    public List<Customer> getWaitingQueue() { return List.copyOf(waitingQueue); }
    public List<Customer> getCustomers() { return List.copyOf(customers); }
    public List<Order> getActiveOrders() { return List.copyOf(activeOrders); }
    public Menu getMenu() { return menu; }
    public Waiter getWaiter() { return waiter; }
    public Chef getChef() { return chef; }
    public Cashier getCashier() { return cashier; }
    public boolean isStage3Enabled() { return stage3Enabled; }

    public Host getHost() {
        requireStage3("Host");
        return host;
    }

    public HappyHourPolicy getHappyHour() {
        requireStage3("Happy Hour");
        return happyHour;
    }

    public int getTurn() { return turn; }
    public int getServedCount() { return servedCount; }
    public int getUnhappyDepartures() { return unhappyDepartures; }

    public Table findFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst().orElse(null);
    }

    public Customer findQueuedCustomer(int id) {
        return waitingQueue.stream().filter(customer -> customer.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Customer " + id + " is not in the waiting queue."));
    }

    /** Any customer who has arrived, whatever their state; the staff check the state themselves. */
    public Customer findCustomer(int id) {
        return customers.stream().filter(customer -> customer.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no customer " + id + "."));
    }

    public Table findTable(int number) {
        return tables.stream().filter(table -> table.getNumber() == number).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no table " + number + "."));
    }

    /** The order the customer has placed, if any. */
    public Order getOrder(int customerId) {
        Order order = findCustomer(customerId).getOrder();
        if (order == null) {
            throw new IllegalArgumentException("Customer " + customerId + " has no order.");
        }
        return order;
    }

    public void nextTurn() {
        if (turn >= GameConfig.MAX_TURNS) {
            throw new IllegalStateException("All " + GameConfig.MAX_TURNS + " turns have been played.");
        }
        turn++;
    }

    public void addArrival(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Arriving customer is missing.");
        }
        if (customer.getState() != CustomerState.QUEUED) {
            throw new IllegalArgumentException("An arriving customer must be QUEUED.");
        }
        if (customers.stream().anyMatch(known -> known.getId() == customer.getId())) {
            throw new IllegalArgumentException("Customer " + customer.getId() + " has already arrived.");
        }
        customers.add(customer);
        waitingQueue.add(customer);
    }

    public void removeFromQueue(Customer customer) {
        if (!waitingQueue.remove(customer)) {
            throw new IllegalArgumentException("Customer " + (customer == null ? "?" : customer.getId()) + " is not in the queue.");
        }
    }

    public void addOrder(Order order) {
        if (order == null || activeOrders.contains(order)) {
            throw new IllegalArgumentException("That order is missing or already registered.");
        }
        activeOrders.add(order);
    }

    public void removeOrder(Order order) {
        if (!activeOrders.remove(order)) {
            throw new IllegalArgumentException("That order is not active.");
        }
    }

    public void recordServed() {
        servedCount++;
    }

    public void handleDeparture(Customer customer) {
        if (customer == null || !customers.contains(customer)) {
            throw new IllegalArgumentException("Unknown customer.");
        }
        boolean waiting = customer.getState() == CustomerState.QUEUED || customer.getState() == CustomerState.SEATED;
        if (!waiting || customer.getSatisfaction() != 0) {
            throw new IllegalStateException("Only a waiting customer at satisfaction 0 can leave unhappy.");
        }
        Table oldTable = customer.getTable();
        Order order = customer.getOrder();
        if (order != null) {
            order.cancel();
            activeOrders.remove(order);
        }
        customer.leave();
        if (oldTable != null) {
            oldTable.release();
        }
        waitingQueue.remove(customer);
        unhappyDepartures++;
    }

    private void requireStage3(String what) {
        if (!stage3Enabled) {
            throw new IllegalStateException(what + " only exists in Stage 3.");
        }
    }
}
