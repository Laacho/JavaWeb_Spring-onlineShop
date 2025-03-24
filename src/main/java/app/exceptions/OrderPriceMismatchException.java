package app.exceptions;

public class OrderPriceMismatchException extends RuntimeException {
    public OrderPriceMismatchException(String message) {
        super(message);
    }
}
