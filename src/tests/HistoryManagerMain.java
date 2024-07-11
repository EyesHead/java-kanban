package tests;

import service.file.FileBackedTaskManager;
import service.ManagersCreator;
import model.Epic;
import model.Subtask;
import model.Task;
import tests.util.LDTRandomizer;

import static model.Status.NEW;

public class HistoryManagerMain {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        FileBackedTaskManager taskManager =  ManagersCreator.getDefaultFileManager();

        Task taskToAdd1 = new Task(null,"Имя Задачи1", "Описание задачи1", NEW,
                LDTRandomizer.getRandomLDT(), 120);
        Task addedTask1 = taskManager.createTask(taskToAdd1);

        Task taskToAdd2 = new Task(null,"Имя Задачи2", "Описание задачи2", NEW,
                LDTRandomizer.getRandomLDT(), 60);
        Task addedTask2 = taskManager.createTask(taskToAdd2);

        Epic epicToAdd = new Epic(null,"Эпик с тремя подзадачами", "Разделяется на 3 подзадачи", NEW, null,0);
        Epic addedEpic = taskManager.createEpic(epicToAdd);
        Subtask subtask1ToAdd = new Subtask(null,"Подзадача 1.1", "Subtask1 for epic1", NEW,
                LDTRandomizer.getRandomLDT(), 50, addedEpic.getId());
        Subtask subtask2ToAdd = new Subtask(null,"Подзадача 1.2", "Subtask2 for epic1", NEW,
                LDTRandomizer.getRandomLDT(), 20, addedEpic.getId());
        Subtask subtask3ToAdd = new Subtask(null,"Подзадача 1.3", "Subtask3 for epic1", NEW,
                LDTRandomizer.getRandomLDT(), 100, addedEpic.getId());

        Subtask addedSubtask1 = taskManager.createSubtask(subtask1ToAdd);
        Subtask addedSubtask2 = taskManager.createSubtask(subtask2ToAdd);
        Subtask addedSubtask3 = taskManager.createSubtask(subtask3ToAdd);


        Epic epicToAdd2 = new Epic(0,"Эпик без подзадач","Тут нет подзадач", NEW, null, 0);
        Epic addedEpic2 = taskManager.createEpic(epicToAdd2);


        //История просмотров
        taskManager.getEpicById(addedEpic.getId());
        taskManager.getEpicById(addedEpic2.getId());
        taskManager.printAllHistory();

        System.out.println();
        System.out.println("Добавим ещё задачи, в том числе дубликаты (их отобразиться не должно)");
        taskManager.getTaskById(addedTask1.getId());
        taskManager.getEpicById(addedEpic2.getId());
        taskManager.getTaskById(addedTask2.getId());
        taskManager.getEpicById(addedEpic2.getId());
        taskManager.printAllHistory();

        System.out.println();
        System.out.println("Добавим ещё подзадачи, при чём по несколько раз одни и те же" +
                ", дубликатов быть всё так же не может");
        taskManager.getSubtaskById(addedSubtask1.getId());
        taskManager.getSubtaskById(addedSubtask3.getId());
        taskManager.getSubtaskById(addedSubtask2.getId());
        taskManager.getSubtaskById(addedSubtask2.getId());
        taskManager.getSubtaskById(addedSubtask1.getId());
        taskManager.getSubtaskById(addedSubtask1.getId());
        taskManager.getEpicById(addedEpic.getId());
        taskManager.printAllHistory();

    }


}

