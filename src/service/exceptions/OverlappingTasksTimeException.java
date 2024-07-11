package service.exceptions;

public class OverlappingTasksTimeException extends RuntimeException {
    public OverlappingTasksTimeException() {
        super("Tasks are overlapping");
    }
}
