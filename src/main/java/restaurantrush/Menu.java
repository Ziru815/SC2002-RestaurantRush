package restaurantrush;

import java.util.List;

/** Owner A. Contains exactly the three compulsory Stage 1 dishes. */
public final class Menu {
    private final List<MenuItem> items = List.of(
            new MenuItem("Salad", 900, 1),
            new MenuItem("Burger", 1500, 1),
            new MenuItem("Pasta", 1800, 2));

    public List<MenuItem> getItems() { return items; }

    public MenuItem find(String name) {
        return items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown menu item: " + name));
    }
}
