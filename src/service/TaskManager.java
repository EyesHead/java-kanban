package service;

import org.jetbrains.annotations.NotNull;
import service.exceptions.OverlappingTasksTimeException;
import service.exceptions.TaskNotFoundException;
import service.history.InMemoryHistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    //методы добавления
    Task createTask(Task task) throws OverlappingTasksTimeException;
    Epic createEpic(Epic epic);
    Subtask createSubtask(Subtask subtask) throws OverlappingTasksTimeException;

    //методы удаления
    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    void deleteTaskById(@NotNull Integer taskId);
    void deleteEpicById(@NotNull Integer epicId);
    void deleteSubtaskById(@NotNull Integer subtaskId);
    void deleteAll();

    //методы обновления
    void updateTask(Task task) throws OverlappingTasksTimeException;
    void updateSubtask(Subtask subtask) throws OverlappingTasksTimeException;

    Set<Task> getPrioritizedTasks();

    //геттеры, добавляющие задачи в historyManager
    Task getTaskById(int taskId) throws TaskNotFoundException;
    Epic getEpicById(int epicId) throws TaskNotFoundException;
    List<Subtask> getEpicSubtasks(int epicId) throws TaskNotFoundException;
    Subtask getSubtaskById(int subtaskId) throws TaskNotFoundException;
    InMemoryHistoryManager getHistoryManager();

    //гетеры, но без управления historyManager
    ArrayList<Task> getTasksAsList();
    ArrayList<Epic> getEpicsAsList();
    ArrayList<Subtask> getSubtasksAsList();
    ArrayList<Task> getAll();

}
