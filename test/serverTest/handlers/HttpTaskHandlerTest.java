package serverTest.handlers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import serverTest.HttpManagerTest;
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

public class HttpTaskHandlerTest extends HttpManagerTest {

    private final String TASK_DEFAULT_URI = URL + "/tasks";

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    public void shutDown() {
        super.shutDown();
    }

    @Override
    @Test
    public void createRequestTest() throws IOException, InterruptedException {
        // конвертируем task в JSON
        String taskJson = gson.toJson(task1);
        System.out.println(taskJson);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = postRequest(TASK_DEFAULT_URI, taskJson);

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasksAsList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");

        System.out.println(tasksFromManager.getFirst());

        client.close();
    }

    @Override
    @Test
    @DisplayName("DELETE TASK_DEFAULT_URI/{id} возвращает 200 при успешном удалении")
    public void deleteRequestTest() throws IOException, InterruptedException {
        manager.createTask(task1);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = deleteRequest(TASK_DEFAULT_URI + "/" + task1.getId());

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasksAsList();
        assertEquals(0, tasksFromManager.size(), "Задача не была удалена");
    }

    @Override
    @Test
    @DisplayName("POST url/tasks возвращает 201 если создана или 400 если тело было составлено неверно")
    public void updateRequestTest() throws IOException, InterruptedException {
        manager.createTask(task1);

        Task updatedTask = new Task(task1.getId(), "Updated Task 1", task1.getDescription(),
                Status.DONE, LocalDateTime.now(), 10);
        String updatedTaskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = postRequest(TASK_DEFAULT_URI, updatedTaskJson);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasksAsList();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Updated Task 1", tasksFromManager.getFirst().getName(), "Задача не была обновлена");
        assertEquals(Status.DONE, tasksFromManager.getFirst().getStatus(), "Задача не была обновлена");
    }

    @Override
    @Test
    @DisplayName("GET /tasks возвращает в теле ответа массив задач в формате json")
    public void getListRequestTest() throws IOException, InterruptedException {
        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(TASK_DEFAULT_URI);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasksFromResponse = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasksFromResponse, "Задачи не возвращаются");
        assertEquals(2, tasksFromResponse.length, "Некорректное количество задач");
    }
    
}