package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
import taskManager.exceptions.OverlapValidationException;
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
                handleTaskPathWithId(exchange, path);

            } else if ("/tasks".equals(path)) {
                handleTaskPathWithoutId(exchange);
            } else {
                System.out.println("Invalid URL/Path: " + path);
                sendURLErrorResponse(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleTaskPathWithId(HttpExchange exchange, String path)
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
                        sendNotFoundResponse(exchange, "Task with id " + id + " not found");
                    }
                case "DELETE":
                    manager.deleteTaskById(id);
                    sendDeleteResponse(exchange, "Task");
                    break;
                default:
                    //405
                    System.out.println("Method not allowed here: " + method);
                    sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }

    private void handleTaskPathWithoutId(HttpExchange exchange)
            throws IOException, InvalidParameterException, OverlapValidationException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseTasks = gson.toJson(manager.getTasksAsList());
                sendResponse(exchange, responseTasks, 200);
            case "POST":
                String requestBody = readText(exchange);
                Task task = gson.fromJson(requestBody, Task.class);
                System.out.println("(POST) task deserialized: " + task);
                if (task.getId() != null && taskAlreadyExistInManager(task)) { // Task UPDATE
                    try {
                        //406 overlap
                        manager.updateTask(task);
                        sendResponse(exchange, "Task updated successfully", 201);
                    } catch (OverlapValidationException e) {
                        sendOverlapErrorResponse(exchange, "Overlap while updating task");
                    }
                    break;
                }
                if (task.getId() == null && !taskAlreadyExistInManager(task)) { // Task CREATE
                    try {
                        // 406 overlap
                        manager.createTask(task);
                        sendCreateResponse(exchange, "Task created successfully");
                    } catch (OverlapValidationException e) {
                        sendOverlapErrorResponse(exchange, "Overlap while creating task");
                    }
                    break;
                }
                sendJsonErrorResponse(exchange);
                break;
            default:
                System.out.println("Method not allowed: " + method); //405
                sendMethodErrorResponse(exchange,  method);
        }
    }
    private boolean taskAlreadyExistInManager(Task task) {
        return manager.getTasksAsList().contains(task);
    }
}
