package service.exceptions;

/**
 * TaskNotFoundException - это кастомное исключение, созданное для обработки случаев,
 * связанными с ошибками поиска задач в памяти менеджера задач
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }

}
