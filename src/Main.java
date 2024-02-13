import java.util.Scanner;
public class Main {
    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        // создание
        Task task = new Task("Задача", "Это описание задачи");
        Task task1 = new Task("Ещё одна задача", "Это описание для новой задачи");
        taskManager.createTask(task);
        taskManager.createTask(task1);

        Epic epic = new Epic("Эпик NEW", "Тут будут подзадачи с статусами NEW");
        Epic epicInProgress = new Epic("Эпик IN_PROGRESS", "Тут будут подзадачи с разными статусами");
        Epic epicDone = new Epic("Эпик DONE", "Тут будут подзадачи с статусами DONE");


        taskManager.createEpic(epic);
        taskManager.createEpic(epicInProgress);
        taskManager.createEpic(epicDone);

        Subtask subtask = new Subtask("подзадача эпика NEW", "new", epic.getID());
        Subtask subtask1 = new Subtask("Ещё одна подзадача эпика NEW", " new", epic.getID());
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);


        Subtask subtask2 = new Subtask("подзадача эпика IN_PROGRESS", "NEW -> DONE", epicInProgress.getID());
        Subtask subtask3 = new Subtask("Ещё одна подзадача эпика IN_PROGRESS", "NEW -> In Progress", epicInProgress.getID());
        Subtask subtask4 = new Subtask("Ещё одна подзадача эпика IN_PROGRESS", "new", epicInProgress.getID());
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        taskManager.createSubtask(subtask4);

        Subtask subtask5 = new Subtask("подзадача эпика DONE", "DONE", epic.getID(), Status.DONE);
        Subtask subtask6 = new Subtask("Ещё одна подзадача эпика DONE", "DONE", epic.getID(), Status.DONE);
        taskManager.createSubtask(subtask5);
        taskManager.createSubtask(subtask6);

        //НЕ ЗАВЕРШЕН
    }
}


    //        "1. Получение списка всех задач."
    //        "2. Удаление всех задач."
    //        "3. Получение по идентификатору."
    //        "4. Создание. Сам объект должен передаваться в качестве параметра."
    //        "5 .Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра."
    //        "6. Удаление по идентификатору."
    //        "7. Получение списка всех подзадач определённого эпика (по ID эпика)."

