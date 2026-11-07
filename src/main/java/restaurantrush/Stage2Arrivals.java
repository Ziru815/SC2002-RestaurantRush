package restaurantrush;

/** Owner D. Setup only; customer-specific rules remain in customer subclasses. */
public final class Stage2Arrivals {
    private static final int VIP_ID = 2;
    // ID 4 normally chooses Salad, so its Critic Pasta preference is visible.
    private static final int CRITIC_ID = 4;

    private Stage2Arrivals() { }

    public static Customer create(int id) {
        int arrivals = GameConfig.ARRIVAL_TURNS.size();
        if (id < 1 || id > arrivals) {
            throw new IllegalArgumentException("Customer ID must be between 1 and " + arrivals + ".");
        }
        if (id == VIP_ID) {
            return new VIPCustomer(id);
        }
        if (id == CRITIC_ID) {
            return new CriticCustomer(id);
        }
        return new RegularCustomer(id);
    }
}
