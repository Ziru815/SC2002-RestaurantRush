package restaurantrush;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Stage 3 setup, the Manager, Host and Happy Hour prompts, launch options and complete 18-turn games. */
public final class Stage3ControllerTests {
    private static int passed;
    private static final int TOTAL = 12;

    private Stage3ControllerTests() { }

    public static void main(String[] args) {
        testRestaurantModes();
        testStage3ScreenAndMenu();
        testManagerChoiceEndsOnceHappyHourIsUsed();
        testRecoveryHappensOncePerActiveTurn();
        testHostAndWaiterBothSeatInOneTurn();
        testWrongPromptAndBadInputDoNotUseTheTask();
        testHappyHourOnTheLastTurn();
        testInputEndingAtTheManagerPrompt();
        testEarlierStagesAreUnchanged();
        testStage3Victory();
        testStage3Defeat();
        testLaunchOptions();
        System.out.println("PASS " + passed + "/" + TOTAL + " Stage 3 controller scenarios");
    }

    private static void testRestaurantModes() {
        Restaurant earlier = new Restaurant();
        check(!earlier.isStage3Enabled(), "the default restaurant must be Stage 1/2");
        check(earlier.getMenu().getItems().size() == 3 && !earlier.getMenu().hasCombo(), "Stage 1/2 menu has three dishes");
        expectStateError(earlier::getHost, "Host must not exist outside Stage 3");
        expectStateError(earlier::getHappyHour, "Happy Hour must not exist outside Stage 3");
        check(!new Restaurant(false).isStage3Enabled(), "Restaurant(false) is the earlier setup");

        Restaurant stage3 = new Restaurant(true);
        check(stage3.isStage3Enabled(), "Restaurant(true) must be Stage 3");
        check(stage3.getMenu().hasCombo() && stage3.getMenu().getItems().size() == 4, "Stage 3 menu has the combo");
        check(stage3.getHost() != null && stage3.getHost() == stage3.getHost(), "exactly one Host");
        check(stage3.getHappyHour() != null && !stage3.getHappyHour().hasActivated(), "Happy Hour starts unused");
        passed++;
    }

    private static void testStage3ScreenAndMenu() {
        String text = play("start\n" + idleTurns(1, true), true);
        check(text.contains("Restaurant Rush - Stage 3"), "the welcome must name Stage 3");
        check(text.contains("Manager: happyhour | wait") && text.contains("Host:   seat"), "help must list the new prompts");
        check(text.contains("Host: available"), "the state must show the Host");
        check(text.contains("Happy Hour: not used yet"), "the state must show Happy Hour unused");
        check(text.contains("C1 (Regular)"), "Stage 3 keeps the customer type tags");
        passed++;
    }

    private static void testManagerChoiceEndsOnceHappyHourIsUsed() {
        // Happy Hour on turn 1, then three idle turns that must not ask the Manager again.
        String text = play("start\nhappyhour\nwait\nwait\nwait\n" + idleTurns(17, false), true);
        check(count(text, "Manager> ") == 1, "the Manager must be asked once, not again after activation: " + count(text, "Manager> "));
        check(text.contains("Happy Hour: ACTIVE, 2 turns left including this one"), "turn 1 shows two turns left");
        check(text.contains("Happy Hour: ACTIVE, 1 turn left including this one"), "turn 2 shows one turn left");
        check(text.contains("Happy Hour: used and finished"), "turn 3 onward shows it finished");
        check(count(text, "Happy Hour: waiting customers recover") == 2, "recovery must run on exactly two turns");
        check(text.contains("Turns used: 18"), "the game still uses 18 turns");
        passed++;
    }

    private static void testRecoveryHappensOncePerActiveTurn() {
        // Customer 1 waits unseated from turn 1. Happy Hour starts on turn 4; status, help and a wrong command are retries.
        String commands = "start\n" + idleTurns(3, true)
                + "happyhour\nstatus\nhelp\nhappyhour\nwait\nstatus\nwait\nwait\n"
                + "wait\nwait\nwait\n"
                + idleTurns(13, false);
        String text = play(commands, true);
        check(text.contains("C1 (Regular) | satisfaction: 76 | QUEUED"), "turn 4 starts at 76 after three waiting turns");
        check(text.contains("C1 (Regular) | satisfaction: 91 | QUEUED"), "turn 4 recovery adds 15 once");
        check(text.contains("C1 (Regular) | satisfaction: 83 | QUEUED"), "turn 4 ends with the normal loss of 8");
        check(text.contains("C1 (Regular) | satisfaction: 98 | QUEUED"), "turn 5 recovery adds 15 once");
        check(text.contains("C1 (Regular) | satisfaction: 90 | QUEUED"), "turn 6 has no recovery");
        check(text.contains("C2 (VIP) | satisfaction: 100 | QUEUED"), "a new arrival stays capped at 100");
        check(count(text, "Happy Hour: waiting customers recover") == 2, "status, help and retries must not repeat recovery");
        passed++;
    }

