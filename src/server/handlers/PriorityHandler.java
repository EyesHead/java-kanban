package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import taskManager.interfaces.TaskManager;

import java.io.IOException;

public class PriorityHandler {
    TaskManager taskManager;
    public PriorityHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    public void handlePriority(HttpExchange exchange) throws IOException {
        try (exchange) {
            String response = taskManager.getPrioritizedTasks().toString();
            exchange.getResponseBody().write(response.getBytes());
            exchange.sendResponseHeaders(200, response.length());
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        }
    }
}
