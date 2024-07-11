package service.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.exceptions.TaskNotFoundException;
import service.TaskManager;
import model.Epic;

import java.io.IOException;
import java.util.Arrays;

public class EpicHandler extends BaseHandler {

    public EpicHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/epics")) {
                handleEpicPathWithoutId(exchange);
            } else if (path.matches("^/epics/\\d+$")) {
                handleEpicPathWithId(exchange, path);
            } else if (path.matches("^/epics/\\d+/subtasks$")) {
                handleEpicSubtasksPath(exchange, path);
            } else {
                sendResponse(exchange, "Resource wasn't found", 404);
            }
        } catch (Exception e) {
            sendResponse(exchange, "stack trace : " + Arrays.toString(e.getStackTrace()), 500);
            System.out.println("Server crashed: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleEpicPathWithId(HttpExchange exchange, String path) throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/epics/", "")); //400 Number format
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Epic epic = manager.getEpicById(id); //404 not found
                        sendResponse(exchange, gson.toJson(epic), 200);
                    } catch (TaskNotFoundException e) {
                        sendResponse(exchange, "Epic with id " + id + " not found", 404);
                    }
                    break;
                case "DELETE":
                    manager.deleteEpicById(id);
                    sendResponse(exchange, "Epic was deleted!",200);
                    break;
                default:
                    sendResponse(exchange, "Method not supported there", 405);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Not a valid id resource", 400);
        }
    }

    private void handleEpicPathWithoutId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                String responseEpics = gson.toJson(manager.getEpicsAsList());
                sendResponse(exchange, responseEpics, 200);
                break;
            case "POST":
                Epic epic = gson.fromJson(readText(exchange), Epic.class);
                System.out.println("(POST) Epic deserialized from json: " + epic);
                if (epic.getId() == null) { // Epic create
                    manager.createEpic(epic);
                    sendResponse(exchange, "Epic successfully created!", 201);
                    break;
                }
                sendResponse(exchange,"Invalid Json entry of Java object", 400);
                break;
            default:
                sendResponse(exchange, "("+method +") method not allowed here", 405);
                System.out.println("log ("+method+")= Method not allowed here");
        }
    }

    private void handleEpicSubtasksPath(HttpExchange exchange, String path) throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/epics/", "")
                    .replace("/subtasks", "")); //400 number format exc

            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                try {
                    Epic epic = manager.getEpicById(id);
                    sendResponse(exchange, gson.toJson(epic.getEpicSubtasks()), 200);
                } catch (IllegalArgumentException e) {
                    sendResponse(exchange, "Epic with id " + id + " not found", 404);
                }
            } else {
                sendResponse(exchange, "("+method +") method not allowed here", 405);
                System.out.println("log ("+method+")= Method not allowed here");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Not a valid id resource", 400);                }
    }
}
