package managersTest;

import factories.TaskFactory;
import model.Epic;
import service.ManagersCreator;
import service.memory.InMemoryTaskManager;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static model.Status.*;
import static org.junit.jupiter.api.Assertions.*;

/*
В данном классе уже есть менеджер, в котором созданы 2 задачи, и эпик с тремя подзадачами,
а так же заполнен prioritizedTasks список (так как он заполняется по мере добавления
(обновления и удаления) задач и подзадач)
 */
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    public InMemoryTaskManager createManager() throws IOException{
        return ManagersCreator.getDefaultTaskManager();
    }

    @Override
    @BeforeEach
    void beforeEach() throws IOException {
        super.beforeEach();
    }

    @Override
    @Test
    void testEpicNew() {
        Epic epicToCreate = TaskFactory.generateEpic("Epic","Epic Description");
        manager.createEpic(epicToCreate);

        assertEquals(NEW, manager.getEpicsAsList().getFirst().getStatus(),"Статус эпика должен быть NEW");
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

        // Обновляем статус SubtaskA на IN_PROGRESS => статус эпика меняется на IN_PROGRESS
        subtaskA.setStatus(IN_PROGRESS);
        manager.updateSubtask(subtaskA);//обновляем подзадачу

        assertEquals(IN_PROGRESS, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика после обновления должен быть IN_PROGRESS");
        assertEquals(IN_PROGRESS, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(NEW, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен быть NEW!");

        subtaskB.setStatus(DONE);
        manager.updateSubtask(subtaskB);
        // epic - IN_PROGRESS
        // subtaskA - IN_PROGRESS
        // subtaskB - DONE

        assertEquals(2, manager.getSubtasksAsList().size(), "В менеджере должно быть 3 subtask");
        assertEquals(IN_PROGRESS, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика после обновления должен быть IN_PROGRESS");
        assertEquals(IN_PROGRESS, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(DONE, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен быть NEW!");

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

        assertEquals(NEW, manager.getSubtaskById(subtaskA.getId()).getStatus(), "Начальный статус subA = NEW");
        assertEquals(NEW, manager.getSubtaskById(subtaskB.getId()).getStatus(), "Начальный статус subB = NEW");
        subtaskA.setStatus(DONE);
        subtaskB.setStatus(DONE);
        manager.updateSubtask(subtaskA);
        manager.updateSubtask(subtaskB);
        assertEquals(DONE, manager.getEpicById(epic.getId()).getStatus(), "Статус эпика после обновления должен быть DONE");
        assertEquals(DONE, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи A должен быть DONE!");
        assertEquals(DONE, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен быть DONE!");
    }

    @Test
    void updateTask() {
        Task task1ToCreate = TaskFactory.generateTask("Task1", "descr task1");
        Task task2ToCreate = TaskFactory.generateTask("Task2", "descr task2");
        task2ToCreate.setStartTime(task1ToCreate.getStartTime().plusDays(30));
        Task task1 = manager.createTask(task1ToCreate);
        Task task2 = manager.createTask(task2ToCreate);

        task1.setStatus(IN_PROGRESS);
        manager.updateTask(task1);

        final List<Task> tasks = manager.getTasksAsList();
        assertEquals(2, tasks.size(), "Задач больше (меньше), чем должно быть");
        assertEquals(IN_PROGRESS, manager.getTaskById(task1.getId()).getStatus(), "У первой задачи статус не должен меняться");
        assertEquals(NEW, manager.getTaskById(task2.getId()).getStatus(), "У второй задачи статус должен был поменяться");
    }

    @Test
    void deleteAllTasksAndCheckHistory() {
        Task task1ToCreate = TaskFactory.generateTask("Task1", "descr task1");
        Task task2ToCreate = TaskFactory.generateTask("Task2", "descr task2");
        Task task1 = manager.createTask(task1ToCreate);
        Task task2 = manager.createTask(task2ToCreate);

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        assertEquals(List.of(task1, task2), manager.getHistoryManager().getAll(),
                "Задачи не добавились в историю просмотров");

        assertNotNull(manager.getTasksAsList(), "Задач нет в списке!");
        manager.deleteAllTasks();
        assertEquals(0, manager.getTasksAsList().size(), "Задачи не удалились из списка!");

        assertEquals(0, manager.getHistoryManager().getAll().size(),
                "Задачи не удалились из истории просмотров");
    }
}