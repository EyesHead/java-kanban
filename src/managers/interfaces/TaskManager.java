package managers.interfaces;

import managers.memory_classes.InMemoryHistoryManager;
import models.Epic;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    //методы добавления
    void addTask(Task task);
    void addEpic(Epic epic);
    void addSubtask(Subtask subtask);

    //методы удаления
    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    //методы обновления
    void updateTask(Task task);
    void updateSubtask(Subtask subtask);

    //управление historyManager'ом
    Task getTaskById(int taskId);
    Epic getEpicById(int epicId);
    Subtask getSubtaskById(int subtaskId);
    InMemoryHistoryManager getHistoryManager();

    void deleteTaskById(int taskId);
    void deleteEpicById(int epicId);
    void deleteSubtaskById(int subtaskId);
    void clearAll();
}
