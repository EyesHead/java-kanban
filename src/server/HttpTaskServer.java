package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import managersCreator.Managers;
import server.handlers.*;
import taskManager.interfaces.TaskManager;
import tasksModels.Task;
import tasksModels.Status;
import testUtil.LDTRandomizer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private static HttpServer server;
    private final Gson gson;
    private final TaskHandler taskHandler;
    private final SubtaskHandler subtaskHandler;
    private final EpicHandler epicHandler;
    private final HistoryHandler historyHandler;
    private final PriorityHandler priorityHandler;

    public HttpTaskServer() {
        this(Managers.getDefaultTaskManager());
    }

    public HttpTaskServer(TaskManager manager) {
        this.gson = getGson();
        this.taskHandler = new TaskHandler(manager, gson);
        this.subtaskHandler = new SubtaskHandler(manager, gson);
        this.epicHandler = new EpicHandler(manager, gson);
        this.historyHandler = new HistoryHandler(manager, gson);
        this.priorityHandler = new PriorityHandler(manager, gson);
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/tasks", this::handleTask);
        server.createContext("/subtasks", this::handleSubtask);
        server.createContext("/epics", this::handleEpic);
        server.createContext("/history", this::handleHistory);
        server.createContext("/priority", this::handlePriority);

    }

    private void handleTask(HttpExchange exchange) throws IOException {
        taskHandler.handle(exchange);
    }

    private void handleSubtask(HttpExchange exchange) throws IOException {
        subtaskHandler.handle(exchange);
    }

    private void handleEpic(HttpExchange exchange) throws IOException {
        epicHandler.handle(exchange);
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        historyHandler.handle(exchange);
    }

    private void handlePriority(HttpExchange exchange) throws IOException {
        priorityHandler.handle(exchange);
    }

    public void start() {
        System.out.println("Сервер запущен на сокете: http://localhost:" + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен на порту: " + PORT);
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonAdapters.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new GsonAdapters.DurationAdapter())
                .serializeNulls()
                .create();
    }

    public static void main(String[] args) {
        Task task1 = new Task("????123?<><>", "123123213", Status.NEW,
                LDTRandomizer.getRandomLDT(), 30);
        TaskManager managerNew = Managers.getDefaultTaskManager();
        managerNew.createTask(task1);
        HttpTaskServer serverNew = new HttpTaskServer(managerNew);
        serverNew.start();
    }
}
