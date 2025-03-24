package app.exceptions;

public class NotificationFeignClientNotWorkingException extends RuntimeException {
    public NotificationFeignClientNotWorkingException(String message) {
        super(message);
    }

}
