package program_behavior;

import managers.memory_classes.FileBackedTaskManager;
import models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static models.Status.*;

public class FileManagerMain {
    public static void main(String[] args) throws IOException {
        test();
    }

    private static void test() throws IOException {

        FileBackedTaskManager fileManager = getFileManager();

        FileBackedTaskManager fileManagerReload = FileBackedTaskManager.loadFromFile(fileManager.getPath());

        System.out.println("Все задачи, которые были в старом менеджере, есть в новом? Ответ " +
                assertEquals(fileManager.getTasksAsList(), fileManagerReload.getTasksAsList()));
        System.out.println("Все эпики, которые были в старом менеджере, есть в новом? Ответ " +
                assertEquals(fileManager.getEpicsAsList(), fileManagerReload.getEpicsAsList()));
        System.out.println("Все подзадачи, которые были в старом менеджере, есть в новом? Ответ " +
                assertEquals(fileManager.getSubtasksAsList(), fileManagerReload.getSubtasksAsList()));
    }

    private static FileBackedTaskManager getFileManager() throws IOException {
        FileBackedTaskManager fileManager = new FileBackedTaskManager();

        Task task1 = new Task(0,"Имя Задачи1", "Описание задачи1", NEW);
        fileManager.addTask(task1);

        Task task2 = new Task(0,"Имя Задачи2", "Описание задачи2", NEW);
        fileManager.addTask(task2);

        Epic epic1 = new Epic(0, "Эпик с тремя подзадачами", "Разделяется на 3 подзадачи", NEW);
        fileManager.addEpic(epic1);
        Subtask subtask1_1 = new Subtask("Подзадача 1.1", "Subtask1 for epic1", epic1.getId(), NEW);
        Subtask subtask1_2 = new Subtask("Подзадача 1.2", "Subtask2 for epic1", epic1.getId(), IN_PROGRESS);
        Subtask subtask1_3 = new Subtask("Подзадача 1.3", "Subtask3 for epic1", epic1.getId(), DONE);
        fileManager.addSubtask(subtask1_1);
        fileManager.addSubtask(subtask1_2);
        fileManager.addSubtask(subtask1_3);
        return fileManager;
    }

    private static boolean assertEquals(ArrayList<? extends Task> expected, ArrayList<? extends Task> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {

            int expectedId = expected.get(i).getId();
            int actualId = actual.get(i).getId();

            String expectedName = expected.get(i).getName();
            String actualName = actual.get(i).getName();

            String expectedDescription = expected.get(i).getDescription();
            String actualDescription = actual.get(i).getDescription();

            Status expectedStatus = expected.get(i).getStatus();
            Status actualStatus = actual.get(i).getStatus();

            TaskType expectedType = expected.get(i).getType();
            TaskType actualType = actual.get(i).getType();

            //проверка для сравнения задач и эпиков
            if (expectedType != actualType
                    || expectedId != actualId
                    || !Objects.equals(expectedName, actualName)
                    || !Objects.equals(expectedDescription, actualDescription)
                    || expectedStatus != actualStatus) {
                return false;
            }
            //проверка для сравнения подзадач
            if (expectedType == TaskType.SUBTASK) {
                if (!Objects.equals(expected.get(i).getEpicId(), actual.get(i).getEpicId())) {
                    return false;
                }
            }
        }
        return true;
    }
}
