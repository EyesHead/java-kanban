package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.InvalidSubtaskDataException;
import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.ValidationException;
import taskManager.interfaces.TaskManager;
import tasksModels.Subtask;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class SubtaskHandler extends BaseHandler {

    public SubtaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    public void handleSubtask(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if (Pattern.matches("^/subtasks/\\d+$", path)) {
                handleSubtaskById(exchange, path);

            } else if ("/subtasks".equals(path)) {
                handleSubtaskWithoutId(exchange);
            } else {
                System.out.println("Invalid URL/Path: " + path);
                sendResponse(exchange, "Invalid URL/Path", 400);
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, Arrays.toString(e.getStackTrace()), 500);
        }
    }

    private void handleSubtaskById(HttpExchange exchange, String path) throws IOException {
        int id = parsePathToId(path.replace("/subtasks/", ""));
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
                                "Subtask with id "+id+" does`t exist", 404);
                        break;
                    }
                case "DELETE":
                    manager.deleteSubtaskById(id);
                    sendResponse(exchange, "Subtask was deleted", 200);
                    break;
                default:
                    System.out.println("Method not allowed here: " + method);
                    sendResponse(exchange, "Method not allowed here: " + method, 405);
            }
        } else {
            System.out.println("Invalid Id!");
            sendResponse(exchange, "Invalid Id!", 400);
        }
    }

    private void handleSubtaskWithoutId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String responseSubtasks = gson.toJson(manager.getSubtasksAsList());
            sendResponse(exchange, responseSubtasks, 200);
        } else if ("POST".equalsIgnoreCase(method)) {
            String requestBody = readText(exchange);
            JsonObject subtaskJson = gson.fromJson(requestBody, JsonObject.class);

            if (isValidSubtaskJson(subtaskJson)) {
                if (subtaskJson.has("id")) { // Task UPDATE
                    updateSubtaskOnServer(subtaskJson, exchange);
                } else { // Task CREATE
                    createSubtaskOnServer(subtaskJson, exchange);
                }
            } else {
                System.out.println("Invalid subtask data");
                sendResponse(exchange, "Invalid subtask data", 400);
            }

        } else {
            System.out.println("Method not supported: " + method);
            sendResponse(exchange, "Method not supported: " + method, 405);
        }
    }

    private void createSubtaskOnServer(JsonObject subtaskJson,
                                       HttpExchange exchange) throws IOException {
        Subtask newSubtask = gson.fromJson(subtaskJson, Subtask.class);
        try {
            manager.createSubtask(newSubtask);
            System.out.println("Task created");
            sendResponse(exchange, "Task created successfully", 201);
        } catch (InvalidSubtaskDataException e) {
            sendErrorResponse(exchange, "Incorrect epic id", 400);
        } catch (ValidationException e) {
            sendErrorResponse(exchange, "Validation Error", 406);
        }
    }

    private void updateSubtaskOnServer(JsonObject subtaskJson,
                                       HttpExchange exchange) throws IOException {
        try {
            Subtask taskUpdated = gson.fromJson(subtaskJson, Subtask.class);
            manager.updateTask(taskUpdated);
            sendResponse(exchange, "Task updated successfully", 201);
        } catch (ValidationException e) {
            sendErrorResponse(exchange, "Tasks overlaps", 406);
        }
    }


    private boolean isValidSubtaskJson(JsonObject subtaskJson) {
        return super.isValidTaskJson(subtaskJson) && subtaskJson.has("epicId");
    }
}
