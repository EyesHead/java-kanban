package tests;

import model.*;
import service.file.FileBackedTaskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static model.Status.*;

public class FileManagerMain {
    public static void main(String[] args) {
        test();
    }

    private static void test() {

        FileBackedTaskManager fileManager = getFileManager();

        FileBackedTaskManager fileManagerReload = FileBackedTaskManager.loadManagerFromFile(fileManager.getPath());

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

    private static FileBackedTaskManager getFileManager() {
        FileBackedTaskManager fileManager = new FileBackedTaskManager();

        Task task1ToAdd = new Task(null,"Имя Задачи1", "Описание задачи1", NEW,
                LocalDateTime.of(2022, 12, 13, 12, 45), 15);
        fileManager.createTask(task1ToAdd);

        Task task2ToAdd = new Task(null,"Имя Задачи2", "Описание задачи2", NEW,
                LocalDateTime.of(2023, 2, 27, 2, 15), 200);
        fileManager.createTask(task2ToAdd);

        Epic epic1ToAdd = new Epic(null,"Эпик с тремя подзадачами", "Разделяется на 3 подзадачи", NEW,
                LocalDateTime.now(), 0);
        Epic epic1Added = fileManager.createEpic(epic1ToAdd);

        Subtask subtask1ToAdd = new Subtask(null, "Подзадача 1.1", "Subtask1 for epic1Added", NEW,
                LocalDateTime.of(2021, 2, 27, 1, 50), 50, epic1Added.getId());
        fileManager.createSubtask(subtask1ToAdd);

        Subtask subtask2ToAdd = new Subtask(null,"Подзадача 1.2", "Subtask2 for epic1Added", IN_PROGRESS,
                LocalDateTime.of(2023, 3, 27, 2, 15), 200, epic1Added.getId());
        fileManager.createSubtask(subtask2ToAdd);

        Subtask subtask3ToAdd = new Subtask(null,"Подзадача 1.3", "Subtask3 for epic1Added", DONE,
                LocalDateTime.of(2025, 4, 26, 23, 35), 25, epic1Added.getId());
        fileManager.createSubtask(subtask3ToAdd);

        return fileManager;
    }

    private static boolean assertEquals(List<? extends Task> expected, List<? extends Task> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }

        for (int i = 0; i < expected.size(); i++) {
            Task expectedTask = expected.get(i);
            Task actualTask = actual.get(i);

            if (!expectedTask.equals(actualTask)) {
                return false;
            }

            if (!expectedTask.getName().equals(actualTask.getName()) ||
                    !expectedTask.getDescription().equals(actualTask.getDescription()) ||
                    !expectedTask.getStatus().equals(actualTask.getStatus()) ||
                    !expectedTask.getStartTime().equals(actualTask.getStartTime()) ||
                    !expectedTask.getEndTime().equals(actualTask.getEndTime()) ||
                    !expectedTask.getDurationInMinutes().equals(actualTask.getDurationInMinutes()) ||
                    !expectedTask.getType().equals(actualTask.getType())) {
                return false;
            }
        }

        return true;
    }
}
