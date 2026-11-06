package restaurantrush;

/** Owner A. Stage 2 scaffold: not connected to the default Stage 1 game. */
public final class CriticCustomer extends Customer {
    public CriticCustomer(int id) { super(id); }

    @Override
    public String getTypeLabel() { return "Critic"; }

    @Override
    public int waitingLoss() {
        return 12;
    }

    @Override
    public MenuItem chooseMeal(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu is required.");
        }
        return menu.find("Pasta");
    }
}
