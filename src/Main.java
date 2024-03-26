import managerUtil.Managers;
import managers.*;
import taskModels.*;

import static taskModels.Status.*;

public class Main {
    public static void main(String[] args) {
        test();
    }

    private static void test() {
        Managers manager = new Managers();

        InMemoryTaskManager taskManager =  manager.getDefaultTasks();

        Task task1 = new Task("Создание Мобильного Приложения", "Разработка интерфейса, " +
                "Разработка пользовательских сценариев " +
                "Создание прототипа приложения", NEW);
        taskManager.addTask(task1);
        Epic epic1 = new Epic("Разработка интерфейса", "Разделяется на 3 подзадачи", NEW);
        taskManager.addEpic(epic1);
        Subtask subtask1point1 = new Subtask("Подзадача 1.1", "Дизайн пользовательского интерфейса", epic1.getId(), NEW);
        Subtask subtask1point2 = new Subtask("Подзадача 1.2", "Разработка пользовательских сценариев", epic1.getId(), NEW);
        Subtask subtask1point3 = new Subtask("Подзадача 1.3", "Создание прототипа приложения", epic1.getId(), NEW);
        taskManager.addSubtask(subtask1point1);
        taskManager.addSubtask(subtask1point2);
        taskManager.addSubtask(subtask1point3);
        Epic epic2 = new Epic("Реализация функционала","Разделяется на 2 подзадачи", NEW);
        taskManager.addEpic(epic2);
        Subtask subtask2point1 = new Subtask("Подзадача 2.1", "Определение основных функций приложения", epic2.getId(), NEW);
        Subtask subtask2point2 = new Subtask("Подзадача 2.2", "Разработка бэкенда приложения", epic2.getId(), NEW);
        taskManager.addSubtask(subtask2point1);
        taskManager.addSubtask(subtask2point2);

        //История просмотров
        taskManager.getTaskById(0);
        taskManager.getSubtaskById(4);
        taskManager.getEpicById(1);

        taskManager.printAllTasks(taskManager);

    }
}

