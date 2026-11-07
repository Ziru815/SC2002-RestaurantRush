package restaurantrush;

/** Focused Stage 2 checks for polymorphic quotes and immutable order bills. */
public final class Stage2OrderTests {
    private static int passed;

    private Stage2OrderTests() { }

    public static void main(String[] args) {
        testCustomerTypeQuotesAreLocked();
        testQuoteIsRequestedExactlyOnceForTheOrderedItem();
        testLaterQuoteChangesDoNotChangeTheLockedBill();
        testConstructorValidatesReferencesBeforeQuoting();
        testOrderingDoesNotMutateTheMenuItemPrice();
        testPreparationUnitsAndReadyTurnRemainUnchanged();
        testReadyOrderCanOnlyBeServedOnALaterTurn();
        testCancellationAndPaymentGuardsRemainUnchanged();
        System.out.println("PASS " + passed + "/8 Stage 2 order pricing scenarios");
    }

    private static void testCustomerTypeQuotesAreLocked() {
        MenuItem burger = new Menu().find("Burger");

        Order regular = new Order(new RegularCustomer(1), burger);
        Order vip = new Order(new VIPCustomer(2), burger);
        Order critic = new Order(new CriticCustomer(4), burger);

        check(regular.getLockedPriceCents() == 1500,
                "a Regular customer's Burger bill must remain the full 1500 cents");
        check(vip.getLockedPriceCents() == 1350,
                "a VIP customer's Burger bill must lock the discounted 1350 cents");
        check(critic.getLockedPriceCents() == 1500,
                "a Critic customer's Burger bill must remain the full 1500 cents");
        passed++;
    }

    private static void testQuoteIsRequestedExactlyOnceForTheOrderedItem() {
        MenuItem burger = new Menu().find("Burger");
        MutableQuoteCustomer customer = new MutableQuoteCustomer(1, 1234);

        Order order = new Order(customer, burger);
        order.getLockedPriceCents();
        order.getLockedPriceCents();

        check(customer.getQuoteCalls() == 1,
                "Order construction must request exactly one customer quote");
        check(customer.getLastQuotedItem() == burger,
                "Order must quote the same MenuItem that it stores");
        check(order.getLockedPriceCents() == 1234,
                "Order must store the single polymorphic quote");
        passed++;
    }

    private static void testLaterQuoteChangesDoNotChangeTheLockedBill() {
        MutableQuoteCustomer customer = new MutableQuoteCustomer(1, 1200);
        Order order = new Order(customer, new Menu().find("Salad"));

        customer.setQuoteCents(700);

        check(order.getLockedPriceCents() == 1200,
                "a later customer-pricing change must not alter an existing order");
        check(customer.getQuoteCalls() == 1,
                "reading a locked bill must never request another quote");
        passed++;
    }

    private static void testConstructorValidatesReferencesBeforeQuoting() {
        MenuItem burger = new Menu().find("Burger");
        expect(NullPointerException.class, () -> new Order(null, burger));

        MutableQuoteCustomer customer = new MutableQuoteCustomer(1, 1000);
        expect(NullPointerException.class, () -> new Order(customer, null));
        check(customer.getQuoteCalls() == 0,
                "a missing MenuItem must be rejected before customer pricing is invoked");
        passed++;
    }

    private static void testOrderingDoesNotMutateTheMenuItemPrice() {
        MenuItem burger = new Menu().find("Burger");

        Order vipOrder = new Order(new VIPCustomer(2), burger);

        check(vipOrder.getLockedPriceCents() == 1350,
                "the VIP order must retain its discounted bill");
        check(burger.getPriceCents() == 1500,
                "customer discounts must not modify the shared MenuItem price");
        passed++;
    }

    private static void testPreparationUnitsAndReadyTurnRemainUnchanged() {
        Order pasta = new Order(new MutableQuoteCustomer(1, 1620), new Menu().find("Pasta"));

        pasta.advance(3);
        check(pasta.getUnitsDone() == 1 && pasta.getStatus() == OrderStatus.PREPARING
                        && pasta.getReadyTurn() == -1,
                "the first Pasta unit must leave the discounted order PREPARING");

        pasta.advance(4);
        check(pasta.getUnitsDone() == 2 && pasta.getStatus() == OrderStatus.READY
                        && pasta.getReadyTurn() == 4,
                "the second Pasta unit must record the actual READY turn");
        check(pasta.getLockedPriceCents() == 1620,
                "preparation must not change the locked bill");
        passed++;
    }

    private static void testReadyOrderCanOnlyBeServedOnALaterTurn() {
        Order order = new Order(new MutableQuoteCustomer(1, 810), new Menu().find("Salad"));

        order.advance(5);
        check(!order.isServable(5),
                "an order must not be servable on the turn it becomes READY");
        check(order.isServable(6),
                "an order must be servable on a later turn");

        order.markServed();
        check(order.getStatus() == OrderStatus.SERVED && !order.isServable(7),
                "a served order must enter SERVED and stop being servable");
        passed++;
    }

    private static void testCancellationAndPaymentGuardsRemainUnchanged() {
        Order cancelled = new Order(new MutableQuoteCustomer(1, 1350), new Menu().find("Burger"));
        cancelled.cancel();
        expect(IllegalStateException.class, () -> cancelled.advance(2));
        expect(IllegalStateException.class, cancelled::markServed);
        expect(IllegalStateException.class, cancelled::markPaid);
        check(cancelled.getStatus() == OrderStatus.CANCELLED
                        && cancelled.getLockedPriceCents() == 1350,
                "rejected transitions must preserve a cancelled order and its bill");

        Order paid = new Order(new MutableQuoteCustomer(2, 1500), new Menu().find("Burger"));
        paid.advance(1);
        paid.markServed();
        paid.markPaid();
        expect(IllegalStateException.class, paid::markPaid);
        expect(IllegalStateException.class, paid::cancel);
        check(paid.getStatus() == OrderStatus.PAID && paid.getLockedPriceCents() == 1500,
                "rejected repeated payment must preserve the paid order and its bill");
        passed++;
    }

    /** Test-only customer whose quote can change and whose calls are observable. */
    private static final class MutableQuoteCustomer extends Customer {
        private int quoteCents;
        private int quoteCalls;
        private MenuItem lastQuotedItem;

        private MutableQuoteCustomer(int id, int quoteCents) {
            super(id);
            this.quoteCents = quoteCents;
        }

        @Override
        public int waitingLoss() {
            return 8;
        }

        @Override
        public int priceFor(MenuItem item) {
            if (item == null) {
                throw new IllegalArgumentException("Menu item is required.");
            }
            quoteCalls++;
            lastQuotedItem = item;
            return quoteCents;
        }

        private int getQuoteCalls() {
            return quoteCalls;
        }

        private MenuItem getLastQuotedItem() {
            return lastQuotedItem;
        }

        private void setQuoteCents(int quoteCents) {
            this.quoteCents = quoteCents;
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
