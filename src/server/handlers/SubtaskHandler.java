package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.ValidationException;
import taskManager.interfaces.TaskManager;
import tasksModels.Subtask;

import java.io.IOException;
import java.security.InvalidParameterException;
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
                sendURLErrorResponse(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleSubtaskById(HttpExchange exchange, String path)
            throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/subtasks/", ""));
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Subtask subtask = manager.getSubtaskById(id); // 404 not found exc
                        sendResponse(exchange, gson.toJson(subtask), 200);
                    } catch (NotFoundException e) {
                        sendNotFoundResponse(exchange);
                    }
                    break;
                case "DELETE":
                    manager.deleteSubtaskById(id);
                    sendDeleteResponse(exchange, "Subtask");
                    break;
                default:
                    System.out.println("Method not allowed here: " + method);
                    sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }

    private void handleSubtaskWithoutId(HttpExchange exchange)
            throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseSubtasks = gson.toJson(manager.getSubtasksAsList());
                sendResponse(exchange, responseSubtasks, 200);
                break;

            case "POST":
                String requestBody = readText(exchange);
                JsonObject subtaskJson = gson.fromJson(requestBody, JsonObject.class);

                try {
                    isValidSubtaskJson(subtaskJson);
                } catch (JsonSyntaxException e) {
                    sendJsonErrorResponse(exchange);
                }

                if (subtaskJson.has("id")) { // Task UPDATE
                    updateSubtaskOnServer(subtaskJson, exchange);
                } else { // Task CREATE
                    createSubtaskOnServer(subtaskJson, exchange);
                }
                break;
            default:
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange, method);
        }
    }

    private void createSubtaskOnServer(JsonObject subtaskJson, HttpExchange exchange)
            throws IOException {
        Subtask newSubtask = gson.fromJson(subtaskJson, Subtask.class);
        try {
            manager.createSubtask(newSubtask); //406 overlap exception
            System.out.println("Task created");
            sendCreateResponse(exchange, "Task");
        } catch (ValidationException e) {
            sendOverlapErrorResponse(exchange);
        }
    }

    private void updateSubtaskOnServer(JsonObject subtaskJson, HttpExchange exchange)
            throws IOException {
        Subtask subtask = gson.fromJson(subtaskJson, Subtask.class);
        try {
            manager.updateSubtask(subtask); //400 Invalid Parameter exc
            sendUpdateResponse(exchange, "Task");
        } catch (InvalidParameterException e) {
            sendParameterErrorResponse(exchange, subtask.getId().toString());
        }
    }


    private void isValidSubtaskJson(JsonObject subtaskJson) throws JsonSyntaxException {
        super.validateTaskJson(subtaskJson);
        if (!subtaskJson.has("id")) throw new JsonSyntaxException("Subtask id is missing");
    }
}
