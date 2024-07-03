package serverTest;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import taskManager.interfaces.TaskManager;
import taskManager.memory.InMemoryTaskManager;
import tasksModels.Epic;
import tasksModels.Status;
import tasksModels.Subtask;
import tasksModels.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;

public abstract class HttpManagerTest {
    protected TaskManager manager = new InMemoryTaskManager();
    protected HttpTaskServer taskServer = new HttpTaskServer(manager);
    protected Gson gson = HttpTaskServer.getGson();
    protected String URL = "http://localhost:8080";

    protected Task task1 = new Task("Task 1", "Description Task 1", Status.NEW,
            LocalDateTime.now().minusDays(10), 60);
    protected Task task2 = new Task("Task 2", "Description Task 2", Status.NEW,
            LocalDateTime.now().minusDays(9), 60);

    protected Epic epicWithoutSubs = new Epic("Epic without subs", "Epic without subtasks", Status.NEW);
    protected Epic epic = new Epic("Epic with subs", "Epic with 2 subtasks", Status.NEW);
    protected Subtask subtask1;
    protected Subtask subtask2;

    @BeforeEach
    protected void setUp() {
        manager.deleteAll();
        taskServer.start();
    }

    @AfterEach
    protected void shutDown() {
        taskServer.stop();
    }
    @Test
    protected abstract void createRequestTest() throws IOException, InterruptedException;
    @Test
    protected abstract void updateRequestTest() throws IOException, InterruptedException;

    @Test
    @DisplayName("DELETE TASK_DEFAULT_URI/{id} возвращает 200 при успешном удалении")
    public abstract void deleteRequestTest() throws IOException, InterruptedException;
    @Test
    protected abstract void getListRequestTest() throws IOException, InterruptedException;

    protected void initSubtasksAfterCreateEpic() {
        subtask1 = new Subtask("Subtask 1", "Subtask of epic 1", Status.NEW,
                epic.getId(), LocalDateTime.now(), 60);
        subtask2 = new Subtask("Subtask 2", "Subtask of epic 2", Status.NEW,
                epic.getId(), LocalDateTime.now().plusMinutes(60), 60);
    }

    protected HttpRequest postRequest(String url, String body) {
        return HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }
    protected HttpRequest deleteRequest(String url) {
        return HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
    }
    protected HttpRequest getRequest(String url) {
        return HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
    }
}
