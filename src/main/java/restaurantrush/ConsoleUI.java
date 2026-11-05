package restaurantrush;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Owner D. Input/output only: no state transitions, pricing or victory calculation. */
public final class ConsoleUI {
    private final Scanner input;
    private final PrintStream output;
    private final int stage;

    /** The Stage 1 screen. */
    public ConsoleUI(InputStream input, PrintStream output) {
        this(input, output, 1);
    }

    /** From Stage 2 on, the screen also shows each customer's type. */
    public ConsoleUI(InputStream input, PrintStream output, int stage) {
        if (stage < 1) {
            throw new IllegalArgumentException("Stage must be 1 or higher.");
        }
        this.input = new Scanner(input, StandardCharsets.UTF_8);
        this.output = output;
        this.stage = stage;
    }

    public void showWelcome() {
        output.println("Restaurant Rush - Stage " + stage);
        output.println("You are the manager. Each turn you choose one task for the Waiter and one for the Chef.");
        output.println("Tables: " + GameConfig.TABLE_COUNT + "; turns: " + GameConfig.MAX_TURNS);
        output.println("Arrivals on turns: " + GameConfig.ARRIVAL_TURNS.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        output.println("To win, after the last turn: paid customers >= " + GameConfig.MIN_PAID_CUSTOMERS
                + ", revenue >= " + money(GameConfig.MIN_REVENUE_CENTS)
                + ", paid-customer average satisfaction >= " + GameConfig.MIN_AVERAGE_SATISFACTION);
        showHelp();
    }

    public String readCommand() {
        return readCommand("> ");
    }

    /** Prints the prompt and returns the next line, or null when the input has ended. */
    public String readCommand(String prompt) {
        output.print(prompt);
        output.flush();
        return input.hasNextLine() ? input.nextLine() : null;
    }

    public void showHelp() {
        output.println("Before the game: status | menu | tasks | help | start | quit");
        output.println("Waiter: seat <customerId> <tableNo> | order <customerId> | serve <customerId> | wait");
        output.println("Chef:   cook <customerId> | wait");
        output.println("In any prompt: status shows the restaurant again; help shows this list.");
    }

    public void showTasks() {
        output.println("A: Customer / RegularCustomer / CustomerState / Menu / MenuItem");
        output.println("B: Table / Order / OrderStatus");
        output.println("C: Staff / Waiter / Chef / Cashier");
        output.println("D: Restaurant / GameController / ConsoleUI / Main / GameConfig + integration");
        output.println("See docs/TEAM_GUIDE.md for contracts and docs/STAGE1_ACCEPTANCE.md for rule checks.");
    }

    public void showMenu(Menu menu) {
        for (MenuItem item : menu.getItems()) {
            output.println(item.getName() + " | " + money(item.getPriceCents())
                    + " | preparation units: " + item.getPrepUnits());
        }
    }

    public void showTurnState(Restaurant restaurant) {
        Cashier cashier = restaurant.getCashier();
        output.println();
        output.println("===== Turn " + restaurant.getTurn() + " / " + GameConfig.MAX_TURNS + " =====");
        output.println("Revenue: " + money(cashier.getRevenueCents()) + " | Paid: " + cashier.getPaidCount()
                + " | Paid average: " + average(cashier));
        output.println("Waiting queue: " + (restaurant.getWaitingQueue().isEmpty() ? "empty"
                : restaurant.getWaitingQueue().stream().map(customer -> "C" + customer.getId()).collect(Collectors.joining(", "))));
        for (Table table : restaurant.getTables()) {
            output.println("Table " + table.getNumber() + ": " + (table.isFree() ? "free" : "C" + table.getOccupant().getId()));
        }
        for (Customer customer : restaurant.getCustomers()) {
            output.println("C" + customer.getId() + typeTag(customer) + " | satisfaction: " + customer.getSatisfaction()
                    + " | " + customer.getState() + " | order: " + describeOrder(customer.getOrder()));
        }
        output.println("Waiter: " + availability(restaurant.getWaiter()) + " (seat / order / serve / wait)");
        output.println("Chef: " + availability(restaurant.getChef()) + " (cook one order / wait)");
        output.println("Cashier: " + availability(cashier) + " (automatic payment)");
        output.println("Served: " + restaurant.getServedCount() + " | Unhappy departures: " + restaurant.getUnhappyDepartures());
    }

    public void showMessage(String text) { output.println(text); }

    /** Shows the dish the customer chose, once the order has been placed. */
    public void showOrderTaken(Order order) {
        output.println("Waiter takes customer " + order.getCustomer().getId() + "'s order: "
                + order.getItem().getName() + " (" + money(order.getLockedPriceCents()) + ").");
    }

    public void showCooking(Order order) {
        String progress = order.getStatus() == OrderStatus.READY
                ? "READY"
                : order.getUnitsDone() + "/" + order.getItem().getPrepUnits();
        output.println("Chef cooks customer " + order.getCustomer().getId() + "'s "
                + order.getItem().getName() + ": " + progress + ".");
    }

    public void showPayment(Customer customer, int cents) {
        output.println("Cashier collects " + money(cents) + " from customer " + customer.getId()
                + " (satisfaction recorded: " + customer.getSatisfaction() + ").");
    }

    /** The end-of-turn results required by the brief. */
    public void showTurnEnd(Restaurant restaurant) {
        Cashier cashier = restaurant.getCashier();
        output.println("End of turn " + restaurant.getTurn() + ": served " + restaurant.getServedCount()
                + " | unhappy departures " + restaurant.getUnhappyDepartures()
                + " | revenue " + money(cashier.getRevenueCents())
                + " | paid average " + average(cashier)
                + " | turns used " + restaurant.getTurn());
    }

    public void showResult(Restaurant restaurant, boolean victory) {
        Cashier cashier = restaurant.getCashier();
        output.println();
        output.println("===== Game over =====");
        output.println("Turns used: " + restaurant.getTurn());
        output.println("Served: " + restaurant.getServedCount() + " | Paid: " + cashier.getPaidCount()
                + " | Unhappy departures: " + restaurant.getUnhappyDepartures());
        output.println("Revenue: " + money(cashier.getRevenueCents()));
        output.println("Average satisfaction of paid customers: " + average(cashier));
        output.println(victory ? "VICTORY" : "DEFEAT");
    }

    /** Display only: the type comes from the customer, the screen never decides anything from it. */
    private String typeTag(Customer customer) {
        return stage >= 2 ? " (" + customer.getTypeLabel() + ")" : "";
    }

    private String describeOrder(Order order) {
        return order == null ? "none" : order.getItem().getName() + " " + order.getStatus();
    }

    private String average(Cashier cashier) {
        return cashier.getPaidCount() == 0 ? "N/A"
                : String.format(Locale.ROOT, "%.1f", cashier.getAverageSatisfaction());
    }

    private String availability(Staff staff) { return staff.hasActed() ? "used" : "available"; }

    private String money(int cents) {
        return String.format(Locale.ROOT, "$%d.%02d", cents / 100, cents % 100);
    }
}
