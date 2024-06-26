package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime = null;

    //Конструктор создания нового эпика
    public Epic(String name, String description, Status status) {
        super(name, description, status, LocalDateTime.now(), 0);
        this.startTime = DEFAULT_LOCAL_DATE_TIME_START;
        this.duration = Duration.ofMinutes(DEFAULT_DURATION);
        this.endTime = DEFAULT_LOCAL_DATE_TIME_END;
    }

    //Конструктор для загрузки эпика из файла в менеджер (нужен параметр id)
    public Epic(int id, String name, String description, Status status,
                LocalDateTime startTime, int minutesOfDuration) {
        super(id, name, description, status, startTime,
                minutesOfDuration);
        endTime = getEndTime();
    }

    public void addTask(Subtask subtask) {
        subtasks.add(subtask);
        duration = duration.plus(subtask.duration);
        updateTime();
        updateStatus();
    }

    public void removeTask(Subtask subtask) {
        subtasks.remove(subtask);
        if (subtasks.isEmpty()) {
            duration = Duration.ofMinutes(0);
        }
        duration = duration.minus(subtask.duration);
        updateTime();
        updateStatus();
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public LocalDateTime getStartTime() {
        Optional<LocalDateTime> startTime = subtasks.stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo);
        // время начала самой ранней подзадачи
        return startTime.orElse(DEFAULT_LOCAL_DATE_TIME_START);
    }

    @Override
    public LocalDateTime getEndTime() {
        Optional<LocalDateTime> endTime = subtasks.stream()
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo);
        // время начала самой ранней подзадачи
        return endTime.orElse(DEFAULT_LOCAL_DATE_TIME_END);
    }

    @Override
    public int getDurationInMinutes() {
        if (subtasks.isEmpty()) {
            return 0;
        }
        return (int) duration.toMinutes();
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
                Objects.equals(getId(), epic.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    private void updateTime() {
        this.startTime = getStartTime();
        this.endTime = getEndTime();
    }

    private void updateStatus() {
        boolean allCompleted = subtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.DONE);
        boolean allNew = subtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.NEW);

        if (allCompleted) {
            this.status = Status.DONE;
        } else if (allNew) {
            this.status = Status.NEW;
        } else {
            this.status = Status.IN_PROGRESS;
        }
    }
}
