package restaurantrush;

/** Owner C. Stage 3 staff contracts, including Stage 1/2 compatibility. */
public final class Stage3HostStaffTests {
    private static int passed;

    private Stage3HostStaffTests() { }

    public static void main(String[] args) {
        run(Stage3HostStaffTests::testHostSeatingAndQueueOwnership);
        run(Stage3HostStaffTests::testHostAndWaiterCanActInTheSameTurn);
        run(Stage3HostStaffTests::testHostActionLimitSkipAndReset);
        run(Stage3HostStaffTests::testInvalidHostActionsPreserveState);
        run(Stage3HostStaffTests::testHostAndWaiterCannotSeatTwice);
        run(Stage3HostStaffTests::testOrderingValidatesArgumentsBeforeSelection);
        run(Stage3HostStaffTests::testOrderingRejectsInvalidCustomerState);
        run(Stage3HostStaffTests::testInvalidChoicesAndQuotesAllowRetry);
        run(Stage3HostStaffTests::testSelectionAndPricingHappenExactlyOnce);
        run(Stage3HostStaffTests::testHappyHourPricingAndExpiry);
        run(Stage3HostStaffTests::testOldOrderingAndSharedWaiterActionLimit);
        run(Stage3HostStaffTests::testServiceAndLockedPaymentForAllCustomerTypes);
        run(Stage3HostStaffTests::testGenericThreeUnitMeal);
        System.out.println("PASS " + passed + "/13 Stage 3 host/staff scenarios");
    }

    private static void testHostSeatingAndQueueOwnership() {
        Restaurant restaurant = new Restaurant();
        Customer customer = new RegularCustomer(1);
        restaurant.addArrival(customer);
        Table table = restaurant.findTable(1);
        Host host = new Host();
        host.seat(customer, table);
        checkSeated(customer, table);
        check(host.hasActed(), "successful seating consumes the Host action");
        check(restaurant.getWaitingQueue().contains(customer),
                "Host must leave queue removal to the controller");
        restaurant.removeFromQueue(customer);
        check(restaurant.getWaitingQueue().isEmpty(), "controller can remove the seated customer");
    }

    private static void testHostAndWaiterCanActInTheSameTurn() {
        Host host = new Host();
        Waiter waiter = new Waiter();
        Customer customer = new VIPCustomer(2);
        host.seat(customer, new Table(1));
        check(!waiter.hasActed(), "Host seating must not consume the Waiter action");
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(4);
        Order order = waiter.takeOrder(customer, new Menu(), policy, 4);
        check(host.hasActed() && waiter.hasActed(), "both staff may act in the same turn");
        check(customer.getOrder() == order && order.getCustomer() == customer,
                "the new order must be attached in both directions");
        check(order.getLockedPriceCents() == 1080, "VIP discount precedes Happy Hour");
        host.resetTurn();
        check(!host.hasActed() && waiter.hasActed(), "resetting Host must not reset Waiter");
    }

    private static void testHostActionLimitSkipAndReset() {
        Host host = new Host();
        Customer first = new RegularCustomer(1);
        Customer second = new RegularCustomer(2);
        Table firstTable = new Table(1);
        Table secondTable = new Table(2);
        host.seat(first, firstTable);
        expect(IllegalStateException.class, () -> host.seat(second, secondTable));
        expect(IllegalStateException.class, host::skipTurn);
        checkQueued(second, secondTable);
        checkSeated(first, firstTable);
        check(host.hasActed(), "rejected repeat action retains the consumed action");
        host.resetTurn();
        host.skipTurn();
        expect(IllegalStateException.class, () -> host.seat(second, secondTable));
        checkQueued(second, secondTable);
        host.resetTurn();
        host.seat(second, secondTable);
        checkSeated(second, secondTable);
    }

    private static void testInvalidHostActionsPreserveState() {
        Host host = new Host();
        Customer queued = new RegularCustomer(1);
        Table free = new Table(1);
        expect(IllegalArgumentException.class, () -> host.seat(null, free));
        expect(IllegalArgumentException.class, () -> host.seat(queued, null));
        checkQueued(queued, free);
        check(!host.hasActed(), "null arguments must not consume the Host action");

        Customer occupant = new RegularCustomer(2);
        Table occupied = new Table(2);
        new Waiter().seat(occupant, occupied);
        expect(IllegalStateException.class, () -> host.seat(queued, occupied));
        checkSeated(occupant, occupied);
        checkQueued(queued, free);
        check(!host.hasActed(), "occupied-table failure must not consume the action");

        Customer left = new RegularCustomer(3);
        while (left.getSatisfaction() > 0) {
            left.loseWaitingSatisfaction();
        }
        left.leave();
        expect(IllegalStateException.class, () -> host.seat(left, free));
        check(left.getState() == CustomerState.LEFT && left.getTable() == null && free.isFree(),
                "a departed customer cannot be seated");
        check(!host.hasActed(), "invalid customer must not consume the action");
        host.seat(queued, free);
        checkSeated(queued, free);
    }

