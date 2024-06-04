package managers.memory_classes;

import managers.Managers;
import managers.custom_exceptions.ManagerSaveException;
import models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static models.TaskType.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path path;

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
        manager.init();
        return manager;
    }

    private void init() { loadFromFile(); }

    private void loadFromFile() {
        // ищем максимальный id, чтобы при добавлении новых задач в файл не повторялись id (см. реализацию generateId())
        int maxId = 0;
        try (final BufferedReader reader = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))){
            reader.readLine();
            while (reader.ready()) {
                String line = reader.readLine();
                // Добавляем все задачи в таблицу
                final Task task = fromString(line);
                final int id = task.getId();
                if (task.getType() == TASK) {
                    tasks.put(id, task);
                } else if (task.getType() == EPIC) {
                    epics.put(id, (Epic) task);
                } else if (task.getType() == SUBTASK) {
                    subtasks.put(id, (Subtask) task);
                }
                // Связываем подзадачи из таблицы со всеми эпиками
                for (Subtask subtask : subtasks.values()) {
                    addSubtaskIdAtEpic(subtask.getId());
                }
                // Находим максимальный id
                if (id > maxId) maxId = id;
            }
            super.setId(maxId);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при восстановлении менеджера из файла: " + e.getMessage());
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic(optional)\n");
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
            throw new ManagerSaveException("Ошибка в файле: " + path.getFileName());
        }
    }

    private static Task fromString(String line){
        if (line == null || line.isEmpty()) return null;
        String[] taskData = line.split(","); // [id,type,name,status,description,epic]

        int id = Integer.parseInt(taskData[0]);
        String name = taskData[2];
        Status status = Status.valueOf(taskData[3]);
        String description = taskData[4];
        TaskType type = TaskType.valueOf(taskData[1]);

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(taskData[5]);
                return new Subtask(id, name, description, epicId, status);

        }
        return null;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время записи задачи");
        }

    }
    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время записи эпика");
        }
    }
    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время записи подзадачи");
        }
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время обновления задачи");
        }
    }
    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время обновления подзадачи");
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время удаления всех задач");
        }
    }
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время удаления всех эпиков");
        }
    }
    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время удаления всех подзадач");
        }
    }

    @Override
    public void deleteTaskById(int taskId) {
        super.deleteTaskById(taskId);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время удаления задачи по id");
        }
    }
    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время удаления эпика по id");
        }
    }
    @Override
    public void deleteSubtaskById(int subtaskId) {
        super.deleteSubtaskById(subtaskId);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println("Ошибка во время удаления подзадачи по id");
        }
    }


}
