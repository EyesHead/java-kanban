package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.Set;

public class PriorityHandler extends BaseHandler {
    public PriorityHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                if (manager != null) {
                    Set<Task> prioritizedTasks = manager.getPrioritizedTasks();
                    String responseJson = gson.toJson(prioritizedTasks);

                    sendResponse(exchange, responseJson, 200);
                    System.out.println("(GET) priority = " + responseJson);
                } else {
                    sendResponse(exchange, "TaskManager не найден", 404);
                }
            } else {
                sendResponse(exchange, "(" + method + ") method not allowed here", 405);
                System.out.println("log (" + method + ")= Method not allowed here");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, gson.toJson(e.getStackTrace()), 500);
        }
    }
}
