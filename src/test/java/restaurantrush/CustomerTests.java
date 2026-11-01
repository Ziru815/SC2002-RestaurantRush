package restaurantrush;

import java.lang.reflect.Field;

/** Focused Stage 1 checks for owner A's customer and menu behavior. */
public final class CustomerTests {
    private static int passed;

    private CustomerTests() { }

    public static void main(String[] args) {
        testRegularWaitingLossAndFloor();
        testMealChoiceCyclesByCustomerId();
        testMealChoiceRejectsMissingMenu();
        testCustomerCanBeSeatedOnce();
        testSeatingRejectsMissingOrRepeatedTable();
        testCustomerAcceptsOnlyOwnFirstOrder();
        testServiceRequiresTheCustomersServedOrder();
        testEatingAndPaymentRequireLaterTurns();
        testPaidCustomerClearsTableButKeepsOrderHistory();
        testServedCustomerStopsLosingSatisfaction();
        testDepartureRequiresZeroSatisfactionWhileWaiting();
        System.out.println("PASS " + passed + "/11 Part A customer scenarios");
    }

    private static void testRegularWaitingLossAndFloor() {
        Customer customer = new RegularCustomer(1);
        customer.loseWaitingSatisfaction();
        check(customer.getSatisfaction() == 92, "a Regular customer must lose exactly 8 satisfaction");
        for (int turn = 0; turn < 20; turn++) {
            customer.loseWaitingSatisfaction();
        }
        check(customer.getSatisfaction() == 0, "satisfaction must stop at zero rather than become negative");
        passed++;
    }

    private static void testMealChoiceCyclesByCustomerId() {
        Menu menu = new Menu();
        check(new RegularCustomer(1).chooseMeal(menu).getName().equals("Salad"), "customer 1 must choose Salad");
        check(new RegularCustomer(2).chooseMeal(menu).getName().equals("Burger"), "customer 2 must choose Burger");
        check(new RegularCustomer(3).chooseMeal(menu).getName().equals("Pasta"), "customer 3 must choose Pasta");
        check(new RegularCustomer(4).chooseMeal(menu).getName().equals("Salad"), "the menu cycle must repeat for customer 4");
        passed++;
    }

    private static void testMealChoiceRejectsMissingMenu() {
        expect(IllegalArgumentException.class, () -> new RegularCustomer(1).chooseMeal(null));
        passed++;
    }

    private static void testCustomerCanBeSeatedOnce() {
        Customer customer = new RegularCustomer(1);
        Table table = new Table(1);
        customer.seat(table);
        check(customer.getState() == CustomerState.SEATED, "seating must change the customer state to SEATED");
        check(customer.getTable() == table, "seating must retain the assigned table");
        passed++;
    }

    private static void testSeatingRejectsMissingOrRepeatedTable() {
        Customer customer = new RegularCustomer(1);
        expect(IllegalArgumentException.class, () -> customer.seat(null));
        check(customer.getState() == CustomerState.QUEUED && customer.getTable() == null,
                "a failed seating attempt must not mutate the customer");
        Table first = new Table(1);
        customer.seat(first);
        expect(IllegalStateException.class, () -> customer.seat(new Table(2)));
        check(customer.getTable() == first, "repeated seating must preserve the original table");
        passed++;
    }

    private static void testCustomerAcceptsOnlyOwnFirstOrder() {
        MenuItem salad = new Menu().find("Salad");
        Customer customer = new RegularCustomer(1);
        customer.seat(new Table(1));
        Customer other = new RegularCustomer(2);
        Order foreignOrder = new Order(other, salad);
        expect(IllegalArgumentException.class, () -> customer.placeOrder(foreignOrder));
        check(customer.getOrder() == null, "a foreign order must not be attached");

        Order first = new Order(customer, salad);
        customer.placeOrder(first);
        check(customer.getOrder() == first, "the customer's own order must be attached");
        expect(IllegalStateException.class, () -> customer.placeOrder(new Order(customer, salad)));
        check(customer.getOrder() == first, "a repeated order must preserve the original order");
        passed++;
    }

