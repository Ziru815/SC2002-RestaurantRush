package restaurantrush;

/** Owner B. Only unfinished orders may be cancelled on abandonment. */
public enum OrderStatus {
    PLACED, PREPARING, READY, SERVED, PAID, CANCELLED
}
