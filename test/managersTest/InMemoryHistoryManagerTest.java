package managersTest;

import factories.TaskFactory;
import service.history.InMemoryHistoryManager;
import service.memory.InMemoryTaskManager;
import service.ManagersCreator;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {
    static InMemoryTaskManager taskManager = ManagersCreator.getDefaultTaskManager();

    Task task1 = TaskFactory.generateTask("Task 1", "Task 1 description");

    Task task2 = TaskFactory.generateTask("Task 2", "Task 2 description");
    Task task3 = TaskFactory.generateTask("Task 3", "Task 3 description");

    @BeforeEach
    public void beforeEach() {
        task1 = taskManager.createTask(task1);
        task2 = taskManager.createTask(task2);
        task3 = taskManager.createTask(task3);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        //Сейчас история просмотров имеет вид:
        // null <-> task1(node) <-> task2(node) <-> task3(node) <-> null
    }
    @DisplayName("Проверка на пустую историю задач")
    @Test
    void checkEmptyHistory() {
        //Сейчас в истории 3 задачи
        assertEquals(taskManager.getHistoryManager().getAll().size(), 3, "В истории должно быть 3 задачи");
        //Удалим их
        taskManager.deleteAll();
        InMemoryHistoryManager historyManager = taskManager.getHistoryManager();
        assertEquals(historyManager.size(), 0, "Задачи не удалились из истории");
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
