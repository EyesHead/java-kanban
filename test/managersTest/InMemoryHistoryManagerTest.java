package managersTest;

import managers.memory_classes.InMemoryHistoryManager;
import managers.memory_classes.InMemoryTaskManager;
import managers.Managers;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {
    static InMemoryTaskManager taskManager = Managers.getDefaultTaskManager();

    Task task1 =
            new Task("Задача1", "Описание задачи 1", NEW,
                    LocalDateTime.now(), 50);
    Task task2 =
            new Task("Задача2", "Описание задачи 2", NEW,
                    LocalDateTime.now(), 50);
    Task task3 =
            new Task("Задача3", "Описание задачи 3", NEW,
                    LocalDateTime.now(), 50);

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
    @DisplayName("Проверка на пустую историю задач")
    @Test
    void checkEmptyHistory() {
        //Сейчас в истории 3 задачи
        assertEquals(taskManager.getHistoryManager().getAll().size(), 3, "В истории должно быть 3 задачи");
        //Удалим их
        taskManager.clearAll();
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();
        assertEquals(historyManager.getAll().size(), 0, "Задачи не удалились из истории");
    }
    //Удаление из истории: начало, середина, конец
    @DisplayName("Проверка истории после удаления задачи с первой позиции")
    @Test
    void checkHistoryAfterRemoveTaskOnFirstPlace() {
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();

        historyManager.remove(task1.getId());
        //ожидаем историю вида:
        // null <-> task2(node) <-> task3(node)
        assertEquals(historyManager.getAll(), List.of(task2, task3),
                "Задача не удалилась из начала связанного списка истории просмотров");
    }

    @DisplayName("Проверка истории после удаления задачи со второй позиции")
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

    @DisplayName("Проверка истории после удаления задачи с конца")
    @Test
    void checkHistoryAfterRemoveTaskOnLastPlace() {
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();

        historyManager.remove(task3.getId());
        //ожидаем историю вида:
        // task1(node) <-> null <-> task3(node)
        System.out.println(historyManager.getAll());
        assertEquals(historyManager.getAll(), List.of(task1, task2),
                "Задача не удалилась из середины связанного списка истории просмотров");
    }

    //проверка на дубликаты, после просмотра одних и тех же задач
    @DisplayName("Проверка истории на дублирование задач")
    @Test
    void checkForDuplicateTaskInHistory() {
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();
        historyManager.add(task1);
        historyManager.add(task3);
        historyManager.add(task3);
        historyManager.add(task2);
        // сейчас история должна иметь вид:
        // task1 <-> task3 <-> task2
        System.out.println(historyManager.getAll()); // так и есть)
        assertEquals(historyManager.getAll().size(), 3, "История просмотров содержит дубликаты!");
        assertEquals(historyManager.getAll(), List.of(task1, task3, task2),
                "Порядок в historyManager должен был измениться!");

    }


}
