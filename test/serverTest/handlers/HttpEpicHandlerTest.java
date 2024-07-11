package serverTest.handlers;

import factories.TaskFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serverTest.HttpManagerTestConfig;
import model.Epic;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpEpicHandlerTest extends HttpManagerTestConfig {
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

    @Test
    public void createRequestTest() throws IOException, InterruptedException {
        Epic epicWithoutSubs = TaskFactory.generateEpic("blank name","ignored");
        String epicJson = gson.toJson(epicWithoutSubs);
        System.out.println(epicJson);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = postRequest(EPIC_DEFAULT_URI, epicJson);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код статусы");
        assertEquals(1, testManager.getEpicsAsList().size(), "Эпик не был добавлен в менеджер");
    }

    @Test
    public void deleteRequestTest() throws IOException, InterruptedException {
        Epic epicToCreate = TaskFactory.generateEpic("Epic","Epic Description");
        Epic epic = testManager.createEpic(epicToCreate);
        testManager.createSubtask(TaskFactory.generateSubtask("subtaskA","subA description", epic.getId()));
        testManager.createSubtask(TaskFactory.generateSubtask("subtaskB","subB description", epic.getId()));

        String url = String.format("%s/%d",EPIC_DEFAULT_URI, epic.getId());
        assertEquals(3, testManager.getAll().size(),"В менеджере должно быть 3 задачи");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = deleteRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса DELETE должен быть 200");
        assertEquals(0, testManager.getAll().size(), "Должен был удалиться эпик с подзадачами");
    }

    @Test
    public void getListRequestTest() throws IOException, InterruptedException {
        Epic epic = testManager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        testManager.createSubtask(TaskFactory.generateSubtask("subtaskA","subA description", epic.getId()));
        testManager.createSubtask(TaskFactory.generateSubtask("subtaskB","subB description", epic.getId()));
        testManager.createEpic(TaskFactory.generateEpic("EpicWithNoSubs","Epic no subs description"));
        assertEquals(2, testManager.getEpicsAsList().size(), "В менеджер должно попасть 2 эпика");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(EPIC_DEFAULT_URI);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "200 - код статуса GET .../epics");

        Epic[] epicsFromResponse = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epicsFromResponse.length, "Неверное количество эпиков, полученных в ответе");

    }
}
