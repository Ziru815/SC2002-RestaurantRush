package restaurantrush;

/** Owner A. Immutable menu data; money is in cents. This is a concrete class. */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public MenuItem(String name, int priceCents, int prepUnits) {
        if (name == null || name.isBlank() || priceCents < 0 || prepUnits < 1) {
            throw new IllegalArgumentException("Menu item needs a name, nonnegative price and positive preparation units.");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() { return name; }
    public int getPriceCents() { return priceCents; }
    public int getPrepUnits() { return prepUnits; }
}
