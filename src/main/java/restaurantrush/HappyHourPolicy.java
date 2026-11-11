package restaurantrush;

import java.util.Objects;

/** Owner B. One Happy Hour per game, covering its activation turn and the next. */
public final class HappyHourPolicy {
    private static final int DURATION = 2;
    private int activationTurn = -1;

    public void activate(int turn) {
        requirePlayableTurn(turn);
        if (hasActivated()) {
            throw new IllegalStateException("Happy Hour has already been activated.");
        }
        activationTurn = turn;
    }

    public boolean hasActivated() { return activationTurn >= 0; }

    public boolean isActive(int turn) { return remainingTurns(turn) > 0; }

    public int remainingTurns(int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException("Turn cannot be negative.");
        }
        if (!hasActivated() || turn < activationTurn) {
            return 0;
        }
        return Math.max(0, activationTurn + DURATION - turn);
    }

    /** Apply the promotion to the customer's already-rounded quote, in cents. */
    public int quote(Customer customer, MenuItem item, int turn) {
        requirePlayableTurn(turn);
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(item, "item");
        int cents = customer.priceFor(item);
        if (cents < 0) {
            throw new IllegalArgumentException("Customer quote cannot be negative.");
        }
        return isActive(turn) ? (int) ((cents * 80L + 50) / 100) : cents;
    }

    private static void requirePlayableTurn(int turn) {
        if (turn < 1 || turn > GameConfig.MAX_TURNS) {
            throw new IllegalArgumentException("Turn must be between 1 and " + GameConfig.MAX_TURNS + ".");
        }
    }
}
