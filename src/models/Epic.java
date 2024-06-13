package models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static models.Status.NEW;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String name, String description, Status status, LocalDateTime startTime, int durationMinutes) {
        super(id, name, description, status, startTime, durationMinutes);
    }

    @Override
    public LocalDateTime getStartTime() {
        // время начала самой ранней подзадачи
    }

    @Override
    public LocalDateTime getEndTime() {
        // время начала самой ранней подзадачи + общая продолжительность всех подзадач
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void removeTask(int subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(subtaskIds, epic.subtaskIds) &&
                getId() == epic.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }
}
