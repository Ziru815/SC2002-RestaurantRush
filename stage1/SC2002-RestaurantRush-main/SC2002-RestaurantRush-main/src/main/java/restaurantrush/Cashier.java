package restaurantrush;

import java.util.ArrayList;
import java.util.List;

/** Owner C. D selects the oldest eligible payer; cashier validates and charges once. */
public final class Cashier extends Staff {
    private int revenueCents;
    private final List<Integer> paidSatisfactions = new ArrayList<>();

    public int getRevenueCents() { return revenueCents; }
    public int getPaidCount() { return paidSatisfactions.size(); }

    /** NaN means no payments yet; the UI must display N/A. */
    public double getAverageSatisfaction() {
        return paidSatisfactions.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN);
    }

    public int collectPayment(Customer customer, int turn) {
        requireAvailable();
        // TODO[C]: Validate customer.canPay(turn), order SERVED, occupied table.
        // Save the table reference; mark order PAID and customer PAID; release table.
        // Add lockedPriceCents and snapshot satisfaction exactly once; markActed.
        // Return amount in cents. D removes the paid order from activeOrders.
        throw new UnsupportedOperationException("TODO[C] Cashier.collectPayment");
    }
}
