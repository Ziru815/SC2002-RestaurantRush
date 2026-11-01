package restaurantrush;

/** Owner C. Shared one-action-per-turn guard; never use Object.wait() for game waiting. */
public abstract class Staff {
    private boolean actedThisTurn;

    public boolean hasActed() { return actedThisTurn; }
    public void resetTurn() { actedThisTurn = false; }

    public void skipTurn() {
        requireAvailable();
        markActed();
    }

    protected final void requireAvailable() {
        if (actedThisTurn) {
            throw new IllegalStateException("This staff member has already acted this turn.");
        }
    }

    protected final void markActed() {
        requireAvailable();
        actedThisTurn = true;
    }
}
