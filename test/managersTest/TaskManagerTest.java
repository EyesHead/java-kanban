package managersTest;

import managers.interfaces.TaskManager;
import managers.memory_classes.FileBackedTaskManager;
import managers.memory_classes.InMemoryTaskManager;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import program_behavior.LDTRandomizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static models.Status.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    InMemoryTaskManager taskManager;
    FileBackedTaskManager fileManager;
    File tempFile;
    Task task1 = new Task(0,"Закончить ТЗ-8", "Реализовать Duration и LocalDateTime в задачах", NEW,
            LocalDateTime.now(), 200);
    Task task2 = new Task(0,"Обработать проектную документацию", "Тиражировать ПД склада по Севастополю", NEW,
            LocalDateTime.now().minusDays(2), 70);
    Epic epic = new Epic(0, "Реализовать ТЗ-8",
            "Реализовать: новые поля в тасках, приоритетный список, методы по работе с новым списком", NEW);

    @BeforeEach
    protected void setUp() throws IOException {
        tempFile = File.createTempFile("taskTmp", ".csv");
        fileManager = new FileBackedTaskManager(Path.of(tempFile.getAbsolutePath()));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addEpic(epic);
        Subtask subtaskA =
                new Subtask(0,"Подзадача 1", "Дизайн пользовательского интерфейса", epic.getId(), NEW,
                        LDTRandomizer.getRandomLDT(), 50);
        Subtask subtaskB =
                new Subtask(0,"Подзадача 2", "Разработка пользовательских сценариев", epic.getId(), NEW,
                        LDTRandomizer.getRandomLDT(), 120);
        taskManager.addSubtask(subtaskA);
        taskManager.addSubtask(subtaskB);

    }

    @Test
    protected abstract void addTask();
    @Test
    protected abstract void updateTask();
    @Test
    protected abstract void deleteTask();

    @Test
    protected abstract void addEpic();
    @Test
    protected abstract void deleteEpic();




}
