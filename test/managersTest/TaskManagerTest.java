package managersTest;

import factories.TaskFactory;
import model.Epic;
import model.Subtask;
import model.Task;
import service.HistoryManager;
import service.TaskManager;
import org.junit.jupiter.api.*;
import service.exceptions.TaskNotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static model.Status.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    public T manager;

    protected abstract T createManager() throws IOException;

    @BeforeEach
    void beforeEach() throws IOException {
        manager = createManager();
    }

    @DisplayName("Проверка статуса у эпика. Все подзадачи со статусом NEW")
    @Test
    abstract void testEpicNew();

    @DisplayName("Проверка статуса у эпика. Подзадачи со статусами NEW и DONE")
    @Test
    abstract void testEpicInProgress();

    @DisplayName("Проверка статуса у эпика. Подзадачи со статусом IN_PROGRESS")
    @Test
    abstract void testEpicDone();

    //Для подзадач нужно дополнительно проверить наличие эпика,
    //Для эпика проверить расчёт статуса
    @DisplayName("Создать задачу + задать ей уникальный идентификатор")
    @Test
    void shouldCreateTaskWithUniqueId() {
        Task taskUnique = TaskFactory.generateTask("Task unique", "Some description");

        taskUnique = manager.createTask(taskUnique);
        Task recievedTask = manager.getTaskById(taskUnique.getId());
        assertSame(taskUnique, recievedTask);

        assertTrue(manager.getTasksAsList().contains(taskUnique), "Задача не была добавлена в менеджер");
    }

    @DisplayName("Создать эпик + задать ему уникальный идентификатор")
    @Test
    void shouldCreateEpicWithUniqueId() {

        Epic epicUnique = TaskFactory.generateEpic("Epic unique", "Epic description");

        epicUnique = manager.createEpic(epicUnique);
        Epic recievedEpic = manager.getEpicById(epicUnique.getId());
        assertSame(epicUnique, recievedEpic);

        assertTrue(manager.getEpicsAsList().contains(epicUnique), "Задача не была добавлена в менеджер");
    }

    @DisplayName("Создать подзадачу + задать ей уникальный идентификатор")
    @Test
    void shouldCreateSubtaskWithUniqueId() {
        var epic = TaskFactory.generateEpic("Epic for sub","interesting description");
        epic = manager.createEpic(epic);
        var subtask = TaskFactory.generateSubtask("Subtask1","Some description",epic.getId());
        manager.createSubtask(subtask);

        var uniqueSubtask = TaskFactory.generateSubtask("Очередная задача", "???", epic.getId());
        uniqueSubtask.setStartTime(LocalDateTime.now().plusDays(30));
        uniqueSubtask = manager.createSubtask(uniqueSubtask);
        manager.createSubtask(uniqueSubtask);
        manager.getSubtaskById(uniqueSubtask.getId());

        assertTrue(manager.getAll().contains(uniqueSubtask), "Подзадача не была добавлена в менеджер");
    }

    @DisplayName("Обновить статус и время у существующей задачи и проверить, что она изменилась в менеджере")
    @Test
    void updateTask() {
        Task task = TaskFactory.generateTask("Task","Task description", NEW);
        task = manager.createTask(task);
        Task taskInProgress = task;
        taskInProgress.setStatus(IN_PROGRESS);

        manager.updateTask(taskInProgress);
        assertEquals(1, manager.getTasksAsList().size(),
                "Размер менеджера после обновления изменился!");
        assertNotSame(manager.getTasksAsList().getFirst().getStatus(), NEW, "Статус не поменялся");
    }

    @DisplayName("Обновить статус и время у существующей задачи и проверить, что она изменилась в менеджере")
    @Test
    void updateSubtask() {
        var epic = manager.createEpic(TaskFactory.generateEpic("epic","some epic things"));
        var subtask = manager.createSubtask(TaskFactory
                .generateSubtask("SubTask","SubTask description", epic.getId()));
        subtask.setStatus(DONE);

        manager.updateSubtask(subtask);
        assertEquals(DONE, manager.getSubtaskById(subtask.getId()).getStatus(),
                "Статус должен измениться на DONE!");
    }

    @Test
    void deleteAllEpicsAndCheckHistory() {
        Epic epic = TaskFactory.generateEpic("SubTask","SubTask description");
        epic = manager.createEpic(epic);

        Subtask subtask1 = TaskFactory.generateSubtask("SubTask1","SubTask description1", epic.getId());
        Subtask subtask2 = TaskFactory.generateSubtask("SubTask2","SubTask description2", epic.getId());
        subtask2.setStartTime(subtask1.getStartTime().plusDays(30));
        subtask1 = manager.createSubtask(subtask1);
        subtask2 = manager.createSubtask(subtask2);
        // Заполняем историю просмотров + получаем менеджер истории просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());

        assertNotNull(manager.getEpicsAsList(), "Задач нет в списке!");
        assertNotNull(manager.getSubtasksAsList(), "Подзадач нет в списке!");

        assertEquals(List.of(epic, subtask1, subtask2), manager.getHistoryManager().getAll(),
                "Задачи не были добавлены в историю просмотров");

        manager.deleteAllEpics();
        assertEquals(0, manager.getEpicsAsList().size(),
                "Эпики не удалились из списка!");
        assertEquals(0, manager.getSubtasksAsList().size(),
                "Подзадачи не удалились из списка вместе с эпиками!");
        assertEquals(0, manager.getHistoryManager().getAll().size(),
                "Эпики с подзадачами не удалились из истории просмотров");
    }

    @Test
    void deleteAllSubtasksAndCheckHistory() {
        Epic epic = TaskFactory.generateEpic("SubTask","SubTask description");
        epic = manager.createEpic(epic);

        Subtask subtask1 = TaskFactory.generateSubtask("SubTask1","SubTask description1", epic.getId());
        Subtask subtask2 = TaskFactory.generateSubtask("SubTask2","SubTask description2", epic.getId());
        subtask2.setStartTime(subtask1.getStartTime().plusDays(30));
        subtask1 = manager.createSubtask(subtask1);
        subtask2 = manager.createSubtask(subtask2);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());
        assertEquals(List.of(epic, subtask1, subtask2), manager.getHistoryManager().getAll(),
                "Эпики с подзадачами не были добавлены в историю просмотров");

        manager.deleteAllSubtasks();
        assertEquals(0, manager.getSubtasksAsList().size(),
                "Подзадачи не удалились из map подзадач!");
        assertEquals(1, manager.getEpicsAsList().size(),
                "Эпик не должен удаляться, поскольку удаляются только подзадачи");

        assertEquals(List.of(epic), manager.getHistoryManager().getAll(),
                "Подзадачи не удалились из истории просмотров");
    }

    @Test
    void deleteTaskByIdAndCheckHistory() {
        Task task1 = TaskFactory.generateTask("Task1", "descr task1");
        Task task2 = TaskFactory.generateTask("Task2", "descr task2");

        Task task1Created = manager.createTask(task1);
        Task task2Created = manager.createTask(task2);
        manager.getTaskById(task1Created.getId());
        manager.getTaskById(task2Created.getId());

        HistoryManager historyManager = manager.getHistoryManager();
        assertEquals(List.of(task1Created, task2Created), historyManager.getAll(),
                "Задачи не были добавлены в историю просмотров");

        manager.deleteTaskById(task1Created.getId());
        assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(task1Created.getId()),
                "Задача всё ещё в списке!");

        historyManager = manager.getHistoryManager();
        assertEquals(List.of(task2Created), historyManager.getAll(),
                "Задача1 не были удалены из истории просмотров");
    }

    @Test
    void deleteEpicByIdAndCheckHistory() {
        Epic epic = TaskFactory.generateEpic("epic","epic with subs");
        epic = manager.createEpic(epic);
        Subtask subtaskA  = TaskFactory.generateSubtask("subtaskA","subA description", epic.getId());
        Subtask subtaskB  = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());
        subtaskB.setStartTime(subtaskA.getStartTime().plusDays(30));
        subtaskA = manager.createSubtask(subtaskA);
        subtaskB = manager.createSubtask(subtaskB);

        // Заполняем историю просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getSubtaskById(subtaskB.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getEpicById(epic.getId());
        manager.getEpicById(epic.getId());

        assertEquals(manager.getHistoryManager().getAll().size(), 3,
                "В историю должно было попасть 3 задачи!");


        manager.deleteEpicById(epic.getId());
        assertEquals(0, manager.getSubtasksAsList().size(), "Подзадачи не были удалены из менеджера");
        assertEquals(manager.getHistoryManager().getAll().size(), 0, "История должна быть пустая!");
    }

    @Test
    void deleteSubtaskByIdAndCheckHistory() {
        Epic epic = TaskFactory.generateEpic("epic","epic with subs");
        epic = manager.createEpic(epic);

        Subtask subtaskA  = TaskFactory.generateSubtask("subtaskA","subA description", epic.getId());
        Subtask subtaskB  = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());
        subtaskA = manager.createSubtask(subtaskA);
        subtaskB = manager.createSubtask(subtaskB);

        // Заполняем историю просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getSubtaskById(subtaskB.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getEpicById(epic.getId());
        manager.getEpicById(epic.getId());

        assertEquals(manager.getHistoryManager().getAll().size(), 3,
                "В историю должно было попасть 3 задачи!");


        manager.deleteEpicById(epic.getId());
        Subtask finalSubtaskA = subtaskA;
        assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(finalSubtaskA.getId()),
                "Подзадача A не была удалена из менеджера");
        Subtask finalSubtaskB = subtaskB;
        assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(finalSubtaskB.getId()),
                "Подзадача B не была удалена из менеджера");
        assertEquals(manager.getHistoryManager().getAll().size(), 0, "История должна быть пустая!");
    }

    @DisplayName("Удалять NEW подзадачу из эпика и проверить статус эпика (статус эпика должен стать DONE)")
    @Test
    void deleteSubtaskNewWhileOtherAreDone() {
        Epic epic = TaskFactory.generateEpic("epic","epic with subs");
        epic = manager.createEpic(epic);

        Subtask subtaskA  = TaskFactory.generateSubtask("subtaskA","subA description", epic.getId());
        Subtask subtaskB  = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());
        Subtask subtaskC  = TaskFactory.generateSubtask("subtaskC","subC description", epic.getId());

        subtaskA = manager.createSubtask(subtaskA);
        subtaskB = manager.createSubtask(subtaskB);
        subtaskC = manager.createSubtask(subtaskC);

        subtaskA.setStatus(DONE);
        subtaskB.setStatus(DONE);

        manager.updateSubtask(subtaskA);
        manager.updateSubtask(subtaskB);

        /*
        epic - IN_PROGRESS
         -> SubtaskA - DONE
         -> SubtaskB - DONE
         -> SubtaskC - NEW
        */

        assertEquals(DONE, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи А должен измениться на DONE");
        assertEquals(DONE, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен измениться на DONE");
        assertEquals(IN_PROGRESS, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика должен быть IN_PROGRESS");

        manager.deleteSubtaskById(subtaskC.getId());
        /*
            epic - DONE
             -> SubtaskA - DONE
             -> SubtaskB - DONE
        */
        assertEquals(DONE, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи А должен измениться на DONE");
        assertEquals(DONE, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен измениться на DONE");
        assertEquals(DONE, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика должен быть DONE");
    }
}
