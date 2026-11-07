package restaurantrush;

/** Owner A. Stage 2 scaffold: not connected to the default Stage 1 game. */
public final class VIPCustomer extends Customer {
    public VIPCustomer(int id) { super(id); }

    @Override
    public String getTypeLabel() { return "VIP"; }

    @Override
    public int waitingLoss() {
        return 5;
    }

    @Override
    public int priceFor(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item is required.");
        }
        return Math.toIntExact((item.getPriceCents() * 90L + 50L) / 100L);
    }
}
