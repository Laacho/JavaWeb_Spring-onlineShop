package app.exceptions;

public class InvalidUsernameOrAddressException extends RuntimeException {
    public InvalidUsernameOrAddressException(String message) {
        super(message);
    }
}
