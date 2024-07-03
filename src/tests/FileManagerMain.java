package tests;

import taskManager.memory.FileBackedTaskManager;
import tasksModels.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tasksModels.Status.*;

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
        System.out.println("Приоритетный список загруженного менеджера такой же, как и у оригинального?" +
                assertEquals(new ArrayList<>(fileManager.getPrioritizedTasks()),
                             new ArrayList<>(fileManagerReload.getPrioritizedTasks())));

        System.out.println("Приоритетный список оригинального менеджера:");
        System.out.println(fileManager.getPrioritizedTasks());
        System.out.println("Приоритетный список менеджера загруженного из файла:");
        System.out.println(fileManagerReload.getPrioritizedTasks());
    }

    private static FileBackedTaskManager getFileManager() throws IOException {
        FileBackedTaskManager fileManager = new FileBackedTaskManager();

        Task task1 = new Task("Имя Задачи1", "Описание задачи1", NEW,
                LocalDateTime.of(2022, 12, 13, 12, 45), 15);
        fileManager.createTask(task1);

        Task task2 = new Task("Имя Задачи2", "Описание задачи2", NEW,
                LocalDateTime.of(2023, 2, 27, 2, 15), 200);
        fileManager.createTask(task2);

        Epic epic1 = new Epic("Эпик с тремя подзадачами", "Разделяется на 3 подзадачи", NEW);
        fileManager.createEpic(epic1);

        Subtask subtask1_1 = new Subtask("Подзадача 1.1", "Subtask1 for epic1", NEW, epic1.getId(),
                LocalDateTime.of(2021, 2, 27, 1, 50), 50);
        fileManager.createSubtask(subtask1_1);

        Subtask subtask1_2 = new Subtask("Подзадача 1.2", "Subtask2 for epic1", IN_PROGRESS, epic1.getId(),
                LocalDateTime.of(2023, 3, 27, 2, 15), 200);
        fileManager.createSubtask(subtask1_2);

        Subtask subtask1_3 = new Subtask("Подзадача 1.3", "Subtask3 for epic1", DONE, epic1.getId(),
                LocalDateTime.of(2025, 4, 26, 23, 35), 25);
        fileManager.createSubtask(subtask1_3);

        return fileManager;
    }

    private static boolean assertEquals(List<? extends Task> expected, List<? extends Task> actual) {
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
