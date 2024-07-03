package serverTest.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import serverTest.HttpManagerTest;
import tasksModels.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tasksModels.Status.*;

public class HttpSubtaskHandlerTest extends HttpManagerTest {
    String SUBTASK_URI = URL + "/subtasks";

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
        manager.createEpic(epic);
        initSubtasksAfterCreateEpic();

        String subtaskJson = gson.toJson(subtask1);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = postRequest(SUBTASK_URI, subtaskJson);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код статусы");
        assertEquals(1, manager.getEpicsAsList().size(), "Подзадача не была добавлена в менеджер");
    }

    @Override
    @Test
    @DisplayName("При обновлении статусов подзадач, статусы эпиков должны меняться")
    public void updateRequestTest() throws IOException, InterruptedException {
        manager.createEpic(epic);
        initSubtasksAfterCreateEpic();
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        assertEquals(NEW, manager.getEpicById(0).getStatus(), "Статус эпика должен = NEW");

        // epic -> sub1(IN_PROGRESS), sub2(NEW)
        subtask1.setStatus(IN_PROGRESS);
        HttpClient client = HttpClient.newHttpClient();
        String subtaskJson = gson.toJson(subtask1);
        HttpRequest request = postRequest(SUBTASK_URI, subtaskJson);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неправильный статус-код");
        assertEquals(IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен = IN_PROGRESS");
        assertEquals(IN_PROGRESS, manager.getSubtaskById(subtask1.getId()).getStatus(),
                "Статус sub1 должен = IN_PROGRESS");
        assertEquals(NEW, manager.getSubtaskById(subtask2.getId()).getStatus(),
                "Статус sub2 должен = NEW");

        // epic -> sub1(IN_PROGRESS), sub2(DONE)
        subtask2.setStatus(DONE);
        subtaskJson = gson.toJson(subtask2);
        request = postRequest(SUBTASK_URI, subtaskJson);
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный статус-код");
        assertEquals(IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен = IN_PROGRESS");
        assertEquals(IN_PROGRESS, manager.getSubtaskById(subtask1.getId()).getStatus(),
                "Статус sub1 должен = IN_PROGRESS");
        assertEquals(DONE, manager.getSubtaskById(subtask2.getId()).getStatus(),
                "Статус sub2 должен = DONE");

        //epic -> sub1(DONE), sub2(DONE)
        subtask1.setStatus(DONE);
        subtaskJson = gson.toJson(subtask1);
        request = postRequest(SUBTASK_URI, subtaskJson);
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный статус-код");
        assertEquals(DONE, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен = DONE");
        assertEquals(DONE, manager.getSubtaskById(subtask1.getId()).getStatus(),
                "Статус sub1 должен = DONE");
        assertEquals(DONE, manager.getSubtaskById(subtask2.getId()).getStatus(),
                "Статус sub2 должен = DONE");
    }

    @Override
    @Test
    public void deleteRequestTest() throws IOException, InterruptedException {
        manager.createEpic(epic);
        initSubtasksAfterCreateEpic();
        subtask1.setStatus(DONE);
        manager.createSubtask(subtask1);

        assertEquals(1, manager.getSubtasksAsList().size(), "Задача должна быть в списке");
        assertEquals(DONE, manager.getEpicsAsList().getFirst().getStatus(), "Статус epic = DONE");

        // epic -> subtask1(NEW)
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = deleteRequest(SUBTASK_URI + "/" + subtask1.getId());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, manager.getSubtasksAsList().size(), "Задача должно была удалиться");
        assertEquals(200, response.statusCode(), "200 - статус-код корректного удаления");
        assertEquals(NEW, manager.getEpicsAsList().getFirst().getStatus(),
                "Статус эпика должен стать NEW");
    }

    @Override
    @Test
    public void getListRequestTest() throws IOException, InterruptedException {
        manager.createEpic(epic);
        initSubtasksAfterCreateEpic();
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getRequest(SUBTASK_URI);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(subtasks.length, manager.getSubtasksAsList().size(),
                "Размер листа subtasks менеджера равен размеру листа из http ответа");

    }
}
