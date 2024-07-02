package taskManager.exceptions;

public class OverlapValidationException extends RuntimeException {
    public OverlapValidationException() {
        super();
    }

    public OverlapValidationException(String message) {
        super(message);
    }
}