    private static void testHostAndWaiterBothSeatInOneTurn() {
        // Customers 1 and 2 are both queued on turn 4. The Host seats customer 2, then the Waiter tries the same
        // table, is refused without losing the task, and seats customer 1 at the other table.
        String commands = "start\n" + idleTurns(3, true)
                + "wait\nseat 2 1\nseat 1 1\nseat 1 2\nwait\n"
                + idleTurns(14, true);
        String text = play(commands, true);
        check(text.contains("Host seats customer 2 at table 1."), "the Host seats customer 2");
        check(text.contains("Waiter> Invalid choice: Table is occupied. The task is not used."), "the refused seat does not use the Waiter's task");
        check(text.contains("Waiter seats customer 1 at table 2."), "the Waiter also seats a customer the same turn");
        check(text.indexOf("Host seats customer 2") < text.indexOf("Waiter seats customer 1"), "the Host acts before the Waiter");
        passed++;
    }

    private static void testWrongPromptAndBadInputDoNotUseTheTask() {
        String commands = "start\n"
                + "seat 1 1\ndance\nwait\n"
                + "happyhour\nseat 9 1\nseat 1 9\nseat 1 1\n"
                + "happyhour\nwait\n"
                + "wait\n"
                + idleTurns(17, true);
        String text = play(commands, true);
        check(text.contains("Unknown Manager command \"seat\""), "seat at the Manager prompt is explained");
        check(text.contains("Unknown Manager command \"dance\""), "an unknown Manager command is explained");
        check(text.contains("Unknown Host command \"happyhour\""), "happyhour at the Host prompt is explained");
        check(text.contains("Customer 9 is not in the waiting queue."), "an unknown customer is explained");
        check(text.contains("There is no table 9."), "a bad table number is explained");
        check(text.contains("Host seats customer 1 at table 1."), "the Host's task is still available after the errors");
        check(text.contains("Unknown Waiter command \"happyhour\""), "happyhour at the Waiter prompt is explained");
        check(text.contains("Waiter waits.") && text.contains("Chef waits."), "the Waiter and Chef tasks are still available");
        check(count(text, "Happy Hour: waiting customers recover") == 0, "no wrong command may start Happy Hour");
        passed++;
    }

    private static void testHappyHourOnTheLastTurn() {
        String commands = "start\n" + idleTurns(17, true) + "happyhour\nwait\nwait\nwait\n";
        Restaurant restaurant = new Restaurant(true);
        String text = play(commands, restaurant);
        check(restaurant.getTurn() == 18, "activating on turn 18 must not extend the game");
        check(restaurant.getHappyHour().hasActivated(), "activation on turn 18 is allowed");
        check(text.contains("Happy Hour: ACTIVE, 2 turns left including this one"), "the display shows the full duration");
        check(!text.contains("Turn 19"), "there is no turn 19");
        check(text.contains("DEFEAT"), "idle play still ends in defeat");
        passed++;
    }

    private static void testInputEndingAtTheManagerPrompt() {
        String text = play("start\n", true);
        check(text.contains("Manager> "), "the first prompt after start is the Manager's");
        check(text.contains("Input ended before turn 18 finished"), "ending the input must be reported, not crash");
        check(!text.contains("VICTORY") && !text.contains("DEFEAT"), "an interrupted run has no result");
        passed++;
    }

    private static void testEarlierStagesAreUnchanged() {
        String stage1 = play("start\n" + "wait\nwait\n".repeat(18), false);
        check(!stage1.contains("Host") && !stage1.contains("Happy Hour") && !stage1.contains("Manager"),
                "Stage 1/2 screens must not mention Stage 3 features");
        check(stage1.contains("Turns used: 18"), "Stage 1 still plays 18 turns with two tasks per turn");
        passed++;
    }

