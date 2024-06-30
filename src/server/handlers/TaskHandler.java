package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.ValidationException;
import taskManager.interfaces.TaskManager;
import tasksModels.Task;

import java.io.IOException;
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
                sendResponse(exchange, "Invalid URL/Path", 400);
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, Arrays.toString(e.getStackTrace()), 500);
        }
    }

    private void handleTaskById(HttpExchange exchange, String path) throws IOException {
        int id = parsePathToId(path.replace("/tasks/", ""));
        if (id != -1) {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Task task = manager.getTaskById(id);
                        sendResponse(exchange, gson.toJson(task), 200);
                        break;
                    } catch (NotFoundException e) {
                        sendErrorResponse(exchange,
                                "Task with id "+id+" does`t exist", 404);
                        break;
                    }
                case "DELETE":
                    manager.deleteTaskById(id);
                    sendResponse(exchange, "Task was deleted", 200);
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

    private void handleTasksWithoutId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String responseTasks = gson.toJson(manager.getTasksAsList());
            sendResponse(exchange, responseTasks, 200);

        } else if ("POST".equalsIgnoreCase(method)) {
            String requestBody = readText(exchange);
            JsonObject taskJson = gson.fromJson(requestBody, JsonObject.class);

            if (isValidTaskJson(taskJson)) {
                if (taskJson.has("id")) { // Task UPDATE
                    try {
                        Task taskUpdated = gson.fromJson(taskJson, Task.class);
                        manager.updateTask(taskUpdated);
                        sendResponse(exchange, "Task updated successfully", 201);
                    } catch (ValidationException e) {
                            sendErrorResponse(exchange, "Tasks overlaps", 406);
                    }
                } else { // Task CREATE
                    Task newTask = gson.fromJson(taskJson, Task.class);
                    try {
                        manager.createTask(newTask);
                        System.out.println("Task created");
                        sendResponse(exchange, "Task created successfully", 201);
                    } catch (ValidationException e) {
                        sendErrorResponse(exchange, "Validation Error", 406);
                    }
                }
            } else {
                System.out.println("Invalid task data");
                sendResponse(exchange, "Invalid task data", 400);
            }

        } else {
            System.out.println("Method not supported: " + method);
            sendResponse(exchange, "Method not supported: " + method, 405);
        }
    }


}
