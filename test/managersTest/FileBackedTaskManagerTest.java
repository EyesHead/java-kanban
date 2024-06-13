package managersTest;

import managers.memory_classes.FileBackedTaskManager;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    FileBackedTaskManager fileManager;
    Task task1;
    Task task2;
    Epic epic;
    Subtask subtaskA;
    Subtask subtaskB;
    File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        //почти заполнили файл менеджер
        tempFile = File.createTempFile("taskTmp", ".csv");
        fileManager = new FileBackedTaskManager(Path.of(tempFile.getAbsolutePath()));
        task1 = new Task(0,"Создание Мобильного Приложения", "Разработка интерфейса", NEW);
        task2 = new Task(0,"Новая задача", "Описание новой задачи", NEW);
        epic = new Epic(0, "Разработка интерфейса", "Разделяется на 3 подзадачи", NEW);
        fileManager.addTask(task1);
        fileManager.addTask(task2);
        fileManager.addEpic(epic);

        //Заполнили историю просмотров
        fileManager.getTaskById(task1.getId());
        fileManager.getTaskById(task2.getId());
        fileManager.getEpicById(epic.getId());

    }

    @Test
    public void compareLoadedFileManagerFromBlankFileManagerTest() {

        fileManager.clearAll();
        FileBackedTaskManager fileManagerLoad = FileBackedTaskManager.loadFromFile(fileManager.getPath());

        assertTrue(fileManagerLoad.getHistoryManager().getAll().isEmpty(), "История не пустая");
        assertTrue(fileManagerLoad.getAll().isEmpty(), "Задачи с пустого менеджера тоже должны быть пустыми");
    }

    @Test
    public void saveNewTasksTest() {
        subtaskA = new Subtask(0,"Подзадача 1", "Дизайн пользовательского интерфейса", epic.getId(), NEW);
        subtaskB = new Subtask(0,"Подзадача 2", "Разработка пользовательских сценариев", epic.getId(), NEW);
        fileManager.addSubtask(subtaskA);
        fileManager.addSubtask(subtaskB);

        assertEquals(5, fileManager.getAll().size(), "Ожидалось создание 5 задач в файл");
    }

    @Test
    public void compareLoadedFileManagerFromFilledFileManagerTest() {
        subtaskA = new Subtask(0, "Подзадача 1", "Дизайн пользовательского интерфейса", epic.getId(), NEW);
        subtaskB = new Subtask(0, "Подзадача 2", "Разработка пользовательских сценариев", epic.getId(), NEW);
        fileManager.addSubtask(subtaskA);
        fileManager.addSubtask(subtaskB);

        fileManager.getSubtaskById(subtaskA.getId());
        fileManager.getSubtaskById(subtaskB.getId());
        fileManager.getTaskById(task1.getId());

        FileBackedTaskManager fileManagerLoad = FileBackedTaskManager.loadFromFile(fileManager.getPath());
        assertEquals(fileManager.getAll().size() ,fileManagerLoad.getAll().size(),
                "Количество всех задач в обоих файлах должно быть одинаковым");

    }

    @AfterEach
    public void deleteTempFile() {
        tempFile.deleteOnExit();
    }
}
