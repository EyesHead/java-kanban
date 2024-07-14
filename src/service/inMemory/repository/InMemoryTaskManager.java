package service.inMemory.repository;

import org.jetbrains.annotations.NotNull;
import service.ManagersCreator;
import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import service.exceptions.OverlappingTasksTimeException;
import service.exceptions.TaskNotFoundException;
import service.history.InMemoryHistoryManager;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int managerId = 0;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final InMemoryHistoryManager historyManager;
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager() {
        historyManager = ManagersCreator.getDefaultHistory();
    }

    public InMemoryTaskManager(InMemoryHistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return managerId++;
    }

    @Override
    public InMemoryHistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public Task getTaskById(int taskId) throws TaskNotFoundException {
        Optional<Task> optionalTask = Optional.ofNullable(tasks.get(taskId));
        if (optionalTask.isPresent()) {
            historyManager.add(tasks.get(taskId));
        }
        return optionalTask.orElseThrow(() -> new TaskNotFoundException("Task was not found by id"));
    }

    @Override
    public Epic getEpicById(int epicId) throws TaskNotFoundException {
        Optional<Epic> optionalEpic = Optional.ofNullable(epics.get(epicId));
        if (optionalEpic.isPresent()) {
            historyManager.add(epics.get(epicId));
        }
        return optionalEpic.orElseThrow(() -> new TaskNotFoundException("Epic was not found by id"));
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) throws TaskNotFoundException {
        Optional<Epic> optionalEpic = Optional.ofNullable(epics.get(epicId));
        if (optionalEpic.isPresent()) {
            historyManager.add(epics.get(epicId));
        }
        return optionalEpic.orElseThrow(() -> new TaskNotFoundException("Epic not found by id"))
                .getEpicSubtasks();
    }

    @Override
    public Subtask getSubtaskById(int subtaskId) throws TaskNotFoundException {
        Optional<Subtask> optionalSubtask = Optional.ofNullable(subtasks.get(subtaskId));
        if (optionalSubtask.isPresent()) {
            historyManager.add(subtasks.get(subtaskId));
        }
        return optionalSubtask.orElseThrow(() -> new TaskNotFoundException("Subtask was not found by id"));
    }

    @Override
    public Task createTask(Task task) {
        Task newTask = new Task(generateId(), task.getName(), task.getDescription(), task.getStatus(),
                task.getStartTime(), (int) task.getDurationInMinutes().toMinutes());

        try {
            validateTimeOnOverlap(newTask); // 406 Validation exc
            prioritizedTasks.add(newTask);
            System.out.println(newTask);
        } catch (OverlappingTasksTimeException _) {
            System.out.println("was not added to priority:\n" + task);
        }

        tasks.put(newTask.getId(), newTask);
        return newTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = new Epic(generateId(), epic.getName(), epic.getDescription(), epic.getStatus(),
                epic.getStartTime(), (int) epic.getDurationInMinutes().toMinutes());
        epics.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = new Subtask(generateId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                subtask.getStartTime(), (int) subtask.getDurationInMinutes().toMinutes(), subtask.getEpicId());
        // обновляем список подзадач у эпика
        Epic epic = epics.get(newSubtask.getEpicId());
        epic.addTask(newSubtask);
        epic.configureEpic();

        subtasks.put(newSubtask.getId(), newSubtask);
        epics.put(epic.getId(), epic);

        try {
            validateTimeOnOverlap(newSubtask);
            prioritizedTasks.add(newSubtask);
        } catch (OverlappingTasksTimeException _) {
            System.out.println("was not added to priority:\n" + newSubtask);
        }

        return newSubtask;
    }

    @Override
    public void updateTask(Task newTask) throws TaskNotFoundException {
        var oldTask = Optional
                .ofNullable(tasks.get(newTask.getId()))
                .orElseThrow(() -> new TaskNotFoundException("Updatable task id doesnt exist in manager"));

        try {
            prioritizedTasks.remove(oldTask);
            validateTimeOnOverlap(newTask);
            prioritizedTasks.add(newTask);
        } catch (OverlappingTasksTimeException _) {
            System.out.println("was not added to priority:\n" + newTask);
        }
        tasks.put(newTask.getId(), newTask);
    }

    @Override
    public void updateSubtask(Subtask newSubtask) throws TaskNotFoundException, OverlappingTasksTimeException {
        var oldSubtask = Optional
                .ofNullable(subtasks.get(newSubtask.getId()))
                .orElseThrow(() -> new TaskNotFoundException("Updatable subtask id doesnt exist in manager"));

        Epic epicOfSubtask = epics.get(newSubtask.getEpicId());
        epicOfSubtask.removeTask(oldSubtask);
        epicOfSubtask.addTask(newSubtask);
        epicOfSubtask.configureEpic();

        try {
            prioritizedTasks.remove(oldSubtask);
            validateTimeOnOverlap(newSubtask);
            prioritizedTasks.add(newSubtask);
        } catch (OverlappingTasksTimeException _) {
            System.out.println("was not added to priority:\n" + newSubtask);
        }

        subtasks.put(newSubtask.getId(), newSubtask);
        epics.put(epicOfSubtask.getId(), epicOfSubtask);
    }

    @Override
    public void deleteAllTasks() {
        for (int taskId : tasks.keySet()) historyManager.remove(taskId);

        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (int epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        for (int subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }

        prioritizedTasks.removeAll(subtasks.values());
        prioritizedTasks.removeAll(epics.values());
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.setEpicSubtasks(new ArrayList<>());
        }
        prioritizedTasks.removeAll(subtasks.values());
        for (int subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
    }

    @Override
    public void deleteAll() {
        deleteAllTasks();
        deleteAllEpics();
        deleteAllSubtasks();
    }

    @Override
    public void deleteTaskById(@NotNull Integer taskId) {
        if (!tasks.containsKey(taskId)) {
            throw new TaskNotFoundException("Task not found by id in manager");
        }

        prioritizedTasks.remove(tasks.get(taskId));
        historyManager.remove(taskId);
        tasks.remove(taskId);
    }

    @Override
    public void deleteEpicById(@NotNull Integer epicId) throws TaskNotFoundException {
        if (!epics.containsKey(epicId)) {
            throw new TaskNotFoundException("Epic not found by id in manager");
        }

        var subtaskListFromEpic = Optional.of(epics.get(epicId).getEpicSubtasks());
        subtaskListFromEpic.ifPresent(subtaskList -> subtaskList.forEach(subtask -> {
            prioritizedTasks.remove(subtask);
            subtasks.remove(subtask.getId(), subtask);
            historyManager.remove(subtask.getId());
        }));
        // Удаляем сам эпик из prioritizedTasks и historyManager
        historyManager.remove(epicId);
        epics.remove(epicId);
    }

    @Override
    public void deleteSubtaskById(@NotNull Integer subtaskId) {
        if (!subtasks.containsKey(subtaskId)) {
            throw new TaskNotFoundException("Epic not found by id in manager");
        }
        var subtask = subtasks.get(subtaskId);
        var epic = epics.get(subtask.getEpicId());
        epic.removeTask(subtask);
        epic.configureEpic();

        prioritizedTasks.remove(subtask);
        historyManager.remove(subtaskId);
        subtasks.remove(subtaskId);
    }

    @Override
    public ArrayList<Task> getTasksAsList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpicsAsList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasksAsList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Task> getAll() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(epics.values());
        allTasks.addAll(subtasks.values());
        return allTasks;
    }

    protected Epic getEpicBySubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        return epics.get(epicId);
    }

    /**
     * @Variation1
     *
     * @param taskToAdd задача, которую мы хотим проверить на пересечение с существующими задачами из
     *                  списка {@code prioritizedTask}. Для корректной работы убедитесь, что у задачи определены поля
     *                  {@code startTime} и {@code durationInMinutes}.
     * @throws OverlappingTasksTimeException возникает в том случае, когда временной промежуток {@code taskToAdd}
     * пересекается имеет пересечение с временным промежутком хотя бы одной задачи из {@code prioritizedTasks}
     * списка или совпадает с ним.
     */
    private void validateTimeOnOverlap(Task taskToAdd) throws OverlappingTasksTimeException {
        if (prioritizedTasks.isEmpty()) return;
        var start2 = taskToAdd.getStartTime();
        var end2 = taskToAdd.getEndTime();
        for (Task existingTask : prioritizedTasks) {
            if (Objects.equals(existingTask.getId(), taskToAdd.getId())) {
                continue;
            }
            var start1 = existingTask.getStartTime();
            var end1 = existingTask.getEndTime();

            if (!(end1.isBefore(start2) || end2.isBefore(start1))) {
                throw new OverlappingTasksTimeException();
            }
        }
    }
}