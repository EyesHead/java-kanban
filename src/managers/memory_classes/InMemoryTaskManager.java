package managers.memory_classes;

import managers.Managers;
import managers.custom_exceptions.NotFoundException;
import managers.custom_exceptions.ValidationException;
import managers.interfaces.TaskManager;
import models.*;

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

        if (task.getId() != null) {
            this.id = task.getId();
        } else {
            task.setId(generateId());
        }

        try {
            validateTaskTime(task);
            prioritizedTasks.add(task);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        tasks.put(task.getId(), task);
    }
    @Override
    public void addEpic(Epic epic) {
        if (epic == null) throw new RuntimeException("Задача не может быть пустой");

        if (epic.getId() != null) {
            this.id = epic.getId();
        } else {
            epic.setId(generateId());
        }

        epics.put(epic.getId(), epic);
    }
    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) throw new RuntimeException("При добавлении подзадача не может быть пустой");

        if (subtask.getId() != null) {
            this.id = subtask.getId();
        } else {
            subtask.setId(generateId());
        }

        // обновляем список подзадач у эпика
        Epic epic = epics.get(subtask.getEpicId());
        epic.addTask(subtask);

        // обновляем статус эпика
        if (epic.getStatus() == DONE) epic.setStatus(Status.IN_PROGRESS);

        // обновление задачи в приоритизированном списке
        try {
            validateTaskTime(subtask);
            prioritizedTasks.add(subtask);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        subtasks.put(subtask.getId(), subtask);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task updatedTask){
        Task original =
                Optional.ofNullable(tasks.get(updatedTask.getId()))
                        .orElseThrow(() -> new NotFoundException("Task with id " + updatedTask.getId() + " not found"));

        // обновление задачи в приоритизированном списке
        try {
            validateTaskTime(updatedTask);
            prioritizedTasks.remove(original);
            prioritizedTasks.add(updatedTask);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        tasks.put(updatedTask.getId(), updatedTask);
    }
    @Override
    public void updateSubtask(Subtask updatedSubtask){
        Subtask originalSub =
                Optional.ofNullable(subtasks.get(updatedSubtask.getId()))
                        .orElseThrow(() -> new NotFoundException("Subtask with id " + updatedSubtask.getId() + " not found"));

        Epic epic = epics.get(updatedSubtask.getEpicId());
        // полный перерасчёт времени начала, конца, длительности и статуса ЭПИКа
        epic.removeTask(originalSub);
        epic.addTask(updatedSubtask);
        // обновление задачи в приоритизированном списке
        try {
            validateTaskTime(updatedSubtask);
            prioritizedTasks.remove(originalSub);
            prioritizedTasks.add(updatedSubtask);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        epics.put(epic.getId(), epic);
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
        prioritizedTasks.removeAll(epics.values());
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
        subtasks.clear();
    }
    @Override
    public void clearAll() {
        deleteAllTasks();
        deleteAllEpics();
        deleteAllSubtasks();
    }

    @Override
    public void deleteTaskById(int taskId) {
        prioritizedTasks.remove(tasks.get(taskId));
        historyManager.remove(taskId);
        tasks.remove(taskId);
    }
    @Override
    public void deleteEpicById(int epicId) {
        // удаляем все подзадачи из subtasks <id, subtask>, связанные с удаляемым эпиком
        ArrayList<Subtask> subtaskList = (ArrayList<Subtask>) epics.get(epicId).getSubtasks();
        if (!subtaskList.isEmpty()) {
            for (Subtask subtask : subtaskList) {
                prioritizedTasks.remove(subtask);
                subtasks.remove(subtask.getId(), subtask);
                historyManager.remove(subtask.getId());
            }
        }
        prioritizedTasks.remove(epics.get(epicId));
        historyManager.remove(epicId);
        epics.remove(epicId);
    }
    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());

        // обновляем эпик. Статус обновляется при вызове метода removeTask
        epic.removeTask(subtask);
        epics.put(epic.getId(), epic);

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


    public void printAllHistory() {
        System.out.println("История:");
        historyManager.getAll().forEach(System.out::println);
    }

    // локальные методы
    protected Epic getEpicBySubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        return epics.get(epicId);
    }

    private void validateTaskTime(Task taskToAdd) throws ValidationException {
        /*
        1) Рассмотрим 2 события, при которых задачи пересекаются во времени:
        tStart----eStart----tEnd----eEnd
        eStart----tStart----eEnd-----tEnd
        где tStart и tEnd - это поля localDateTime у передаваемой в параметр задачи,
        а eStart и eEnd - соответствующие поля у задачи из списка prioritizedTasks
        2) Если у задач одинаковый id, значит эта одна и та же задача
        и ее в любом случае нужно обновить в prioritizedTasks
         */
        if (prioritizedTasks.isEmpty()) return;

        for (Task existingTask : prioritizedTasks) {
            if (existingTask.getId() == taskToAdd.getId()) {continue;}
            if (taskToAdd.getEndTime().isBefore(existingTask.getEndTime()) &&
                    taskToAdd.getEndTime().isAfter(existingTask.getStartTime())) {
                throw new ValidationException("Временной интервал задачи пересекается с другой задачей.");

            } else if (taskToAdd.getStartTime().isBefore(existingTask.getEndTime()) &&
                    taskToAdd.getStartTime().isAfter(existingTask.getStartTime())) {
                throw new ValidationException("Временной интервал задачи пересекается с другой задачей.");
            }
        }
        // нет ValidationException? значит валидация прошла успешна
    }
}