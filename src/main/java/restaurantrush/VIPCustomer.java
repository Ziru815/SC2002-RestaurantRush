package restaurantrush;

/** Owner A. Stage 2 scaffold: not connected to the default Stage 1 game. */
public final class VIPCustomer extends Customer {
    public VIPCustomer(int id) { super(id); }

    @Override
    public String getTypeLabel() { return "VIP"; }

    @Override
    public int waitingLoss() {
        // TODO[A-S2]: return 5; inherited loss logic handles states and zero clamp.
        throw new UnsupportedOperationException("TODO[A-S2]: VIP waiting loss");
    }

    @Override
    public int priceFor(MenuItem item) {
        // TODO[A-S2]: reject null; quote 90% in cents, nearest-cent half-up.
        // Use long intermediate arithmetic; do not change the MenuItem price.
        throw new UnsupportedOperationException("TODO[A-S2]: VIP price quote");
    }
}
