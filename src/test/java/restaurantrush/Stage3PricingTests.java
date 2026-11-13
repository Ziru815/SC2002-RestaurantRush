package restaurantrush;

/** Part B: activation, staged discounts and immutable order bills. */
public final class Stage3PricingTests {
    private Stage3PricingTests() { }

    public static void main(String[] args) {
        activationWindow();
        invalidAndLastTurn();
        customerDiscountBeforeHappyHour();
        monetaryBoundaries();
        quoteOnceAndLock();
        invalidQuotesAndOrders();
        checkoutAfterExpiry();
        System.out.println("PASS 7/7 Stage 3 pricing scenarios");
    }

    private static void activationWindow() {
        HappyHourPolicy policy = new HappyHourPolicy();
        check(!policy.hasActivated() && policy.remainingTurns(0) == 0 && !policy.isActive(1), "unused");
        policy.activate(4);
        check(policy.hasActivated() && policy.remainingTurns(3) == 0, "no retroactive activation");
        check(policy.isActive(4) && policy.remainingTurns(4) == 2, "activation turn");
        expect(IllegalStateException.class, () -> policy.activate(5));
        check(policy.isActive(5) && policy.remainingTurns(5) == 1, "second turn, unchanged by retry");
        check(!policy.isActive(6) && policy.remainingTurns(6) == 0, "expired");
        expect(IllegalStateException.class, () -> policy.activate(7));
        check(policy.hasActivated() && !policy.isActive(7), "expired activation cannot be reused");
    }

    private static void invalidAndLastTurn() {
        HappyHourPolicy policy = new HappyHourPolicy();
        expect(IllegalArgumentException.class, () -> policy.activate(0));
        expect(IllegalArgumentException.class, () -> policy.activate(19));
        expect(IllegalArgumentException.class, () -> policy.remainingTurns(-1));
        expect(IllegalArgumentException.class, () -> policy.isActive(-1));
        check(!policy.hasActivated(), "invalid activation must not use the opportunity");
        policy.activate(18);
        check(policy.remainingTurns(18) == 2 && policy.isActive(18), "turn 18 may activate");
    }

    private static void customerDiscountBeforeHappyHour() {
        HappyHourPolicy policy = new HappyHourPolicy();
        MenuItem burger = new Menu().find("Burger");
        Customer vip = new VIPCustomer(2);
        policy.activate(4);
        check(policy.quote(vip, burger, 3) == 1350, "before activation");
        check(policy.quote(vip, burger, 4) == 1080 && policy.quote(vip, burger, 5) == 1080, "VIP + Happy Hour");
        check(policy.quote(vip, burger, 6) == 1350, "VIP after expiry");
        check(policy.quote(new CriticCustomer(4), burger, 4) == 1200 && burger.getPriceCents() == 1500,
                "non-VIP receives Happy Hour only; shared item unchanged");
        check(policy.quote(vip, new MenuItem("Six cents", 6, 1), 4) == 4, "VIP rounding must happen first");
        check(policy.quote(vip, new MenuItem("Two cents", 2, 1), 4) == 2, "round each discount separately");
    }

    private static void monetaryBoundaries() {
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(1);
        Customer customer = new RegularCustomer(1);
        int[] prices = {0, 2, 3, Integer.MAX_VALUE};
        int[] expected = {0, 2, 2, 1_717_986_918};
        for (int i = 0; i < prices.length; i++) {
            check(policy.quote(customer, new MenuItem("Item", prices[i], 1), 1) == expected[i],
                    "rounded cents for " + prices[i]);
        }
    }

    private static void quoteOnceAndLock() {
        MenuItem item = new Menu().find("Salad");
        QuotingCustomer customer = new QuotingCustomer();
        HappyHourPolicy policy = new HappyHourPolicy();
        Order before = new Order(customer, item);
        policy.activate(4);
        Order during = new Order(customer, item, policy.quote(customer, item, 4));
        check(customer.calls == 2, "one call per order; explicit-price constructor must not quote");
        customer.cents = 500;
        int after = policy.quote(customer, item, 6);
        check(after == 500 && before.getLockedPriceCents() == 1000 && during.getLockedPriceCents() == 800,
                "existing orders retain prices across activation, changed customer quote and expiry");
        check(customer.calls == 3 && during.getCustomer() == customer && during.getItem() == item,
                "reading a saved order does not invoke pricing");
    }

    private static void invalidQuotesAndOrders() {
        HappyHourPolicy policy = new HappyHourPolicy();
        QuotingCustomer customer = new QuotingCustomer();
        MenuItem item = new Menu().find("Salad");
        expect(IllegalArgumentException.class, () -> policy.quote(customer, item, 0));
        expect(IllegalArgumentException.class, () -> policy.quote(customer, item, 19));
        expect(NullPointerException.class, () -> policy.quote(null, item, 1));
        expect(NullPointerException.class, () -> policy.quote(customer, null, 1));
        expect(NullPointerException.class, () -> new Order(null, item, 0));
        expect(NullPointerException.class, () -> new Order(customer, null, 0));
        expect(IllegalArgumentException.class, () -> new Order(customer, item, -1));
        check(customer.calls == 0, "invalid inputs rejected before asking for a quote");
        customer.cents = -1;
        expect(IllegalArgumentException.class, () -> policy.quote(customer, item, 1));
        policy.activate(1);
        expect(IllegalArgumentException.class, () -> policy.quote(customer, item, 1));
        check(new Order(customer, item, 0).getLockedPriceCents() == 0, "zero is a valid explicit bill");
    }

    private static void checkoutAfterExpiry() {
        Customer vip = new VIPCustomer(2);
        Table table = new Table(1);
        Waiter waiter = new Waiter();
        waiter.seat(vip, table);
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(4);
        MenuItem burger = new Menu().find("Burger");
        Order order = new Order(vip, burger, policy.quote(vip, burger, 4));
        vip.placeOrder(order);
        new Chef().advance(order, 4);
        waiter.resetTurn();
        waiter.serve(order, 5);
        vip.finishEating(6);
        Cashier cashier = new Cashier();
        check(!policy.isActive(7) && cashier.collectPayment(vip, 7) == 1080
                && cashier.getRevenueCents() == 1080 && table.isFree(), "checkout uses original locked bill");
    }

    private static final class QuotingCustomer extends Customer {
        private int calls;
        private int cents = 1000;

        QuotingCustomer() { super(1); }
        @Override public int waitingLoss() { return 8; }
        @Override public int priceFor(MenuItem item) { calls++; return cents; }
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException ex) {
            if (type.isInstance(ex)) return;
            throw ex;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
