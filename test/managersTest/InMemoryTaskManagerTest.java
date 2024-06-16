package managersTest;

import managers.Managers;
import managers.memory_classes.InMemoryTaskManager;
import managers.interfaces.HistoryManager;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static models.Status.*;
import static org.junit.jupiter.api.Assertions.*;

/*
В данном классе уже есть менеджер, в котором созданы 2 задачи, и эпик с двумя подзадачами,
а так же заполнен prioritizedTasks список (так как он заполняется по мере добавления
(обновления и удаления) задач и подзадач)
 */
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {


    @Override
    protected InMemoryTaskManager createManager() {
        return Managers.getDefaultTaskManager();
    }

    @BeforeEach
    void setUp() {
        super.setUp();
    }



    @Override
    @Test
    void testEpicNew() {
        final List<Subtask> subtasksBeforeUpdate = manager.getSubtasksAsList();
        assertEquals(NEW, manager.getEpicsAsList().getFirst().getStatus(),"Статус эпика должен быть NEW");
        assertEquals(NEW, subtasksBeforeUpdate.getFirst().getStatus(),"Статус подзадачи A должен быть NEW!");
        assertEquals(NEW, subtasksBeforeUpdate.getLast().getStatus(), "Статус подзадачи B должен быть NEW!");
    }

    @Override
    @Test
    void testEpicInProgress() {
        // Обновляем статус SubtaskA на IN_PROGRESS => статус эпика меняется на IN_PROGRESS
        Subtask subtaskAUpdated = new Subtask(subtaskA.getId(), "&!#!@#", "***", IN_PROGRESS, epic.getId(),
                subtaskA.getStartTime(), subtaskA.getDurationInMinutes());
        manager.updateSubtask(subtaskAUpdated);//обновляем подзадачу

        assertEquals(IN_PROGRESS, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика после обновления должен быть IN_PROGRESS");
        assertEquals(IN_PROGRESS, manager.getSubtaskById(subtaskA.getId()).getStatus(),
                "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(NEW, manager.getSubtaskById(subtaskB.getId()).getStatus(),
                "Статус подзадачи B должен быть NEW!");
    }

    @Override
    @Test
        void testEpicDone() {
        Subtask subtaskADone = new Subtask(subtaskA.getId(),"Done A Subtask", "some description a",
                DONE, epic.getId(), subtaskA.getStartTime(), subtaskA.getDurationInMinutes());
        Subtask subtaskBDone = new Subtask(subtaskB.getId(),"Done B Subtask", "some description b", DONE,
                epic.getId(), subtaskB.getStartTime(), subtaskB.getDurationInMinutes());
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

    @Override
    void updateTaskAndCheckToPrioritizedList() {

    }

    @Test
    void updateTask() {
        manager.addTask(task1);
        manager.addTask(task2);

        // Хотим обновить ВТОРУЮ задачу (с id = 1)
        Task newTask = new Task(1, "Обновленная задача", "Какое-то описание", IN_PROGRESS,
                task1.getStartTime(), task1.getDurationInMinutes());
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
        manager.addTask(task1);
        manager.addTask(task2);
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

    @Test
    void deleteAllEpicsAndCheckHistory() {
        manager.addEpic(epic);
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getSubtaskById(subtaskB.getId());
        assertEquals(List.of(epic, subtaskA, subtaskB), manager.getHistoryManager().getAll(),
                "Задачи не были добавлены в историю просмотров");

        assertNotNull(manager.getEpicsAsList(), "Задач нет в списке!");
        assertNotNull(manager.getSubtasksAsList(), "Подзадач нет в списке!");

        manager.deleteAllEpics();
        assertEquals(0, manager.getEpicsAsList().size(),
                "Эпики не удалились из списка!");
        assertEquals(0, manager.getSubtasksAsList().size(),
                "Подзадачи не удалились из списка вместе с эпиками!");

        assertEquals(0, manager.getHistoryManager().getAll().size(),
                "Эпики с подзадачами не удалились из истории просмотров");
    }

    @Test
    void deleteAllSubtasksAndCheckHistory() {

        manager.addEpic(epic);
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtaskA.getId());
        manager.getSubtaskById(subtaskB.getId());
        assertEquals(List.of(epic, subtaskA, subtaskB), manager.getHistoryManager().getAll(),
                "Эпики с подзадачами не были добавлены в историю просмотров");

        manager.deleteAllSubtasks();
        assertEquals(0, manager.getSubtasksAsList().size(), "Подзадачи не удалились из map подзадач!");
        assertEquals(1, manager.getEpicsAsList().size(), "Эпики не должны удаляться, " +
                "поскольку удаляются только подзадачи");

        assertEquals(List.of(epic), manager.getHistoryManager().getAll(), "Подзадачи не удалились из истории просмотров");
    }
    @Test
    void deleteTaskByIdAndCheckHistory() {
        manager.addTask(task1);// id = 0
        manager.addTask(task2);// id = 1
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        HistoryManager historyManager = manager.getHistoryManager();
        assertEquals(List.of(task1, task2), historyManager.getAll(),
                "Задачи не были добавлены в историю просмотров");


        assertEquals(2, manager.getTasksAsList().size(), "Задачи не были добавлены в список!");
        manager.deleteTaskById(task1.getId());
        assertNull(manager.getTaskById(0), "Задача всё ещё в списке!");

        historyManager = manager.getHistoryManager();
        assertEquals(List.of(task2), historyManager.getAll(),
                "Задачи не были добавлены в историю просмотров");
    }

    @Test
    void deleteSubtaskByIdAndCheckHistory() {
    //
    }

    @Test
    void deleteEpicByIdAndCheckHistory() {
        // Эпик должен удалиться из map`ы эпиков
        // Должны удалиться все подзадачи из map`ы подзадач
        // Эпик должен удалиться из истории просмотров

        manager.addEpic(epic);// id = 0
        manager.addSubtask(subtaskA);// id = 1
        manager.addSubtask(subtaskB);// id = 2
        assertEquals(2, manager.getSubtasksAsList().size(), "Подзадачи не добавлены в map подзадач");

        manager.deleteEpicById(epic.getId());
        assertEquals(0, manager.getSubtasksAsList().size(), "Подзадачи не удалены из map подзадач");
        assertEquals(0, manager.getEpicsAsList().size(), "Эпик все еще в map эпиков");
    }
}