package managersTest;

import managers.Managers;
import managers.memory_classes.FileBackedTaskManager;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static models.Status.*;
import static org.junit.jupiter.api.Assertions.*;

/*
Остальные проверки на работоспособность методов менеджера находятся в InMemoryTaskManagerTest, здесь же будут
исключительно проверки на загрузку из файла всех типов задач с сохранением prioritizedTasks списка
ТЕСТЫ В ЭТОМ КЛАССЕ СЛЕДУЕТ ЗАПУСКАТЬ ПОСЛЕ ТЕСТОВ В InMemoryTaskManagerTest
 */

/*
Логика тестов:
1)Что-то делаем (или не делаем) с состоянием менеджера
2) Загружаем (дублируем) менеджер с помощью метода loadFromFile
3) Сверяем состояние предыдущего менеджера с текущим (лучше создать отельный метод для проверки)
 */
public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    // ПРОВЕРИТЬ ДОБАВЛЕНИЕ НОВЫХ ЗАДАЧ (С УНИКАЛЬНЫИ ID) ПОСЛЕ ЗАГРУЗКИ ИЗ ФАЙЛА!!!
    @Override
    protected FileBackedTaskManager createManager() throws IOException {
        return Managers.getDefaultFileManager();
    }

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @Override
    @Test
    void testEpicNew() {
        // все эпики в менеджере уже new, добавим парочку для тестов
        Epic newEpic1 = new Epic("Новый эпик", "Какое-то описание (фантазия на уровне)", NEW);
        Epic newEpic2 = new Epic("Ещё эпик", "Супер-оригинальное описание", NEW);
        manager.addEpic(newEpic1);
        manager.addEpic(newEpic2);
        assertEquals(manager.getEpicsAsList().size(), 3, "Эпик не был добавлен в менеджер");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(manager.getPath());
        loadedManager.getEpicsAsList().forEach(epic -> assertEquals(epic.getStatus(), NEW,
                "Статусы всех эпиков загружаемого файла должны быть NEW"));
        System.out.println(loadedManager.getAll());
        System.out.println(manager.getAll());
    }

    @Override
    @Test
    void testEpicInProgress() {

    }

    @Override
    @Test
    void testEpicDone() {

    }

    @Override
    @Test
    void updateTaskAndCheckToPrioritizedList() {

    }

//    private boolean checkAllEpicSubtasks(FileBackedTaskManager originalManager, FileBackedTaskManager loadedManager) {
//        List<Subtask> originalSubtasks = originalManager.getEpicsAsList().stream()
//                .flatMap(epic -> epic.getSubtasks().stream()).sorted(Integer.compare(Epic::getId))
//                .toList();
//
//        List<Subtask> loadedSubtasks = loadedManager.getEpicsAsList().stream()
//                .flatMap(epic -> epic.getSubtasks().stream())
//                .toList();
//
//        if (originalSubtasks.size() != loadedSubtasks.size()) {
//            return false;
//        }
//
//        originalSubtasks.sort(Integer.compare(Task::getId));
//        return true;
//    }
}
