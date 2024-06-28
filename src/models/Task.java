package models;
import java.time.*;
import java.util.Objects;
import java.util.TimeZone;


public class Task {
    private Integer id;
    private final String name;
    private final String description;
    protected Status status;
    protected Duration duration;
    protected LocalDateTime startTime;

    protected LocalDateTime DEFAULT_LOCAL_DATE_TIME_START =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(0),
                    TimeZone.getDefault().toZoneId());
    protected int DEFAULT_DURATION = 60;
    protected LocalDateTime DEFAULT_LOCAL_DATE_TIME_END =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(0),
                    TimeZone.getDefault().toZoneId()).plusMinutes(DEFAULT_DURATION);



    // стандартный способ создания новой задачи
    public Task(String name, String description, Status status,
                LocalDateTime startTime, int durationMinutes) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = Duration.ofMinutes(durationMinutes);
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
        return startTime.plus(duration);
    }

    public int getDurationInMinutes() {
        return (int) this.duration.toMinutes();
    }

    public TaskType getType() {
        return TaskType.TASK;
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

    public void setDuration(int minutes) {
        this.duration = Duration.ofMinutes(minutes);
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
        return String.format("%d,%s,%s,%s,%s,%d,%d,%s\n",
                id, getType(), name, status, description, getEpicId(), getDurationInMinutes(), getStartTime());
    }
}