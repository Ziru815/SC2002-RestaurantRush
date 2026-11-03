package restaurantrush;

/** Owner D. Setup only; customer-specific rules remain in customer subclasses. */
public final class Stage2Arrivals {
    private Stage2Arrivals() { }

    public static Customer create(int id) {
        // TODO[D-S2]: validate 1..6. ID 2 -> VIP, ID 4 -> Critic, others -> Regular.
        // ID 4 normally chooses Salad, so its Critic Pasta preference is visible.
        // Keep IDs and arrival turns unchanged. This factory selects construction,
        // not pricing, waiting loss or order preference.
        throw new UnsupportedOperationException("TODO[D-S2]: Stage 2 arrival factory");
    }
}
