package serverTest;

import com.google.gson.Gson;
import server.config.GsonConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import server.HttpTaskServer;
import service.TaskManager;
import service.inMemory.repository.InMemoryTaskManager;

import java.net.URI;
import java.net.http.HttpRequest;

public class HttpManagerTestConfig {
    protected final TaskManager testManager = new InMemoryTaskManager();
    protected final HttpTaskServer testServer = new HttpTaskServer(testManager);
    protected final Gson gson = GsonConfig.getGson();
    protected final String URL = "http://localhost:8080";

    @BeforeEach
    protected void setUp() {
        testManager.deleteAll();
        testServer.start();
    }

    @AfterEach
    protected void shutDown() {
        testServer.stop();
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
