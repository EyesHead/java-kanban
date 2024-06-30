package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.ValidationException;
import taskManager.interfaces.TaskManager;
import tasksModels.Task;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TaskHandler extends BaseHandler {

    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    public void handleTask(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if (Pattern.matches("^/tasks/\\d+$", path)) {
                handleTaskById(exchange, path);

            } else if ("/tasks".equals(path)) {
                handleTasksWithoutId(exchange);
            } else {
                System.out.println("Invalid URL/Path: " + path);
                sendURLErrorResponse(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleTaskById(HttpExchange exchange, String path)
        throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/tasks/", "")); //400 NFE

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Task task = manager.getTaskById(id); //404 not found exception
                        sendResponse(exchange, gson.toJson(task), 200);
                        break;
                    } catch (NotFoundException e) {
                        sendNotFoundResponse(exchange);
                    }
                case "DELETE":
                    manager.deleteTaskById(id);
                    sendDeleteResponse(exchange, "Task");
                    break;
                default:
                    System.out.println("Method not allowed here: " + method);
                    sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }

    private void handleTasksWithoutId(HttpExchange exchange)
            throws IOException, InvalidParameterException, ValidationException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseTasks = gson.toJson(manager.getTasksAsList());
                sendResponse(exchange, responseTasks, 200);
            case "POST":
                String requestBody = readText(exchange);
                JsonObject taskJson = gson.fromJson(requestBody, JsonObject.class);

                try {
                    validateTaskJson(taskJson); //400 Json Syntax Exception
                } catch (JsonSyntaxException e) {
                    sendJsonErrorResponse(exchange);
                }

                if (taskJson.has("id")) { // Task UPDATE
                    Task taskUpdated = gson.fromJson(taskJson, Task.class);
                    try {
                        manager.updateTask(taskUpdated); //400 Invalid Parameter
                        sendResponse(exchange, "Task updated successfully", 201);
                    } catch (InvalidParameterException e) {
                        sendParameterErrorResponse(exchange, String.valueOf(taskJson.get("id")));
                    }
                    break;
                } else { // Task CREATE
                    Task newTask = gson.fromJson(taskJson, Task.class);
                    try {
                        manager.createTask(newTask); // 406 overlap
                        System.out.println("Task created");
                        sendCreateResponse(exchange, "Task");
                    } catch (ValidationException e) {
                        sendOverlapErrorResponse(exchange);
                    }
                    break;
                }

            default:
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange,  method);
        }
    }
}
