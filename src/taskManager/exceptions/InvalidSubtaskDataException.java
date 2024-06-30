package taskManager.exceptions;

public class InvalidSubtaskDataException extends RuntimeException {
    public InvalidSubtaskDataException(String message) {
        super(message);
    }

    public InvalidSubtaskDataException() {
        super();
    }
}
