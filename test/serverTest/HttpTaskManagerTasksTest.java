package serverTest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import server.HttpTaskServer;
import managersCreator.Managers;
import taskManager.interfaces.TaskManager;
import taskManager.memory.InMemoryTaskManager;
import tasksModels.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static server.HttpTaskServer.getGson;

public class HttpTaskManagerTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAll();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1",
                Status.NEW, LocalDateTime.now(), 10);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);
        System.out.println(taskJson);
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем метод, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasksAsList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldReturnTasksList() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            final List<Task> tasks = gson.fromJson(response.body(), new TypeToken<ArrayList<Task>>() {
            }.getType());

            assertNotNull(tasks);
            assertEquals(manager.getTasksAsList().size(), tasks.size(),
                    "Количество задач должно быть эквивалентным.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1",
                Status.NEW, LocalDateTime.MIN, 30);
        manager.createTask(task);

        int taskId = task.getId();

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks/" + taskId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            Task returnedTask = gson.fromJson(response.body(), Task.class);

            assertNotNull(returnedTask, "Задача не возвращается");
            assertEquals(taskId, returnedTask.getId(), "Некорректный ID задачи");
            assertEquals("Test 1", returnedTask.getName(), "Некорректное имя задачи");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        // создаём несколько задач
        Task task1 = new Task("Task 1", "Description 1",
                Status.NEW, LocalDateTime.now().minus(Duration.ofMinutes(40)), 30);
        Task task2 = new Task("Task 2", "Description 2",
                Status.NEW, LocalDateTime.now(), 30);
        Task task3 = new Task("Task 2", "Description 2",
                Status.NEW, LocalDateTime.now().plus(Duration.ofMinutes(40)), 30);

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        int taskIdToDelete = task2.getId();

        // создаём HTTP-клиент и запрос на удаление задачи
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks/" + taskIdToDelete);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();

            // вызываем метод, отвечающий за удаление задачи
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // проверяем код ответа
            assertEquals(200, response.statusCode(), "Некорректный код ответа на удаление задачи");

            // проверяем, что задача была удалена
            List<Task> tasksFromManager = manager.getTasksAsList();
            assertEquals(2, tasksFromManager.size(), "Некорректное количество задач после удаления");

            // проверяем, что оставшиеся задачи не содержат удалённую задачу
            for (Task task : tasksFromManager) {
                assertNotEquals(taskIdToDelete, task.getId(), "Удалённая задача всё ещё присутствует в списке");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateTask() {

    }

    @Test
    public void testCreateSubtask() {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", Status.NEW);
        manager.createEpic(epic);

        // создаём подзадачу для эпика
        Subtask subtask = new Subtask("Subtask 1", "Testing subtask 1", Status.NEW, epic.getId(),
                LocalDateTime.now(), 20);
        // конвертируем её в JSON
        String subtaskJson = gson.toJson(subtask);
        System.out.println(subtaskJson);
        // создаём HTTP-клиент и запрос
        try (HttpClient client = HttpClient.newHttpClient()) {

            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();

            // вызываем метод, отвечающий за создание подзадач
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // проверяем код ответа
            assertEquals(201, response.statusCode());

            // проверяем, что создалась одна подзадача с корректным именем
            List<Subtask> subtasksFromManager = manager.getSubtasksAsList();

            assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
            assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
            assertEquals("Subtask 1", subtasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldReturnSubtasksList() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            final List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<ArrayList<Subtask>>() {}.getType());

            assertNotNull(subtasks);
            assertEquals(manager.getSubtasksAsList().size(), subtasks.size(),
                    "Количество подзадач должно быть эквивалентным." );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSubtaskById() {
        // создаём эпик и подзадачу
        Epic epic = new Epic("Epic 1", "Testing epic 1", Status.NEW);
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Testing subtask 1",
                Status.NEW, epic.getId(), LocalDateTime.MIN, 30);
        manager.createSubtask(subtask);

        int subtaskId = subtask.getId();

        try (HttpClient client = HttpClient.newHttpClient()){
            URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            Subtask returnedSubtask = gson.fromJson(response.body(), Subtask.class);

            assertNotNull(returnedSubtask, "Подзадача не возвращается");
            assertEquals(subtaskId, returnedSubtask.getId(), "Некорректный ID подзадачи");
            assertEquals("Subtask 1", returnedSubtask.getName(), "Некорректное имя подзадачи");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void subtaskDeleteById() {
        // создаём эпик и несколько подзадач
        Epic epic = new Epic("Epic 1", "Testing epic 1", Status.NEW);
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1",
                Status.NEW, epic.getId(), LocalDateTime.now().minus(Duration.ofMinutes(40)), 30);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2",
                Status.NEW, epic.getId(), LocalDateTime.now(), 30);
        Subtask subtask3 = new Subtask("Subtask 3", "Description 3",
                Status.NEW, epic.getId(), LocalDateTime.now().plus(Duration.ofMinutes(40)), 30);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);

        int subtaskIdToDelete = subtask2.getId();

        // создаём HTTP-клиент и запрос на удаление подзадачи
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks/" + subtaskIdToDelete);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();

            // вызываем метод, отвечающий за удаление подзадачи
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // проверяем код ответа
            assertEquals(200, response.statusCode(), "Некорректный код ответа на удаление подзадачи");

            // проверяем, что подзадача была удалена
            List<Subtask> subtasksFromManager = manager.getSubtasksAsList();
            assertEquals(2, subtasksFromManager.size(), "Некорректное количество подзадач после удаления");

            // проверяем, что оставшиеся подзадачи не содержат удалённую подзадачу
            for (Subtask subtask : subtasksFromManager) {
                assertNotEquals(subtaskIdToDelete, subtask.getId(), "Удалённая подзадача всё ещё присутствует в списке");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}