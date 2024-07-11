package managersTest;

import factories.TaskFactory;
import service.file.FileBackedTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static model.Status.*;
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
        Epic epicToCreate = TaskFactory.generateEpic("Epic","Epic Description");
        Epic epic = manager.createEpic(epicToCreate);
        Subtask subtaskAToCreate = TaskFactory.generateSubtask("subtaskA","subA description", epic.getId());
        Subtask subtaskBToCreate = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());
        subtaskBToCreate.setStartTime(subtaskAToCreate.getStartTime().plusDays(30));
        manager.createSubtask(subtaskAToCreate);
        manager.createSubtask(subtaskBToCreate);

        assertEquals(manager.getEpicsAsList().size(), 1, "Эпик не был добавлен в менеджер");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadManagerFromFile(manager.getPath());
        assertSame(loadedManager.getEpicsAsList().getFirst().getStatus(), NEW,
                "Статус epic у двух менеджеров не совпадает");

        assertTrue(areAllTasksAreSame(manager, loadedManager),
                "Состояние задач из двух менеджеров не совпадает");
        assertTrue(areSameSubtasksAtEpics(manager, loadedManager),
                "Связанные подзадачи у эпиков не совпадают в двух менеджерах");
    }

    @Override
    @Test
    void testEpicInProgress() {
        Epic epic = manager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        Subtask subtaskAToCreate = TaskFactory.generateSubtask("subtaskA","subA description", epic.getId());
        Subtask subtaskBToCreate = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());
        subtaskBToCreate.setStartTime(subtaskAToCreate.getStartTime().plusDays(30));
        Subtask subtaskA = manager.createSubtask(subtaskAToCreate);
        Subtask subtaskB = manager.createSubtask(subtaskBToCreate);

        subtaskA.setStatus(IN_PROGRESS);
        subtaskB.setStatus(DONE);

        manager.updateSubtask(subtaskA);
        manager.updateSubtask(subtaskB);

        assertEquals(IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика после обновлений подзадач должен быть IN_PROGRESS");

        subtaskB.setStatus(IN_PROGRESS);
        manager.updateSubtask(subtaskB);
        assertEquals(IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика после обновлений подзадач должен быть IN_PROGRESS");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadManagerFromFile(manager.getPath());
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
        Epic epic = manager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        Subtask subtaskAToCreate = TaskFactory.generateSubtask("subtaskA","subA description", epic.getId());
        Subtask subtaskBToCreate = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());
        subtaskBToCreate.setStartTime(subtaskAToCreate.getStartTime().plusDays(30));
        Subtask subtaskA = manager.createSubtask(subtaskAToCreate);
        Subtask subtaskB = manager.createSubtask(subtaskBToCreate);

        subtaskA.setStatus(DONE);
        manager.updateSubtask(subtaskA);

        subtaskB.setStatus(DONE);
        manager.updateSubtask(subtaskB);

        assertEquals(DONE, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика после обновлений подзадач должен быть DONE");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadManagerFromFile(manager.getPath());
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
                    !Objects.equals(task.getStatus(), loadedTask.getStatus()))
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
