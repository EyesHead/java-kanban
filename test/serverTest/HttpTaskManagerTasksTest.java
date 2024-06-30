package serverTest;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import server.HttpTaskServer;
import taskManager.interfaces.TaskManager;
import taskManager.memory.InMemoryTaskManager;
import tasksModels.Status;
import tasksModels.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    String TASK_DEFAULT_URI = "http://localhost:8080/tasks";

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
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, LocalDateTime.now(), 5);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASK_DEFAULT_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasksAsList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    @DisplayName("DELETE TASK_DEFAULT_URI/{id} возвращает 200 при успешном удалении")
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test 3", "Testing task 3",
                Status.NEW, LocalDateTime.now(), 5);
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASK_DEFAULT_URI + "/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasksAsList();
        assertEquals(0, tasksFromManager.size(), "Задача не была удалена");
    }

    @Test
    @DisplayName("POST url/tasks возвращает 201 если создана или 400 если тело было составлено неверно")
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Test 4", "Testing task 4",
                Status.NEW, LocalDateTime.now(), 5);
        manager.createTask(task);

        Task updatedTask = new Task(task.getId(), "Updated Test 4", task.getDescription(),
                Status.DONE, LocalDateTime.now(), 10);
        String updatedTaskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASK_DEFAULT_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasksAsList();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Updated Test 4", tasksFromManager.get(0).getName(), "Задача не была обновлена");
        assertEquals(Status.DONE, tasksFromManager.get(0).getStatus(), "Задача не была обновлена");
    }

    @Test
    @DisplayName("GET /tasks возвращает в теле ответа массив задач в формате json")
    public void shouldGetTasksList() throws IOException, InterruptedException {
        Task task1 = new Task("Test 5", "Testing task 5",
                Status.NEW, LocalDateTime.now(), 5);
        Task task2 = new Task("Test 6", "Testing task 6",
                Status.NEW, LocalDateTime.now().plusMinutes(10), 5);
        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASK_DEFAULT_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasksFromResponse = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasksFromResponse, "Задачи не возвращаются");
        assertEquals(2, tasksFromResponse.length, "Некорректное количество задач");
    }
    
}