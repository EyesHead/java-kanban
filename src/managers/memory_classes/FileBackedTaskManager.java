package managers.memory_classes;

import managers.Managers;
import managers.custom_exceptions.ManagerIOException;
import models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static models.TaskType.EPIC;
import static models.TaskType.TASK;

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
        /*
        1) ЗАГРУЖАЕМЫЙ ФАЙЛ МОЖЕТ ВЫГЛЯДЕТЬ СЛЕДУЩИМ ОБРАЗОМ
        id = 0 TASK DATA
        id = 5 SUBTASK DATA
        id = 1 EPIC DATA
        и т.д, если добавлять задачи одну за другой, то они получат id по порядку, так как методы addTask, addSubtask,
        addEpic генерируют id по порядку
        Решение:
        Сначала задачи записываются в TreeSet<Task> allTasks, который сортирует их по id, только после этого
        отсортированные задачи добавляются в менеджер в нужном порядке

        2) Ищем максимальный id, чтобы при добавлении новых задач в файл после загрузки из файла
        у новых добавляемых задач id не повторялись со старыми (см. реализацию generateId())
         */
        int maxId = 0;
        try (final BufferedReader reader = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))){
            reader.readLine();
            while (reader.ready()) {
                String line = reader.readLine();

                final Task task = fromString(line);
                maxId = Math.max(maxId, task.getId());

                    switch (task.getType()) {
                        case TASK:
                            tasks.put(task.getId(), task);
                            continue;
                        case EPIC:
                            epics.put(task.getId(), (Epic) task);
                            continue;
                        case SUBTASK:
                            subtasks.put(task.getId(), (Subtask) task);
                            continue;
                    }
            }
            id = maxId;
        } catch (ManagerIOException | IOException e) {
            throw new RuntimeException("Ошибка при восстановлении менеджера из файла: " + e.getMessage());
        }

        // Связываем подзадачи из таблицы со всеми эпиками и обновляем статус у эпиков
        for (Subtask subtask : subtasks.values()) {
            Epic newEpic = getEpicBySubtask(subtask);
            newEpic.addTask(subtask); // тут происходит перерасчет статуса, времени и длительности для Epic
            epics.put(newEpic.getId(), newEpic);
        }

        updatePrioritizedList();

    }

    private void updatePrioritizedList() {
        prioritizedTasks.addAll(tasks.values());
        prioritizedTasks.addAll(epics.values());
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

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, startTime, duration);
            case EPIC:
                return new Epic(id, name, description, status, startTime, duration);
            case SUBTASK:
                int epicId = Integer.parseInt(taskData[5]);
                return new Subtask(id, name, description, status, epicId, startTime, duration);

        }
        throw new ManagerIOException("Задача не была прочтена");
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи задачи");
        }

    }
    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        try {
            save();
        } catch (ManagerIOException e) {
            System.out.println("Ошибка во время записи эпика");
        }
    }
    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
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
