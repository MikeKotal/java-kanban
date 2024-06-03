package ru.yandex.praktikum.task_server.task_handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.praktikum.exceptions.NotFoundException;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_server.task_serializers.TaskDurationAdapter;
import ru.yandex.praktikum.task_server.task_serializers.TaskStartTimeDeserializer;
import ru.yandex.praktikum.task_server.task_serializers.TaskStartTimeSerializer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import static ru.yandex.praktikum.Constants.ERROR_MESSAGE;

public abstract class BaseHttpHandler implements HttpHandler {

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager manager;
    protected JsonObject object;
    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TaskStartTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new TaskStartTimeDeserializer())
            .registerTypeAdapter(Duration.class, new TaskDurationAdapter())
            .setPrettyPrinting()
            .create();

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    enum Endpoint {
        GET_TASKS,
        GET_TASK_BY_ID,
        GET_EPIC_SUBTASK,
        CREATE_TASK,
        UPDATE_TASK,
        DELETE_TASK,
        UNKNOWN
    }

    protected void sendText(HttpExchange exchange, String responseText, int responseCode) throws IOException {
        byte[] response = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(responseCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendError(HttpExchange exchange, NotFoundException e) throws IOException {
        object = new JsonObject();
        object.addProperty(ERROR_MESSAGE, e.getMessage());
        String error = gson.toJson(object);
        sendText(exchange, error, e.getStatusCode());
    }
}
