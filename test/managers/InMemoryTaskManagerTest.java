package managers;

import managers.util.Managers;
import org.junit.jupiter.api.Test;
import models.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static models.Status.*;

class InMemoryTaskManagerTest {
    static Managers managers = new Managers();
    static InMemoryTaskManager taskManager =  managers.getDefaultTasks();
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

        final List<Task> tasks = taskManager.getTasks();

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

        final List<Epic> epics = taskManager.getEpics();

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

        final List<Subtask> subtasks = taskManager.getSubtasks();

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

        final List<Task> tasks = taskManager.getTasks();
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
        final List<Subtask> subtasksBeforeUpdate = taskManager.getSubtasks();
        assertEquals(NEW, taskManager.getEpics().getFirst().getStatus(), "Статус эпика должен быть NEW");
        assertEquals(NEW, subtasksBeforeUpdate.getFirst().getStatus(), "Статус подзадачи A должен быть NEW!");
        assertEquals(NEW, subtasksBeforeUpdate.getLast().getStatus(), "Статус подзадачи B должен быть NEW!");

        // Обновляем статус подзадачи A на IN_PROGRESS => статус эпика меняется на IN_PROGRESS
        Subtask subtaskAInProgress = new Subtask("&!#!@#", "***", epic.getId(), IN_PROGRESS);
        subtaskAInProgress.setId(subtaskA.getId());
        taskManager.updateEpicSubtask(subtaskAInProgress);

        final List<Subtask> subtasksAfterFirstUpdate = taskManager.getSubtasks();
        assertEquals(IN_PROGRESS, taskManager.getEpics().getFirst().getStatus(), "Статус эпика после обновления должен быть IN_PROGRESS");
        assertEquals(IN_PROGRESS, subtasksAfterFirstUpdate.getFirst().getStatus(), "Статус подзадачи A должен быть IN_PROGRESS!");
        assertEquals(NEW, subtasksAfterFirstUpdate.getLast().getStatus(), "Статус подзадачи B должен быть NEW!");

        //Обновляем статус подзадачи A и B на DONE => статус эпика меняется на DONE
        Subtask subtaskADone = new Subtask("Done A Subtask", "some description a", epic.getId(), DONE);
        Subtask subtaskBDone = new Subtask("Done B Subtask", "some description b", epic.getId(), DONE);
        subtaskADone.setId(subtaskA.getId());
        subtaskBDone.setId(subtaskB.getId());
        taskManager.updateEpicSubtask(subtaskADone);
        taskManager.updateEpicSubtask(subtaskBDone);
        final List<Subtask> subtasksAfterSecondUpdate = taskManager.getSubtasks();
        assertEquals(DONE, taskManager.getEpics().get(epic.getId()).getStatus(), "Статус эпика после обновления должен быть DONE");
        assertEquals(DONE, subtasksAfterSecondUpdate.getFirst().getStatus(), "Статус подзадачи A должен быть DONE!");
        assertEquals(DONE, subtasksAfterSecondUpdate.getLast().getStatus(), "Статус подзадачи B должен быть DONE!");

    }

    @Test
    void deleteAllTasks() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        assertNotNull(taskManager.getTasks(), "Задач нет в списке!");
        taskManager.deleteAllTasks();
        assertEquals(0, taskManager.getTasks().size(), "Задачи не удалились из списка!");
    }

    @Test
    void deleteAllEpics() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtaskA);
        taskManager.addSubtask(subtaskB);
        assertNotNull(taskManager.getEpics(), "Задач нет в списке!");
        assertNotNull(taskManager.getSubtasks(), "Подзадач нет в списке!");
        taskManager.deleteAllEpics();
        assertEquals(0, taskManager.getEpics().size(), "Эпики не удалились из списка!");
        assertEquals(0, taskManager.getSubtasks().size(), "Подзадачи не удалились из списка вместе с эпиками!");
    }

    @Test
    void deleteAllSubtasks() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtaskA);
        taskManager.addSubtask(subtaskB);

        taskManager.deleteAllSubtasks();
        assertEquals(0, taskManager.getSubtasks().size(), "Подзадачи не удалились из списка!");
        assertEquals(1, taskManager.getEpics().size(), "Эпики не должны удаляться, " +
                "поскольку удаляются только подзадачи");
    }
    @Test
    void deleteTaskById() {
        taskManager.addTask(task1);// id = 0
        taskManager.addTask(task2);// id = 1
        assertEquals(2, taskManager.getTasks().size(), "Задачи не были добавлены в список!");
        taskManager.deleteTaskById(task1.getId());
        assertNull(taskManager.getTaskById(0), "Задача всё ещё в списке!");
    }
}