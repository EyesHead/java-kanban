package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
import taskManager.interfaces.TaskManager;
import tasksModels.Epic;

import java.io.IOException;
import java.util.Arrays;

public class EpicHandler extends BaseHandler {

    public EpicHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    public void handleEpic(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/epics")) {
                handleEpicWithoutId(exchange);
            } else if (path.matches("^/epics/\\d+$")) {
                handleEpicWithId(exchange, path);
            } else if (path.matches("^/epics/\\d+/subtasks$")) {
                handleEpicSubtasks(exchange, path);
            } else {
                sendURLErrorResponse(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleEpicWithId(HttpExchange exchange, String path)
            throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/epics/", "")); //400 Number format
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Epic epic = manager.getEpicById(id); //404 not found
                        sendResponse(exchange, gson.toJson(epic), 200);
                    } catch (NotFoundException e) {
                        sendNotFoundResponse(exchange);
                    }
                    break;
                case "DELETE":
                    manager.deleteEpicById(id);
                    sendDeleteResponse(exchange, "Epic");
                    break;
                default:
                    System.out.println("Method not allowed here: " + method);
                    sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }

    private void handleEpicWithoutId(HttpExchange exchange)
            throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseEpics = gson.toJson(manager.getEpicsAsList());
                sendResponse(exchange, responseEpics, 200);
                break;
            case "POST":
                JsonObject epicJson = gson.fromJson(readText(exchange), JsonObject.class);

                try {
                    validateTaskJson(epicJson); //400 JsonSyntaxException
                } catch (JsonSyntaxException e) {
                    sendJsonErrorResponse(exchange);
                }

                Epic epic = gson.fromJson(epicJson, Epic.class);
                manager.createEpic(epic);
                sendCreateResponse(exchange, "Epic");
                break;
            default:
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange, method);
        }
    }

    private void handleEpicSubtasks(HttpExchange exchange, String path)
            throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/epics/", "")
                    .replace("/subtasks", "")); //400 number format exc

            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                try {
                    Epic epic = manager.getEpicById(id); //404 Not found
                    sendResponse(exchange, gson.toJson(epic.getSubtasks()), 200);
                } catch (NotFoundException e) {
                    sendNotFoundResponse(exchange);
                }
            } else {
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }
}
