package taskManager.memory;

import managersCreator.Managers;
import taskManager.exceptions.InvalidSubtaskDataException;
import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.ValidationException;
import taskManager.interfaces.TaskManager;
import tasksModels.*;

import java.util.*;
import static tasksModels.Status.DONE;

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
    public Task getTaskById(int taskId) throws NotFoundException{
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new NotFoundException("Задача с id = " + taskId + " - не найдена");
        }
        historyManager.add(tasks.get(taskId));
        return tasks.get(taskId);
    }
    @Override
    public Epic getEpicById(int epicId) throws NotFoundException{
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с id = " + epicId + " - не найден");
        }
        historyManager.add(epics.get(epicId));
        return epics.get(epicId);
    }
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return epics.get(epicId).getSubtasks();
    }

    @Override
    public Subtask getSubtaskById(int subtaskId) throws NotFoundException {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id = " + subtaskId + " - не найдена");
        }
        historyManager.add(subtasks.get(subtaskId));
        return subtasks.get(subtaskId);
    }

    @Override
    public void createTask(Task task) {
        task.setId(generateId());
        validateTaskTime(task); // VALIDATION EXCEPTION HERE
        prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
    }
    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }
    @Override
    public void createSubtask(Subtask subtask) throws InvalidSubtaskDataException, ValidationException {
        int epicId = subtask.getEpicId();

        if (!epics.containsKey(epicId)) {
            throw new InvalidSubtaskDataException("Некорректно указан epicId у подзадачи");
        }
        subtask.setId(generateId());

        // обновляем список подзадач у эпика
        Epic epic = epics.get(subtask.getEpicId());
        epic.addTask(subtask);

        // обновляем статус эпика
        if (epic.getStatus() == DONE) epic.setStatus(Status.IN_PROGRESS);
        // обновление задачи в приоритизированном списке
        validateTaskTime(subtask); //VALIDATION EXCEPTION HERE
        prioritizedTasks.add(subtask);

        subtasks.put(subtask.getId(), subtask);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task updatedTask) throws ValidationException, NotFoundException {
        Task original = tasks.get(updatedTask.getId());
        if (original == null)
            throw new NotFoundException("Task with id " + updatedTask.getId() + " not found");

        // обновление задачи в приоритизированном списке
        prioritizedTasks.remove(original);
        prioritizedTasks.add(updatedTask);

        tasks.put(updatedTask.getId(), updatedTask);
    }
    @Override
    public void updateSubtask(Subtask updatedSubtask) throws ValidationException, NotFoundException {
        Subtask originalSub = subtasks.get(updatedSubtask.getId());
        if (originalSub == null)
            throw new NotFoundException("Task with id " + updatedSubtask.getId() + " not found");

        Epic epic = epics.get(updatedSubtask.getEpicId());
        // полный перерасчёт времени начала, конца, длительности и статуса ЭПИКа
        epic.removeTask(originalSub);
        epic.addTask(updatedSubtask);
        // обновление задачи в приоритизированном списке

        validateTaskTime(updatedSubtask);
        prioritizedTasks.remove(originalSub);
        prioritizedTasks.add(updatedSubtask);


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
    public void deleteAll() {
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
        ArrayList<Subtask> subtaskList = (ArrayList<Subtask>) epics.get(epicId).getSubtasks();
        if (subtaskList != null && !subtaskList.isEmpty()) {
            // Удаляем все подзадачи эпика из prioritizedTasks, subtasks и historyManager
            subtaskList.forEach(subtask -> {
                prioritizedTasks.remove(subtask);
                subtasks.remove(subtask.getId(), subtask);
                historyManager.remove(subtask.getId());
            });
        }
        // Удаляем сам эпик из prioritizedTasks и historyManager
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
            if (Objects.equals(existingTask.getId(), taskToAdd.getId())) {continue;}
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