    private static void testHostAndWaiterCannotSeatTwice() {
        for (boolean hostFirst : new boolean[] { true, false }) {
            Host host = new Host();
            Waiter waiter = new Waiter();
            Customer customer = new RegularCustomer(1);
            Table original = new Table(1);
            Table other = new Table(2);
            if (hostFirst) {
                host.seat(customer, original);
                expect(IllegalStateException.class, () -> waiter.seat(customer, other));
                check(!waiter.hasActed(), "double seating must not consume Waiter action");
                waiter.seat(new RegularCustomer(2), other);
            } else {
                waiter.seat(customer, original);
                expect(IllegalStateException.class, () -> host.seat(customer, other));
                check(!host.hasActed(), "double seating must not consume Host action");
                host.seat(new RegularCustomer(2), other);
            }
            checkSeated(customer, original);
            check(other.getOccupant() != customer, "customer must occupy only the original table");
        }
    }

    private static void testOrderingValidatesArgumentsBeforeSelection() {
        Waiter waiter = new Waiter();
        ProbeCustomer customer = new ProbeCustomer(1);
        Table table = new Table(1);
        new Host().seat(customer, table);
        Menu menu = new Menu();
        HappyHourPolicy policy = new HappyHourPolicy();
        expect(IllegalArgumentException.class, () -> waiter.takeOrder(null, menu, policy, 1));
        expect(IllegalArgumentException.class, () -> waiter.takeOrder(customer, null, policy, 1));
        expect(IllegalArgumentException.class, () -> waiter.takeOrder(customer, menu, null, 1));
        for (int turn : new int[] { Integer.MIN_VALUE, -1, 0, 19, Integer.MAX_VALUE }) {
            expect(IllegalArgumentException.class, () -> waiter.takeOrder(customer, menu, policy, turn));
        }
        check(customer.selections == 0 && customer.quotes == 0,
                "invalid arguments must fail before selection or pricing");
        checkNoOrder(waiter, customer, table);
        check(!policy.hasActivated(), "ordering must not activate the policy");
        Order order = waiter.takeOrder(customer, menu, policy, 18);
        check(order == customer.getOrder(), "last playable turn remains valid");
    }

    private static void testOrderingRejectsInvalidCustomerState() {
        Waiter waiter = new Waiter();
        Menu menu = new Menu();
        HappyHourPolicy policy = new HappyHourPolicy();
        ProbeCustomer queued = new ProbeCustomer(1);
        expect(IllegalStateException.class, () -> waiter.takeOrder(queued, menu, policy, 1));
        check(queued.getState() == CustomerState.QUEUED && queued.getTable() == null
                        && queued.getOrder() == null,
                "queued customer must remain unchanged");
        ProbeCustomer mismatched = new ProbeCustomer(2);
        Table empty = new Table(1);
        mismatched.seat(empty);
        expect(IllegalStateException.class, () -> waiter.takeOrder(mismatched, menu, policy, 1));
        check(empty.isFree() && mismatched.getTable() == empty && mismatched.getOrder() == null,
                "ordering must not repair or mutate a mismatched association");
        Table occupied = new Table(2);
        Customer occupant = new RegularCustomer(3);
        new Host().seat(occupant, occupied);
        ProbeCustomer wrongOccupant = new ProbeCustomer(4);
        wrongOccupant.seat(occupied);
        expect(IllegalStateException.class, () -> waiter.takeOrder(wrongOccupant, menu, policy, 1));
        checkSeated(occupant, occupied);
        check(wrongOccupant.getOrder() == null && wrongOccupant.getTable() == occupied,
                "ordering must preserve an incorrectly linked customer");
        check(queued.selections == 0 && mismatched.selections == 0 && wrongOccupant.selections == 0
                        && !waiter.hasActed(),
                "invalid customer/table links fail before selection and do not use a task");

        ProbeCustomer seated = new ProbeCustomer(5);
        new Host().seat(seated, new Table(3));
        Order order = waiter.takeOrder(seated, menu, policy, 1);
        waiter.resetTurn();
        expect(IllegalStateException.class, () -> waiter.takeOrder(seated, menu, policy, 2));
        check(seated.getOrder() == order && seated.selections == 1 && seated.quotes == 1
                        && !waiter.hasActed(),
                "duplicate order must preserve the existing bill and unused action");
    }

