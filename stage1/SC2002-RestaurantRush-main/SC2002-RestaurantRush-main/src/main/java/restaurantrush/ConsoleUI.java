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

    public ConsoleUI(InputStream input, PrintStream output) {
        this.input = new Scanner(input, StandardCharsets.UTF_8);
        this.output = output;
    }

    public void showWelcome() {
        output.println("Restaurant Rush - Stage 1 SKELETON");
        output.println("Preview mode: game rules are TODOs, not a completed Stage 1 game.");
        output.println("Tables: " + GameConfig.TABLE_COUNT + "; maximum turns: " + GameConfig.MAX_TURNS);
        output.println("Arrivals: " + GameConfig.ARRIVAL_TURNS.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        output.println("Targets: paid >= " + GameConfig.MIN_PAID_CUSTOMERS + ", revenue >= "
                + money(GameConfig.MIN_REVENUE_CENTS) + ", paid average >= " + GameConfig.MIN_AVERAGE_SATISFACTION);
        showHelp();
    }

    public String readCommand() {
        output.print("preview> ");
        output.flush();
        return input.hasNextLine() ? input.nextLine() : null;
    }

    public void showHelp() {
        output.println("Commands: status | menu | tasks | help | start | quit");
        output.println("start currently reports the controller TODO; it does not simulate turns.");
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
        output.println("Turn: " + restaurant.getTurn() + " / " + GameConfig.MAX_TURNS);
        output.println("Revenue: " + money(cashier.getRevenueCents()) + " | Paid: " + cashier.getPaidCount()
                + " | Paid average: " + average(cashier));
        output.println("Waiting queue: " + (restaurant.getWaitingQueue().isEmpty() ? "empty"
                : restaurant.getWaitingQueue().stream().map(customer -> "C" + customer.getId()).collect(Collectors.joining(", "))));
        for (Table table : restaurant.getTables()) {
            output.println("Table " + table.getNumber() + ": " + (table.isFree() ? "free" : "C" + table.getOccupant().getId()));
        }
        for (Customer customer : restaurant.getCustomers()) {
            output.println("C" + customer.getId() + " | satisfaction: " + customer.getSatisfaction()
                    + " | " + customer.getState() + " | order: "
                    + (customer.getOrder() == null ? "none" : customer.getOrder().getStatus()));
        }
        output.println("Waiter: " + availability(restaurant.getWaiter()) + " (seat / order / serve / wait)");
        output.println("Chef: " + availability(restaurant.getChef()) + " (advance one order / wait)");
        output.println("Cashier: " + availability(cashier) + " (automatic payment once implemented)");
        output.println("Served: " + restaurant.getServedCount() + " | Unhappy departures: " + restaurant.getUnhappyDepartures());
    }

    public void showMessage(String text) { output.println(text); }

    public void showResult(Restaurant restaurant, boolean victory) {
        output.println(victory ? "VICTORY" : "DEFEAT");
        showTurnState(restaurant);
    }

    private String average(Cashier cashier) {
        return cashier.getPaidCount() == 0 ? "N/A"
                : String.format(Locale.ROOT, "%.2f", cashier.getAverageSatisfaction());
    }

    private String availability(Staff staff) { return staff.hasActed() ? "used" : "available"; }

    private String money(int cents) {
        return String.format(Locale.ROOT, "$%d.%02d", cents / 100, cents % 100);
    }
}
