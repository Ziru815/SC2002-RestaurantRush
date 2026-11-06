package restaurantrush;

/** Owner A. Stage 2 scaffold: not connected to the default Stage 1 game. */
public final class CriticCustomer extends Customer {
    public CriticCustomer(int id) { super(id); }

    @Override
    public String getTypeLabel() { return "Critic"; }

    @Override
    public int waitingLoss() {
        // TODO[A-S2]: return 12; inherited loss logic handles states and zero clamp.
        throw new UnsupportedOperationException("TODO[A-S2]: Critic waiting loss");
    }

    @Override
    public MenuItem chooseMeal(Menu menu) {
        // TODO[A-S2]: documented preference: always select Pasta; reject null.
        // This is a team design choice, not a dish mandated by the assignment.
        throw new UnsupportedOperationException("TODO[A-S2]: Critic Pasta preference");
    }
}
