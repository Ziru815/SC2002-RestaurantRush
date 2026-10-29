package restaurantrush;

/** Owner D. Java entry point. No game rules belong here. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI(System.in, System.out);
        Restaurant restaurant = new Restaurant();
        if (args.length == 1 && args[0].equals("--check")) {
            ui.showWelcome();
            ui.showMenu(restaurant.getMenu());
            ui.showTurnState(restaurant);
            ui.showMessage("Scaffold wiring OK. Gameplay is not implemented yet.");
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
