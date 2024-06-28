package managersTest;

import managers.custom_exceptions.NotFoundException;
import managers.interfaces.HistoryManager;
import managers.interfaces.TaskManager;
import models.*;
import org.junit.jupiter.api.*;
import util.LDTRandomizer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static models.Status.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    public T manager;

    public Task task1;
    public Task task2;
    public Epic epic;

    public Subtask subtaskA;
    public Subtask subtaskB;
    public Subtask subtaskC;

    protected abstract T createManager() throws IOException;

    @BeforeEach
    void beforeEach() throws IOException {
        manager = createManager();
    }

    void initTasks() {
        task1 = new Task("Создание Мобильного Приложения", "Разработка интерфейса", NEW,
                LDTRandomizer.getRandomLDT(), 50);
        task2 = new Task("Новая задача", "Описание новой задачи", NEW,
                LDTRandomizer.getRandomLDT(), 40);
    }

    void initEpic() {
        epic = new Epic("Разработка интерфейса", "Разделяется на 3 подзадачи", NEW);
    }

    void initSubtasks() {
        subtaskA = new Subtask("Подзадача 1", "Дизайн пользовательского интерфейса", NEW,
                epic.getId(), LocalDateTime.of(2020, 1, 1, 0, 0), 60);
        subtaskB = new Subtask("Подзадача 2", "Разработка пользовательских сценариев", NEW,
                epic.getId(), LocalDateTime.of(2021, 1, 2, 0, 0), 60);
        subtaskC = new Subtask("Подзадача 2", "Разработка пользовательских сценариев", NEW,
                epic.getId(), LocalDateTime.of(2022, 1, 3, 0, 0), 60);
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
        initTasks();
        manager.addTask(task1);
        Task taskUnique =
                new Task( "Очередная задача", "???", NEW, LocalDateTime.now(), 10);
        Task taskCopied = task1;

        manager.addTask(taskUnique);
        assertSame(task1, taskCopied);

        manager.addTask(taskUnique);
        manager.getTaskById(taskUnique.getId());

        assertTrue(manager.getAll().contains(taskUnique), "Задача не была добавлена в менеджер");
    }

    @DisplayName("Создать эпик + задать ему уникальный идентификатор")
    @Test
    void shouldCreateEpicWithUniqueId() {
        initEpic();
        manager.addEpic(epic);
        Epic uniqueEpic =
                new Epic("123213", "???", NEW);
        Epic epicCopied = epic;

        manager.addTask(uniqueEpic);
        assertSame(epic, epicCopied);

        manager.addTask(uniqueEpic);
        manager.getTaskById(uniqueEpic.getId());

        assertTrue(manager.getAll().contains(uniqueEpic), "Эпик не был добавлен в менеджер");
    }

    @DisplayName("Создать подзадачу + задать ей уникальный идентификатор")
    @Test
    void shouldCreateSubtaskWithUniqueId() {
        initEpic();
        manager.addEpic(epic);
        initSubtasks();
        manager.addSubtask(subtaskA);
        Subtask uniqueSubtask =
                new Subtask("Очередная задача", "???", NEW, epic.getId(),LocalDateTime.now(), 10);
        Subtask subtaskCopied = subtaskA;

        manager.addSubtask(uniqueSubtask);
        assertSame(subtaskCopied, subtaskA);

        manager.addSubtask(uniqueSubtask);
        manager.getSubtaskById(uniqueSubtask.getId());

        assertTrue(manager.getAll().contains(uniqueSubtask), "Подзадача не была добавлена в менеджер");
    }

    @DisplayName("Обновить статус и время у существующей задачи и проверить, что она изменилась в менеджере")
    @Test
    void updateTask() {
        initTasks();
        manager.addTask(task1);
        Task task1InProgress = task1;
        task1InProgress.setStatus(IN_PROGRESS);

        manager.updateTask(task1InProgress);
    }

    @DisplayName("Обновить статус и время у существующей задачи и проверить, что она изменилась в менеджере")
    @Test
    void updateSubtask() {
        manager.addTask(task1);
        Task task1Done = task1;

        task1Done.setStatus(DONE);

        manager.updateTask(task1Done);
        assertEquals(DONE, manager.getTaskById(task1Done.getId()).getStatus(), "Статус должен быть DONE!");
    }

    @Test
    void deleteAllEpicsAndCheckHistory() {
        initEpic();
        manager.addEpic(epic);

        initSubtasks();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getSubtaskById(subtaskB.getId());
        assertEquals(List.of(epic, subtaskA, subtaskB), manager.getHistoryManager().getAll(),
                "Задачи не были добавлены в историю просмотров");

        assertNotNull(manager.getEpicsAsList(), "Задач нет в списке!");
        assertNotNull(manager.getSubtasksAsList(), "Подзадач нет в списке!");

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
        initEpic();
        manager.addEpic(epic);

        initSubtasks();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getSubtaskById(subtaskB.getId());
        assertEquals(List.of(epic, subtaskA, subtaskB), manager.getHistoryManager().getAll(),
                "Эпики с подзадачами не были добавлены в историю просмотров");

        manager.deleteAllSubtasks();
        assertEquals(0, manager.getSubtasksAsList().size(), "Подзадачи не удалились из map подзадач!");
        assertEquals(1, manager.getEpicsAsList().size(), "Эпики не должны удаляться, " +
                "поскольку удаляются только подзадачи");

        assertEquals(List.of(epic), manager.getHistoryManager().getAll(), "Подзадачи не удалились из истории просмотров");
    }

    @Test
    void deleteTaskByIdAndCheckHistory() {
        initTasks();
        manager.addTask(task1);// id = 0
        manager.addTask(task2);// id = 1
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        HistoryManager historyManager = manager.getHistoryManager();
        assertEquals(List.of(task1, task2), historyManager.getAll(),
                "Задачи не были добавлены в историю просмотров");

        manager.deleteTaskById(task1.getId());
        assertThrows(NotFoundException.class,
                () -> manager.getTaskById(task1.getId()),
                "Задача всё ещё в списке!");

        historyManager = manager.getHistoryManager();
        assertEquals(List.of(task2), historyManager.getAll(),
                "Задачи не были удалены из истории просмотров");
    }

    @Test
    void deleteEpicByIdAndCheckHistory() {
        // Эпик должен удалиться из map`ы эпиков
        // Должны удалиться все подзадачи из map`ы подзадач
        // Эпик должен удалиться из истории просмотров
        initEpic();
        manager.addEpic(epic);// id = 0

        initSubtasks();
        manager.addSubtask(subtaskA);// id = 1
        manager.addSubtask(subtaskB);// id = 2

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
        assertThrows(NotFoundException.class, () -> manager.getTaskById(subtaskA.getId()),
                "Подзадача A не была удалена из менеджера");
        assertThrows(NotFoundException.class, () -> manager.getTaskById(subtaskB.getId()),
                "Подзадача B не была удалена из менеджера");
        assertEquals(manager.getHistoryManager().getAll().size(), 0, "История должна быть пустая!");
    }

    @Test
    void deleteSubtaskByIdAndCheckHistory() {
        // Эпик должен удалиться из map`ы эпиков
        // Должны удалиться все подзадачи из map`ы подзадач
        // Эпик должен удалиться из истории просмотров
        initEpic();
        manager.addEpic(epic);// id = 0

        initSubtasks();
        manager.addSubtask(subtaskA);// id = 1
        manager.addSubtask(subtaskB);// id = 2

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
        assertThrows(NotFoundException.class, () -> manager.getTaskById(subtaskA.getId()),
                "Подзадача A не была удалена из менеджера");
        assertThrows(NotFoundException.class, () -> manager.getTaskById(subtaskB.getId()),
                "Подзадача B не была удалена из менеджера");
        assertEquals(manager.getHistoryManager().getAll().size(), 0, "История должна быть пустая!");
    }

    @DisplayName("Удалять NEW подзадачу из эпика и проверить статус эпика")
    @Test
    void deleteSubtaskNewWhileOtherAreDone() {
        initEpic();
        manager.addEpic(epic);

        initSubtasks();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

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
                "Статус эпика должен быть IN_PROGRESS");
    }
}
