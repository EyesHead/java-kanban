package managers;

import managers.util.Managers;
import models.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static models.Status.*;

public class InMemoryHistoryManagerTasksOnlyTest {
    static Managers managers = new Managers();
    static InMemoryTaskManager taskManager =  managers.getDefaultTasks();
    Task task1 =
            new Task("Задача1", "Описание задачи 1", NEW);
    Task task2 =
            new Task("Задача2", "Описание задачи 2", NEW);
    Task task3 =
            new Task("Задача3", "Описание задачи 3", NEW);
    
}
