package serverTest.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serverTest.HttpManagerTest;
import tasksModels.Epic;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpEpicHandlerTest extends HttpManagerTest {
    private final String EPIC_DEFAULT_URI = URL + "/epics";

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

    @Override
    @Test
    public void createRequestTest() throws IOException, InterruptedException {
        String epicJson = gson.toJson(epicWithoutSubs);
        System.out.println(epicJson);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = postRequest(EPIC_DEFAULT_URI, epicJson);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код статусы");
        assertEquals(1, manager.getEpicsAsList().size(), "Эпик не был добавлен в менеджер");
    }

    @Override
    @Test
    protected void updateRequestTest() {
        // EPIC CANT UPDATE ITSELF. ONLY BY ITS SUBTASKS
    }

    @Override
    @Test
    public void deleteRequestTest() throws IOException, InterruptedException {
        manager.createEpic(epic);
        initSubtasksAfterCreateEpic();
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        String url = EPIC_DEFAULT_URI + "/" + manager.getEpicsAsList().getFirst().getId();
        // создан эпик с 2мя подзадачами
        assertEquals(3, manager.getAll().size(),"В менеджере должно быть 3 задачи");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = deleteRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса DELETE должен быть 200");
        assertEquals(0, manager.getAll().size(), "Должен был удалиться эпик с подзадачами");
    }

    @Override
    @Test
    public void getListRequestTest() throws IOException, InterruptedException {
        manager.createEpic(epic);
        initSubtasksAfterCreateEpic();
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createEpic(epicWithoutSubs);
        assertEquals(2, manager.getEpicsAsList().size(), "В менеджер должно попасть 2 эпика");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(EPIC_DEFAULT_URI);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "200 - код статуса GET .../epics");

        Epic[] epicsFromResponse = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epicsFromResponse.length, "Неверное количество эпиков, полученных в ответе");

    }
}
