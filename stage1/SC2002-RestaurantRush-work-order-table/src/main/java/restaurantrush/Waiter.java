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
        requireAvailable();
        if (customer == null || menu == null) {
            throw new IllegalArgumentException("Customer and menu are required.");
        }
        if (customer.getState() != CustomerState.SEATED) {
            throw new IllegalStateException("Customer " + customer.getId() + " is not seated.");
        }
        if (customer.getOrder() != null) {
            throw new IllegalStateException("Customer " + customer.getId() + " already has an order.");
        }

        MenuItem item = customer.chooseMeal(menu);
        if (item == null) {
            throw new IllegalStateException("Customer did not choose a menu item.");
        }
        Order order = new Order(customer, item);
        customer.placeOrder(order);
        markActed();
        return order;
    }

    public void serve(Order order, int turn) {
        requireAvailable();
        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }
        Customer customer = order.getCustomer();
        if (customer == null || customer.getOrder() != order
                || customer.getState() != CustomerState.SEATED) {
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
