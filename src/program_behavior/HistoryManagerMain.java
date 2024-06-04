package program_behavior;

import managers.memory_classes.InMemoryTaskManager;
import managers.Managers;
import models.Epic;
import models.Subtask;
import models.Task;

import static models.Status.NEW;

public class HistoryManagerMain {
    public static void main(String[] args) {
        test();
    }

    private static void test() {

        InMemoryTaskManager taskManager =  Managers.getDefault();

        Task task1 = new Task("Имя Задачи1", "Описание задачи1", NEW);
        taskManager.addTask(task1);

        Task task2 = new Task("Имя Задачи2", "Описание задачи2", NEW);
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик с тремя подзадачами", "Разделяется на 3 подзадачи", NEW);
        taskManager.addEpic(epic1);
        Subtask subtask1_1 = new Subtask("Подзадача 1.1", "Subtask1 for epic1", epic1.getId(), NEW);
        Subtask subtask1_2 = new Subtask("Подзадача 1.2", "Subtask2 for epic1", epic1.getId(), NEW);
        Subtask subtask1_3 = new Subtask("Подзадача 1.3", "Subtask3 for epic1", epic1.getId(), NEW);
        taskManager.addSubtask(subtask1_1);
        taskManager.addSubtask(subtask1_2);
        taskManager.addSubtask(subtask1_3);


        Epic epic2 = new Epic("Эпик без подзадач","Тут нет подзадач", NEW);
        taskManager.addEpic(epic2);


        //История просмотров
        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());
        taskManager.printAllHistory();

        System.out.println();
        System.out.println("Добавим ещё задачи, в том числе дубликаты (их отобразиться не должно)");
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getEpicById(epic2.getId());
        taskManager.printAllHistory();

        System.out.println();
        System.out.println("Добавим ещё подзадачи, при чём по несколько раз одни и те же" +
                ", дубликатов быть всё так же не может");
        taskManager.getSubtaskById(subtask1_1.getId());
        taskManager.getSubtaskById(subtask1_3.getId());
        taskManager.getSubtaskById(subtask1_2.getId());
        taskManager.getSubtaskById(subtask1_2.getId());
        taskManager.getSubtaskById(subtask1_1.getId());
        taskManager.getSubtaskById(subtask1_1.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.printAllHistory();

        System.out.println();
        System.out.println("Удаляю все обычные задачи через deleteAllTasks()");
        taskManager.deleteAllTasks();
        taskManager.printAllHistory();
    }
}

