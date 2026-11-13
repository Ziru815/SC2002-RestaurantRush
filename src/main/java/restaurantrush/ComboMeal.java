package restaurantrush;

/** Owner A. A single orderable dish made from two existing menu items. */
public final class ComboMeal extends MenuItem {
    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(String name, MenuItem first, MenuItem second) {
        super(name, comboPrice(first, second),
                checkedInt((long) first.getPrepUnits() + second.getPrepUnits()));
        this.first = first;
        this.second = second;
    }

    public MenuItem getFirst() { return first; }
    public MenuItem getSecond() { return second; }

    private static int comboPrice(MenuItem first, MenuItem second) {
        if (first == null || second == null || first == second
                || first instanceof ComboMeal || second instanceof ComboMeal) {
            throw new IllegalArgumentException("A combo requires two distinct basic menu items.");
        }
        long total = (long) first.getPriceCents() + second.getPriceCents();
        return checkedInt((total * 90 + 50) / 100);
    }

    private static int checkedInt(long value) {
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Combo price or preparation units exceed the supported range.");
        }
        return (int) value;
    }
}