    private static void testStage3Victory() {
        Restaurant restaurant = new Restaurant(true);
        String text = play(fixture("victory"), restaurant);
        Cashier cashier = restaurant.getCashier();
        check(restaurant.getTurn() == 18 && restaurant.getCustomers().size() == 6, "victory uses all 18 turns and six arrivals");
        check(cashier.getPaidCount() == 5, "customers 1 to 5 are paid: " + cashier.getPaidCount());
        check(cashier.getRevenueCents() == 8010, "900 + 1080 + 1800 + 1800 + 2430 = 8010: " + cashier.getRevenueCents());
        check(Math.abs(cashier.getAverageSatisfaction() - 85.6) < 1e-9, "paid average is 85.6: " + cashier.getAverageSatisfaction());
        int[] locked = {900, 1080, 1800, 1800, 2430};
        for (int id = 1; id <= locked.length; id++) {
            int cents = restaurant.findCustomer(id).getOrder().getLockedPriceCents();
            check(cents == locked[id - 1], "customer " + id + " bill must be locked at " + locked[id - 1] + " but was " + cents);
        }
        check(restaurant.findCustomer(2).getOrder().getItem().getName().equals("Burger"),
                "the VIP discount and Happy Hour stack on the VIP's Burger");
        check(restaurant.findCustomer(5).getOrder().getItem().getName().equals("Combo"), "customer 5 orders the combo");
        check(text.contains("Waiter takes customer 2's order: Burger ($10.80)."), "the Happy Hour bill is shown when ordered");
        check(text.contains("VICTORY") && !text.contains("Invalid choice:") && !text.contains("Not implemented:"),
                "a valid replay finishes cleanly with a victory");
        passed++;
    }

    private static void testStage3Defeat() {
        Restaurant restaurant = new Restaurant(true);
        String text = play(fixture("defeat"), restaurant);
        check(restaurant.getTurn() == 18 && restaurant.getCustomers().size() == 6, "defeat still uses all turns and arrivals");
        check(restaurant.getCashier().getPaidCount() == 0 && restaurant.getCashier().getRevenueCents() == 0,
                "waiting without payment earns nothing");
        check(!restaurant.getHappyHour().hasActivated(), "the all-wait game never starts Happy Hour");
        check(text.contains("DEFEAT") && text.contains("N/A"), "no payments means defeat and no average");
        check(!text.contains("Invalid choice:") && !text.contains("Not implemented:"), "the defeat replay finishes cleanly");
        passed++;
    }

    private static void testLaunchOptions() {
        String stage3Check = launch(0, "--stage3", "--check");
        check(stage3Check.contains("Restaurant Rush - Stage 3"), "--stage3 selects Stage 3");
        check(stage3Check.contains("Combo | $24.30 | preparation units: 3"), "the Stage 3 menu shows the combo");
        check(!launch(0, "--stage2", "--check").contains("Combo"), "Stage 2 has no combo");
        check(!launch(0, "--check").contains("Combo"), "the default game has no combo");
        check(launch(0, "--stage3", "--help").contains("--stage3"), "help documents the new option");
        check(launch(2, "--stage2", "--stage3").contains("Usage:"), "Stage 2 and Stage 3 together fail");
        check(launch(2, "--stage3", "--check", "--help").contains("Usage:"), "conflicting flags still fail");
        passed++;
    }

    /** Four waits per turn while the Manager is still asked, three once Happy Hour has been used. */
    private static String idleTurns(int turns, boolean managerAsked) {
        return (managerAsked ? "wait\n" : "").concat("wait\nwait\nwait\n").repeat(turns);
    }

    private static int count(String text, String part) {
        int found = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
            found++;
        }
        return found;
    }

    private static void expectStateError(Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError(message);
        } catch (IllegalStateException expected) {
            check(expected.getMessage().contains("Stage 3"), "the message must say it is a Stage 3 feature");
        }
    }

    private static String fixture(String name) {
        try {
            return Files.readString(Path.of("src/test/resources/stage3-" + name + ".input.txt"));
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private static String launch(int expectedExit, String... args) {
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), "restaurantrush.Main"));
        command.addAll(List.of(args));
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            process.getOutputStream().close();
            String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            check(process.waitFor() == expectedExit, "launch exit status for " + command);
            return text;
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError("CLI test interrupted", error);
        }
    }

    private static String play(String commands, boolean stage3) {
        if (stage3) {
            return play(commands, new Restaurant(true));
        }
        return run(commands, new Restaurant(), "Stage 1", RegularCustomer::new);
    }

    private static String play(String commands, Restaurant restaurant) {
        return run(commands, restaurant, "Stage 3", Stage2Arrivals::create);
    }

    private static String run(String commands, Restaurant restaurant, String label,
            java.util.function.IntFunction<Customer> arrivals) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8), label);
        new GameController(restaurant, ui, arrivals).run();
        return output.toString(StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
