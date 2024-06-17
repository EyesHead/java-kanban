package managersTest;

import managers.memory_classes.FileBackedTaskManager;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Остальные проверки на работоспособность методов менеджера находятся в InMemoryTaskManagerTest, здесь же будут
исключительно проверки на загрузку из файла всех типов задач с сохранением prioritizedTasks списка
ТЕСТЫ В ЭТОМ КЛАССЕ СЛЕДУЕТ ЗАПУСКАТЬ ПОСЛЕ ТЕСТОВ В InMemoryTaskManagerTest
 */

/*
Логика тестов:
1)Что-то делаем (или не делаем) с состоянием менеджера
2) Загружаем (дублируем) менеджер с помощью метода loadFromFile
3) Сверяем состояние предыдущего менеджера с текущим (наличие и id всех типов задач + приоритизированный список)
 */
public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    // ПРОВЕРИТЬ ДОБАВЛЕНИЕ НОВЫХ ЗАДАЧ (С УНИКАЛЬНЫИ ID) ПОСЛЕ ЗАГРУЗКИ ИЗ ФАЙЛА!!!

    @Override
    public FileBackedTaskManager createManager() throws IOException {
        return new FileBackedTaskManager(File.createTempFile("resources/taskTemp", ".tmp").toPath());
    }

    @Override
    @BeforeEach
    void beforeEach() throws IOException {
        super.beforeEach();
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

        System.out.printf("Текущее состояние менеджера: \n" + manager.getAll() + "\n");
        System.out.printf("Текущее состояние менеджера после загрузки из файла: \n" + loadedManager.getAll() + "\n\n");
        System.out.printf("Текущее состояние приоритизированного списка: \n" + manager.getPrioritizedTasks() + "\n");
        System.out.printf("Текущее состояние приоритизированного списка после загрузки из файла: \n" +
                loadedManager.getPrioritizedTasks());

        assertTrue(areAllTasksAreSame(manager, loadedManager),
                "Состояние задач из двух менеджеров не совпадает");
        assertTrue(areSameSubtasksAtEpics(manager, loadedManager),
                "Связанные подзадачи у эпиков не совпадают в двух менеджерах");
        // допилить проверку двух менеджеров
    }

    @Override
    @Test
    void testEpicInProgress() {

    }

    @Override
    @Test
    void testEpicDone() {

    }

    private boolean areAllTasksAreSame(FileBackedTaskManager manager, FileBackedTaskManager loadedManager) {
        List<Task> originalTasks = manager.getAll();
        List<Task> loadedTasks = loadedManager.getAll();

        if (originalTasks.size() != loadedTasks.size()) {
            return false;
        }
        for (int i = 0; i < originalTasks.size(); i++) {
            Task task = originalTasks.get(i);
            Task loadedTask = loadedTasks.get(i);

            if (!Objects.equals(task.getId(), loadedTask.getId()) ||
                    !Objects.equals(task.getType(), loadedTask.getType()) ||
                    !Objects.equals(task.getName(), loadedTask.getName()) ||
                    !Objects.equals(task.getDescription(), loadedTask.getDescription()) ||
                    !Objects.equals(task.getStatus(), loadedTask.getStatus()) ||
                    !Objects.equals(task.getType(), loadedTask.getType()))
                return false;
        }

        return true;
    }

    private boolean areSameSubtasksAtEpics(FileBackedTaskManager manager, FileBackedTaskManager loadedManager) {
        List<Epic> originalEpics = manager.getEpicsAsList();
        List<Epic> loadedEpics = loadedManager.getEpicsAsList();

        return originalEpics.equals(loadedEpics);
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
