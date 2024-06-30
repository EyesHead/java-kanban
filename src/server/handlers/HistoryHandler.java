package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import taskManager.interfaces.HistoryManager;

import java.io.IOException;

public class HistoryHandler {
    HistoryManager historyManager;
    Gson gson;
    public HistoryHandler(HistoryManager historyManager, Gson gson) {
        this.historyManager = historyManager;
        this.gson = gson;
    }

    public void handleHistory(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                String response = historyManager.getAll().toString();
                exchange.sendResponseHeaders(200, response.length());
            } else {
                System.out.println("Invalid method: " + method);
                exchange.sendResponseHeaders(404, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        }
    }


}
