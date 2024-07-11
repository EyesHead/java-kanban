package serverTest.handlers;

import factories.TaskFactory;
import model.Epic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import serverTest.HttpManagerTestConfig;
import model.Subtask;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static model.Status.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpSubtaskHandlerTest extends HttpManagerTestConfig {
    String SUBTASK_URI = URL + "/subtasks";

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    public void shutDown() {
        super.shutDown();
    }

    @Test
    public void createSubtaskRequestTest() throws IOException, InterruptedException {
        Epic epic = testManager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        testManager.createSubtask(TaskFactory.generateSubtask("subtaskA","subA description", epic.getId()));
        Subtask newSubtask = TaskFactory.generateSubtask("subtaskB","subB description", epic.getId());

        String subtaskJson = gson.toJson(newSubtask);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = postRequest(SUBTASK_URI, subtaskJson);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код статусы");
        assertEquals(2,testManager.getEpicById(epic.getId()).getEpicSubtasks().size(),
                "Подзадача не была присвоена эпику");
        assertEquals(2, testManager.getSubtasksAsList().size(),
                "Подзадача не была добавлена в менеджер");
    }

    @Test
    @DisplayName("При обновлении статусов подзадач, статусы эпиков должны меняться")
    public void updateRequestTest() throws IOException, InterruptedException {
        var epic = testManager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        var subtask1 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskA","subA description", NEW, epic.getId()));
        var subtask2 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskB","subB description", NEW, epic.getId()));


        assertEquals(NEW, testManager.getEpicById(0).getStatus(), "Статус эпика должен = NEW");

        // epic -> sub1(IN_PROGRESS), sub2(NEW)
        subtask1.setStatus(IN_PROGRESS);
        HttpClient client = HttpClient.newHttpClient();
        String subtaskJson = gson.toJson(subtask1);
        HttpRequest request = postRequest(SUBTASK_URI, subtaskJson);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неправильный статус-код");
        assertEquals(IN_PROGRESS, testManager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен = IN_PROGRESS");
        assertEquals(IN_PROGRESS, testManager.getSubtaskById(subtask1.getId()).getStatus(),
                "Статус sub1 должен = IN_PROGRESS");
        assertEquals(NEW, testManager.getSubtaskById(subtask2.getId()).getStatus(),
                "Статус sub2 должен = NEW");

        // epic -> sub1(IN_PROGRESS), sub2(DONE)
        subtask2.setStatus(DONE);
        subtaskJson = gson.toJson(subtask2);
        request = postRequest(SUBTASK_URI, subtaskJson);
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный статус-код");
        assertEquals(IN_PROGRESS, testManager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен = IN_PROGRESS");
        assertEquals(IN_PROGRESS, testManager.getSubtaskById(subtask1.getId()).getStatus(),
                "Статус sub1 должен = IN_PROGRESS");
        assertEquals(DONE, testManager.getSubtaskById(subtask2.getId()).getStatus(),
                "Статус sub2 должен = DONE");

        //epic -> sub1(DONE), sub2(DONE)
        subtask1.setStatus(DONE);
        subtaskJson = gson.toJson(subtask1);
        request = postRequest(SUBTASK_URI, subtaskJson);
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный статус-код");
        assertEquals(DONE, testManager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен = DONE");
        assertEquals(DONE, testManager.getSubtaskById(subtask1.getId()).getStatus(),
                "Статус sub1 должен = DONE");
        assertEquals(DONE, testManager.getSubtaskById(subtask2.getId()).getStatus(),
                "Статус sub2 должен = DONE");
    }

    @Test
    public void deleteRequestTest() throws IOException, InterruptedException {
        var epic = testManager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        var subtask1 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskA","subA description", DONE, epic.getId()));

        assertEquals(1, testManager.getSubtasksAsList().size(), "subtask должна быть в списке");
        assertEquals(DONE, testManager.getEpicsAsList().getFirst().getStatus(), "Статус epic = DONE");

        var client = HttpClient.newHttpClient();
        var request = deleteRequest(SUBTASK_URI + "/" + subtask1.getId());
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, testManager.getSubtasksAsList().size(), "subtask должна была удалиться");
        assertEquals(200, response.statusCode(), "200 - статус-код корректного удаления");
        assertEquals(NEW, testManager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика должен измениться на NEW после удаления subtask");
    }

    @Test
    public void getListRequestTest() throws IOException, InterruptedException {
        var epic = testManager.createEpic(TaskFactory.generateEpic("Epic","Epic Description"));
        var subtask1 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskA","subA description", DONE, epic.getId()));
        var subtask2 = testManager.createSubtask(TaskFactory
                .generateSubtask("subtaskB","subB description", DONE, epic.getId()));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(SUBTASK_URI);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        var subtasksList = List.of(subtasks);
        assertTrue(subtasksList.contains(subtask1), "response should have sub1");
        assertTrue(subtasksList.contains(subtask2), "response should have sub2");

    }
}
