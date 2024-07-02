package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import taskManager.interfaces.TaskManager;
import taskManager.interfaces.HistoryManager;
import tasksModels.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHandler {
    HistoryManager historyManager = manager.getHistoryManager();
    public HistoryHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                List<Task> taskHistory = historyManager.getAll();
                String responseEpics = gson.toJson(taskHistory);
                sendResponse(exchange, responseEpics, 200);
            } else {
                System.out.println("Invalid method: " + method);
                exchange.sendResponseHeaders(405, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        }
    }


}
