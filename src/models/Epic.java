package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private final LocalDateTime DEFAULT_START_TIME =
            LocalDateTime.of(9999999,1,1,0,0);
    private final LocalDateTime DEFAULT_END_TIME =
            LocalDateTime.of(9999999,12,31,23,59);
    private List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime = null;

    //Конструктор создания нового эпика
    public Epic(String name, String description, Status status) {
        super(0, name, description, status, null, 0);
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
        return startTime.orElse(LocalDateTime.of(9999999,1,1,0,0));
    }

    @Override
    public LocalDateTime getEndTime() {
        Optional<LocalDateTime> endTime = subtasks.stream()
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo);
        // время начала самой ранней подзадачи
        return endTime.orElse(LocalDateTime.of(9999999,12,31,23,59));
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
                getId() == epic.getId();
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
        boolean anyInProgress = subtasks.stream().anyMatch(subtask -> subtask.getStatus() == Status.IN_PROGRESS);

        if (allCompleted) {
            this.status = Status.DONE;
        } else if (anyInProgress) {
            this.status = Status.IN_PROGRESS;
        } else {
            this.status = Status.NEW;
        }
    }
}
