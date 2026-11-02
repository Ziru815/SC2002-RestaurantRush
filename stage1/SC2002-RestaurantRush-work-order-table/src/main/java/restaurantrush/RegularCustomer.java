package restaurantrush;

/** Owner A. Stage 1 contains only this customer subtype. */
public final class RegularCustomer extends Customer {
    public RegularCustomer(int id) { super(id); }

    @Override
    public int waitingLoss() { return 8; }
}
