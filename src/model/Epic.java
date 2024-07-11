package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static model.Status.NEW;

public class Epic extends Task {
    private List<Subtask> epicSubtasks = new ArrayList<>();

    /**
     * Этот конструктор используется исключительно для сериализации/десериализации задач, которые УЖЕ были добавлены
     * в менеджер
     * @param id создается и присваивается в менеджере. При использовании конструктора должнен быть определен не null
     *           значением
     */
    public Epic(Integer id, String name, String description, Status status,
                LocalDateTime startTime, int durationInMinutes) {
        super(id, name, description, status, startTime, durationInMinutes);
        setType(TaskType.EPIC);
        setEpicSubtasks(new ArrayList<>());
    }

    private void updateStatus() {
        boolean areAllEpicSubsDone = epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.DONE);
        boolean areAllEpicSubsNew = epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == NEW);

        if (areAllEpicSubsNew || epicSubtasks.isEmpty()) {
            setStatus(NEW);
        } else if (areAllEpicSubsDone) {
            setStatus(Status.DONE);
        } else {
            setStatus(Status.IN_PROGRESS);
        }
    }

    /**
     * Настраивает поля {@code status}, {@code startTime}, {@code durationInMinutes} на основе содержащихся подзадач
     * в {@code epicSubtasks}
     * <p>Следует вызывать после добавления подзадач в менеджер</p>
     */
    public void configureEpic() {
        updateStatus();
        updateStartTime();
        updateDurationMinutes();
    }

    public void addTask(Subtask subtask) {
        epicSubtasks.add(subtask);
    }

    public void removeTask(Subtask subtask) {
        epicSubtasks.remove(subtask);
    }

    public void setEpicSubtasks(List<Subtask> epicSubtasks) {
        this.epicSubtasks = epicSubtasks;
    }

    public List<Subtask> getEpicSubtasks() {
        return epicSubtasks;
    }

    @Override
    public LocalDateTime getEndTime() {
        // получаем самое крайнее время завершения из всех подзадач эпика
        Optional<LocalDateTime> endTimeMaxSub = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo);

        // получаем общую длительность всех подзадач эпика
        int totalSubtaskDurationMinutes = (int) epicSubtasks.stream()
                .map(Subtask::getDurationInMinutes)
                .reduce(Duration.ZERO, Duration::plus)
                .toMinutes();

        Optional<LocalDateTime> endTimeSumSubDurations = Optional.of(getStartTime()
                .plusMinutes(totalSubtaskDurationMinutes));

        LocalDateTime newEndTime = endTimeMaxSub.map(localDateTime ->
                localDateTime.isAfter(endTimeSumSubDurations.get())
                ? localDateTime : endTimeSumSubDurations.get())
                .orElse(LocalDateTime.now());
        return newEndTime;
    }

    @Override
    public Duration getDurationInMinutes() {
        if (epicSubtasks.isEmpty()) {
            return Duration.ofMinutes(0);
        }
        return super.getDurationInMinutes();
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

    private void updateDurationMinutes() {
        Duration newDuration = epicSubtasks.stream()
                .map(Subtask::getDurationInMinutes)
                .reduce(Duration.ZERO, Duration::plus);
        setDuration(newDuration);
    }

    private void updateStartTime() {
        Optional<LocalDateTime> startTime = epicSubtasks.stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo);
        // время начала самой ранней подзадачи
        LocalDateTime newStartTime = startTime.orElse(LocalDateTime.now());
        setStartTime(newStartTime);
    }
}
