package restaurantrush;

import java.util.Comparator;
import java.util.Locale;

/** Owner D. Runs the 18-turn game; asks the staff and customers to do the work and prints nothing itself. */
public final class GameController {
    private final Restaurant restaurant;
    private final ConsoleUI ui;

    public GameController(Restaurant restaurant, ConsoleUI ui) {
        this.restaurant = restaurant;
        this.ui = ui;
    }

    /** The manager's command line ran out of input before the game finished. */
    private static final class InputEnded extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public void run() {
        ui.showWelcome();
        ui.showTurnState(restaurant);
        while (true) {
            String input = ui.readCommand();
            if (input == null) {
                ui.showMessage("End of input. Closed before the game started.");
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
                    case "start" -> {
                        processTurn();
                        return;
                    }
                    case "quit", "exit" -> {
                        ui.showMessage("Goodbye.");
                        return;
                    }
                    default -> ui.showMessage("Unknown command. Type help and choose again.");
                }
            } catch (InputEnded ended) {
                ui.showMessage("\nInput ended before turn " + GameConfig.MAX_TURNS
                        + " finished. The run was interrupted, so there is no result.");
                return;
            } catch (UnsupportedOperationException error) {
                ui.showMessage("Not implemented: " + error.getMessage());
            } catch (IllegalArgumentException | IllegalStateException error) {
                ui.showMessage("Invalid choice: " + error.getMessage() + " Please choose again.");
            }
        }
    }

    /** Plays all 18 turns, then reports the result. The game never ends early. */
    private void processTurn() {
        for (int played = 1; played <= GameConfig.MAX_TURNS; played++) {
            restaurant.nextTurn();
            resetStaffActions();
            processArrivals();
            ui.showTurnState(restaurant);
            processWaiterChoice();
            processChefChoice();
            processEatingAndPayments();
            processWaitingAndAbandonment();
            ui.showTurnEnd(restaurant);
        }
        ui.showResult(restaurant, checkVictory());
    }

    // TODO[D-S2]: support a configured arrival factory while preserving this
    // class's existing constructor and default Regular-only regression path.
    // Stage2Arrivals::create belongs in setup; no instanceof dispatch in game rules.
    private void processArrivals() {
        if (GameConfig.ARRIVAL_TURNS.contains(restaurant.getTurn())) {
            int id = restaurant.getCustomers().size() + 1;
            restaurant.addArrival(new RegularCustomer(id));
            ui.showMessage("Customer " + id + " arrives and joins the queue.");
        }
    }

    private void processWaiterChoice() {
        Waiter waiter = restaurant.getWaiter();
        while (!waiter.hasActed()) {
            String[] words = readWords("Waiter> ");
            try {
                switch (words[0]) {
                    case "" -> throw new IllegalArgumentException("Please type a command.");
                    case "wait" -> {
                        expectWords(words, 1, "wait");
                        waiter.skipTurn();
                        ui.showMessage("Waiter waits.");
                    }
                    case "seat" -> {
                        expectWords(words, 3, "seat <customerId> <tableNo>");
                        Customer customer = restaurant.findQueuedCustomer(parseNumber(words[1], "customer ID"));
                        Table table = restaurant.findTable(parseNumber(words[2], "table number"));
                        waiter.seat(customer, table);
                        restaurant.removeFromQueue(customer);
                        ui.showMessage("Waiter seats customer " + customer.getId() + " at table " + table.getNumber() + ".");
                    }
                    case "order" -> {
                        expectWords(words, 2, "order <customerId>");
                        Customer customer = restaurant.findCustomer(parseNumber(words[1], "customer ID"));
                        Order order = waiter.takeOrder(customer, restaurant.getMenu());
                        restaurant.addOrder(order);
                        ui.showOrderTaken(order);
                    }
                    case "serve" -> {
                        expectWords(words, 2, "serve <customerId>");
                        Order order = restaurant.getOrder(parseNumber(words[1], "customer ID"));
                        waiter.serve(order, restaurant.getTurn());
                        restaurant.recordServed();
                        ui.showMessage("Waiter serves customer " + order.getCustomer().getId() + ".");
                    }
                    case "status" -> ui.showTurnState(restaurant);
                    case "help" -> ui.showHelp();
                    default -> throw new IllegalArgumentException("Unknown Waiter command \"" + words[0]
                            + "\". Try seat <customerId> <tableNo>, order <customerId>, serve <customerId> or wait.");
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                ui.showMessage("Invalid choice: " + error.getMessage() + " The task is not used. Please choose again.");
            }
        }
    }

    private void processChefChoice() {
        Chef chef = restaurant.getChef();
        while (!chef.hasActed()) {
            String[] words = readWords("Chef> ");
            try {
                switch (words[0]) {
                    case "" -> throw new IllegalArgumentException("Please type a command.");
                    case "wait" -> {
                        expectWords(words, 1, "wait");
                        chef.skipTurn();
                        ui.showMessage("Chef waits.");
                    }
                    case "cook" -> {
                        expectWords(words, 2, "cook <customerId>");
                        Order order = restaurant.getOrder(parseNumber(words[1], "customer ID"));
                        chef.advance(order, restaurant.getTurn());
                        ui.showCooking(order);
                    }
                    case "status" -> ui.showTurnState(restaurant);
                    case "help" -> ui.showHelp();
                    default -> throw new IllegalArgumentException("Unknown Chef command \"" + words[0]
                            + "\". Try cook <customerId> or wait.");
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                ui.showMessage("Invalid choice: " + error.getMessage() + " The task is not used. Please choose again.");
            }
        }
    }

    private void processEatingAndPayments() {
        int turn = restaurant.getTurn();
        for (Customer customer : restaurant.getCustomers()) {
            CustomerState before = customer.getState();
            customer.finishEating(turn);
            if (before == CustomerState.SERVED && customer.getState() == CustomerState.READY_TO_PAY) {
                ui.showMessage("Customer " + customer.getId() + " finishes eating and is ready to pay.");
            }
        }
        Customer payer = restaurant.getCustomers().stream()
                .filter(customer -> customer.canPay(turn))
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId))
                .orElse(null);
        if (payer != null) {
            Order order = payer.getOrder();
            int cents = restaurant.getCashier().collectPayment(payer, turn);
            restaurant.removeOrder(order);
            ui.showPayment(payer, cents);
        }
    }

    private void processWaitingAndAbandonment() {
        for (Customer customer : restaurant.getCustomers()) {
            CustomerState state = customer.getState();
            if (state != CustomerState.QUEUED && state != CustomerState.SEATED) {
                continue;
            }
            customer.loseWaitingSatisfaction();
            if (customer.getSatisfaction() == 0) {
                restaurant.handleDeparture(customer);
                ui.showMessage("Customer " + customer.getId()
                        + " leaves unhappy. Any unfinished order is cancelled and the table is freed.");
            }
        }
    }

    private void resetStaffActions() {
        restaurant.getWaiter().resetTurn();
        restaurant.getChef().resetTurn();
        restaurant.getCashier().resetTurn();
    }

    private boolean checkVictory() {
        Cashier cashier = restaurant.getCashier();
        return cashier.getPaidCount() >= GameConfig.MIN_PAID_CUSTOMERS
                && cashier.getRevenueCents() >= GameConfig.MIN_REVENUE_CENTS
                && cashier.getAverageSatisfaction() >= GameConfig.MIN_AVERAGE_SATISFACTION;
    }

    /** Reads one command line as lower-case words; blank lines give one empty word. */
    private String[] readWords(String prompt) {
        String line = ui.readCommand(prompt);
        if (line == null) {
            throw new InputEnded();
        }
        return line.trim().toLowerCase(Locale.ROOT).split("\\s+");
    }

    private void expectWords(String[] words, int count, String usage) {
        if (words.length != count) {
            throw new IllegalArgumentException("Use: " + usage + ".");
        }
    }

    private int parseNumber(String text, String what) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("The " + what + " must be a whole number, not \"" + text + "\".");
        }
    }
}
