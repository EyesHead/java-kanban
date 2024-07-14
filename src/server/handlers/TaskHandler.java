package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;
import model.Task;
import service.exceptions.OverlappingTasksTimeException;
import service.exceptions.TaskNotFoundException;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TaskHandler extends BaseHandler {

    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if (Pattern.matches("^/tasks/\\d+$", path)) {
                handleTaskWithIdAtPath(exchange, path);

            } else if ("/tasks".equals(path)) {
                handleTaskWithoutIdAtPath(exchange);
            } else {
                System.out.println("log: Invalid URL/Path: " + path);
                sendResponse(exchange, "Not a valid path: " + path, 400);
            }
        } catch (Exception e) {
            sendResponse(exchange, "Server ERROR. Stack trace : " + Arrays.toString(e.getStackTrace()), 500);
            System.out.println("log: Server crashed: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleTaskWithIdAtPath(HttpExchange exchange, String path) throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/tasks/", "")); //400 NFE

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Task task = manager.getTaskById(id); //404 not found exception
                        sendResponse(exchange, gson.toJson(task), 200);
                        break;
                    } catch (TaskNotFoundException e) {
                        sendResponse(exchange, e.getMessage(), 404);
                    }
                case "DELETE":
                    manager.deleteTaskById(id);
                    sendResponse(exchange, "Task was successfully deleted", 200);
                    System.out.println("log (DELETE) = Task with id " + id + " was successfully deleted");
                    break;
                default:
                    sendResponse(exchange, "("+method +") method not allowed here", 405);
                    System.out.println("log ("+method+")= Method not allowed here");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Not a valid id resource", 400);                }
    }

    private void handleTaskWithoutIdAtPath(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseTasks = gson.toJson(manager.getTasksAsList());
                sendResponse(exchange, responseTasks, 200);
            case "POST":
                String requestBody = readText(exchange);
                Task task = gson.fromJson(requestBody, Task.class);
                System.out.println("(POST) task deserialized: " + task);
                if (task.getId() != null && isTaskAlreadyExistInManager(task)) { // Task UPDATE
                    try {
                        manager.updateTask(task); //406 overlap
                        sendResponse(exchange, "Task updated successfully", 201);
                    } catch (OverlappingTasksTimeException e) {
                        sendResponse(exchange, e.getMessage(), 406);
                        System.out.println("log = (ERROR) Overlap while updating task");
                    }
                    break;
                }
                if (task.getId() == null && !isTaskAlreadyExistInManager(task)) { // Task CREATE
                    try {
                        manager.createTask(task); // overlap
                        sendResponse(exchange, "Task successfully created!", 201);
                        System.out.println("log = (POST) Task successfully created!");
                    } catch (OverlappingTasksTimeException e) {
                        sendResponse(exchange, e.getMessage(), 406);
                        System.out.println("log = (ERROR) Overlap while creating task");
                    }
                    break;
                }
                sendResponse(exchange,"Invalid Json entry of Java object", 400);
                System.out.println("log = Invalid Json entry of Java object");
                break;
            default:
                sendResponse(exchange, "("+method +") method not allowed here", 405);
                System.out.println("log ("+method+")= Method not allowed here");
        }
    }
    private boolean isTaskAlreadyExistInManager(Task task) {
        return manager.getTasksAsList().contains(task);
    }
}
