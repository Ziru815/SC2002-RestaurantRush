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
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        Order order = customer.getOrder();
        Table table = customer.getTable();
        if (order == null || order.getCustomer() != customer
                || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("Customer has no payable served order.");
        }
        if (table == null || table.getOccupant() != customer) {
            throw new IllegalStateException("Customer has no occupied table.");
        }
        if (!customer.canPay(turn)) {
            throw new IllegalStateException("Customer cannot pay this turn.");
        }

        // Customer-specific pricing was locked by Order; collect it without repricing.
        // Charge the locked bill once; never apply a second VIP discount here.
        int amount = order.getLockedPriceCents();
        int satisfaction = customer.getSatisfaction();
        order.markPaid();
        customer.markPaid();
        table.release();
        revenueCents += amount;
        paidSatisfactions.add(satisfaction);
        markActed();
        return amount;
    }
}
