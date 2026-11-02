package restaurantrush;

import java.util.List;

/** Owner D. The compulsory base setting; no optional difficulty settings yet. */
public final class GameConfig {
    public static final int MAX_TURNS = 18;
    public static final int TABLE_COUNT = 2;
    public static final int MIN_PAID_CUSTOMERS = 4;
    public static final int MIN_REVENUE_CENTS = 4500;
    public static final int MIN_AVERAGE_SATISFACTION = 60;
    public static final List<Integer> ARRIVAL_TURNS = List.of(1, 4, 7, 10, 13, 16);

    private GameConfig() { }
}
