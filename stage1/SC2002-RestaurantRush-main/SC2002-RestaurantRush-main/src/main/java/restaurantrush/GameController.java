package restaurantrush;

import java.util.Locale;

/** Owner D. The preview loop is wired; the actual 18-turn game is still to implement. */
public final class GameController {
    private final Restaurant restaurant;
    private final ConsoleUI ui;

    public GameController(Restaurant restaurant, ConsoleUI ui) {
        this.restaurant = restaurant;
        this.ui = ui;
    }

    public void run() {
        ui.showWelcome();
        ui.showTurnState(restaurant);
        while (true) {
            String input = ui.readCommand();
            if (input == null) {
                ui.showMessage("End of input. Preview closed.");
                return;
            }
            String command = input.trim().toLowerCase(Locale.ROOT);
            try {
                switch (command) {
                    case "" -> { }
                    case "status" -> ui.showTurnState(restaurant);
                    case "menu" -> ui.showMenu(restaurant.getMenu());
                    case "tasks" -> ui.showTasks();
                    case "help" -> ui.showHelp();
                    case "start" -> processTurn();
                    case "quit", "exit" -> {
                        ui.showMessage("Goodbye.");
                        return;
                    }
                    default -> ui.showMessage("Unknown command. Type help and choose again.");
                }
            } catch (UnsupportedOperationException error) {
                ui.showMessage("Not implemented: " + error.getMessage());
            } catch (IllegalArgumentException | IllegalStateException error) {
                ui.showMessage("Invalid choice: " + error.getMessage() + " Please choose again.");
            }
        }
    }

    private void processTurn() {
        // TODO[D]: Replace preview start with an 18-turn gameplay loop. For each turn:
        // nextTurn, reset staff, arrivals, state display, waiter choice, chef choice,
        // eating/payment, waiting/abandonment, end-of-turn display. At turn 18 only:
        // ui.showResult(restaurant, checkVictory()). Do not end early.
        // Do NOT remove this fail-fast guard until the whole turn is integrated.
        throw new UnsupportedOperationException("TODO[D] GameController.processTurn: connect the Stage 1 turn sequence");
    }

    private void processArrivals() {
        // TODO[D]: At 1/4/7/10/13/16 create exactly one RegularCustomer with IDs 1..6.
        throw new UnsupportedOperationException("TODO[D] GameController.processArrivals");
    }

    private void processWaiterChoice() {
        // TODO[D]: Parse seat <customerId> <tableNo> / order <customerId> /
        // serve <customerId> / wait. Validate/retry until one successful action or wait.
        // On success perform Restaurant queue/order/count bookkeeping exactly once.
        throw new UnsupportedOperationException("TODO[D] GameController.processWaiterChoice");
    }

    private void processChefChoice() {
        // TODO[D]: Parse cook <customerId> / wait; one valid action, retry invalid input.
        // It is legal to cook an order taken by the waiter in this same turn.
        throw new UnsupportedOperationException("TODO[D] GameController.processChefChoice");
    }

    private void processEatingAndPayments() {
        // TODO[D]: finishEating(turn) on customers, then select canPay(turn), oldest
        // readyToPayTurn first (ID tie-break). Charge at most one, remove paid active order.
        // The canPay guard prevents same-turn READY_TO_PAY and payment.
        throw new UnsupportedOperationException("TODO[D] GameController.processEatingAndPayments");
    }

    private void processWaitingAndAbandonment() {
        // TODO[D]: Apply loss to QUEUED/SEATED customers only; handle departure at 0.
        // Iterate getCustomers snapshot to avoid modifying a queue while traversing it.
        throw new UnsupportedOperationException("TODO[D] GameController.processWaitingAndAbandonment");
    }

    private void resetStaffActions() {
        restaurant.getWaiter().resetTurn();
        restaurant.getChef().resetTurn();
        restaurant.getCashier().resetTurn();
    }

    private boolean checkVictory() {
        // TODO[D]: Require all three GameConfig targets using paid count, paid revenue
        // and paid average, not servedCount. Called only after all 18 turns.
        throw new UnsupportedOperationException("TODO[D] GameController.checkVictory");
    }
}
