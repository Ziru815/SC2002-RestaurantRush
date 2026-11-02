package restaurantrush;

/** Scaffold contract checks only; these do not certify Stage 2 gameplay. */
public final class CustomerExtensionTests {
    private CustomerExtensionTests() { }

    public static void main(String[] args) {
        Customer regular = new RegularCustomer(1);
        Menu menu = new Menu();
        for (MenuItem item : menu.getItems()) {
            if (regular.priceFor(item) != item.getPriceCents()) {
                throw new AssertionError("Regular customer must retain the full menu price.");
            }
        }
        if (!"Regular".equals(regular.getTypeLabel())) {
            throw new AssertionError("Default customer display label must be Regular.");
        }
        try {
            regular.priceFor(null);
            throw new AssertionError("Null menu item must be rejected.");
        } catch (IllegalArgumentException expected) {
            // The caller supplied no item; no quote can be produced.
        }
        System.out.println("PASS 5/5 scaffold customer extension checks (not Stage 2 acceptance)");
    }
}
