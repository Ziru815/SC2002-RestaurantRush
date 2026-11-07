package restaurantrush;

/** Owner C. Behavioral employee workflow and invalid-action checks. */
public final class Stage2StaffTests {
    private static int passed;

    private Stage2StaffTests() { }

    public static void main(String[] args) {
        testAllCustomerTypesUseTheSameFlow();
        testInvalidActionsDoNotConsumeStaffTasks();
        testPaymentIsOnePerTurnAndStatisticsAreStable();
        System.out.println("PASS " + passed + "/3 Stage 2 staff scenarios");
    }

    private static void testAllCustomerTypesUseTheSameFlow() {
        Customer[] customers = { new RegularCustomer(1), new VIPCustomer(2), new CriticCustomer(4) };
        int[] expectedPrices = { 900, 1350, 1800 };
        for (int i = 0; i < customers.length; i++) {
            Customer customer = customers[i];
            Waiter waiter = new Waiter();
            Chef chef = new Chef();
            Cashier cashier = new Cashier();
            Table table = new Table(i + 1);
            waiter.seat(customer, table);
            check(customer.getState() == CustomerState.SEATED && table.getOccupant() == customer,
                    "seating must update both customer and table");
            waiter.resetTurn();
            Order order = waiter.takeOrder(customer, new Menu());
            check(customer.getOrder() == order && order.getCustomer() == customer,
                    "the customer and order must be linked exactly once");

            chef.resetTurn();
            chef.advance(order, 1);
            check(order.getUnitsDone() == 1, "Chef must advance exactly one preparation unit");
            if (order.getItem().getPrepUnits() == 2) {
                check(order.getStatus() == OrderStatus.PREPARING, "Pasta needs two preparation actions");
                chef.resetTurn();
                chef.advance(order, 2);
            }
            check(order.getStatus() == OrderStatus.READY, "the order must become READY after preparation");

            waiter.resetTurn();
            expect(IllegalStateException.class, () -> waiter.serve(order, order.getReadyTurn()));
            check(!waiter.hasActed(), "same-turn service must not consume Waiter action");
            waiter.serve(order, order.getReadyTurn() + 1);
            check(customer.getState() == CustomerState.SERVED, "service must update customer state");
            customer.finishEating(order.getReadyTurn() + 1);
            check(customer.getState() == CustomerState.SERVED, "eating cannot finish in serving turn");
            customer.finishEating(order.getReadyTurn() + 2);
            check(customer.getState() == CustomerState.READY_TO_PAY, "eating needs a complete turn");

            expect(IllegalStateException.class,
                    () -> cashier.collectPayment(customer, order.getReadyTurn() + 2));
            check(!cashier.hasActed(), "same-turn payment must not consume Cashier action");
            int amount = cashier.collectPayment(customer, order.getReadyTurn() + 3);
            check(amount == expectedPrices[i], "payment must use the locked polymorphic price: expected "
                    + expectedPrices[i] + ", got " + amount + " for " + customer.getTypeLabel());
            check(order.getStatus() == OrderStatus.PAID && customer.getState() == CustomerState.PAID,
                    "successful payment must mark both order and customer PAID");
            check(table.isFree(), "successful payment must release the table");
        }
        passed++;
    }

    private static void testInvalidActionsDoNotConsumeStaffTasks() {
        Waiter waiter = new Waiter();
        Table firstTable = new Table(1);
        Table secondTable = new Table(2);
        Customer seated = new RegularCustomer(10);
        Customer queued = new VIPCustomer(11);
        waiter.seat(seated, firstTable);
        waiter.resetTurn();

        expect(IllegalStateException.class, () -> waiter.seat(seated, secondTable));
        check(!waiter.hasActed() && seated.getTable() == firstTable && firstTable.getOccupant() == seated,
                "seating a non-queued customer must preserve state and task availability");
        expect(IllegalStateException.class, () -> waiter.seat(queued, firstTable));
        check(!waiter.hasActed() && queued.getState() == CustomerState.QUEUED && secondTable.isFree(),
                "an occupied table must reject seating without mutation");
        waiter.seat(queued, secondTable);

        waiter.resetTurn();
        Order firstOrder = waiter.takeOrder(queued, new Menu());
        waiter.resetTurn();
        expect(IllegalStateException.class, () -> waiter.takeOrder(queued, new Menu()));
        check(!waiter.hasActed() && queued.getOrder() == firstOrder,
                "duplicate ordering must not consume the Waiter action");

        Chef chef = new Chef();
        waiter.resetTurn();
        expect(IllegalStateException.class, () -> waiter.serve(firstOrder, 1));
        check(!waiter.hasActed() && firstOrder.getStatus() == OrderStatus.PLACED,
                "an unready order must not be served or consume the task");
        chef.advance(firstOrder, 1);
        chef.resetTurn();
        expect(IllegalStateException.class, () -> chef.advance(firstOrder, 2));
        check(!chef.hasActed() && firstOrder.getStatus() == OrderStatus.READY,
                "completed preparation must reject another Chef action");

        waiter.serve(firstOrder, 2);
        waiter.resetTurn();
        expect(IllegalStateException.class, () -> waiter.serve(firstOrder, 3));
        check(!waiter.hasActed() && firstOrder.getStatus() == OrderStatus.SERVED,
                "repeated service must preserve the served order and task availability");
        passed++;
    }

    private static void testPaymentIsOnePerTurnAndStatisticsAreStable() {
        Cashier cashier = new Cashier();
        Customer regular = readyToPay(new RegularCustomer(20), 1);
        Customer vip = readyToPay(new VIPCustomer(21), 1);
        int regularSatisfaction = regular.getSatisfaction();
        int vipSatisfaction = vip.getSatisfaction();
        cashier.collectPayment(regular, 4);
        check(cashier.getPaidCount() == 1 && cashier.getRevenueCents() == 900,
                "the first payment must be counted exactly once");
        expect(IllegalStateException.class, () -> cashier.collectPayment(vip, 4));
        check(cashier.getPaidCount() == 1 && cashier.getRevenueCents() == 900 && cashier.hasActed(),
                "a second payment in the same turn must not consume the Cashier task");
        cashier.resetTurn();
        cashier.collectPayment(vip, 5);
        check(cashier.getPaidCount() == 2 && cashier.getRevenueCents() == 1710,
                "the next turn must accept the next payment and preserve the discount");
        check(cashier.getAverageSatisfaction() == (regularSatisfaction + vipSatisfaction) / 2.0,
                "average satisfaction must use floating-point calculation");
        check(regular.getSatisfaction() == regularSatisfaction && vip.getSatisfaction() == vipSatisfaction,
                "recorded payment satisfaction must remain stable");
        expect(IllegalStateException.class, () -> cashier.collectPayment(regular, 6));
        check(cashier.getPaidCount() == 2 && cashier.getRevenueCents() == 1710,
                "a paid customer cannot be charged twice");
        passed++;
    }

    private static Customer readyToPay(Customer customer, int servedTurn) {
        Table table = new Table(customer.getId());
        table.seat(customer);
        customer.seat(table);
        Order order = new Order(customer, new Menu().find("Salad"));
        customer.placeOrder(order);
        order.advance(servedTurn);
        order.markServed();
        customer.markServed(servedTurn);
        customer.finishEating(servedTurn + 1);
        check(customer.getState() == CustomerState.READY_TO_PAY, "fixture must use normal eating transition");
        return customer;
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
