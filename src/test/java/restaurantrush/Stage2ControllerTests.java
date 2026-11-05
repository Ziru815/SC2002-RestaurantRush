package restaurantrush;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Owner D. Checks how the mixed Stage 2 arrivals are wired into the game: who arrives and when,
 * what the screen shows, and that the default game is still Stage 1. These scenarios use only the
 * arrival factory, the controller and the screen, so they do not depend on the VIP and Critic rules
 * (owner A) or the locked bills (owner B). Run it by hand:
 * java -cp out/main:out/test restaurantrush.Stage2ControllerTests
 *
 * The complete Stage 2 victory and defeat games need A's and B's rules, so they are added after
 * those are merged, with results taken from a real run.
 */
public final class Stage2ControllerTests {
    private static final String NL = System.lineSeparator();
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};
    private static int passed;
    private static final int TOTAL = 7;

    private Stage2ControllerTests() { }

    /** Turns 1 to 3 of the serve-first game: customer 1 is seated, served, and customer 2 arrives on turn 4. */
    private static final String UNTIL_TURN_4 = String.join("\n",
            "start", "seat 1 1", "wait", "order 1", "cook 1", "serve 1", "wait", "");

    public static void main(String[] args) {
        testFactoryBuildsTheMixedArrivals();
        testFactoryRejectsIdsOutsideTheArrivalList();
        testControllerAsksTheFactoryOnEachArrivalTurn();
        testStage2ScreenNamesTheStageAndEachCustomerType();
        testDefaultGameStaysStage1();
        testInvalidInputInTheStage2GameDoesNotUseTheTask();
        testMissingSettingsAreRejected();
        System.out.println("PASS " + passed + "/" + TOTAL + " Stage 2 controller scenarios");
    }

    private static void testFactoryBuildsTheMixedArrivals() {
        String[] expected = {"Regular", "VIP", "Regular", "Critic", "Regular", "Regular"};
        for (int id = 1; id <= expected.length; id++) {
            Customer customer = Stage2Arrivals.create(id);
            check(customer.getId() == id, "customer " + id + " must keep its ID");
            check(customer.getTypeLabel().equals(expected[id - 1]),
                    "customer " + id + " must be " + expected[id - 1] + " but was " + customer.getTypeLabel());
            check(customer.getSatisfaction() == 100, "customer " + id + " must start at 100 satisfaction");
            check(customer.getState() == CustomerState.QUEUED, "customer " + id + " must start queued");
        }
        passed++;
    }

    private static void testFactoryRejectsIdsOutsideTheArrivalList() {
        for (int bad : new int[] {0, 7, -1}) {
            try {
                Stage2Arrivals.create(bad);
                throw new AssertionError("ID " + bad + " must be rejected");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("between 1 and 6"), "the message must say the valid range");
            }
        }
        passed++;
    }

    private static void testControllerAsksTheFactoryOnEachArrivalTurn() {
        List<Integer> asked = new ArrayList<>();
        String text = play(allWait(), "Stage 1", id -> {
            asked.add(id);
            return new RegularCustomer(id);
        });
        check(asked.equals(List.of(1, 2, 3, 4, 5, 6)), "the factory must be asked for IDs 1 to 6 in order, once each: " + asked);
        for (int i = 0; i < ARRIVAL_TURNS.length; i++) {
            String arrival = "Customer " + (i + 1) + " arrives and joins the queue." + NL + NL
                    + "===== Turn " + ARRIVAL_TURNS[i] + " / 18 =====";
            check(text.contains(arrival), "customer " + (i + 1) + " must arrive at the start of turn " + ARRIVAL_TURNS[i]);
        }
        check(text.contains("Turns used: 18"), "the game must still use 18 turns");
        passed++;
    }

    private static void testStage2ScreenNamesTheStageAndEachCustomerType() {
        String text = play(UNTIL_TURN_4, "Stage 2", Stage2Arrivals::create);
        check(text.contains("Restaurant Rush - Stage 2"), "the Stage 2 welcome must name the stage");
        check(!text.contains("Restaurant Rush - Stage 1"), "the Stage 2 welcome must not say Stage 1");
        check(text.contains("C1 (Regular) | satisfaction:"), "customer 1 must be shown as Regular");
        check(text.contains("C2 (VIP) | satisfaction: 100 | QUEUED"), "customer 2 must be shown as VIP when it arrives");
        passed++;
    }

    private static void testDefaultGameStaysStage1() {
        String text = play(UNTIL_TURN_4, "Stage 1", RegularCustomer::new);
        check(text.contains("Restaurant Rush - Stage 1"), "the default game must say Stage 1");
        check(text.contains("C2 | satisfaction: 100 | QUEUED"), "the Stage 1 customer line must have no type label");
        check(!text.contains("(Regular)") && !text.contains("(VIP)") && !text.contains("(Critic)"),
                "the Stage 1 screen must not show customer types");
        passed++;
    }

    private static void testInvalidInputInTheStage2GameDoesNotUseTheTask() {
        String text = play("start\ndance\nseat 9 1\nseat 1 9\nseat 1 1\ncook x\nwait\n", "Stage 2", Stage2Arrivals::create);
        check(text.contains("Unknown Waiter command \"dance\""), "an unknown command must be explained");
        check(text.contains("Customer 9 is not in the waiting queue."), "an unknown customer must be explained");
        check(text.contains("There is no table 9."), "a bad table number must be explained");
        check(text.contains("Waiter seats customer 1 at table 1."), "the Waiter's task must still be available after the errors");
        check(text.contains("Chef waits."), "the Chef's task must still be available after the errors");
        passed++;
    }

    private static void testMissingSettingsAreRejected() {
        ConsoleUI ui = new ConsoleUI(new ByteArrayInputStream(new byte[0]), new PrintStream(new ByteArrayOutputStream()));
        try {
            new GameController(new Restaurant(), ui, null);
            throw new AssertionError("a missing arrival factory must be rejected");
        } catch (NullPointerException expected) {
            check(expected.getMessage().equals("arrivalFactory"), "the message must name the missing argument");
        }
        try {
            new ConsoleUI(new ByteArrayInputStream(new byte[0]), new PrintStream(new ByteArrayOutputStream()), " ");
            throw new AssertionError("a blank stage label must be rejected");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("Stage label"), "the message must mention the stage label");
        }
        passed++;
    }

    private static String allWait() {
        return "start\n" + "wait\nwait\n".repeat(18);
    }

    private static String play(String commands, String stageLabel, IntFunction<Customer> arrivalFactory) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8), stageLabel);
        new GameController(new Restaurant(), ui, arrivalFactory).run();
        return output.toString(StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
