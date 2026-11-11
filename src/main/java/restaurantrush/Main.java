package restaurantrush;

import java.util.function.IntFunction;

/** Owner D. Java entry point. No game rules belong here. */
public final class Main {
    private static final String USAGE = "Usage: java -cp out/main restaurantrush.Main [--stage2|--stage3] [--check|--help]";

    private Main() { }

    public static void main(String[] args) {
        boolean stage2 = false;
        boolean stage3 = false;
        boolean check = false;
        boolean help = false;
        for (String arg : args) {
            switch (arg) {
                case "--stage2" -> stage2 = true;
                case "--stage3" -> stage3 = true;
                case "--check" -> check = true;
                case "--help" -> help = true;
                default -> usageError();
            }
        }
        if ((check && help) || (stage2 && stage3)) {
            usageError();
        }

        ConsoleUI ui = new ConsoleUI(System.in, System.out, stage3 ? "Stage 3" : stage2 ? "Stage 2" : "Stage 1");
        Restaurant restaurant = new Restaurant(stage3);
        if (check) {
            ui.showWelcome();
            ui.showMenu(restaurant.getMenu());
            ui.showTurnState(restaurant);
            ui.showMessage("Wiring OK. Type start to play.");
            return;
        }
        if (help) {
            ui.showHelp();
            ui.showMessage("Launch options: --stage2 plays Stage 2 with VIP and Critic customers; "
                    + "--stage3 adds the Host, the combo meal and Happy Hour; "
                    + "without either you play Stage 1 with Regular customers only.");
            return;
        }
        IntFunction<Customer> arrivalFactory = (stage2 || stage3) ? Stage2Arrivals::create : RegularCustomer::new;
        new GameController(restaurant, ui, arrivalFactory).run();
    }

    private static void usageError() {
        System.err.println(USAGE);
        System.exit(2);
    }
}
