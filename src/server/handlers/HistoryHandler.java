package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;
import service.HistoryManager;
import model.Task;

import java.io.IOException;
import java.util.Arrays;
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
                sendResponse(exchange, "("+method +") method not allowed here", 405);
                System.out.println("log ("+method+")= Method not allowed here");
            }
        } catch (Exception e) {
            sendResponse(exchange, "stack trace : " + Arrays.toString(e.getStackTrace()), 500);
            System.out.println("Server crashed: " + Arrays.toString(e.getStackTrace()));
        }
    }


}
