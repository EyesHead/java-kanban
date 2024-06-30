package taskManager.interfaces;

import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.ValidationException;
import taskManager.memory.InMemoryHistoryManager;
import tasksModels.Epic;
import tasksModels.Subtask;
import tasksModels.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    //методы добавления
    void createTask(Task task) throws ValidationException;
    void createEpic(Epic epic);
    void createSubtask(Subtask subtask) throws ValidationException;

    //методы удаления
    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    void deleteTaskById(int taskId) throws NotFoundException;
    void deleteEpicById(int epicId) throws NotFoundException;
    void deleteSubtaskById(int subtaskId) throws NotFoundException;
    void deleteAll();

    //методы обновления
    void updateTask(Task task) throws ValidationException, NotFoundException;
    void updateSubtask(Subtask subtask) throws ValidationException, NotFoundException;

    Set<Task> getPrioritizedTasks();

    //геттеры, добавляющие задачи в historyManager
    Task getTaskById(int taskId) throws NotFoundException;
    Epic getEpicById(int epicId) throws NotFoundException;
    List<Subtask> getEpicSubtasks(int epicId);
    Subtask getSubtaskById(int subtaskId);
    InMemoryHistoryManager getHistoryManager();

    //гетеры, но без управления historyManager
    ArrayList<Task> getTasksAsList();
    ArrayList<Epic> getEpicsAsList();
    ArrayList<Subtask> getSubtasksAsList();
    ArrayList<Task> getAll();

}
