package restaurantrush;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Owner D. Checks the whole game through the command line, so it needs the Customer,
 * Table, Order and staff methods (owners A, B, C) to be implemented. Run it by hand
 * once they are in: java -cp out/main:out/test restaurantrush.ControllerTests
 *
 * The expected results come from a separate model of the brief's rules, not from this code.
 * Customers 1 to 6 choose Salad, Burger, Pasta, Salad, Burger, Pasta.
 */
public final class ControllerTests {
    private static int passed;

    private ControllerTests() { }

    /** Turn by turn: waiter command, then chef command. The Waiter serves customer 3 first on turn 10. */
    private static final String SERVE_FIRST = String.join("\n",
            "start", "seat 1 1", "wait", "order 1", "cook 1", "serve 1", "wait",
            "seat 2 2", "wait", "order 2", "cook 2", "serve 2", "wait",
            "seat 3 1", "wait", "order 3", "cook 3", "wait", "cook 3",
            "serve 3", "wait", "seat 4 2", "wait", "order 4", "cook 4", "serve 4", "wait",
            "seat 5 1", "wait", "order 5", "cook 5", "serve 5", "wait",
            "seat 6 2", "wait", "order 6", "cook 6", "");

    /** The same game, but the Waiter seats customer 4 before serving customer 3 on turn 10. */
    private static final String SEAT_FIRST = String.join("\n",
            "start", "seat 1 1", "wait", "order 1", "cook 1", "serve 1", "wait",
            "seat 2 2", "wait", "order 2", "cook 2", "serve 2", "wait",
            "seat 3 1", "wait", "order 3", "cook 3", "wait", "cook 3",
            "seat 4 2", "wait", "serve 3", "wait", "order 4", "cook 4", "serve 4", "wait",
            "seat 5 1", "wait", "order 5", "cook 5", "serve 5", "wait",
            "seat 6 2", "wait", "order 6", "cook 6", "");

    public static void main(String[] args) {
        testVictoryServeFirst();
        testSameCommandsGiveTheSameGame();
        testVictorySeatFirstChangesTheAverage();
        testDefeatWhenTheManagerOnlyWaits();
        testInvalidChoicesDoNotUseTheTask();
        testInputEndingMidGameIsAnInterruption();
        System.out.println("PASS " + passed + "/6 controller scenarios");
    }

    private static void testVictoryServeFirst() {
        String text = play(SERVE_FIRST);
        check(text.contains("Turns used: 18"), "the game must always use 18 turns");
        check(text.contains("Served: 5 | Paid: 5 | Unhappy departures: 0"), "expected 5 served and 5 paid");
        check(text.contains("Revenue: $66.00"), "expected revenue $66.00");
        check(text.contains("Average satisfaction of paid customers: 79.2"), "expected average 79.2");
        check(text.trim().endsWith("VICTORY"), "expected VICTORY");
        passed++;
    }

    private static void testSameCommandsGiveTheSameGame() {
        check(play(SERVE_FIRST).equals(play(SERVE_FIRST)), "the same commands must always give the same game");
        passed++;
    }

    private static void testVictorySeatFirstChangesTheAverage() {
        String text = play(SEAT_FIRST);
        check(text.contains("Revenue: $66.00") && text.contains("Paid: 5"), "seating first should still pay 5 customers");
        check(text.contains("Average satisfaction of paid customers: 77.6"), "the manager's choice must change the average to 77.6");
        passed++;
    }

    private static void testDefeatWhenTheManagerOnlyWaits() {
        String text = play("start\n" + "wait\nwait\n".repeat(18));
        check(text.contains("Turns used: 18"), "the game must not end early");
        check(text.contains("Served: 0 | Paid: 0 | Unhappy departures: 2"), "expected nobody served and 2 unhappy departures");
        check(text.contains("Revenue: $0.00") && text.contains("Average satisfaction of paid customers: N/A"),
                "expected no revenue and N/A average");
        check(text.trim().endsWith("DEFEAT"), "expected DEFEAT");
        passed++;
    }

    private static void testInvalidChoicesDoNotUseTheTask() {
        String text = play("start\n\ndance\nseat\nseat a 1\nseat 1 9\nseat 5 1\nserve 1\norder 1\nseat 1 1\ncook 1\ncook x\nwait\n");
        check(text.contains("Please type a command."), "blank input must be explained");
        check(text.contains("Unknown Waiter command \"dance\""), "unknown command must be explained");
        check(text.contains("The customer ID must be a whole number"), "non-numeric ID must be explained");
        check(text.contains("There is no table 9."), "bad table number must be explained");
        check(text.contains("Customer 5 is not in the waiting queue."), "unknown queued customer must be explained");
        check(text.contains("Customer 1 has no order."), "serving without an order must be explained");
        check(text.contains("Customer 1 is not seated."), "ordering before seating must be explained");
        check(text.contains("Waiter seats customer 1 at table 1."), "the Waiter's task must still be available after the errors");
        check(text.contains("Chef waits."), "the Chef's task must still be available after the errors");
        passed++;
    }

    private static void testInputEndingMidGameIsAnInterruption() {
        String text = play("start\nseat 1 1\nwait\nwait\n");
        check(text.contains("interrupted"), "running out of input mid-game must be reported");
        check(!text.contains("VICTORY") && !text.contains("DEFEAT"), "an interrupted game must not report a result");
        passed++;
    }

    private static String play(String commands) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        new GameController(new Restaurant(), ui).run();
        return output.toString(StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
