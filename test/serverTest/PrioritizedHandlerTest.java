package serverTest;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class PrioritizedHandlerTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = HttpTaskServer.getGson();
    private final URI uri = URI.create("http://localhost:8080/priority");

    @BeforeEach
    void setUp() {
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testOverlappingTasks() throws IOException, InterruptedException {
        Task task = new Task("Task1","Task1 descr", Status.NEW,
                LocalDateTime.now(), 60);
        Task taskOverlap = new Task("Task overlap","Task2 overlap", Status.NEW,
                LocalDateTime.now().minusMinutes(30), 60);

        manager.createTask(task);
        manager.createTask(taskOverlap);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] prioritizes = gson.fromJson(response.body(), Task[].class);

        System.out.println(prioritizes);

    }
}