    private static void testInvalidChoicesAndQuotesAllowRetry() {
        Waiter waiter = new Waiter();
        ProbeCustomer customer = new ProbeCustomer(1);
        Table table = new Table(1);
        new Host().seat(customer, table);
        Menu menu = new Menu();
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(1);

        customer.item = null;
        expect(IllegalStateException.class, () -> waiter.takeOrder(customer, menu, policy, 1));
        check(customer.quotes == 0, "null selection must fail before pricing");
        checkNoOrder(waiter, customer, table);
        customer.failSelection = true;
        expect(IllegalArgumentException.class, () -> waiter.takeOrder(customer, menu, policy, 1));
        checkNoOrder(waiter, customer, table);
        customer.failSelection = false;
        customer.item = menu.find("Salad");
        customer.failQuote = true;
        expect(IllegalArgumentException.class, () -> waiter.takeOrder(customer, menu, policy, 1));
        checkNoOrder(waiter, customer, table);
        customer.failQuote = false;
        customer.quoteCents = -1;
        expect(IllegalArgumentException.class, () -> waiter.takeOrder(customer, menu, policy, 1));
        checkNoOrder(waiter, customer, table);
        check(policy.remainingTurns(1) == 2, "failed ordering must not change the promotion");
        customer.quoteCents = 1000;
        check(waiter.takeOrder(customer, menu, policy, 1).getLockedPriceCents() == 800,
                "a valid retry after failure must succeed");
    }

    private static void testSelectionAndPricingHappenExactlyOnce() {
        ProbeCustomer customer = new ProbeCustomer(1);
        Table table = new Table(1);
        new Host().seat(customer, table);
        Waiter waiter = new Waiter();
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(4);
        Order order = waiter.takeOrder(customer, new Menu(), policy, 4);
        check(customer.selections == 1 && customer.quotes == 1,
                "one selection and one customer quote per order");
        check(order.getItem() == customer.item && customer.quotedItem == customer.item,
                "the exact chosen item must be priced and stored");
        check(order.getLockedPriceCents() == 988, "policy quote must be locked without repricing");
        customer.quoteCents = 5000;
        check(!policy.isActive(6) && order.getLockedPriceCents() == 988 && customer.quotes == 1,
                "later pricing and policy expiry must not change an existing bill");
        check(customer.item.getPriceCents() == 900, "ordering must not mutate shared menu data");
    }

    private static void testHappyHourPricingAndExpiry() {
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(4);
        int[] turns = { 3, 4, 5, 6 };
        int[] prices = { 1350, 1080, 1080, 1350 };
        for (int i = 0; i < turns.length; i++) {
            Customer vip = new VIPCustomer(2);
            new Host().seat(vip, new Table(1));
            Order order = new Waiter().takeOrder(vip, new Menu(), policy, turns[i]);
            check(order.getLockedPriceCents() == prices[i], "promotion applies only during its two turns");
        }
        Customer vip = new VIPCustomer(2);
        new Host().seat(vip, new Table(1));
        HappyHourPolicy unused = new HappyHourPolicy();
        Order order = new Waiter().takeOrder(vip, new Menu(), unused, 1);
        check(order.getLockedPriceCents() == 1350 && !unused.hasActivated(),
                "unused promotion retains the normal VIP quote");
        unused.activate(2);
        check(order.getLockedPriceCents() == 1350, "activation must not discount an existing order");
    }

    private static void testOldOrderingAndSharedWaiterActionLimit() {
        Waiter waiter = new Waiter();
        Customer vip = new VIPCustomer(2);
        Table table = new Table(1);
        waiter.seat(vip, table);
        expect(IllegalStateException.class,
                () -> waiter.takeOrder(vip, new Menu(), new HappyHourPolicy(), 1));
        check(vip.getOrder() == null && waiter.hasActed(), "seating uses the shared Waiter action");
        waiter.resetTurn();
        Order legacy = waiter.takeOrder(vip, new Menu());
        check(legacy.getLockedPriceCents() == 1350, "old overload retains Stage 2 pricing");

        ProbeCustomer next = new ProbeCustomer(3);
        new Host().seat(next, new Table(2));
        HappyHourPolicy policy = new HappyHourPolicy();
        expect(IllegalStateException.class, () -> waiter.takeOrder(next, new Menu(), policy, 1));
        check(next.selections == 0 && next.getOrder() == null,
                "new overload cannot bypass action consumed by the old overload");
        waiter.resetTurn();
        waiter.skipTurn();
        expect(IllegalStateException.class, () -> waiter.takeOrder(next, new Menu(), policy, 2));
        check(next.selections == 0, "skip consumes the same action as ordering");
        waiter.resetTurn();
        waiter.takeOrder(next, new Menu(), policy, 3);
        Customer third = new RegularCustomer(4);
        new Host().seat(third, new Table(3));
        expect(IllegalStateException.class, () -> waiter.takeOrder(third, new Menu()));
        check(third.getOrder() == null, "old overload cannot bypass the new overload's action");
    }

