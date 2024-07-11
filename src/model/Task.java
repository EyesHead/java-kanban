package model;
import java.time.Duration;
import java.time.LocalDateTime;

import java.util.Objects;

public class Task {
    protected TaskType type;
    private final Integer id;
    private final String name;
    private final String description;
    private Status status;
    private Duration durationInMinutes;
    private LocalDateTime startTime;

    public Task (Integer id, String name, String description, Status status,
                LocalDateTime startTime, int durationInMinutes) {
        this.type = TaskType.TASK;
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.durationInMinutes = Duration.ofMinutes(durationInMinutes);
        this.startTime = startTime;
    }

    public void setType(TaskType type) {
        this.type = type;
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
        return startTime.plus(durationInMinutes);
    }

    public Duration getDurationInMinutes() {
        return durationInMinutes;
    }

    public TaskType getType() {
        return type;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDuration(Duration durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
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
        return String.format("%s,%d,%s,%s,%s,%s,%s%s",
                getType(), getId(), getName(), getDescription(), getStatus(),
                getStartTime(), getDurationInMinutes().toMinutes(), System.lineSeparator());
    }
}