package managers;

import managers.interfaces.TaskManager;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static models.Status.DONE;
import static models.Status.IN_PROGRESS;

public class InMemoryTaskManager implements TaskManager {
    private int id = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final InMemoryHistoryManager historyManager = new InMemoryHistoryManager();


    @Override
    public Task getTaskById(int taskId){
        historyManager.add(tasks.get(taskId));
        return tasks.get(taskId);
    }
    @Override
    public Epic getEpicById(int epicId){
        historyManager.add(epics.get(epicId));
        return epics.get(epicId);
    }
    @Override
    public Subtask getSubtaskById(int subtaskId){
        historyManager.add(subtasks.get(subtaskId));
        return subtasks.get(subtaskId);
    }

    @Override
    public void addTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }
    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }
    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        // 1. Добавляем подзадачу в таблицу подзадач
        subtasks.put(subtask.getId(), subtask);

        //2. Добавляем id подзадачи в лист связанного эпика
        Epic epic = addSubtaskIdAtEpic(subtask.getId());

        // 3. эпик DONE становится эпиком IN_PROGRESS при создании подзадачи
        if (epic.getStatus() == DONE) {
            epic.setStatus(Status.IN_PROGRESS);
        }

        // 3. обновляем эпик в epics
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task){
        tasks.put(task.getId(), task);
    }
    @Override
    public void updateEpicSubtask(Subtask subtask){
        // 1. обновляем подзадачу в таблице subtasks
        subtasks.put(subtask.getId(), subtask);


        int epicId = subtask.getEpicId();

        Epic epic = epics.get(epicId);


        if (subtask.getStatus() == DONE) {
            boolean areAllSubtasksDone = true;// Создаем флаг для проверки статусов всех подзадач эпика на DONE
            ArrayList<Subtask> subtasks = getSubtaskListByEpicId(epic.getId());
            for (Subtask subtaskByEpicId : subtasks) {
                if (subtaskByEpicId.getStatus() != DONE) {
                    // Если хоть одна подзадача эпика не DONE, меняем флаг на false
                    areAllSubtasksDone = false;
                    break;
                }
            }
            // если флаг не изменился, значит статус всех подзадач DONE
            if (areAllSubtasksDone) {
                epic.setStatus(DONE);
                epics.put(epic.getId(), epic);
            }
        } else { // При обновлении статуса подзадачи эпик станет IN_PROGRESS
            epic.setStatus(IN_PROGRESS);
            epics.put(epic.getId(), epic);
        }




    }


    @Override
    public void deleteAllTasks() {
        for (int taskId : tasks.keySet()) historyManager.remove(taskId);
        tasks.clear();
    }
    @Override
    public void deleteAllEpics() {
        for (int epicId : epics.keySet()) historyManager.remove(epicId);
        for (int subtaskId : subtasks.keySet()) historyManager.remove(subtaskId);


        epics.clear();
        subtasks.clear();
    }
    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) { // очищаем список id подзадач у каждого объекта-эпика в epics
            epic.setSubtaskIds(new ArrayList<>());
        }

        for (int subtaskId : subtasks.keySet()) historyManager.remove(subtaskId);
        subtasks.clear(); // очищаем таблицу подзадач
    }



    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteTaskById(int taskId) {
        historyManager.remove(taskId);
        tasks.remove(taskId);
    }
    @Override
    public void deleteEpicById(int epicId) {
        // удаляем все подзадачи из subtasks <id, subtask>, связанные с удаляемым эпиком
        ArrayList<Subtask> subtaskListByEpicId = getSubtaskListByEpicId(epicId);
        if (!subtaskListByEpicId.isEmpty()) {
            for (Subtask subtask : subtaskListByEpicId) {
                subtasks.remove(subtask.getId(), subtask);
                historyManager.remove(subtask.getId());
            }
        }
        //если подзадач у эпика ещё нет, то просто удаляем эпик из epics map
        epics.remove(epicId);
        historyManager.remove(epicId);
    }
    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());
        // Удаляем id подзадачи в листе связанного эпика и обновляем эпик в таблице epics
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        subtaskIds.remove(subtask.getId());
        epic.setSubtaskIds(subtaskIds);

        // обновляем эпик в map`е эпиков
        epics.put(epic.getId(), epic);

        // Потом удаляем саму подзадачу из таблицы подзадач
        subtasks.remove(subtaskId);
        historyManager.remove(subtaskId);
    }


     public void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : getTasks()) {
            System.out.println(task);
        }
    }
    public void printAllEpicsSubtasks(){
        System.out.println("Эпики:");
        for (Epic epic : getEpics()) {
            System.out.println(epic);
            for (Subtask subtask : getSubtaskListByEpicId(epic.getId())) {
                System.out.println("--> " + subtask);
            }
        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : getSubtasks()) {
            System.out.println(subtask);
        }
    }
    public void printAllHistory() {
        System.out.println("История:");
        for (Task task : historyManager.getAll()) {
            System.out.println(task);
        }
    }

    public ArrayList<Subtask> getSubtaskListByEpicId(int epicId) {
        ArrayList<Subtask> subtaskListByEpicId = new ArrayList<>();

        Epic epic = epics.get(epicId);
        for (Integer subtaskId : subtasks.keySet()) {
            for (Integer subtaskIdFromEpic : epic.getSubtaskIds()) {
                if (subtaskId.equals(subtaskIdFromEpic)) {
                    subtaskListByEpicId.add(subtasks.get(subtaskIdFromEpic));
                }
            }
        }
        return subtaskListByEpicId;
    }

    public InMemoryHistoryManager getHistoryManager() {
        return historyManager;
    }


    // локальные методы
    private int generateId() {
        return id++;
    }

    private Epic addSubtaskIdAtEpic(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Epic epic = epics.get(subtask.getEpicId());
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        subtaskIds.add(subtaskId);
        epic.setSubtaskIds(subtaskIds);

        return epic;
    }

}