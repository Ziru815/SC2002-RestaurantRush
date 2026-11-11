package restaurantrush;

import java.util.List;

/** Owner A. The three base dishes, plus an optional Stage 3 combo. */
public final class Menu {
    private final List<MenuItem> items;
    private final ComboMeal combo;

    public Menu() { this(false); }

    public Menu(boolean includeCombo) {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        combo = includeCombo ? new ComboMeal("Combo", salad, pasta) : null;
        items = includeCombo ? List.of(salad, burger, pasta, combo) : List.of(salad, burger, pasta);
    }

    public boolean hasCombo() { return combo != null; }

    public ComboMeal getCombo() {
        if (!hasCombo()) {
            throw new IllegalStateException("This menu does not offer a combo.");
        }
        return combo;
    }

    public List<MenuItem> getItems() { return items; }

    public MenuItem find(String name) {
        return items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown menu item: " + name));
    }
}
