package managers.memory_classes;

import managers.Managers;
import managers.custom_exceptions.NotFoundException;
import managers.custom_exceptions.ValidationException;
import managers.interfaces.TaskManager;
import models.*;

import java.time.LocalDateTime;
import java.util.*;
import static models.Status.DONE;
import static models.Status.IN_PROGRESS;

public class InMemoryTaskManager implements TaskManager {
    protected int id = 0;
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected InMemoryHistoryManager historyManager;
    protected TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager() {
        historyManager = Managers.getDefaultHistory();
    }
    public InMemoryTaskManager(InMemoryHistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return id++;
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
    public Task getTaskById(int taskId){
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new NotFoundException("Задача с id = " + taskId + " - не найдена");
        }
        historyManager.add(tasks.get(taskId));
        return tasks.get(taskId);
    }
    @Override
    public Epic getEpicById(int epicId){
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с id = " + epicId + " - не найден");
        }
        historyManager.add(epics.get(epicId));
        return epics.get(epicId);
    }
    @Override
    public Subtask getSubtaskById(int subtaskId){
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id = " + subtaskId + " - не найдена");
        }
        historyManager.add(subtasks.get(subtaskId));
        return subtasks.get(subtaskId);
    }

    @Override
    public void addTask(Task task) {
        if (task == null) throw new RuntimeException("Задача не может быть пустой");
        task.setId(generateId());
        tasks.put(task.getId(), task);

        if (prioritizedTasks.isEmpty() || validateTaskTime(task)) prioritizedTasks.add(task);
    }
    @Override
    public void addEpic(Epic epic) {
        if (epic == null) throw new RuntimeException("Задача не может быть пустой");
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }
    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) throw new RuntimeException("При добавлении подзадача не может быть пустой");
        subtask.setId(generateId());

        subtasks.put(subtask.getId(), subtask);
        // обновляем список подзадач у эпика
        Epic epic = epics.get(subtask.getEpicId());
        epic.addTask(subtask);
        // обновляем статус эпика
        if (epic.getStatus() == DONE) epic.setStatus(Status.IN_PROGRESS);

        // обновляем эпик в таблице
        epics.put(epic.getId(), epic);

        // обновление задачи в приоритизированном списке
        if (validateTaskTime(subtask) || prioritizedTasks.isEmpty()) prioritizedTasks.add(subtask);
    }

    @Override
    public void updateTask(Task updatedTask){
        if (updatedTask == null) throw new NotFoundException("Обновляемая задача не найдена");
        Task original =
                Optional.ofNullable(tasks.get(updatedTask.getId()))
                        .orElseThrow(() -> new NotFoundException("Task with id " + updatedTask.getId() + " not found"));
        // обновление задачи в приоритизированном списке
        if (validateTaskTime(updatedTask)) {
            prioritizedTasks.remove(original);
            prioritizedTasks.add(updatedTask);
        }

        tasks.put(updatedTask.getId(), updatedTask);
    }
    @Override
    public void updateSubtask(Subtask updatedSubtask){
        if (updatedSubtask == null)  throw new NotFoundException("Обновляемая подзадача не найдена");
        Subtask originalSub =
                Optional.ofNullable(subtasks.get(updatedSubtask.getId()))
                        .orElseThrow(() -> new NotFoundException("Subtask with id " + updatedSubtask.getId() + " not found"));

        // Обновляем поля эпика
        Epic epic = epics.get(updatedSubtask.getEpicId());
        updateEpicSubtasks(updatedSubtask, epic);
        updateEpicStatus(epic);
        // валидация на пересечение времени задачи
        if (validateTaskTime(updatedSubtask)) {
            prioritizedTasks.remove(updatedSubtask);
            prioritizedTasks.add(originalSub);
        }
        // добавить полный перерасчёт времени для эпика
        subtasks.put(updatedSubtask.getId(), updatedSubtask);
    }

    @Override
    public void deleteAllTasks() {
        for (int taskId : tasks.keySet()) historyManager.remove(taskId);
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }
    @Override
    public void deleteAllEpics() {
        for (int epicId : epics.keySet()) historyManager.remove(epicId);
        for (int subtaskId : subtasks.keySet()) historyManager.remove(subtaskId);
        prioritizedTasks.removeAll(subtasks.values());
        epics.clear();
        subtasks.clear();
    }
    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) { // очищаем список id подзадач у каждого объекта-эпика в epics
            epic.setSubtasks(new ArrayList<>());
        }
        prioritizedTasks.removeAll(subtasks.values());
        for (int subtaskId : subtasks.keySet()) historyManager.remove(subtaskId);
        subtasks.clear(); // очищаем таблицу подзадач
    }
    @Override
    public void clearAll() {
        deleteAllTasks();
        deleteAllEpics();
        deleteAllSubtasks();
        prioritizedTasks.clear();
    }

    @Override
    public void deleteTaskById(int taskId) {
        historyManager.remove(taskId);
        prioritizedTasks.remove(tasks.get(taskId));
        tasks.remove(taskId);
    }
    @Override
    public void deleteEpicById(int epicId) {
        // удаляем все подзадачи из subtasks <id, subtask>, связанные с удаляемым эпиком
        ArrayList<Subtask> subtaskList = (ArrayList<Subtask>) epics.get(epicId).getSubtasks();
        if (!subtaskList.isEmpty()) {
            for (Subtask subtask : subtaskList) {
                historyManager.remove(subtask.getId());
                prioritizedTasks.remove(subtask);
                subtasks.remove(subtask.getId(), subtask);

            }
        }

        epics.remove(epicId);
        historyManager.remove(epicId);
    }
    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());

        // Удаляем id подзадачу в epic и обновляем эпик в таблице epics
        epic.removeTask(subtask);
        epics.put(epic.getId(), epic);

        // Удаляем подзадачу из таблицы подзадач, истории и prioritized
        prioritizedTasks.remove(subtask);
        subtasks.remove(subtaskId);
        historyManager.remove(subtaskId);
    }

    public ArrayList<Task> getTasksAsList() {
        return new ArrayList<>(tasks.values());
    }
    public ArrayList<Epic> getEpicsAsList() {
        return new ArrayList<>(epics.values());
    }
    public ArrayList<Subtask> getSubtasksAsList() {
        return new ArrayList<>(subtasks.values());
    }
    public ArrayList<Task> getAll() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(epics.values());
        allTasks.addAll(subtasks.values());
        return allTasks;
    }

    public void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : getTasksAsList()) {
            System.out.println(task);
        }
    }
    public void printAllEpicsSubtasks(){
        System.out.println("Эпики:");
        for (Epic epic : getEpicsAsList()) {
            System.out.println(epic);
            for (Subtask subtask : epic.getSubtasks()) {
                System.out.println("--> " + subtask);
            }
        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : getSubtasksAsList()) {
            System.out.println(subtask);
        }
    }
    public void printAllHistory() {
        System.out.println("История:");
        historyManager.getAll().forEach(System.out::println);
    }

    // локальные методы
    protected Epic getEpicBySubtask(Subtask subtask) {
        for (Epic epic : epics.values()) {
            if (epic.getSubtasks().contains(subtask)) {
                return epic;
            }
        }

        return null;
    }

    private void updateEpicStatus(Epic epic) {
        if (epic == null) return;
        boolean areAllSubtasksDone = epic.getSubtasks().stream().allMatch(subtask -> subtask.getStatus() == DONE);
        if (areAllSubtasksDone) {
            epic.setStatus(DONE);
        } else {
            epic.setStatus(IN_PROGRESS);
        }
        epics.put(epic.getId(), epic);
    }
    private void updateEpicSubtasks(Subtask subtask, Epic epic) {
        Subtask oldSubtask = epic.getSubtasks().get(subtask.getId());
        epic.removeTask(oldSubtask);
        epic.addTask(subtask);
    }

    private boolean validateTaskTime(Task taskToAdd) throws ValidationException {
        /*  A1 - taskToAdd.getStartTime()
            A2 - taskToAdd.getEndTime()
            T1 - task from priority set .getStartTime()
            T2 - task from priority set .getEndTime()
         */
        // situation: A1---A2   taskStartTime---- (filtered)
        Optional<LocalDateTime> checkDateTime1 = prioritizedTasks.stream()
                .filter(task -> taskToAdd.getId() != task.getId())
                .map(Task::getStartTime)
                .filter(taskStartTime ->
                    taskToAdd.getEndTime().isBefore(taskStartTime))
                .findAny();

        // situation: ---taskEndTime   A1---A2 (filtered)
        Optional<LocalDateTime> checkDateTime2 = prioritizedTasks.stream()
                .filter(task -> taskToAdd.getId() != task.getId())
                .map(Task::getEndTime)
                .filter(taskEndTime ->
                        taskToAdd.getEndTime().isBefore(taskEndTime))
                .findAny();
        return checkDateTime1.isPresent() && checkDateTime2.isPresent();
    }
}