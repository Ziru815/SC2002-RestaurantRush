package restaurantrush;

/** Behaviour tests for real customer subtypes through Customer references. */
public final class Stage2CustomerTests {
    private Stage2CustomerTests() { }

    public static void main(String[] args) {
        testWaitingLoss();
        testServiceFlow();
        testPrices();
        testRounding();
        testPreferences();
        testInvalidInputsAndLabels();
        System.out.println("PASS 6/6 Stage 2 customer scenarios");
    }

    private static Customer[] customers(int id) {
        return new Customer[] {new RegularCustomer(id), new VIPCustomer(id), new CriticCustomer(id)};
    }

    private static void testWaitingLoss() {
        Customer[] all = customers(1);
        int[] loss = {8, 5, 12};
        for (int i = 0; i < all.length; i++) {
            Customer c = all[i];
            check(c.getSatisfaction() == 100 && c.getState() == CustomerState.QUEUED, "initial state");
            check(c.waitingLoss() == loss[i], "polymorphic waiting loss");
            for (int turn = 1; turn <= 22; turn++) {
                if (turn == 2) {
                    new Waiter().seat(c, new Table(1));
                }
                c.loseWaitingSatisfaction();
                check(c.getSatisfaction() == Math.max(0, 100 - turn * loss[i]),
                        "exact queued/seated loss and zero floor");
            }
            c.leave();
            c.loseWaitingSatisfaction();
            check(c.getState() == CustomerState.LEFT && c.getSatisfaction() == 0, "left state");
        }
    }

    private static void testServiceFlow() {
        for (Customer c : customers(2)) {
            Waiter waiter = new Waiter();
            Chef chef = new Chef();
            Table table = new Table(1);
            waiter.seat(c, table);
            c.loseWaitingSatisfaction();
            waiter.resetTurn();
            Order order = waiter.takeOrder(c, new Menu());
            int turn = 2;
            for (int unit = 0; unit < order.getItem().getPrepUnits(); unit++) {
                chef.resetTurn();
                chef.advance(order, turn++);
            }
            waiter.resetTurn();
            waiter.serve(order, turn);
            int satisfaction = c.getSatisfaction();
            c.loseWaitingSatisfaction();
            check(c.getSatisfaction() == satisfaction, "no loss after serving");
            c.finishEating(turn);
            check(c.getState() == CustomerState.SERVED, "cannot finish eating immediately");
            c.finishEating(turn + 1);
            c.loseWaitingSatisfaction();
            check(c.getState() == CustomerState.READY_TO_PAY
                    && c.getSatisfaction() == satisfaction, "no loss while ready to pay");
            Cashier cashier = new Cashier();
            cashier.collectPayment(c, turn + 2);
            c.loseWaitingSatisfaction();
            check(c.getState() == CustomerState.PAID && c.getSatisfaction() == satisfaction,
                    "no loss after payment");
            check(cashier.getAverageSatisfaction() == satisfaction && table.isFree(),
                    "payment records satisfaction and frees table");
        }
    }

    private static void testPrices() {
        Menu menu = new Menu();
        String[] names = {"Salad", "Burger", "Pasta"};
        int[] full = {900, 1500, 1800};
        int[] discounted = {810, 1350, 1620};
        for (int i = 0; i < names.length; i++) {
            MenuItem item = menu.find(names[i]);
            Customer[] all = customers(2);
            for (int type = 0; type < all.length; type++) {
                Customer c = all[type];
                int expected = type == 1 ? discounted[i] : full[i];
                check(c.priceFor(item) == expected && c.priceFor(item) == expected, "repeatable quote");
                check(item.getPriceCents() == full[i], "menu price unchanged");
                check(c.getSatisfaction() == 100 && c.getState() == CustomerState.QUEUED
                        && c.getOrder() == null, "quoting has no state effects");
            }
        }
    }

    private static void testRounding() {
        Customer vip = new VIPCustomer(1);
        int[] prices = {0, 1, 4, 5, 6, 15, Integer.MAX_VALUE};
        int[] expected = {0, 1, 4, 5, 5, 14, 1932735282};
        for (int i = 0; i < prices.length; i++) {
            check(vip.priceFor(new MenuItem("Test", prices[i], 1)) == expected[i],
                    "half-up rounding and large-price overflow protection");
        }
    }

    private static void testPreferences() {
        Menu menu = new Menu();
        String[] cycle = {"Salad", "Burger", "Pasta"};
        for (int id = 1; id <= 6; id++) {
            Customer[] all = customers(id);
            check(all[0].chooseMeal(menu) == menu.find(cycle[(id - 1) % 3]), "Regular cycle");
            check(all[1].chooseMeal(menu) == all[0].chooseMeal(menu), "VIP keeps Regular cycle");
            check(all[2].chooseMeal(menu) == menu.find("Pasta"), "Critic always Pasta, including ID 4");
        }
    }

    private static void testInvalidInputsAndLabels() {
        Customer[] all = customers(1);
        String[] labels = {"Regular", "VIP", "Critic"};
        for (int i = 0; i < all.length; i++) {
            Customer c = all[i];
            expectIllegalArgument(() -> c.priceFor(null));
            expectIllegalArgument(() -> c.chooseMeal(null));
            check(labels[i].equals(c.getTypeLabel()), "display label");
        }
        expectIllegalArgument(() -> new VIPCustomer(0));
        expectIllegalArgument(() -> new VIPCustomer(-1));
        expectIllegalArgument(() -> new CriticCustomer(0));
        expectIllegalArgument(() -> new CriticCustomer(-1));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
