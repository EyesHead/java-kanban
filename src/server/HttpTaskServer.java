package server;

import com.sun.net.httpserver.HttpServer;
import server.handlers.*;
import service.ManagersCreator;
import server.config.GsonConfig;
import service.TaskManager;
import model.Task;
import model.Status;
import tests.utils.LDTRandomizer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private static HttpServer server;

    public HttpTaskServer(TaskManager manager) {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/tasks", new TaskHandler(manager, GsonConfig.getGson()));
        server.createContext("/subtasks", new SubtaskHandler(manager, GsonConfig.getGson()));
        server.createContext("/epics", new EpicHandler(manager, GsonConfig.getGson()));
        server.createContext("/history", new HistoryHandler(manager, GsonConfig.getGson()));
        server.createContext("/priority", new PriorityHandler(manager, GsonConfig.getGson()));

    }

    public void start() {
        System.out.println("Сервер запущен на сокете: http://localhost:" + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен на порту: " + PORT);
    }

    public static void main(String[] args) {
        Task task1 = new Task(0,"????123?<><>", "123123213", Status.NEW,
                LDTRandomizer.getRandomLDT(), 30);
        TaskManager managerNew = ManagersCreator.getDefaultTaskManager();
        managerNew.createTask(task1);
        HttpTaskServer serverNew = new HttpTaskServer(managerNew);
        serverNew.start();
    }
}
