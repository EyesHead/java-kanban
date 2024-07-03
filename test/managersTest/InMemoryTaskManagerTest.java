package managersTest;

import managersCreator.Managers;
import taskManager.memory.InMemoryTaskManager;
import tasksModels.Subtask;
import tasksModels.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static tasksModels.Status.*;
import static org.junit.jupiter.api.Assertions.*;

/*
В данном классе уже есть менеджер, в котором созданы 2 задачи, и эпик с тремя подзадачами,
а так же заполнен prioritizedTasks список (так как он заполняется по мере добавления
(обновления и удаления) задач и подзадач)
 */
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    public InMemoryTaskManager createManager() throws IOException{
        return Managers.getDefaultTaskManager();
    }

    @Override
    @BeforeEach
    void beforeEach() throws IOException {
        super.beforeEach();
    }

    @Override
    @Test
    void testEpicNew() {
        initEpic();
        manager.createEpic(epic);
        initSubtasks();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);

        assertEquals(NEW, manager.getEpicsAsList().getFirst().getStatus(),"Статус эпика должен быть NEW");
        assertEquals(NEW, manager.getSubtasksAsList().getFirst().getStatus(),"Статус подзадачи A должен быть NEW!");
        assertEquals(NEW, manager.getSubtasksAsList().getLast().getStatus(), "Статус подзадачи B должен быть NEW!");
    }

    @Override
    @Test
    void testEpicInProgress() {
        initEpic();
        manager.createEpic(epic);
        initSubtasks();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);

        // Обновляем статус SubtaskA на IN_PROGRESS => статус эпика меняется на IN_PROGRESS
        Subtask subtaskAUpdated = subtaskA;
        subtaskAUpdated.setStatus(IN_PROGRESS);
        manager.updateSubtask(subtaskAUpdated);//обновляем подзадачу

        assertEquals(IN_PROGRESS, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика после обновления должен быть IN_PROGRESS");
        assertEquals(IN_PROGRESS, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(NEW, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен быть NEW!");

        Subtask subtaskBUpdated = subtaskB;
        subtaskBUpdated.setStatus(DONE);
        manager.updateSubtask(subtaskBUpdated);
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
        initEpic();
        manager.createEpic(epic);
        initSubtasks();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);

        Subtask subtaskADone = new Subtask(subtaskA.getId(),"Done A Subtask", "some description a",
                DONE, epic.getId(), subtaskA.getStartTime(), (int) subtaskA.getDuration().toMinutes());
        Subtask subtaskBDone = new Subtask(subtaskB.getId(),"Done B Subtask", "some description b", DONE,
                epic.getId(), subtaskB.getStartTime(), (int) subtaskB.getDuration().toMinutes());
        subtaskADone.setId(subtaskA.getId());
        subtaskBDone.setId(subtaskB.getId());
        manager.updateSubtask(subtaskADone);
        manager.updateSubtask(subtaskBDone);

        assertEquals(DONE, manager.getEpicById(epic.getId()).getStatus(), "Статус эпика после обновления должен быть DONE");
        assertEquals(DONE, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(DONE, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен быть NEW!");
    }

    @Test
    void updateTask() {
        initTasks();
        manager.createTask(task1);
        manager.createTask(task2);

        // Хотим обновить ВТОРУЮ задачу (с id = 2)
        Task newTask = new Task(2, "Обновленная задача", "Какое-то описание", IN_PROGRESS,
                task1.getStartTime(), (int) task1.getDuration().toMinutes());
        newTask.setId(task2.getId());
        manager.updateTask(newTask);

        final List<Task> tasks = manager.getTasksAsList();
        assertEquals(2, tasks.size(), "Задач больше (меньше), чем должно быть");
        assertEquals(tasks.getFirst().getStatus(), NEW, "У первой задачи статус не должен меняться");
        assertEquals(tasks.get(1).getStatus(), IN_PROGRESS, "У второй задачи статус должен был поменяться");
        assertEquals(tasks.get(task2.getId()),
                tasks.get(newTask.getId()),
                "ID старой и новой задачи должны совпадать!");
    }

    @Test
    void deleteAllTasksAndCheckHistory() {
        initTasks();
        manager.createTask(task1);
        manager.createTask(task2);
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