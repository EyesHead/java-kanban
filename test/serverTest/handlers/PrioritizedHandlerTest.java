package serverTest.handlers;

import factories.TaskFactory;
import serverTest.HttpManagerTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.Status;
import model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PrioritizedHandlerTest extends HttpManagerTestConfig {
    private final URI uri = URI.create(URL + "/priority");

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Override
    @AfterEach
    public void shutDown() {
        super.shutDown();
    }

    @Test
    public void testOverlappingTasks() throws IOException, InterruptedException {
        Task task1 = TaskFactory.generateTask(null,"Task1", "Task1 descr", Status.NEW,
                LocalDateTime.now(), 60);
        Task task2 = TaskFactory.generateTask(null,"Task2", "Task2 descr", Status.NEW,
                LocalDateTime.now().minusMinutes(30), 120);

        testManager.createTask(task1);
        testManager.createTask(task2);

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertNotNull(response.body());
        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);

        // Проверяем, что вернулась только одна задача
        assertEquals(1, prioritized.length,
                "Задача одна, так как task1 и task2 - пересекаются по времени");
    }

}
