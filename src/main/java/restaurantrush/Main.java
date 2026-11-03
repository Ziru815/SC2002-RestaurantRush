package restaurantrush;

/** Owner D. Java entry point. No game rules belong here. */
public final class Main {
    private Main() { }

    // TODO[D-S2]: add --stage2 to select Stage2Arrivals::create; keep default
    // Stage 1, --check and --help compatible. Do not enable incomplete stubs.
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI(System.in, System.out);
        Restaurant restaurant = new Restaurant();
        if (args.length == 1 && args[0].equals("--check")) {
            ui.showWelcome();
            ui.showMenu(restaurant.getMenu());
            ui.showTurnState(restaurant);
            ui.showMessage("Wiring OK. Type start to play.");
            return;
        }
        if (args.length == 1 && args[0].equals("--help")) {
            ui.showHelp();
            return;
        }
        if (args.length != 0) {
            System.err.println("Usage: java -cp out/main restaurantrush.Main [--check|--help]");
            System.exit(2);
        }
        new GameController(restaurant, ui).run();
    }
}
