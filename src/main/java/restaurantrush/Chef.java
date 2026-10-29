package restaurantrush;

/** Owner C. May advance a newly placed order, but never a completed/cancelled one. */
public final class Chef extends Staff {
    public void advance(Order order, int turn) {
        requireAvailable();
        // TODO[C]: Validate order, call order.advance(turn), then markActed.
        throw new UnsupportedOperationException("TODO[C] Chef.advance");
    }
}
