package factories;

import model.*;

import java.time.LocalDateTime;

/**
 * <h1>Класс для создания экземпляров {@code Task} и дочерних классов</h1>
 * <p>При внесении изменении в класс {@code Task} и его наследников необходимо убедиться,
 * что этот класс правильно настроен и присваивает валидные значения по умолчанию</p>
 */
public class TaskFactory {
    public static Task generateTask(Integer id, String name, String description, Status status,
                                  LocalDateTime startTime, int durationInMinutes) {
        return new Task(id, name, description, status, startTime, durationInMinutes);
    }

    /**
     * Оставшиеся поля task задаются по умолчанию:
     * <p>{@code Integer id = null}</p>
     * <p>{@code LocalDateTime startTime = LocalDateTime.now()}</p>
     * <p>{@code int durationInMinutes = 60}</p>
     */
    public static Task generateTask(String name, String description, Status status) {
        return new Task(null, name, description, status, LocalDateTime.now(), 60);
    }

    /**
     * Оставшиеся поля task задаются по умолчанию:
     * <p>{@code Integer id = null}</p>
     * <p>{@code Status status = NEW}</p>
     * <p>{@code LocalDateTime startTime = LocalDateTime.now()}</p>
     * <p>{@code int durationInMinutes = 60}</p>
     */
    public static Task generateTask(String name, String description) {
        return new Task(null, name, description, Status.NEW, LocalDateTime.now(), 60);
    }
    /**
     * @param epicId поле указывается <b>через геттер полученного экземпляра {@code Epic}
     *               после его создания в менеджере</b
     */
    public static Subtask generateSubtask(Integer id, String name, String description, Status status,
                                        LocalDateTime startTime, int durationInMinutes, int epicId) {
        return new Subtask(id, name, description, status, startTime, durationInMinutes, epicId);
    }

    /**
     * @param epicId поле указывается <b>через геттер полученного экземпляра {@code Epic}
     *               после его создания в менеджере</b>
     * Оставшиеся поля subtask задаются по умолчанию:
     * <p>{@code Integer id = null}</p>
     * <p>{@code LocalDateTime startTime = LocalDateTime.now()}</p>
     * <p>{@code int durationInMinutes = 60}</p>
     */
    public static Subtask generateSubtask(String name, String description, Status status, int epicId) {
        return new Subtask(null, name, description, status, LocalDateTime.now(), 60, epicId);
    }

    /**
     * @param epicId поле указывается <b>через геттер полученного экземпляра {@code Epic}
     *               после его создания в менеджере</b>
     * Оставшиеся поля subtask задаются по умолчанию:
     * <p>{@code Integer id = null}</p>
     * <p>{@code Status status = NEW}</p>
     * <p>{@code LocalDateTime startTime = LocalDateTime.now()}</p>
     * <p>{@code int durationInMinutes = 60}</p>
     */
    public static Subtask generateSubtask(String name, String description, int epicId) {
        return new Subtask(null, name, description, Status.NEW, LocalDateTime.now(), 60, epicId);
    }

    public static Epic generateEpic(Integer id, String name, String description, Status status,
                                  LocalDateTime startTime, int durationInMinutes) {
        return new Epic(id, name, description, status, startTime, durationInMinutes);
    }

    /**
     * Оставшиеся поля epic задаются по умолчанию:
     * <p>{@code Integer id = null}</p>
     * <p>{@code LocalDateTime startTime = LocalDateTime.now()}</p>
     * <p>{@code int durationInMinutes = 0}</p>
     */
    public static Epic generateEpic(String name, String description, Status status) {
        return new Epic(null, name, description, status, LocalDateTime.now(), 0);
    }

    /**
     * Оставшиеся поля epic задаются по умолчанию:
     * <p>{@code Integer id = null}</p>
     * <p>{@code LocalDateTime startTime = LocalDateTime.now()}</p>
     * <p>{@code int durationInMinutes = 0}</p>
     */
    public static Epic generateEpic(String name, String description) {
        return new Epic(null, name, description, Status.NEW, LocalDateTime.now(), 0);
    }
}
