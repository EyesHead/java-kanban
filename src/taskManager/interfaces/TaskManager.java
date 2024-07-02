package taskManager.interfaces;

import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.OverlapValidationException;
import taskManager.memory.InMemoryHistoryManager;
import tasksModels.Epic;
import tasksModels.Subtask;
import tasksModels.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    //методы добавления
    void createTask(Task task) throws OverlapValidationException;
    void createEpic(Epic epic);
    void createSubtask(Subtask subtask) throws OverlapValidationException;

    //методы удаления
    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    void deleteTaskById(int taskId);
    void deleteEpicById(int epicId);
    void deleteSubtaskById(int subtaskId);
    void deleteAll();

    //методы обновления
    void updateTask(Task task);
    void updateSubtask(Subtask subtask);

    Set<Task> getPrioritizedTasks();

    //геттеры, добавляющие задачи в historyManager
    Task getTaskById(int taskId) throws NotFoundException;
    Epic getEpicById(int epicId) throws NotFoundException;
    List<Subtask> getEpicSubtasks(int epicId) throws NotFoundException;
    Subtask getSubtaskById(int subtaskId) throws NotFoundException;
    InMemoryHistoryManager getHistoryManager();

    //гетеры, но без управления historyManager
    ArrayList<Task> getTasksAsList();
    ArrayList<Epic> getEpicsAsList();
    ArrayList<Subtask> getSubtasksAsList();
    ArrayList<Task> getAll();

}
