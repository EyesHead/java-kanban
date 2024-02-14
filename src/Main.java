import Controller.TaskManager;
import Models.Epic;
import Statuses.Status;
import Models.Subtask;
import Models.Task;

public class Main {
    public static void main(String[] args) {
        test();
    }

    private static void test() {
        TaskManager taskManager = new TaskManager();

        Task task = new Task("Задача", "Это описание задачи"); // id = 0
        Task task1 = new Task("Ещё одна задача", "Это описание для новой задачи"); // id = 1
        taskManager.createTask(task);
        taskManager.createTask(task1);

        Epic epic = new Epic("Эпик NEW", "Тут будут подзадачи с статусами NEW"); // id = 2
        Epic epicInProgress = new Epic("Эпик IN_PROGRESS", "Тут будут подзадачи с разными статусами");  // id = 3
        Epic epicDone = new Epic("Эпик DONE", "Тут будут подзадачи с статусами DONE");  // id = 4
        taskManager.createEpic(epic);
        taskManager.createEpic(epicInProgress);
        taskManager.createEpic(epicDone);

        Subtask subtask1 = new Subtask("Первая подзадача эпика NEW", "new", epic.getId()); // id = 5
        Subtask subtask2 = new Subtask("Вторая подзадача эпика NEW", " new", epic.getId()); // id = 6
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);


        Subtask subtask3= new Subtask("Первая подзадача эпика IN_PROGRESS",
                "NEW -> DONE", epicInProgress.getId()); // id = 7
        Subtask subtask4 = new Subtask("Вторая подзадача эпика IN_PROGRESS",
                "NEW -> In Progress", epicInProgress.getId()); // id = 8
        Subtask subtask5 = new Subtask("Третья подзадача эпика IN_PROGRESS",
                "new", epicInProgress.getId()); // id = 9
        taskManager.createSubtask(subtask3);
        taskManager.createSubtask(subtask4);
        taskManager.createSubtask(subtask5);

        Subtask subtask6 = new Subtask("Первая подзадача эпика DONE",  // id = 10
                "DONE", epicDone.getId(), Status.DONE);
        Subtask subtask7 = new Subtask("Вторая подзадача эпика DONE",  // id = 11
                "DONE", epicDone.getId(), Status.DONE);
        taskManager.createSubtask(subtask6);
        taskManager.createSubtask(subtask7);

        System.out.println("Получение списка всех задач");
        for (Task taskToPrint : taskManager.getAllTasks()) {
            System.out.println(taskToPrint);
        }
        System.out.println();
        for (Epic epicToPrint : taskManager.getAllEpics()) {
            System.out.println(epicToPrint);
        }
        System.out.println();
        for (Subtask subtaskToPrint : taskManager.getAllSubtasks()) {
            System.out.println(subtaskToPrint);
        }
        System.out.println();
        System.out.println("Получение задачи по Id = 1");
        System.out.println(taskManager.getTaskById(1));

        System.out.println();
        System.out.println("Удаление всех задач");
        taskManager.clearAllTasks();
        for (Task taskToPrint : taskManager.getAllTasks()) {
            System.out.println(taskToPrint);
        }
        System.out.println("Сверху ничего нет? Значит задач больше нет");
    }
}

