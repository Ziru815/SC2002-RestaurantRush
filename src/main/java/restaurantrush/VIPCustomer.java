package restaurantrush;

/** VIP customer: loses 5 satisfaction while waiting and receives a 10% discount. */
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
        // Round the discounted bill to the nearest cent, with halves rounded up.
        // A long intermediate prevents overflow even for Integer.MAX_VALUE cents.
        return (int) ((item.getPriceCents() * 90L + 50L) / 100L);
    }
}
