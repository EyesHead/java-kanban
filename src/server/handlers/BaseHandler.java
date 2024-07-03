package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskManager.interfaces.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public BaseHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    protected String readText(HttpExchange httpExchange) throws IOException {
        return new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendResponse(HttpExchange httpExchange, String responseText, int responseCode) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        httpExchange.sendResponseHeaders(responseCode, bytes.length);
        httpExchange.getResponseBody().write(bytes);
        httpExchange.getResponseBody().close();
    }
    //200
    protected void sendDeleteResponse(HttpExchange httpExchange, String deleteSuccessText) throws IOException {
        this.sendResponse(httpExchange, deleteSuccessText, 200);
    }
    protected void sendCreateResponse(HttpExchange httpExchange, String createSuccessText) throws IOException {
        this.sendResponse(httpExchange, createSuccessText, 201);
    }
    protected void sendUpdateResponse(HttpExchange httpExchange, String updateSuccessText) throws IOException {
        this.sendResponse(httpExchange, updateSuccessText, 201);
    }


    //400
    protected void sendIdErrorResponse(HttpExchange httpExchange) throws IOException {
        this.sendResponse(httpExchange, "Not a valid id resource", 400);
    }
    protected void sendJsonErrorResponse(HttpExchange exchange) throws IOException {
        this.sendResponse(exchange,"Invalid Json entry of Java object", 400);
    }
    protected void sendURLErrorResponse(HttpExchange httpExchange, String path) throws IOException {
        this.sendResponse(httpExchange, "Not a valid path: " + path, 400);
    }

    //404
    protected void sendNotFoundResponse(HttpExchange httpExchange, String responseText) throws IOException {
        this.sendResponse(httpExchange, responseText, 404);
    }

    //405
    protected void sendMethodErrorResponse(HttpExchange httpExchange, String method) throws IOException {
        this.sendResponse(httpExchange, "Method not allowed here " + method, 405);
    }

    //406
    protected void sendOverlapErrorResponse(HttpExchange httpExchange, String response) throws IOException {
        this.sendResponse(httpExchange, response, 406);
    }

    //500
    protected void sendServerErrorResponse(HttpExchange httpExchange, String errorMessage) throws IOException {
        this.sendResponse(httpExchange, errorMessage, 500);
    }

    protected void validateTaskJson(JsonObject taskJson) throws JsonSyntaxException {
        if(!(taskJson.has("name") && taskJson.has("description") &&
                taskJson.has("status") && taskJson.has("duration") &&
                taskJson.has("startTime")))
            throw new JsonSyntaxException("Invalid task json");
    }

}
