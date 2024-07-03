package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskManager.interfaces.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PriorityHandler extends BaseHandler {
    TaskManager taskManager;
    public PriorityHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                String response = taskManager.getPrioritizedTasks().toString();
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                exchange.sendResponseHeaders(200, response.length());
            } else {
                System.out.println("Method not supported");
                exchange.sendResponseHeaders(405, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        }
    }
}
