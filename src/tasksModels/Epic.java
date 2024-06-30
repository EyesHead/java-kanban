package tasksModels;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();
    //Конструктор создания нового эпика
    public Epic(String name, String description, Status status) {
        super(name, description, status, LocalDateTime.now(), 120);
        this.type = TaskType.EPIC;
        this.startTime = LocalDateTime.now();
    }

    //Конструктор для загрузки эпика из файла в менеджер (нужен параметр id)
    public Epic(int id, String name, String description, Status status,
                LocalDateTime startTime, int minutesOfDuration) {
        super(id, name, description, status, startTime,
                minutesOfDuration);
        this.type = TaskType.EPIC;
    }

    public void addTask(Subtask subtask) {
        subtasks.add(subtask);
        duration = duration + subtask.duration;
        updateTime();
        updateStatus();
    }

    public void removeTask(Subtask subtask) {
        subtasks.remove(subtask);
        if (subtasks.isEmpty()) {
            duration = 0;
        }
        duration = duration - subtask.duration;
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
        return startTime.orElse(LocalDateTime.now());
    }

    @Override
    public LocalDateTime getEndTime() {
        // получаем самое крайнее время завершения из всех подзадач эпика
        Optional<LocalDateTime> endTimeMaxSub = subtasks.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo);

        // получаем общую длительность всех подзадач эпика
        int totalSubtaskDurationMinutes = subtasks.stream()
                .mapToInt(Subtask::getDuration)
                .sum();

        Optional<LocalDateTime> endTimeSumSubDurations = Optional.of(startTime.plusMinutes(totalSubtaskDurationMinutes));

        return endTimeMaxSub.map(localDateTime ->
                localDateTime.isAfter(endTimeSumSubDurations.get())
                ? localDateTime : endTimeSumSubDurations
                .get()).orElse(LocalDateTime.MAX);
    }


    @Override
    public int getDuration() {
        if (subtasks.isEmpty()) {
            return 0;
        }
        return duration;
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
