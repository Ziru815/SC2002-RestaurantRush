package restaurantrush;

/** Owner C. Validate everything before mutating; markActed only after success. */
public final class Waiter extends Staff {
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

    public Order takeOrder(Customer customer, Menu menu) {
        MenuItem item = chooseOrderItem(customer, menu);
        Order order = new Order(customer, item);
        customer.placeOrder(order);
        markActed();
        return order;
    }

    /** Stage 3: quote once through the policy and lock that final bill. */
    public Order takeOrder(Customer customer, Menu menu, HappyHourPolicy policy, int turn) {
        requireAvailable();
        if (policy == null) {
            throw new IllegalArgumentException("Happy Hour policy is required.");
        }
        if (turn < 1 || turn > GameConfig.MAX_TURNS) {
            throw new IllegalArgumentException("Turn must be between 1 and " + GameConfig.MAX_TURNS + ".");
        }
        MenuItem item = chooseOrderItem(customer, menu);
        int finalPriceCents = policy.quote(customer, item, turn);
        Order order = new Order(customer, item, finalPriceCents);
        customer.placeOrder(order);
        markActed();
        return order;
    }

    /** Shared preconditions keep both ordering paths compatible. */
    private MenuItem chooseOrderItem(Customer customer, Menu menu) {
        requireAvailable();
        if (customer == null || menu == null) {
            throw new IllegalArgumentException("Customer and menu are required.");
        }
        if (customer.getState() != CustomerState.SEATED) {
            throw new IllegalStateException("Customer " + customer.getId() + " is not seated.");
        }
        if (customer.getTable() == null || customer.getTable().getOccupant() != customer) {
            throw new IllegalStateException("Customer " + customer.getId() + " has no occupied table.");
        }
        if (customer.getOrder() != null) {
            throw new IllegalStateException("Customer " + customer.getId() + " already has an order.");
        }

        MenuItem item = customer.chooseMeal(menu);
        if (item == null) {
            throw new IllegalStateException("Customer did not choose a menu item.");
        }
        return item;
    }

    public void serve(Order order, int turn) {
        requireAvailable();
        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }
        Customer customer = order.getCustomer();
        if (customer == null || customer.getOrder() != order
                || customer.getState() != CustomerState.SEATED
                || customer.getTable() == null || customer.getTable().getOccupant() != customer) {
            throw new IllegalStateException("Order is not ready to be served.");
        }
        if (!order.isServable(turn)) {
            throw new IllegalStateException("Order is not ready to be served this turn.");
        }

        order.markServed();
        customer.markServed(turn);
        markActed();
    }
}
