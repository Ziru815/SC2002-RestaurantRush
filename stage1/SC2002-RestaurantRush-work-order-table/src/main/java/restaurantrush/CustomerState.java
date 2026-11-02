package restaurantrush;

/** Owner A. SEATED also covers waiting for an order or food. SERVED covers eating. */
public enum CustomerState {
    QUEUED, SEATED, SERVED, READY_TO_PAY, PAID, LEFT
}
