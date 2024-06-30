package managersTest;

import taskManager.memory.FileBackedTaskManager;
import tasksModels.Epic;
import tasksModels.Subtask;
import tasksModels.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static tasksModels.Status.*;
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
        // эпик в менеджере уже new, добавим парочку для тестов
        initEpic();
        manager.createEpic(epic);
        initSubtasks();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        Epic newEpic1 = new Epic("Новый эпик", "Какое-то описание (фантазия на уровне)", NEW);
        Epic newEpic2 = new Epic("Ещё эпик", "Супер-оригинальное описание", NEW);
        manager.createEpic(newEpic1);
        manager.createEpic(newEpic2);
        assertEquals(manager.getEpicsAsList().size(), 3, "Эпик не был добавлен в менеджер");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(manager.getPath());
        loadedManager.getEpicsAsList().forEach(epic -> assertEquals(epic.getStatus(), NEW,
                "Статусы всех эпиков загружаемого файла должны быть NEW"));

        assertTrue(areAllTasksAreSame(manager, loadedManager),
                "Состояние задач из двух менеджеров не совпадает");
        assertTrue(areSameSubtasksAtEpics(manager, loadedManager),
                "Связанные подзадачи у эпиков не совпадают в двух менеджерах");
    }

    @Override
    @Test
    void testEpicInProgress() {
        initEpic();
        manager.createEpic(epic);
        initSubtasks();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);

        subtaskA.setStatus(IN_PROGRESS);
        subtaskB.setStatus(DONE);
        Subtask aSubtaskUpdated = subtaskA;
        Subtask bSubtaskUpdated = subtaskB;

        manager.updateSubtask(aSubtaskUpdated);
        manager.updateSubtask(bSubtaskUpdated);

        assertEquals(IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика после обновлений подзадач должен быть IN_PROGRESS");

        bSubtaskUpdated.setStatus(IN_PROGRESS);
        manager.updateSubtask(bSubtaskUpdated);
        assertEquals(IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика после обновлений подзадач должен быть IN_PROGRESS");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(manager.getPath());
        assertEquals(IN_PROGRESS, loadedManager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика загружаемого файла должны быть IN_PROGRESS");
        assertTrue(areAllTasksAreSame(manager, loadedManager),
                "Состояние задач из двух менеджеров не совпадает");
        assertTrue(areSameSubtasksAtEpics(manager, loadedManager),
                "Связанные подзадачи у эпиков не совпадают в двух менеджерах");
    }

    @Override
    @Test
    void testEpicDone() {
        testEpicInProgress();
        subtaskA.setStatus(DONE);
        manager.updateSubtask(subtaskA);

        subtaskB.setStatus(DONE);
        manager.updateSubtask(subtaskB);

        assertEquals(DONE, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика после обновлений подзадач должен быть DONE");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(manager.getPath());
        assertEquals(DONE, loadedManager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика загружаемого файла должны быть DONE");
        assertTrue(areAllTasksAreSame(manager, loadedManager),
                "Состояние задач из двух менеджеров не совпадает");
        assertTrue(areSameSubtasksAtEpics(manager, loadedManager),
                "Связанные подзадачи у эпиков не совпадают в двух менеджерах");

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
}
