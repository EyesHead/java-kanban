package controller;

import java.util.ArrayList;
import java.util.HashMap;
import models.*;

public class TaskManager {
    private int id = 0;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }
    public void createSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask); // 1. Добавляем подзадачу в мапу подзадач

        //2. Добавляем id подзадачи в лист эпика
        addSubtaskIdAtEpic(subtask.getId());

        // (3. эпик done становится эпиком in_progress при создании подзадачи)
        Epic epic = getEpicById(subtask.getEpicId());
        if (epic.getStatus() == Status.DONE) { // (3. эпик done становится эпиком in_progress при создании подзадачи)
            epic.setStatus(Status.IN_PROGRESS);
        }

        // 3. обновляем эпик в мапе эпиков
        epics.put(epic.getId(), epic);
    }

    public void updateTask(Task task){
        tasks.put(task.getId(), task);
    }
    public void updateEpic(Epic epic){
        epics.put(epic.getId(), epic);

        if (epic.getStatus() == Status.DONE) { // Если статус эпика = DONE, то статусы всех подзадач эпика = DONE;
            int epicId = epic.getId();
            for (Subtask subtaskNotDone : subtasks.values()) {
                if(subtaskNotDone.getEpicId() == epicId) { // Находим из мапы подзадач подзадачи эпика со статусом DONE
                    int subtaskDoneID = subtaskNotDone.getId();
                    subtasks.put(subtaskDoneID, subtaskNotDone);
                }
            }
        }
    }
    public void updateSubtask(Subtask subtask){

        subtasks.put(subtask.getId(), subtask);// обновляем статус в мапе

        int epicId = subtask.getEpicId();
        boolean areAllSubtasksDone = true;
        Epic epic = getEpicById(epicId);

        if (subtask.getStatus() == Status.DONE) {
            ArrayList<Subtask> subtasksByEpicId = getSubtaskListByEpicId(epicId);
            for (Subtask subtaskByEpicId : subtasksByEpicId) {
                if (subtaskByEpicId.getStatus() != Status.DONE) {
                    areAllSubtasksDone = false;
                    break;
                }
            }
        } else { // в случае если статус эпика был NEW, то при обновлении статуса подзадачи он станет IN_PROGRESS
            epic.setStatus(Status.IN_PROGRESS);
            epics.put(epicId, epic);
        }

        if (areAllSubtasksDone) { // если все подзадачи имеют статус DONE тогда связанному эпику присваивается статус DONE
            epic.setStatus(Status.DONE);
            epics.put(epicId, epic);
        }

    }


    public void deleteAllTasks() {
        tasks.clear();
    }
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) { // очищаем список id сабтасок у каждого объекта-эпика в epics
            epic.setSubtaskIds(new ArrayList<>());
        }

        subtasks.clear(); // очищаем мапу сабтасок
    }

    public Task getTaskById(int taskId){
        if (tasks.containsKey(taskId)) {
            return tasks.get(taskId);
        }
        return null;
    }
    public Epic getEpicById(int epicId){
        if (epics.containsKey(epicId)) {
            return epics.get(epicId);
        }
        return null;
    }
    public Subtask getSubtaskById(int subtaskId){
        if (subtasks.containsKey(subtaskId)) {
            return subtasks.get(subtaskId);
        }
        return null;
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            allTasks.add(task);
        }
        return allTasks;
    }
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteTaskById(int taskId) {
        tasks.remove(taskId);
    }
    public void deleteEpicById(int epicId) {
        // удаляем все сабтаски из subtasks map, связанные с удаляемым эпиком
        ArrayList<Subtask> subtaskListByEpicId = getSubtaskListByEpicId(epicId);
        if (!subtaskListByEpicId.isEmpty()) {
            for (Subtask subtask : subtaskListByEpicId) {
                subtasks.remove(subtask.getId(), subtask);
            }
        }
        //если подзадач у эпика ещё нет то просто удаляем эпик из epics map
        epics.remove(epicId, getEpicById(epicId));
    }
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = getSubtaskById(subtaskId);
        Epic epic = getEpicById(subtask.getEpicId());
        // Удаляем id подзадачи в листе связанного эпика и обновляем эпик в мапе epics
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        subtaskIds.remove((Integer) subtask.getId());
        epic.setSubtaskIds(subtaskIds);
        epics.put(epic.getId(), epic);

        // Потом удаляем саму подзадачу из мапы подзадач
        subtasks.remove((Integer) subtask.getId());
    }



    public ArrayList<Subtask> getSubtaskListByEpicId(int relatedEpicId) {
        ArrayList<Subtask> subtaskListByEpicID = new ArrayList<>();

        Epic relatedEpic = getEpicById(relatedEpicId);
        for (Integer subtaskIdFromMap : subtasks.keySet()) {
            for (Integer subtaskIdFromRelatedEpic : relatedEpic.getSubtaskIds()) {
                if (subtaskIdFromMap.equals(subtaskIdFromRelatedEpic)) {
                    Subtask subtask = subtasks.get(subtaskIdFromMap);
                    subtaskListByEpicID.add(subtask);
                }
            }
        }
        return subtaskListByEpicID;
    }



    private int generateId() {
        return id++;
    }

    private void addSubtaskIdAtEpic(int subtaskId) {
        Subtask subtask = getSubtaskById(subtaskId);
        Epic epic = getEpicById(subtask.getEpicId());
        ArrayList<Integer> subtaskIds = new ArrayList<>();
        subtaskIds.add(subtask.getId());
        epic.setSubtaskIds(subtaskIds);
    }

}