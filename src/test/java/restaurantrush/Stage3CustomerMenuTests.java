package restaurantrush;

import java.util.List;

/** Part A scenarios. Run directly until the Stage 3 suites are wired into test.sh. */
public final class Stage3CustomerMenuTests {
    private Stage3CustomerMenuTests() { }

    public static void main(String[] args) {
        menuModes();
        comboArithmetic();
        invalidComponents();
        customerPreferences();
        waitingRecovery();
        recoveryThroughService();
        departedCustomer();
        System.out.println("PASS 7/7 Stage 3 customer/menu scenarios");
    }

    private static void menuModes() {
        for (Menu menu : List.of(new Menu(), new Menu(false))) {
            check(menu.getItems().size() == 3 && !menu.hasCombo(), "base menu stays at three items");
            expect(IllegalStateException.class, menu::getCombo);
            expect(IllegalArgumentException.class, () -> menu.find("Combo"));
        }
        Menu menu = new Menu(true);
        check(menu.hasCombo() && menu.getItems().size() == 4, "stage 3 offers four items");
        ComboMeal combo = menu.getCombo();
        check(menu.find("combo") == combo && menu.getItems().contains(combo), "single registered combo");
        check(combo.getFirst() == menu.find("Salad") && combo.getSecond() == menu.find("Pasta"),
                "combo shares the two registered dishes");
        check(combo.getPriceCents() == 2430 && combo.getPrepUnits() == 3, "Salad + Pasta");
        check(menu.find("Salad").getPriceCents() == 900 && menu.find("Pasta").getPriceCents() == 1800,
                "base dishes retain their prices");
        expect(UnsupportedOperationException.class, () -> menu.getItems().clear());
    }

    private static void comboArithmetic() {
        MenuItem one = new MenuItem("First", 1, 2);
        ComboMeal below = new ComboMeal("Below half", one, new MenuItem("Second", 5, 1));
        ComboMeal tie = new ComboMeal("Half", one, new MenuItem("Second", 4, 3));
        check(below.getPriceCents() == 5, "5.4 cents rounds down");
        check(tie.getPriceCents() == 5 && tie.getPrepUnits() == 5, "4.5 cents rounds up");
        ComboMeal large = new ComboMeal("Large", new MenuItem("First", 2_000_000_000, 1),
                new MenuItem("Second", 200_000_000, 1));
        check(large.getPriceCents() == 1_980_000_000, "sum may exceed int when final price fits");
    }

    private static void invalidComponents() {
        MenuItem a = new MenuItem("A", 1, 1);
        MenuItem b = new MenuItem("B", 2, 2);
        ComboMeal combo = new ComboMeal("Pair", a, b);
        expect(IllegalArgumentException.class, () -> new ComboMeal("Bad", null, b));
        expect(IllegalArgumentException.class, () -> new ComboMeal("Bad", a, null));
        expect(IllegalArgumentException.class, () -> new ComboMeal("Bad", a, a));
        expect(IllegalArgumentException.class, () -> new ComboMeal("Nested", combo, a));
        expect(IllegalArgumentException.class, () -> new ComboMeal("Nested", a, combo));
        expect(IllegalArgumentException.class, () -> new ComboMeal("Too costly",
                new MenuItem("X", Integer.MAX_VALUE, 1), new MenuItem("Y", Integer.MAX_VALUE, 1)));
        expect(IllegalArgumentException.class, () -> new ComboMeal("Too slow", a,
                new MenuItem("X", 0, Integer.MAX_VALUE)));
    }

    private static void customerPreferences() {
        Menu base = new Menu();
        Menu extended = new Menu(true);
        String[] choices = {"Salad", "Burger", "Pasta", "Pasta", "Combo", "Pasta"};
        for (int id = 1; id <= choices.length; id++) {
            Customer customer = Stage2Arrivals.create(id);
            check(customer.chooseMeal(extended) == extended.find(choices[id - 1]), "meal for ID " + id);
        }
        check(new RegularCustomer(5).chooseMeal(base) == base.find("Burger"), "Stage 2 ID 5 unchanged");
        check(new CriticCustomer(5).chooseMeal(extended) == extended.find("Pasta"), "Critic overrides combo");
    }

    private static void waitingRecovery() {
        Customer customer = new RegularCustomer(1);
        customer.loseWaitingSatisfaction();
        customer.loseWaitingSatisfaction();
        customer.restoreWaitingSatisfaction(0);
        check(customer.getSatisfaction() == 84, "zero recovery");
        expect(IllegalArgumentException.class, () -> customer.restoreWaitingSatisfaction(-1));
        customer.restoreWaitingSatisfaction(15);
        check(customer.getSatisfaction() == 99, "queued customer recovers 15");
        customer.restoreWaitingSatisfaction(Integer.MAX_VALUE);
        check(customer.getSatisfaction() == 100, "large recovery caps at 100");
    }

    private static void recoveryThroughService() {
        Customer customer = new RegularCustomer(5);
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        waiter.seat(customer, new Table(1));
        waiter.resetTurn();
        Order order = waiter.takeOrder(customer, new Menu(true));
        for (int turn = 2; turn <= 4; turn++) {
            chef.resetTurn();
            chef.advance(order, turn);
            customer.loseWaitingSatisfaction();
        }
        check(order.getStatus() == OrderStatus.READY && customer.getSatisfaction() == 76, "combo ready");
        customer.restoreWaitingSatisfaction(15);
        check(customer.getSatisfaction() == 91, "waiting for a READY dish still qualifies");
        waiter.resetTurn();
        waiter.serve(order, 5);
        customer.restoreWaitingSatisfaction(15);
        check(customer.getSatisfaction() == 91, "no recovery while eating");
        customer.finishEating(6);
        customer.restoreWaitingSatisfaction(15);
        check(customer.getSatisfaction() == 91, "no recovery while ready to pay");
        check(new Cashier().collectPayment(customer, 7) == 2430, "ordinary checkout handles the combo");
        customer.restoreWaitingSatisfaction(15);
        check(customer.getSatisfaction() == 91, "no recovery after payment");
    }

    private static void departedCustomer() {
        Customer customer = new CriticCustomer(4);
        for (int turn = 1; turn <= 9; turn++) {
            customer.loseWaitingSatisfaction();
        }
        customer.leave();
        customer.restoreWaitingSatisfaction(15);
        check(customer.getState() == CustomerState.LEFT && customer.getSatisfaction() == 0, "no revival");
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException error) {
            if (type.isInstance(error)) return;
            throw error;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
