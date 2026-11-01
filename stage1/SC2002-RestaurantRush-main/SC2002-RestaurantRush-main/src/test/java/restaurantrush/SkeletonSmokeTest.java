package restaurantrush;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Runtime checks for the scaffold only, not proof of completed Stage 1 rules. */
public final class SkeletonSmokeTest {
    private static int passed;

    private SkeletonSmokeTest() { }

    public static void main(String[] args) {
        testPreviewShowsBaseSetup();
        testInvalidInputAllowsRetry();
        testStartCannotFabricateACompletedGame();
        testEndOfInputExits();
        testCollectionsProtectRestaurantState();
        testStaffSkipAndReset();
        testPendingActionsDoNotConsumeStaffTask();
        testMenuLookupAndValidation();
        testPendingOrderDoesNotSilentlyProgress();
        System.out.println("PASS " + passed + "/9 scaffold smoke scenarios");
        System.out.println("Stage 1 gameplay acceptance: NOT RUN (business methods are TODOs).");
    }

    private static void testPreviewShowsBaseSetup() {
        String text = run("menu\nstatus\nquit\n", new Restaurant());
        check(text.contains("SKELETON") && text.contains("Turn: 0"), "preview must identify pre-game scaffold");
        check(text.contains("Tables: 2") && text.contains("1, 4, 7, 10, 13, 16"), "base setup not displayed");
        check(text.contains("$9.00") && text.contains("$15.00") && text.contains("$18.00"), "menu prices missing");
        check(text.contains("Revenue: $0.00") && text.contains("Paid average: N/A"), "unpaid state reported incorrectly");
        passed++;
    }

    private static void testInvalidInputAllowsRetry() {
        String text = run("\nunknown\n  MeNu  \nquit\n", new Restaurant());
        check(text.contains("Unknown command"), "unknown input must explain failure");
        check(text.contains("Pasta") && text.contains("Goodbye"), "input loop did not recover");
        passed++;
    }

    private static void testStartCannotFabricateACompletedGame() {
        Restaurant restaurant = new Restaurant();
        String text = run("start\nstatus\nquit\n", restaurant);
        check(text.contains("Not implemented") && text.contains("TODO[D]"), "unfinished start must be explicit");
        check(restaurant.getTurn() == 0 && restaurant.getCustomers().isEmpty(), "unfinished start mutated game state");
        check(!text.contains("VICTORY") && !text.contains("DEFEAT"), "scaffold fabricated a game result");
        passed++;
    }

    private static void testEndOfInputExits() {
        check(run("", new Restaurant()).contains("End of input"), "EOF did not exit safely");
        passed++;
    }

    private static void testCollectionsProtectRestaurantState() {
        Restaurant restaurant = new Restaurant();
        expect(UnsupportedOperationException.class, () -> restaurant.getTables().clear());
        expect(UnsupportedOperationException.class, () -> restaurant.getMenu().getItems().clear());
        expect(UnsupportedOperationException.class, () -> restaurant.getWaitingQueue().add(new RegularCustomer(1)));
        check(restaurant.getTables().size() == 2 && restaurant.getMenu().getItems().size() == 3,
                "external code mutated internal setup");
        passed++;
    }

    private static void testStaffSkipAndReset() {
        Waiter waiter = new Waiter();
        waiter.skipTurn();
        check(waiter.hasActed(), "skip must consume the task");
        expect(IllegalStateException.class, waiter::skipTurn);
        waiter.resetTurn();
        check(!waiter.hasActed(), "new-turn reset did not restore task");
        passed++;
    }

    private static void testPendingActionsDoNotConsumeStaffTask() {
        Waiter waiter = new Waiter();
        Table table = new Table(1);
        Customer customer = new RegularCustomer(1);
        expect(UnsupportedOperationException.class, () -> waiter.seat(customer, table));
        check(!waiter.hasActed() && table.isFree() && customer.getState() == CustomerState.QUEUED,
                "unfinished seat silently consumed task or mutated state");
        passed++;
    }

    private static void testMenuLookupAndValidation() {
        MenuItem pasta = new Menu().find("pAsTa");
        check(pasta.getPriceCents() == 1800 && pasta.getPrepUnits() == 2, "menu lookup changed pasta contract");
        expect(IllegalArgumentException.class, () -> new Menu().find("Pizza"));
        expect(IllegalArgumentException.class, () -> new MenuItem("", 900, 1));
        expect(IllegalArgumentException.class, () -> new MenuItem("Salad", -1, 1));
        expect(IllegalArgumentException.class, () -> new MenuItem("Salad", 900, 0));
        passed++;
    }

    private static void testPendingOrderDoesNotSilentlyProgress() {
        Order order = new Order(new RegularCustomer(1), new Menu().find("Pasta"));
        expect(UnsupportedOperationException.class, () -> order.advance(2));
        check(order.getUnitsDone() == 0 && order.getStatus() == OrderStatus.PLACED,
                "unfinished kitchen transition silently changed state");
        passed++;
    }

    private static String run(String commands, Restaurant restaurant) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        new GameController(restaurant, ui).run();
        return output.toString(StandardCharsets.UTF_8);
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
