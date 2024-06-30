package taskManager.memory;

import managersCreator.Managers;
import taskManager.exceptions.ManagerIOException;
import tasksModels.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path path;

    public static final String TASK_CSV = "resources/tasks.csv";

    public FileBackedTaskManager() {
        this(Managers.getDefaultHistory());
    }
    public FileBackedTaskManager(InMemoryHistoryManager historyManager) {
        this(historyManager, Path.of(TASK_CSV));
    }
    public FileBackedTaskManager(Path path) {
        this(Managers.getDefaultHistory(), path);
    }
    public FileBackedTaskManager(InMemoryHistoryManager historyManager, Path path) {
        super(historyManager);
        this.path = path;
    }


    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        manager.loadFromFile();
        return manager;
    }

    private void loadFromFile() {
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))) {
            reader.readLine(); // Пропускаем заголовок

            while (reader.ready()) {
                String line = reader.readLine();
                Task task = fromString(line);
                maxId = Math.max(maxId, task.getId());
                addTaskToManager(task);
            }

            id = maxId;
        } catch (ManagerIOException | IOException e) {
            throw new RuntimeException("Ошибка при восстановлении менеджера из файла: " + e.getMessage());
        }

        linkSubtasksToEpics();
        updatePrioritizedList();
    }

    private void addTaskToManager(Task task) {
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
            epics.put(epic.getId(), epic);
        }
    }


    private void updatePrioritizedList() {
        prioritizedTasks.addAll(tasks.values());
        prioritizedTasks.addAll(subtasks.values());
    }

    private static Task fromString(String line) throws ManagerIOException{
        if (line == null || line.isEmpty()) throw new ManagerIOException("Строка не может быть пустой!");
        String[] taskData = line.split(","); // [id,type,name,status,description,epic]

        int id = Integer.parseInt(taskData[0]);
        TaskType type = TaskType.valueOf(taskData[1]);
        String name = taskData[2];
        Status status = Status.valueOf(taskData[3]);
        String description = taskData[4];
        int duration;
        try {
            duration = Integer.parseInt(taskData[6]);
        } catch (NumberFormatException e) {
            duration = 0;
        }
        LocalDateTime startTime = LocalDateTime.parse(taskData[7]);

        return switch (type) {
            case TASK -> new Task(id, name, description, status, startTime, duration);
            case EPIC -> new Epic(id, name, description, status, startTime, duration);
            case SUBTASK -> {
                int epicId = Integer.parseInt(taskData[5]);
                yield new Subtask(id, name, description, status, epicId, startTime, duration);
            }
        };
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic(optional),duration,startTime\n");
            for (Map.Entry<Integer,Task> taskEntry : tasks.entrySet()) {
                writer.append(taskEntry.getValue().toString());
            }
            for (Map.Entry<Integer,Epic> epicEntry : epics.entrySet()) {
                writer.append(epicEntry.getValue().toString());
            }
            for (Map.Entry<Integer,Subtask> subtaskEntry : subtasks.entrySet()) {
                writer.append(subtaskEntry.getValue().toString());
            }
        } catch (IOException e) {
            throw new ManagerIOException("Ошибка в файле: " + path.getFileName());
        }
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи задачи ");
        }

    }
    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи эпика");
        }
    }
    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи подзадачи");
        }
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
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
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
