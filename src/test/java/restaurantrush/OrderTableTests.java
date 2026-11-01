package restaurantrush;

/** Focused Stage 1 checks for Owner B's table and order lifecycle. */
public final class OrderTableTests {
    private static int passed;

    private OrderTableTests() { }

    public static void main(String[] args) {
        testFreeTableSeatsOnlyQueuedCustomerWithoutUpdatingCustomerSide();
        testInvalidSeatingPreservesTableOccupant();
        testFreeTableReleaseIsANoOpAndPrematureReleaseIsRejected();
        testPaidOrLeftOccupantAllowsRelease();
        testOneUnitDishesBecomeReadyAfterOneAdvance();
        testPastaNeedsTwoAdvancesAndRecordsCompletionTurn();
        testAdvanceRejectsInvalidTurnsAndCompletedWorkWithoutMutation();
        testReadyOrderCanOnlyBeServedOnALaterTurn();
        testMarkServedRequiresAReadyOrder();
        testOnlyServedOrderCanBePaid();
        testUnfinishedOrdersCanBeCancelled();
        testServedPaidAndCancelledOrdersRejectCancellation();
        testTerminalOrdersRejectFurtherTransitions();
        testOrderLocksTheConstructionPrice();
        System.out.println("PASS " + passed + "/14 Part B table/order scenarios");
    }

    private static void testFreeTableSeatsOnlyQueuedCustomerWithoutUpdatingCustomerSide() {
        Table table = new Table(1);
        Customer customer = new RegularCustomer(1);

        table.seat(customer);

        check(table.getOccupant() == customer && !table.isFree(),
                "a free table must retain its queued occupant");
        check(customer.getState() == CustomerState.QUEUED && customer.getTable() == null,
                "Table.seat must not update the customer side");
        passed++;
    }

    private static void testInvalidSeatingPreservesTableOccupant() {
        Table table = new Table(1);
        Customer first = new RegularCustomer(1);
        table.seat(first);

        expect(IllegalArgumentException.class, () -> new Table(2).seat(null));
        expect(IllegalStateException.class, () -> table.seat(new RegularCustomer(2)));
        check(table.getOccupant() == first, "an occupied-table rejection must preserve the original occupant");

        Table otherTable = new Table(2);
        Customer alreadySeated = new RegularCustomer(3);
        alreadySeated.seat(otherTable);
        Table freeTable = new Table(3);
        expect(IllegalStateException.class, () -> freeTable.seat(alreadySeated));
        check(freeTable.isFree(), "rejecting an already-seated customer must leave the table free");

        Customer left = new RegularCustomer(4);
        while (left.getSatisfaction() > 0) {
            left.loseWaitingSatisfaction();
        }
        left.leave();
        expect(IllegalStateException.class, () -> freeTable.seat(left));
        check(freeTable.isFree(), "rejecting a non-QUEUED customer must leave the table free");
        passed++;
    }

    private static void testFreeTableReleaseIsANoOpAndPrematureReleaseIsRejected() {
        Table freeTable = new Table(1);
        freeTable.release();
        check(freeTable.isFree(), "releasing a free table must be a no-op");

        Table occupiedTable = new Table(2);
        Customer customer = new RegularCustomer(1);
        occupiedTable.seat(customer);
        expect(IllegalStateException.class, occupiedTable::release);
        check(occupiedTable.getOccupant() == customer,
                "premature release must preserve the table occupant");
        passed++;
    }

    private static void testPaidOrLeftOccupantAllowsRelease() {
        Table paidTable = new Table(1);
        Customer paid = new RegularCustomer(1);
        paidTable.seat(paid);
        paid.seat(paidTable);
        Order paidOrder = new Order(paid, new Menu().find("Salad"));
        paid.placeOrder(paidOrder);
        paidOrder.advance(1);
        paidOrder.markServed();
        paid.markServed(2);
        paid.finishEating(3);
        paidOrder.markPaid();
        paid.markPaid();
        paidTable.release();
        check(paidTable.isFree(), "a PAID occupant must allow table release");

        Table leftTable = new Table(2);
        Customer left = new RegularCustomer(2);
        leftTable.seat(left);
        left.seat(leftTable);
        while (left.getSatisfaction() > 0) {
            left.loseWaitingSatisfaction();
        }
        left.leave();
        leftTable.release();
        check(leftTable.isFree(), "a LEFT occupant must allow table release");
        passed++;
    }

