package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
import taskManager.interfaces.TaskManager;
import tasksModels.Epic;
import tasksModels.Subtask;

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
                System.out.println("Invalid URL/Path: " + path);
                sendErrorResponse(exchange, "Invalid URL/Path", 400);
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, Arrays.toString(e.getStackTrace()), 500);
        }
    }

    private void handleEpicWithId(HttpExchange exchange, String path) throws IOException {
        int id = parsePathToId(path.replace("/epics/", ""));
        if (id != -1) {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Subtask subtask = manager.getSubtaskById(id);
                        sendResponse(exchange, gson.toJson(subtask), 200);
                        break;
                    } catch (NotFoundException e) {
                        sendErrorResponse(exchange,
                                "Epic with id "+id+" does`t exist", 404);
                        break;
                    }
                case "DELETE":
                    manager.deleteEpicById(id);
                    sendResponse(exchange, "Epic was deleted", 200);
                    break;
                default:
                    System.out.println("Method not allowed here: " + method);
                    sendErrorResponse(exchange, "Method not allowed here: " + method, 405);
            }
        } else {
            System.out.println("Invalid Id!");
            sendErrorResponse(exchange, "Invalid Id!", 400);
        }
    }

    private void handleEpicWithoutId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseEpics = gson.toJson(manager.getEpicsAsList());
                sendResponse(exchange, responseEpics, 200);
                break;
            case "POST":
                String requestBody = readText(exchange);
                JsonObject epicJson = gson.fromJson(requestBody, JsonObject.class);
                if (isValidTaskJson(epicJson)) {
                    createEpicOnServer(epicJson, exchange);
                } else {
                    sendErrorResponse(exchange, "Invalid Epic Json", 400);
                }
                break;
            default:
                System.out.println("Method not supported: " + method);
                sendErrorResponse(exchange, "Method not supported: " + method, 405);
        }
    }

    private void handleEpicSubtasks(HttpExchange exchange,
                                    String path) throws IOException {

        int id = parsePathToId(path.replace("/epics/", "")
                .replace("/subtasks", ""));
        if (id != -1) {
            //
            if (manager.getEpicsAsList().stream().anyMatch(epic -> epic.getId() == id)) {
                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    Epic epic = manager.getEpicById(id);
                    sendResponse(exchange, gson.toJson(epic.getSubtasks()), 200);
                } else {
                    System.out.println("Method not supported: " + method);
                    sendErrorResponse(exchange, "Method not supported: " + method, 405);

                }
            } else {
                System.out.println("Epic with id "+id+" does`t exist");
            }

        } else {
            System.out.println("Invalid Id!");
            sendErrorResponse(exchange, "Invalid Id!", 400);
        }


    }

    private void createEpicOnServer(JsonObject epicJson,
                                       HttpExchange exchange) throws IOException {
        Epic newEpic = gson.fromJson(epicJson, Epic.class);
        manager.createEpic(newEpic);
        System.out.println("Epic created");
        sendResponse(exchange, "Epic created successfully", 201);
    }
}
