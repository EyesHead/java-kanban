package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
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
                        Subtask subtask = manager.getSubtaskById(id);
                        sendResponse(exchange, gson.toJson(subtask), 200);
                        System.out.println("(GET) Subtask successfully returned");
                    } catch (NotFoundException e) {
                        //404
                        sendNotFoundResponse(exchange, "Subtask with id " + id + " not found");
                    }
                    break;
                case "DELETE":
                    manager.deleteSubtaskById(id);
                    sendDeleteResponse(exchange, "Subtask");
                    System.out.println("(DELETE) Subtask was successfully deleted");
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

    private void handleSubtaskWithoutId(HttpExchange exchange)
            throws IOException {
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
                    sendUpdateResponse(exchange, "Subtask updated");
                    System.out.println("(POST) Subtask updated");
                } else { // Task CREATE
                    manager.createSubtask(subtask);
                    sendCreateResponse(exchange, "Subtask created");
                    System.out.println("(POST) Subtask created");
                }
                break;
            default:
                //405
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange, method);
                System.out.println("Subtask method not supported: " + method);
        }
    }
}