    private static void testOneUnitDishesBecomeReadyAfterOneAdvance() {
        Customer saladCustomer = new RegularCustomer(1);
        Order salad = new Order(saladCustomer, new Menu().find("Salad"));
        salad.advance(2);
        check(salad.getUnitsDone() == 1 && salad.getStatus() == OrderStatus.READY
                        && salad.getReadyTurn() == 2,
                "one Salad preparation must complete the order on turn 2");

        Customer burgerCustomer = new RegularCustomer(2);
        Order burger = new Order(burgerCustomer, new Menu().find("Burger"));
        burger.advance(7);
        check(burger.getUnitsDone() == 1 && burger.getStatus() == OrderStatus.READY
                        && burger.getReadyTurn() == 7,
                "one Burger preparation must complete the order on turn 7");
        passed++;
    }

    private static void testPastaNeedsTwoAdvancesAndRecordsCompletionTurn() {
        Order pasta = new Order(new RegularCustomer(3), new Menu().find("Pasta"));

        pasta.advance(2);
        check(pasta.getUnitsDone() == 1 && pasta.getStatus() == OrderStatus.PREPARING
                        && pasta.getReadyTurn() == -1,
                "the first Pasta preparation must leave one unit pending");

        pasta.advance(3);
        check(pasta.getUnitsDone() == 2 && pasta.getStatus() == OrderStatus.READY
                        && pasta.getReadyTurn() == 3,
                "the second Pasta preparation must make it READY on turn 3");
        passed++;
    }

    private static void testAdvanceRejectsInvalidTurnsAndCompletedWorkWithoutMutation() {
        Order order = new Order(new RegularCustomer(1), new Menu().find("Salad"));
        expect(IllegalArgumentException.class, () -> order.advance(0));
        check(order.getUnitsDone() == 0 && order.getStatus() == OrderStatus.PLACED
                        && order.getReadyTurn() == -1,
                "an invalid turn must not advance an order");

        order.advance(1);
        expect(IllegalStateException.class, () -> order.advance(2));
        check(order.getUnitsDone() == 1 && order.getStatus() == OrderStatus.READY
                        && order.getReadyTurn() == 1,
                "preparing completed work must preserve its original READY state");
        passed++;
    }

    private static void testReadyOrderCanOnlyBeServedOnALaterTurn() {
        Order order = new Order(new RegularCustomer(1), new Menu().find("Salad"));
        order.advance(4);

        check(!order.isServable(4), "an order cannot be served on its READY turn");
        check(order.isServable(5), "an order must be servable on a later turn");
        order.markServed();
        check(order.getStatus() == OrderStatus.SERVED && !order.isServable(6),
                "a served order must enter SERVED and stop being servable");
        expect(IllegalStateException.class, order::markServed);
        check(order.getStatus() == OrderStatus.SERVED,
                "repeated service must preserve the SERVED state");
        passed++;
    }

    private static void testMarkServedRequiresAReadyOrder() {
        Order order = new Order(new RegularCustomer(1), new Menu().find("Salad"));
        expect(IllegalStateException.class, order::markServed);
        check(order.getStatus() == OrderStatus.PLACED && order.getUnitsDone() == 0,
                "failed early service must preserve the placed order");
        passed++;
    }

    private static void testOnlyServedOrderCanBePaid() {
        Order order = new Order(new RegularCustomer(1), new Menu().find("Salad"));
        expect(IllegalStateException.class, order::markPaid);
        check(order.getStatus() == OrderStatus.PLACED,
                "failed early payment must preserve the PLACED state");

        order.advance(1);
        expect(IllegalStateException.class, order::markPaid);
        check(order.getStatus() == OrderStatus.READY,
                "failed payment before service must preserve the READY state");

        order.markServed();
        order.markPaid();
        check(order.getStatus() == OrderStatus.PAID,
                "a SERVED order must become PAID");
        expect(IllegalStateException.class, order::markPaid);
        check(order.getStatus() == OrderStatus.PAID,
                "repeated payment must preserve the PAID state");
        passed++;
    }

