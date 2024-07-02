package tasksModels;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private List<Subtask> epicSubtasks = new ArrayList<>();
    //Конструктор создания нового эпика
    public Epic(String name, String description, Status status) {
        super(name, description, status, LocalDateTime.now(), 0);
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

    public void updateStatus() {
        boolean allDone = epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.DONE);
        boolean allNew = epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.NEW);

        if (allNew || epicSubtasks.isEmpty()) {
            this.status = Status.NEW;
        } else if (allDone) {
            this.status = Status.DONE;
        } else {
            this.status = Status.IN_PROGRESS;
        }
    }

    public void addTask(Subtask subtask) {
        epicSubtasks.add(subtask);
        duration.plus(subtask.getDuration());
        updateTime();
    }

    public void removeTask(Subtask subtask) {
        epicSubtasks.remove(subtask);
        if (epicSubtasks.isEmpty()) {
            duration = Duration.ofMinutes(0);
        }
        duration.minus(subtask.getDuration());
        updateTime();
    }

    public void setEpicSubtasks(List<Subtask> epicSubtasks) {
        this.epicSubtasks = epicSubtasks;
    }

    public List<Subtask> getEpicSubtasks() {
        return epicSubtasks;
    }

    @Override
    public LocalDateTime getStartTime() {
        Optional<LocalDateTime> startTime = epicSubtasks.stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo);
        // время начала самой ранней подзадачи
        return startTime.orElse(LocalDateTime.now());
    }

    @Override
    public LocalDateTime getEndTime() {
        // получаем самое крайнее время завершения из всех подзадач эпика
        Optional<LocalDateTime> endTimeMaxSub = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo);

        // получаем общую длительность всех подзадач эпика
        int totalSubtaskDurationMinutes = (int) epicSubtasks.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus)
                .toMinutes();

        Optional<LocalDateTime> endTimeSumSubDurations = Optional.of(startTime.plusMinutes(totalSubtaskDurationMinutes));

        return endTimeMaxSub.map(localDateTime ->
                localDateTime.isAfter(endTimeSumSubDurations.get())
                ? localDateTime : endTimeSumSubDurations.get())
                .orElse(null);
    }

    @Override
    public Duration getDuration() {
        if (epicSubtasks.isEmpty()) {
            return Duration.ofMinutes(0);
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
        return Objects.equals(epicSubtasks, epic.epicSubtasks) &&
                Objects.equals(getId(), epic.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicSubtasks);
    }

    private void updateTime() {
        this.startTime = getStartTime();
    }
}