    private static void testServiceRequiresTheCustomersServedOrder() {
        Customer customer = customerWithPlacedOrder(1);
        expect(IllegalStateException.class, () -> customer.markServed(3));
        check(customer.getState() == CustomerState.SEATED && customer.getServedTurn() == -1,
                "failed service must not mutate the customer");

        setOrderStatus(customer.getOrder(), OrderStatus.SERVED);
        customer.markServed(3);
        check(customer.getState() == CustomerState.SERVED && customer.getServedTurn() == 3,
                "a served order must move the customer to SERVED and record the turn");
        expect(IllegalStateException.class, () -> customer.markServed(4));
        passed++;
    }

    private static void testEatingAndPaymentRequireLaterTurns() {
        Customer customer = servedCustomer(1, 3);
        customer.finishEating(3);
        check(customer.getState() == CustomerState.SERVED && customer.getReadyToPayTurn() == -1,
                "a customer cannot finish eating on the service turn");
        customer.finishEating(4);
        check(customer.getState() == CustomerState.READY_TO_PAY && customer.getReadyToPayTurn() == 4,
                "a customer must become READY_TO_PAY on a later turn");
        check(!customer.canPay(4), "a customer cannot pay on the turn they become READY_TO_PAY");
        check(customer.canPay(5), "a customer may pay on the following turn");
        passed++;
    }

    private static void testPaidCustomerClearsTableButKeepsOrderHistory() {
        Customer customer = servedCustomer(1, 3);
        customer.finishEating(4);
        Order order = customer.getOrder();
        expect(IllegalStateException.class, customer::markPaid);
        setOrderStatus(order, OrderStatus.PAID);
        customer.markPaid();
        check(customer.getState() == CustomerState.PAID, "payment must move the customer to PAID");
        check(customer.getTable() == null, "payment must clear the customer-side table reference");
        check(customer.getOrder() == order, "payment must preserve the customer's order history");
        passed++;
    }

    private static void testServedCustomerStopsLosingSatisfaction() {
        Customer customer = customerWithPlacedOrder(1);
        customer.loseWaitingSatisfaction();
        check(customer.getSatisfaction() == 92, "the seated customer must lose satisfaction before service");
        setOrderStatus(customer.getOrder(), OrderStatus.SERVED);
        customer.markServed(3);
        customer.loseWaitingSatisfaction();
        check(customer.getSatisfaction() == 92, "a served customer must stop losing satisfaction");
        passed++;
    }

    private static void testDepartureRequiresZeroSatisfactionWhileWaiting() {
        Customer customer = new RegularCustomer(1);
        expect(IllegalStateException.class, customer::leave);
        while (customer.getSatisfaction() > 0) {
            customer.loseWaitingSatisfaction();
        }
        customer.leave();
        check(customer.getState() == CustomerState.LEFT && customer.getTable() == null,
                "a waiting customer at zero satisfaction must be able to leave");

        Customer served = servedCustomer(2, 3);
        expect(IllegalStateException.class, served::leave);
        passed++;
    }

    private static Customer customerWithPlacedOrder(int id) {
        Customer customer = new RegularCustomer(id);
        customer.seat(new Table(1));
        Order order = new Order(customer, customer.chooseMeal(new Menu()));
        customer.placeOrder(order);
        return customer;
    }

    private static Customer servedCustomer(int id, int turn) {
        Customer customer = customerWithPlacedOrder(id);
        setOrderStatus(customer.getOrder(), OrderStatus.SERVED);
        customer.markServed(turn);
        return customer;
    }

    private static void setOrderStatus(Order order, OrderStatus status) {
        try {
            Field field = Order.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(order, status);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("Unable to establish the Order test precondition", error);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + error, error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
