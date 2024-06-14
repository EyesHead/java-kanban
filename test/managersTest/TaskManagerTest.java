package managersTest;

import managers.Managers;
import managers.interfaces.TaskManager;
import managers.memory_classes.FileBackedTaskManager;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static models.Status.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    Task task1 = new Task(0,"Закончить ТЗ-8", "Реализовать Duration и LocalDateTime в задачах", NEW,
            LocalDateTime.now(), 200);
    Task task2 = new Task(0,"Обработать проектную документацию", "Тиражировать ПД склада по Севастополю", NEW,
            LocalDateTime.now().minusDays(2), 70);
    Epic epic = new Epic(0, "Реализовать ТЗ-8",
            "Реализовать: новые поля в тасках, приоритетный список, методы по работе с новым списком", NEW,
            LocalDateTime.of(2024,6,14,9,0));

    @BeforeEach
    protected abstract void setUp() throws IOException;

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
