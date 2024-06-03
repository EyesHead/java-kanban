package managersTest;

import managers.memory_classes.InMemoryHistoryManager;
import managers.memory_classes.InMemoryTaskManager;
import managers.Managers;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTasksOnlyTest {
    static InMemoryTaskManager taskManager = Managers.getDefault();

    Task task1 =
            new Task("Задача1", "Описание задачи 1", NEW);
    Task task2 =
            new Task("Задача2", "Описание задачи 2", NEW);
    Task task3 =
            new Task("Задача3", "Описание задачи 3", NEW);

    @BeforeEach
    public void beforeEach() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);

        //Сейчас история просмотров имеет вид:
        // null <-> task1(node) <-> task2(node) <-> task3(node)
    }

    @Test
    void checkHistoryAfterRemoveTaskOnFirstPlace() {
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();

        historyManager.remove(task1.getId());
        //ожидаем историю вида:
        // null <-> task2(node) <-> task3(node)
        assertEquals(historyManager.getAll(), List.of(task2, task3),
                "Задача не удалилась из начала связанного списка истории просмотров");
    }

    @Test
    void checkHistoryAfterRemoveTaskOnMiddlePlace() {
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();

        historyManager.remove(task2.getId());
        //ожидаем историю вида:
        // task1(node) <-> null <-> task3(node)
        System.out.println(historyManager.getAll());
        assertEquals(historyManager.getAll(), List.of(task1, task3),
                "Задача не удалилась из середины связанного списка истории просмотров");
    }

    @Test
    void checkHistoryAfterRemoveTaskOnLastPlace() {
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();

        historyManager.remove(task2.getId());
        //ожидаем историю вида:
        // task1(node) <-> task2(node) <-> null
        System.out.println(historyManager.getAll());
        assertEquals(historyManager.getAll(), List.of(task1, task2),
                "Задача не удалилась из конца связанного списка истории просмотров");
    }
}
