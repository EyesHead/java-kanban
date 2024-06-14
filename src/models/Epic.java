package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(int id, String name, String description, Status status,
                LocalDateTime startTime, int durationMinutes) {
        super(id, name, description, status, startTime, durationMinutes);
    }

    public void addTask(Subtask subtask) {
        if (subtasks.isEmpty()) {
            super.startTime = subtask.startTime;
            endTime = subtask.getEndTime();
        } else {
            if (super.startTime.isAfter(subtask.startTime)) {
                super.startTime = subtask.startTime;
            }
            LocalDateTime subtaskEndTime = subtask.getEndTime();
            if (subtaskEndTime.isAfter(endTime)) {
                endTime = subtaskEndTime;
            }
        }
        subtasks.add(subtask);
        super.duration = super.duration.plus(subtask.duration);
    }

    @Override
    public LocalDateTime getStartTime() {
        Optional<LocalDateTime> startTime = subtasks.stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo);
        // время начала самой ранней подзадачи
        return startTime.orElseThrow(() -> new RuntimeException("Список задач у эпика пуст"));
    }

    @Override
    public LocalDateTime getEndTime() {
        int duration = subtasks.stream()
                .map(Task::getDurationInMinutes)
                .reduce(0, Integer::sum);
        // время начала самой ранней подзадачи + общая продолжительность всех подзадач
        return getStartTime().plusMinutes(duration);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void removeTask(Subtask subtask) {
        subtasks.remove(subtask);
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
        return Objects.equals(subtasks, epic.subtasks) &&
                getId() == epic.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }
}
