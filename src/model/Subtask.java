package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(Integer id, String name, String description, Status status,
                   LocalDateTime startTime, int duration, int epicId) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
        setType(TaskType.SUBTASK);
    }

    public Integer getEpicId() {
        return epicId;
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

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%d%s",
                getType(), getId(), getName(), getDescription(), getStatus(), getStartTime(),
                getDurationInMinutes().toMinutes(), getEpicId(), System.lineSeparator());
    }
}
