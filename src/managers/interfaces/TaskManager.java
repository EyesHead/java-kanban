package managers.interfaces;

import managers.memory_classes.InMemoryHistoryManager;
import models.Epic;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    //методы добавления
    void addTask(Task task);
    void addEpic(Epic epic);
    void addSubtask(Subtask subtask);

    //методы удаления
    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    void deleteTaskById(int taskId);
    void deleteEpicById(int epicId);
    void deleteSubtaskById(int subtaskId);
    void clearAll();

    //методы обновления
    void updateTask(Task task);
    void updateSubtask(Subtask subtask);

    Set<Task> getPrioritizedTasks();

    //геттеры, добавляющие задачи в historyManager
    Task getTaskById(int taskId);
    Epic getEpicById(int epicId);
    Subtask getSubtaskById(int subtaskId);
    InMemoryHistoryManager getHistoryManager();

    //гетеры, но без управления historyManager
    ArrayList<Task> getTasksAsList();
    ArrayList<Epic> getEpicsAsList();
    ArrayList<Subtask> getSubtasksAsList();
    ArrayList<Task> getAll();



}
