package restaurantrush;

/** Critic customer: loses 12 satisfaction while waiting and always chooses Pasta. */
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
        // Team-selected deterministic preference; full-price billing is inherited.
        return menu.find("Pasta");
    }
}
