package managersTest;

import managers.interfaces.TaskManager;
import models.*;
import org.junit.jupiter.api.*;
import program_behavior.LDTRandomizer;

import java.io.IOException;
import java.time.LocalDateTime;

import static models.Status.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    T manager;


    Task task1 =
            new Task("Создание Мобильного Приложения", "Разработка интерфейса", NEW,
                    LDTRandomizer.getRandomLDT(), 50);
    Task task2 =
            new Task("Новая задача", "Описание новой задачи", NEW,
                    LDTRandomizer.getRandomLDT(), 40);
    Epic epic =
            new Epic("Разработка интерфейса", "Разделяется на 3 подзадачи", NEW);

    Subtask subtaskA;
    Subtask subtaskB;

    protected abstract T createManager() throws IOException;

    @BeforeEach
    void init() throws IOException {
        manager = createManager();
    }

    void setUp() {
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic);
        subtaskA =
                new Subtask("Подзадача 1", "Дизайн пользовательского интерфейса", NEW, epic.getId(),
                        LocalDateTime.of(2020, 1, 1, 0, 0), 60);
        subtaskB =
                new Subtask("Подзадача 2", "Разработка пользовательских сценариев", NEW,
                        epic.getId(), LocalDateTime.of(2020, 1, 2, 0, 0), 60);
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        assertEquals(manager.getAll().size(), 5, "В менеджер попали не все задачи");
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
        Subtask uniqueSubtask =
                new Subtask("Очередная задача", "???", NEW, epic.getId(),LocalDateTime.now(), 10);
        Subtask subtaskCopied = subtaskA;

        manager.addSubtask(uniqueSubtask);
        assertSame(subtaskCopied, subtaskA);

        manager.addSubtask(uniqueSubtask);
        manager.getSubtaskById(uniqueSubtask.getId());

        assertTrue(manager.getAll().contains(uniqueSubtask), "Подзадача не была добавлена в менеджер");
    }

    @DisplayName("Обновление задачи до статуса IN_PROGRESS и DONE")
    @Test
    abstract void updateTaskAndCheckToPrioritizedList();



}
