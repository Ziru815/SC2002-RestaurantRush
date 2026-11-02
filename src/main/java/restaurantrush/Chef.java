package restaurantrush;

/** Owner C. May advance a newly placed order, but never a completed/cancelled one. */
public final class Chef extends Staff {
    public void advance(Order order, int turn) {
        requireAvailable();
        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }

        // Order owns preparation progress and the READY/ready-turn transition.
        order.advance(turn);
        markActed();
    }
}
