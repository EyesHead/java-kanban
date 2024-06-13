package managers.custom_exceptions;

public class ManagerIOException extends RuntimeException {
    public ManagerIOException() {
    }

    public ManagerIOException(String message) {
        super(message);
    }
}