    private static void testServiceAndLockedPaymentForAllCustomerTypes() {
        Customer[] customers = { new RegularCustomer(1), new VIPCustomer(2), new CriticCustomer(4) };
        int[] prices = { 720, 1080, 1440 };
        for (int i = 0; i < customers.length; i++) {
            completeService(customers[i], prices[i]);
        }
    }

    private static void testGenericThreeUnitMeal() {
        ProbeCustomer customer = new ProbeCustomer(5);
        customer.item = new MenuItem("Three-unit meal", 2430, 3);
        customer.quoteCents = 2430;
        completeService(customer, 1944);
        check(customer.selections == 1 && customer.quotes == 1,
                "Chef and Cashier must neither reselect nor reprice a multi-unit meal");
    }

    private static void completeService(Customer customer, int expectedPrice) {
        Table table = new Table(1);
        Host host = new Host();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        HappyHourPolicy policy = new HappyHourPolicy();
        policy.activate(4);
        host.seat(customer, table);
        Order order = waiter.takeOrder(customer, new Menu(), policy, 4);
        int units = order.getItem().getPrepUnits();
        for (int unit = 0; unit < units; unit++) {
            chef.resetTurn();
            chef.advance(order, 4 + unit);
            int turn = 4 + unit;
            expect(IllegalStateException.class, () -> chef.advance(order, turn));
            check(order.getUnitsDone() == unit + 1, "Chef gets only one preparation action per turn");
        }
        check(order.getStatus() == OrderStatus.READY, "ordinary preparation must complete the order");
        int readyTurn = order.getReadyTurn();
        waiter.resetTurn();
        expect(IllegalStateException.class, () -> waiter.serve(order, readyTurn));
        check(!waiter.hasActed() && order.getStatus() == OrderStatus.READY,
                "same-turn service must preserve the order and Waiter action");
        waiter.serve(order, readyTurn + 1);
        customer.finishEating(readyTurn + 1);
        check(customer.getState() == CustomerState.SERVED, "eating requires a later turn");
        customer.finishEating(readyTurn + 2);
        expect(IllegalStateException.class, () -> cashier.collectPayment(customer, readyTurn + 2));
        check(!cashier.hasActed() && cashier.getRevenueCents() == 0
                        && order.getStatus() == OrderStatus.SERVED,
                "early payment must leave the bill, revenue and action unchanged");
        check(!policy.isActive(readyTurn + 3), "promotion has expired before payment");
        int paid = cashier.collectPayment(customer, readyTurn + 3);
        check(paid == expectedPrice && cashier.getRevenueCents() == expectedPrice
                        && cashier.getPaidCount() == 1,
                "payment uses the locked discounted bill exactly once");
        check(order.getStatus() == OrderStatus.PAID && customer.getState() == CustomerState.PAID
                        && customer.getTable() == null && table.isFree(),
                "payment must update the order/customer and release the table");
        cashier.resetTurn();
        expect(IllegalStateException.class, () -> cashier.collectPayment(customer, readyTurn + 4));
        check(!cashier.hasActed() && cashier.getPaidCount() == 1
                        && cashier.getRevenueCents() == expectedPrice,
                "repeat payment must preserve revenue and the unused action");
    }

    private static final class ProbeCustomer extends Customer {
        private int selections;
        private int quotes;
        private int quoteCents = 1235;
        private MenuItem item = new Menu().find("Salad");
        private MenuItem quotedItem;
        private boolean failSelection;
        private boolean failQuote;

        private ProbeCustomer(int id) { super(id); }

        @Override
        public int waitingLoss() { return 8; }

        @Override
        public MenuItem chooseMeal(Menu menu) {
            selections++;
            if (failSelection) {
                throw new IllegalArgumentException("Selection rejected.");
            }
            return item;
        }

        @Override
        public int priceFor(MenuItem chosen) {
            quotes++;
            quotedItem = chosen;
            if (failQuote) {
                throw new IllegalArgumentException("Quote rejected.");
            }
            return quoteCents;
        }
    }

    private static void checkSeated(Customer customer, Table table) {
        check(customer.getState() == CustomerState.SEATED && customer.getTable() == table
                        && table.getOccupant() == customer,
                "customer and table must consistently record seating");
    }

    private static void checkQueued(Customer customer, Table freeTable) {
        check(customer.getState() == CustomerState.QUEUED && customer.getTable() == null
                        && customer.getOrder() == null && freeTable.isFree(),
                "rejected seating must leave customer and free table unchanged");
    }

    private static void checkNoOrder(Waiter waiter, Customer customer, Table table) {
        checkSeated(customer, table);
        check(customer.getOrder() == null && !waiter.hasActed(),
                "failed order must not attach an order or consume an action");
    }

    private static void run(Runnable test) {
        test.run();
        passed++;
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
