package serverTest.handlers;

import factories.TaskFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serverTest.HttpManagerTestConfig;
import model.Task;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHandlerTest extends HttpManagerTestConfig {
    private final String HISTORY_URL = URL + "/history";
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    public void shutDown() {
        super.shutDown();
    }

    @Test
    public void getListRequestTest() throws IOException, InterruptedException {
        var epic = testManager.createEpic(TaskFactory
                .generateEpic("Epic","Epic Description"));
        var task2 = testManager.createTask(TaskFactory
                .generateTask("Task1", "Task1 description"));
        var task1 = testManager.createTask(TaskFactory
                .generateTask("Task2", "Task2 description"));
        var subtask1 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskA","subA description", epic.getId()));
        var subtask2 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskB","subB description", epic.getId()));
        // заполняем историю просмотров

        testManager.getSubtaskById(subtask1.getId());
        testManager.getSubtaskById(subtask2.getId());
        testManager.getTaskById(task1.getId());
        testManager.getTaskById(task2.getId());
        testManager.getEpicById(epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(HISTORY_URL);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] tasksHistory = gson.fromJson(response.body(), Task[].class);
        assertEquals(5, tasksHistory.length, "История просмотров некорректная");
        //удалим эпик с подзадачами. Из списка просмотра они тоже должны исчезнуть, итого 2 задачи в списке
        testManager.deleteEpicById(epic.getId());

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        tasksHistory = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasksHistory.length, "История просмотров некорректная");
    }
}
