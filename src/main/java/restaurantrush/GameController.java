package restaurantrush;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

/** Owner D. Runs the 18-turn game; asks the staff and customers to do the work and prints nothing itself. */
public final class GameController {
    /** Satisfaction a waiting customer regains at the start of each Happy Hour turn. */
    private static final int HAPPY_HOUR_RECOVERY = 15;

    private final Restaurant restaurant;
    private final ConsoleUI ui;
    private final IntFunction<Customer> arrivalFactory;

    /** The Stage 1 game: every arriving customer is Regular. */
    public GameController(Restaurant restaurant, ConsoleUI ui) {
        this(restaurant, ui, RegularCustomer::new);
    }

    /** Builds each arriving customer from its ID with the given factory, so a stage chooses who arrives. */
    public GameController(Restaurant restaurant, ConsoleUI ui, IntFunction<Customer> arrivalFactory) {
        this.restaurant = restaurant;
        this.ui = ui;
        this.arrivalFactory = Objects.requireNonNull(arrivalFactory, "arrivalFactory");
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
            if (restaurant.isStage3Enabled()) {
                processHappyHourChoice();
                restoreWaitingCustomers();
                processHostChoice();
            }
            processWaiterChoice();
            processChefChoice();
            processEatingAndPayments();
            processWaitingAndAbandonment();
            ui.showTurnEnd(restaurant);
        }
        ui.showResult(restaurant, checkVictory());
    }

    private void processArrivals() {
        if (GameConfig.ARRIVAL_TURNS.contains(restaurant.getTurn())) {
            int id = restaurant.getCustomers().size() + 1;
            restaurant.addArrival(arrivalFactory.apply(id));
            ui.showMessage("Customer " + id + " arrives and joins the queue.");
        }
    }

    /** Stage 3 only. Until Happy Hour is used, the manager may start it or let the turn go by. */
    private void processHappyHourChoice() {
        HappyHourPolicy policy = restaurant.getHappyHour();
        while (!policy.hasActivated()) {
            String[] words = readWords("Manager> ");
            try {
                switch (words[0]) {
                    case "" -> throw new IllegalArgumentException("Please type a command.");
                    case "wait" -> {
                        expectWords(words, 1, "wait");
                        ui.showMessage("Manager keeps Happy Hour for later.");
                        return;
                    }
                    case "happyhour" -> {
                        expectWords(words, 1, "happyhour");
                        policy.activate(restaurant.getTurn());
                        ui.showMessage("Manager starts Happy Hour. It lasts this turn and the next.");
                    }
                    case "status" -> ui.showTurnState(restaurant);
                    case "help" -> ui.showHelp();
                    default -> throw new IllegalArgumentException("Unknown Manager command \"" + words[0]
                            + "\". Try happyhour or wait.");
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                ui.showMessage("Invalid choice: " + error.getMessage() + " Please choose again.");
            }
        }
    }

    /** Stage 3 only. Called once per turn, so a retry or a status command can never recover twice. */
    private void restoreWaitingCustomers() {
        if (!restaurant.getHappyHour().isActive(restaurant.getTurn())) {
            return;
        }
        for (Customer customer : restaurant.getCustomers()) {
            customer.restoreWaitingSatisfaction(HAPPY_HOUR_RECOVERY);
        }
        ui.showMessage("Happy Hour: waiting customers recover " + HAPPY_HOUR_RECOVERY + " satisfaction.");
        ui.showTurnState(restaurant);
    }

    /** Stage 3 only. The Host seats at most one queued customer, on top of whatever the Waiter does. */
    private void processHostChoice() {
        Host host = restaurant.getHost();
        while (!host.hasActed()) {
            String[] words = readWords("Host> ");
            try {
                switch (words[0]) {
                    case "" -> throw new IllegalArgumentException("Please type a command.");
                    case "wait" -> {
                        expectWords(words, 1, "wait");
                        host.skipTurn();
                        ui.showMessage("Host waits.");
                    }
                    case "seat" -> seatQueuedCustomer(words, host::seat, "Host");
                    case "status" -> ui.showTurnState(restaurant);
                    case "help" -> ui.showHelp();
                    default -> throw new IllegalArgumentException("Unknown Host command \"" + words[0]
                            + "\". Try seat <customerId> <tableNo> or wait.");
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                ui.showMessage("Invalid choice: " + error.getMessage() + " The task is not used. Please choose again.");
            }
        }
    }

    /** The Host and the Waiter seat the same way, so both prompts share this. The staff member does the checking. */
    private void seatQueuedCustomer(String[] words, BiConsumer<Customer, Table> seater, String who) {
        expectWords(words, 3, "seat <customerId> <tableNo>");
        Customer customer = restaurant.findQueuedCustomer(parseNumber(words[1], "customer ID"));
        Table table = restaurant.findTable(parseNumber(words[2], "table number"));
        seater.accept(customer, table);
        restaurant.removeFromQueue(customer);
        ui.showMessage(who + " seats customer " + customer.getId() + " at table " + table.getNumber() + ".");
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
                        seatQueuedCustomer(words, waiter::seat, "Waiter");
                    }
                    case "order" -> {
                        expectWords(words, 2, "order <customerId>");
                        Customer customer = restaurant.findCustomer(parseNumber(words[1], "customer ID"));
                        Order order = restaurant.isStage3Enabled()
                                ? waiter.takeOrder(customer, restaurant.getMenu(), restaurant.getHappyHour(), restaurant.getTurn())
                                : waiter.takeOrder(customer, restaurant.getMenu());
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
        if (restaurant.isStage3Enabled()) {
            restaurant.getHost().resetTurn();
        }
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
