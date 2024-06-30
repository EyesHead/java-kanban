package tasksModels;
import java.time.*;
import java.util.Objects;

import static tasksModels.TaskType.TASK;


public class Task {
    protected TaskType type;
    private Integer id;
    private final String name;
    private final String description;
    protected Status status;
    protected int duration;
    protected LocalDateTime startTime;


    // стандартный способ создания новой задачи
    public Task(String name, String description, Status status,
                LocalDateTime startTime, int duration) {
        this.type = TASK;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    // параметр id указывается для обновления задачи с тем же id в менеджере и загрузки из файла
    public Task(int id, String name, String description, Status status,
                LocalDateTime startTime, int durationMinutes) {
        this(name, description, status, startTime, durationMinutes);
        this.id = id;
    }


    public Integer getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(Duration.ofMinutes(duration));
    }

    public int getDuration() {
        return duration;
    }

    public TaskType getType() {
        return TASK;
    }

    public Integer getEpicId() {
        return null;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d,%d,%s%s",
                id, getType(), name, status, description, getEpicId(), getDuration(), getStartTime(),
                System.lineSeparator());
    }
}