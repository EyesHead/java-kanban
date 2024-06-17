package models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String name, String description, Status status, int epicId,
                   LocalDateTime startTime, int duration) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }
    public Subtask(String name, String description, Status status, int epicId,
                   LocalDateTime startTime, int duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }


    @Override
    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Subtask subtask = (Subtask) object;
        return epicId == subtask.epicId &&
                Objects.equals(getId(), subtask.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
}
