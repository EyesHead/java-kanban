package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import taskManager.interfaces.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHandler {
    TaskManager manager;
    Gson gson;
    public BaseHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    protected String readText(HttpExchange httpExchange) throws IOException {
        return new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendResponse(HttpExchange httpExchange, String responseText, int responseCode) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        httpExchange.sendResponseHeaders(responseCode, bytes.length);
        httpExchange.getResponseBody().write(bytes);
    }

    protected void sendErrorResponse(HttpExchange httpExchange, String errorMessage, int statusCode) throws IOException {
        sendResponse(httpExchange, errorMessage, statusCode);
    }

    protected int parsePathToId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected boolean isValidTaskJson(JsonObject taskJson) {
        return taskJson.has("name") && taskJson.has("description") &&
                taskJson.has("status") && taskJson.has("duration") &&
                taskJson.has("startTime");
    }
}
