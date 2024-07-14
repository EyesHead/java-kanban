package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;
import model.Subtask;
import service.exceptions.TaskNotFoundException;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class SubtaskHandler extends BaseHandler {

    public SubtaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if (Pattern.matches("^/subtasks/\\d+$", path)) {
                handleSubtaskById(exchange, path);

            } else if ("/subtasks".equals(path)) {
                handleSubtaskWithoutId(exchange);
            } else {
                System.out.println("Invalid URL/Path: " + path);
                sendResponse(exchange, "Not a valid path: " + path, 400);
            }
        } catch (Exception e) {
            sendResponse(exchange, "Server ERROR. Stack trace : " + Arrays.toString(e.getStackTrace()), 500);
            System.out.println("Server crashed: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleSubtaskById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/subtasks/", ""));
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Subtask subtask = manager.getSubtaskById(id);
                        sendResponse(exchange, gson.toJson(subtask), 200);
                        System.out.println("log = (GET) Subtask successfully returned");

                    } catch (TaskNotFoundException e) {
                        sendResponse(exchange, e.getMessage(),404);
                        System.out.println("log = (GET) Subtask not found");
                    }
                    break;
                case "DELETE":
                    manager.deleteSubtaskById(id);
                    sendResponse(exchange, "Subtask was successful deleted!",200);
                    System.out.println("log = (DELETE) Subtask was successfully deleted");
                    break;
                default:
                    sendResponse(exchange, "("+method +") method not allowed here", 405);
                    System.out.println("log ("+method+")= Method not allowed here");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Not a valid id resource", 400);                }
    }

    private void handleSubtaskWithoutId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseSubtasks = gson.toJson(manager.getSubtasksAsList());
                sendResponse(exchange, responseSubtasks, 200);
                System.out.println("Subtasks returned: " + responseSubtasks);
                break;

            case "POST":
                String requestTask = readText(exchange);
                Subtask subtask = gson.fromJson(requestTask, Subtask.class);
                System.out.println("(POST) Subtask deserialized: " + subtask);

                if (subtask.getId() != null) { // Subtask UPDATE
                    manager.updateSubtask(subtask);
                    sendResponse(exchange, "Subtask successfully updated", 201);
                    System.out.println("log = (POST) Subtask updated");
                } else { // Task CREATE
                    manager.createSubtask(subtask);
                    sendResponse(exchange, "Subtask successfully created!", 201);
                    System.out.println("log = (POST) Subtask created");
                }
                break;
            default:
                sendResponse(exchange, "("+method +") method not allowed here", 405);
                System.out.println("log ("+method+")= Method not allowed here");
        }
    }
}
