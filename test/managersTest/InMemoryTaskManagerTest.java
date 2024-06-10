package managersTest;

import managers.memory_classes.InMemoryTaskManager;
import managers.interfaces.HistoryManager;
import managers.Managers;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static models.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    static InMemoryTaskManager taskManager =  Managers.getDefault();
    Task task1 =
            new Task("Создание Мобильного Приложения", "Разработка интерфейса", NEW);
    Task task2 =
            new Task("Новая задача", "Описание новой задачи", NEW);
    Epic epic =
            new Epic("Разработка интерфейса", "Разделяется на 3 подзадачи", NEW);
    Subtask subtaskA =
            new Subtask("Подзадача 1", "Дизайн пользовательского интерфейса", epic.getId(), NEW);
    Subtask subtaskB =
            new Subtask("Подзадача 2", "Разработка пользовательских сценариев", epic.getId(), NEW);



    @Test
    void addTask() { //Тест создания задачи.
        taskManager.addTask(task1);

        final Task savedTask = taskManager.getTaskById(task1.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasksAsList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.getFirst(), "Задачи не совпадают.");
    }
    @Test
    void addEpic() { //Тест создания эпика.
        taskManager.addEpic(epic);

        final Epic savedEpic = taskManager.getEpicById(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpicsAsList();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }
    @Test
    void addSubtask() { //Тест создания подзадачи.
        taskManager.addEpic(epic); // Подзадача не может существовать без эпика (логично)
        taskManager.addSubtask(subtaskA);

        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskA.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtaskA, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasksAsList();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtaskA, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    void updateTask() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        // Хотим обновить ВТОРУЮ задачу (с id = 1)
        Task newTask = new Task("Обновленная задача","Какое-то описание", IN_PROGRESS);
        newTask.setId(task2.getId());
        taskManager.updateTask(newTask);

        final List<Task> tasks = taskManager.getTasksAsList();
        assertEquals( 2, tasks.size() , "Задач больше (меньше), чем должно быть");
        assertEquals(tasks.getFirst().getStatus(), NEW, "У первой задачи статус не должен меняться");
        assertEquals(tasks.get(1).getStatus(), IN_PROGRESS, "У второй задачи статус должен был поменяться");
        assertEquals(tasks.get(task2.getId()),
                tasks.get(newTask.getId()),
                "ID старой и новой задачи должны совпадать!");
    }

    @Test
    void updateEpicSubtask() {
        //создаем эпик NEW и 2 подзадачи к нему NEW
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtaskA);
        taskManager.addSubtask(subtaskB);
        final List<Subtask> subtasksBeforeUpdate = taskManager.getSubtasksAsList();
        assertEquals(NEW, taskManager.getEpicsAsList().getFirst().getStatus(),"Статус эпика должен быть NEW");
        assertEquals(NEW, subtasksBeforeUpdate.getFirst().getStatus(),"Статус подзадачи A должен быть NEW!");
        assertEquals(NEW, subtasksBeforeUpdate.getLast().getStatus(), "Статус подзадачи B должен быть NEW!");

        // Обновляем статус подзадачи A на IN_PROGRESS => статус эпика меняется на IN_PROGRESS
        Subtask subtaskAInProgress = new Subtask("&!#!@#", "***", epic.getId(), IN_PROGRESS);
        subtaskAInProgress.setId(subtaskA.getId());
        taskManager.updateSubtask(subtaskAInProgress);

        final List<Subtask> subtasksAfterFirstUpdate = taskManager.getSubtasksAsList();
        assertEquals(IN_PROGRESS, taskManager.getEpicsAsList().getFirst().getStatus(), "Статус эпика после обновления должен быть IN_PROGRESS");
        assertEquals(IN_PROGRESS, subtasksAfterFirstUpdate.getFirst().getStatus(), "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(NEW, subtasksAfterFirstUpdate.getLast().getStatus(), "Статус подзадачи B должен быть NEW!");

        //Обновляем статус подзадачи A и B на DONE => статус эпика меняется на DONE
        Subtask subtaskADone = new Subtask("Done A Subtask", "some description a", epic.getId(), DONE);
        Subtask subtaskBDone = new Subtask("Done B Subtask", "some description b", epic.getId(), DONE);
        subtaskADone.setId(subtaskA.getId());
        subtaskBDone.setId(subtaskB.getId());
        taskManager.updateSubtask(subtaskADone);
        taskManager.updateSubtask(subtaskBDone);
        final List<Subtask> subtasksAfterSecondUpdate = taskManager.getSubtasksAsList();
        assertEquals(DONE, taskManager.getEpicsAsList().get(epic.getId()).getStatus(), "Статус эпика после обновления должен быть DONE");
        assertEquals(DONE, subtasksAfterSecondUpdate.getFirst().getStatus(), "Статус подзадачи A должен быть DONE!");
        assertEquals(DONE, subtasksAfterSecondUpdate.getLast().getStatus(), "Статус подзадачи B должен быть DONE!");

    }

    @Test
    void deleteAllTasksAndCheckHistory() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        assertEquals(List.of(task1, task2), taskManager.getHistoryManager().getAll(),
                "Задачи не добавились в историю просмотров");

        assertNotNull(taskManager.getTasksAsList(), "Задач нет в списке!");
        taskManager.deleteAllTasks();
        assertEquals(0, taskManager.getTasksAsList().size(), "Задачи не удалились из списка!");

        assertEquals(0, taskManager.getHistoryManager().getAll().size(),
                "Задачи не удалились из истории просмотров");
    }

    @Test
    void deleteAllEpicsAndCheckHistory() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtaskA);
        taskManager.addSubtask(subtaskB);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtaskA.getId());
        taskManager.getSubtaskById(subtaskB.getId());
        assertEquals(List.of(epic, subtaskA, subtaskB), taskManager.getHistoryManager().getAll(),
                "Задачи не были добавлены в историю просмотров");

        assertNotNull(taskManager.getEpicsAsList(), "Задач нет в списке!");
        assertNotNull(taskManager.getSubtasksAsList(), "Подзадач нет в списке!");

        taskManager.deleteAllEpics();
        assertEquals(0, taskManager.getEpicsAsList().size(),
                "Эпики не удалились из списка!");
        assertEquals(0, taskManager.getSubtasksAsList().size(),
                "Подзадачи не удалились из списка вместе с эпиками!");

        assertEquals(0, taskManager.getHistoryManager().getAll().size(),
                "Эпики с подзадачами не удалились из истории просмотров");
    }

    @Test
    void deleteAllSubtasksAndCheckHistory() {

        taskManager.addEpic(epic);
        taskManager.addSubtask(subtaskA);
        taskManager.addSubtask(subtaskB);

        // Заполняем историю просмотров + получаем менеджер истории просмотров
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtaskA.getId());
        taskManager.getSubtaskById(subtaskB.getId());
        assertEquals(List.of(epic, subtaskA, subtaskB), taskManager.getHistoryManager().getAll(),
                "Эпики с подзадачами не были добавлены в историю просмотров");

        taskManager.deleteAllSubtasks();
        assertEquals(0, taskManager.getSubtasksAsList().size(), "Подзадачи не удалились из map подзадач!");
        assertEquals(1, taskManager.getEpicsAsList().size(), "Эпики не должны удаляться, " +
                "поскольку удаляются только подзадачи");

        assertEquals(List.of(epic), taskManager.getHistoryManager().getAll(), "Подзадачи не удалились из истории просмотров");
    }
    @Test
    void deleteTaskByIdAndCheckHistory() {
        taskManager.addTask(task1);// id = 0
        taskManager.addTask(task2);// id = 1
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        HistoryManager historyManager = taskManager.getHistoryManager();
        assertEquals(List.of(task1, task2), historyManager.getAll(),
                "Задачи не были добавлены в историю просмотров");


        assertEquals(2, taskManager.getTasksAsList().size(), "Задачи не были добавлены в список!");
        taskManager.deleteTaskById(task1.getId());
        assertNull(taskManager.getTaskById(0), "Задача всё ещё в списке!");

        historyManager = taskManager.getHistoryManager();
        assertEquals(List.of(task2), historyManager.getAll(),
                "Задачи не были добавлены в историю просмотров");
    }

    @Test
    void deleteSubtaskByIdAndCheckHistory() {
        taskManager.addEpic(epic);// id = 0
        taskManager.addSubtask(subtaskA);// id = 1
        taskManager.addSubtask(subtaskB);// id = 2


        //Подзадача должна удалиться из map подзадач и из списка id у эпика
        assertEquals(2, taskManager.getSubtasksAsList().size(), "Подзадачи не были добавлены в список!");


        taskManager.deleteSubtaskById(1);
        assertNull(taskManager.getSubtaskById(subtaskA.getId()), "Задача всё ещё в списке!");

        assertEquals(1, epic.getSubtaskIds().size(), "Не может быть две подзадачи у эпика " +
                "после удаления одной из них");
    }

    @Test
    void deleteEpicByIdAndCheckHistory() {
        // Эпик должен удалиться из map`ы эпиков
        // Должны удалиться все подзадачи из map`ы подзадач
        // Эпик должен удалиться из истории просмотров

        taskManager.addEpic(epic);// id = 0
        taskManager.addSubtask(subtaskA);// id = 1
        taskManager.addSubtask(subtaskB);// id = 2
        assertEquals(2, taskManager.getSubtasksAsList().size(), "Подзадачи не добавлены в map подзадач");

        taskManager.deleteEpicById(epic.getId());
        assertEquals(0, taskManager.getSubtasksAsList().size(), "Подзадачи не удалены из map подзадач");
        assertEquals(0, taskManager.getEpicsAsList().size(), "Эпик все еще в map эпиков");
    }
}