    private static void testUnfinishedOrdersCanBeCancelled() {
        Order placed = new Order(new RegularCustomer(1), new Menu().find("Salad"));
        placed.cancel();
        check(placed.getStatus() == OrderStatus.CANCELLED && placed.getUnitsDone() == 0,
                "a PLACED order must be cancellable without adding preparation");

        Order preparing = new Order(new RegularCustomer(2), new Menu().find("Pasta"));
        preparing.advance(2);
        preparing.cancel();
        check(preparing.getStatus() == OrderStatus.CANCELLED && preparing.getUnitsDone() == 1
                        && preparing.getReadyTurn() == -1,
                "a PREPARING cancellation must retain its recorded work");

        Order ready = new Order(new RegularCustomer(3), new Menu().find("Salad"));
        ready.advance(3);
        ready.cancel();
        check(ready.getStatus() == OrderStatus.CANCELLED && ready.getUnitsDone() == 1
                        && ready.getReadyTurn() == 3,
                "a READY cancellation must retain completion history");
        passed++;
    }

    private static void testServedPaidAndCancelledOrdersRejectCancellation() {
        Order served = servedOrder(1);
        expect(IllegalStateException.class, served::cancel);
        check(served.getStatus() == OrderStatus.SERVED,
                "failed SERVED cancellation must preserve the order");

        Order paid = servedOrder(2);
        paid.markPaid();
        expect(IllegalStateException.class, paid::cancel);
        check(paid.getStatus() == OrderStatus.PAID,
                "failed PAID cancellation must preserve the order");

        Order cancelled = new Order(new RegularCustomer(3), new Menu().find("Salad"));
        cancelled.cancel();
        expect(IllegalStateException.class, cancelled::cancel);
        check(cancelled.getStatus() == OrderStatus.CANCELLED,
                "repeated cancellation must preserve the CANCELLED state");
        passed++;
    }

    private static void testTerminalOrdersRejectFurtherTransitions() {
        Order cancelled = new Order(new RegularCustomer(1), new Menu().find("Salad"));
        cancelled.cancel();
        expect(IllegalStateException.class, () -> cancelled.advance(2));
        expect(IllegalStateException.class, cancelled::markServed);
        expect(IllegalStateException.class, cancelled::markPaid);
        check(cancelled.getStatus() == OrderStatus.CANCELLED && cancelled.getUnitsDone() == 0,
                "illegal CANCELLED transitions must not mutate the order");

        Order paid = servedOrder(2);
        paid.markPaid();
        expect(IllegalStateException.class, () -> paid.advance(3));
        expect(IllegalStateException.class, paid::markServed);
        expect(IllegalStateException.class, paid::markPaid);
        check(paid.getStatus() == OrderStatus.PAID && paid.getUnitsDone() == 1,
                "illegal PAID transitions must not mutate the order");
        passed++;
    }

    private static void testOrderLocksTheConstructionPrice() {
        MutablePriceItem item = new MutablePriceItem("Special", 1200, 1);
        Order order = new Order(new RegularCustomer(1), item);
        item.setPriceCents(2500);

        check(order.getLockedPriceCents() == 1200,
                "an order must retain the price captured at construction");
        passed++;
    }

    private static Order servedOrder(int customerId) {
        Order order = new Order(new RegularCustomer(customerId), new Menu().find("Salad"));
        order.advance(1);
        order.markServed();
        return order;
    }

    private static final class MutablePriceItem extends MenuItem {
        private int currentPriceCents;

        private MutablePriceItem(String name, int priceCents, int prepUnits) {
            super(name, priceCents, prepUnits);
            currentPriceCents = priceCents;
        }

        private void setPriceCents(int priceCents) {
            currentPriceCents = priceCents;
        }

        @Override
        public int getPriceCents() {
            return currentPriceCents;
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
