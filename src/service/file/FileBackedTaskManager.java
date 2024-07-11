package service.file;

import service.ManagersCreator;
import service.exceptions.ManagerIOException;
import model.*;
import service.history.InMemoryHistoryManager;
import service.memory.InMemoryTaskManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path path;

    public static final String TASK_CSV = "resources/tasks.csv";

    public FileBackedTaskManager() {
        this(ManagersCreator.getDefaultHistory());
    }
    public FileBackedTaskManager(InMemoryHistoryManager historyManager) {
        this(historyManager, Path.of(TASK_CSV));
    }
    public FileBackedTaskManager(Path path) {
        this(ManagersCreator.getDefaultHistory(), path);
    }
    public FileBackedTaskManager(InMemoryHistoryManager historyManager, Path path) {
        super(historyManager);
        this.path = path;
    }

    /**
     *
     * @param path - ресурс для чтения
     * @return
     */
    public static FileBackedTaskManager loadManagerFromFile(Path path) {
        FileBackedTaskManager fileManager = new FileBackedTaskManager(path);
        fileManager.loadManagerFromFile();
        return fileManager;
    }

    private void loadManagerFromFile() {
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))) {
            reader.readLine(); // Пропускаем заголовок

            while (reader.ready()) {
                String line = reader.readLine();
                Task task = getTaskFromFileString(line);
                maxId = Math.max(maxId, task.getId());
                addTaskToManagerMemory(task);
            }
            managerId = maxId;
        } catch (ManagerIOException | IOException e) {
            throw new RuntimeException("Ошибка при восстановлении менеджера из файла: " + e.getMessage());
        }

        linkSubtasksToEpics();
        updatePrioritizedList();
    }

    private void addTaskToManagerMemory(Task task) {
        switch (task.getType()) {
            case TASK:
                tasks.put(task.getId(), task);
                break;
            case EPIC:
                epics.put(task.getId(), (Epic) task);
                break;
            case SUBTASK:
                subtasks.put(task.getId(), (Subtask) task);
                break;
        }
    }

    private void linkSubtasksToEpics() {
        for (Subtask subtask : subtasks.values()) {
            Epic epic = getEpicBySubtask(subtask);
            epic.addTask(subtask);
            epic.configureEpic();
            epics.put(epic.getId(), epic);
        }
    }


    private void updatePrioritizedList() {
        prioritizedTasks.addAll(tasks.values());
        prioritizedTasks.addAll(subtasks.values());
    }

    /**
     * Метод десериализует строку из  файла в объект класса Task. В зависимости от первого параметра (TaskType) выводится
     * объект конкретного класса (Task, Subtask или Epic)
     * <p>Элементом строки считаются данные, разделеенные запятой в строковом представлении объектов типа
     * {@code <? extends Task>}
     * @param line - строка, которую нужно десериализовать в Task имеет вид:
     * "0-Type, 1-(Optional) id, 2-name, 3-description, 4-status, 5-startTime, 6-durationInMinutes \n

     * @return {@code Epic}, если первый , полученную из строки с информацией о задаче, записаной в том виде, в котором
     * определен метод toString() у класса Task, Epic или Subtask.
     * @throws ManagerIOException
     */
    private static Task getTaskFromFileString(String line) throws ManagerIOException {
        if (line == null || line.isEmpty()) throw new ManagerIOException("Строка не может быть пустой!");
        String[] taskData = line.split(",");
        TaskType type = TaskType.valueOf(taskData[0]);
        int id;
        try {
            id = Integer.parseInt(taskData[1]);
        } catch (NumberFormatException e) {
            throw new ManagerIOException("Недопустимый формат Id для задачи = " + taskData[1]);
        }
        String name = taskData[2];
        String description = taskData[3];
        Status status = Status.valueOf(taskData[4]);
        LocalDateTime startTime = LocalDateTime.parse(taskData[5]);
        int duration;
        try {
            duration = Integer.parseInt(taskData[6]);
        } catch (NumberFormatException e) {
            duration = 0;
        }
        switch (type) {
            case TASK:
                return new Task(id, name, description, status, startTime, duration);
            case EPIC:
                return new Epic(id, name, description, status, startTime, duration);
            case SUBTASK:
                int epicId = Integer.parseInt(taskData[7]);
                return new Subtask(id, name, description, status, startTime, duration, epicId);
            default:
                throw new ManagerIOException();
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), StandardCharsets.UTF_8))) {
            writer.write("type,id,name,status,description,startTime,duration,epicId(for subtasks)\n");
            for (Task taskFromManager : tasks.values()) {
                writer.append(taskFromManager.toString());
            }
            for (Epic epicFromManager : epics.values()) {
                writer.append(epicFromManager.toString());
            }
            for (Subtask subtaskFromManager : subtasks.values()) {
                writer.append(subtaskFromManager.toString());
            }
        } catch (IOException e) {
            throw new ManagerIOException("Ошибка в файле: " + path.getFileName());
        }
    }

    public Path getPath() {
        return path;
    }

    @Override
    public Task createTask(Task task) {
        Task addedTask = super.createTask(task);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи задачи ");
        }
        return addedTask;
    }
    @Override
    public Epic createEpic(Epic epic) {
        Epic addedEpic = super.createEpic(epic);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи эпика");
        }
        return addedEpic;
    }
    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask addedSubtask = super.createSubtask(subtask);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи подзадачи");
        }
        return addedSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время обновления задачи");
        }
    }
    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время обновления подзадачи");
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время удаления всех задач");
        }
    }
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время удаления всех эпиков");
        }
    }
    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время удаления всех подзадач");
        }
    }

    @Override
    public void deleteTaskById(int taskId) {
        super.deleteTaskById(taskId);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время удаления задачи по id");
        }
    }
    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время удаления эпика по id");
        }
    }
    @Override
    public void deleteSubtaskById(int subtaskId) {
        super.deleteSubtaskById(subtaskId);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время удаления подзадачи по id");
        }
    }

}
