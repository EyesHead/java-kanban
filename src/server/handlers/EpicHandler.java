package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import taskManager.exceptions.NotFoundException;
import taskManager.interfaces.TaskManager;
import tasksModels.Epic;

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
                sendURLErrorResponse(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleEpicPathWithId(HttpExchange exchange, String path)
            throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/epics/", "")); //400 Number format
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    try {
                        Epic epic = manager.getEpicById(id); //404 not found
                        sendResponse(exchange, gson.toJson(epic), 200);
                    } catch (NotFoundException e) {
                        sendNotFoundResponse(exchange, "Epic with id " + id + " not found");
                    }
                    break;
                case "DELETE":
                    manager.deleteEpicById(id);
                    sendDeleteResponse(exchange, "Epic was deleted!");
                    break;
                default:
                    //405
                    sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }

    private void handleEpicPathWithoutId(HttpExchange exchange)
            throws IOException {
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
                    sendCreateResponse(exchange, "Epic successfully created!");
                    break;
                }
                sendJsonErrorResponse(exchange);
                break;

            default:
                //405
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange, method);
        }
    }

    private void handleEpicSubtasksPath(HttpExchange exchange, String path)
            throws IOException {
        try {
            int id = Integer.parseInt(path.replace("/epics/", "")
                    .replace("/subtasks", "")); //400 number format exc

            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                try {
                    Epic epic = manager.getEpicById(id); //404 Not found
                    sendResponse(exchange, gson.toJson(epic.getEpicSubtasks()), 200);
                } catch (NotFoundException e) {
                    sendNotFoundResponse(exchange, "Epic with id " + id + " not found");
                }
            } else {
                //405
                System.out.println("Method not supported: " + method);
                sendMethodErrorResponse(exchange, method);
            }
        } catch (NumberFormatException e) {
            sendIdErrorResponse(exchange);
        }
    }
}
