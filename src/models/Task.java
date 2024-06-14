package models;
import java.time.*;
import java.util.Objects;


public class Task {
    private int id;
    private final String name;
    private final String description;
    protected Status status;
    protected Duration duration;
    protected LocalDateTime startTime;


    public Task(int id, String name, String description, Status status,
                LocalDateTime startTime, int durationMinutes) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = Duration.ofMinutes(durationMinutes);
        this.startTime = startTime;
    }


    public int getId(){
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
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d,%d,%s\n",
                id, this.getType(), name, status, description, getEpicId(), getDurationInMinutes(), getStartTime());
    }
}