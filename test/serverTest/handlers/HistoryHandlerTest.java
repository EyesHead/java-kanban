package serverTest.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serverTest.HttpManagerTest;
import tasksModels.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHandlerTest extends HttpManagerTest {
    private final String HISTORY_URL = URL + "/history";
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    public void shutDown() {
        super.shutDown();
    }

    @Override
    public void createRequestTest()  {
        // Метод не предусмотрен для /history
    }

    @Override
    public void updateRequestTest()  {
        // Метод не предусмотрен для /history
    }

    @Override
    public void deleteRequestTest() {
        // Метод не предусмотрен для /history
    }

    @Override
    @Test
    public void getListRequestTest() throws IOException, InterruptedException {
        manager.createEpic(epic);
        manager.createTask(task2);
        manager.createTask(task1);
        initSubtasksAfterCreateEpic();
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        // заполняем историю просмотров

        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epic.getId()); // 5

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(HISTORY_URL);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] tasksHistory = gson.fromJson(response.body(), Task[].class);
        assertEquals(5, tasksHistory.length, "История просмотров некорректная");
        //удалим эпик с подзадачами. Из списка просмотра они тоже должны исчезнуть, итого 2 задачи в списке
        manager.deleteEpicById(epic.getId());

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        tasksHistory = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasksHistory.length, "История просмотров некорректная");
    }
}
