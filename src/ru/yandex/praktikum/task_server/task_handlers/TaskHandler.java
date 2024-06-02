package ru.yandex.praktikum.task_server.task_handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.praktikum.exceptions.NotFoundException;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_tracker.Task;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ru.yandex.praktikum.Constants.BAD_REQUEST;
import static ru.yandex.praktikum.Constants.CREATED_OK;
import static ru.yandex.praktikum.Constants.DELETE;
import static ru.yandex.praktikum.Constants.DESCRIPTION;
import static ru.yandex.praktikum.Constants.DURATION;
import static ru.yandex.praktikum.Constants.ERROR_DESCRIPTION;
import static ru.yandex.praktikum.Constants.ERROR_MESSAGE;
import static ru.yandex.praktikum.Constants.FORMATTER;
import static ru.yandex.praktikum.Constants.GET;
import static ru.yandex.praktikum.Constants.ID;
import static ru.yandex.praktikum.Constants.INTERNAL_ERROR;
import static ru.yandex.praktikum.Constants.NAME;
import static ru.yandex.praktikum.Constants.NOT_FOUND;
import static ru.yandex.praktikum.Constants.POST;
import static ru.yandex.praktikum.Constants.RESULT;
import static ru.yandex.praktikum.Constants.START_TIME;
import static ru.yandex.praktikum.Constants.SUCCESS;
import static ru.yandex.praktikum.Constants.TASKS;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager manager) {
        super(manager);
    }

    enum Endpoint {
        GET_TASKS,
        GET_TASK_BY_ID,
        CREATE_TASK,
        UPDATE_TASK,
        DELETE_TASK,
        UNKNOWN
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Endpoint endpoint = getEndpoint(exchange, body);

            switch (endpoint) {
                case GET_TASKS:
                    getTasks(exchange);
                    break;
                case GET_TASK_BY_ID:
                    getTaskById(exchange);
                    break;
                case CREATE_TASK:
                    createTask(exchange, body);
                    break;
                case UPDATE_TASK:
                    updateTask(exchange, body);
                case DELETE_TASK:
                    deleteTaskById(exchange);
                default:
                    object = new JsonObject();
                    object.addProperty(ERROR_MESSAGE, "Некорректно вызван метод");
                    String error = gson.toJson(object);
                    sendText(exchange, error, NOT_FOUND);
            }
        } catch (NullPointerException e) {
            object = new JsonObject();
            object.addProperty(ERROR_MESSAGE, "Некорретно переданы входные параметры");
            String error = gson.toJson(object);
            sendText(exchange, error, BAD_REQUEST);
        } catch (Exception e) {
            object = new JsonObject();
            object.addProperty(ERROR_MESSAGE, e.getMessage());
            object.addProperty(ERROR_DESCRIPTION, Arrays.toString(e.getStackTrace()));
            String error = gson.toJson(object);
            sendText(exchange, error, INTERNAL_ERROR);
        }
    }

    private Endpoint getEndpoint(HttpExchange exchange, String body) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        String requestMethod = exchange.getRequestMethod();

        if (pathParts.length == 2 && pathParts[1].equals(TASKS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals(POST) && !body.isEmpty()) {
                JsonElement element = JsonParser.parseString(body);
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    return object.get(ID) == null ? Endpoint.CREATE_TASK : Endpoint.UPDATE_TASK;
                }
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals(TASKS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals(DELETE)) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getTasks();
        object = new JsonObject();
        object.add(TASKS, gson.fromJson(gson.toJson(tasks), JsonElement.class));
        String response = gson.toJson(object);
        sendText(exchange, response, SUCCESS);
    }

    private void getTaskById(HttpExchange exchange) throws IOException {
        try {
            UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
            Task task = manager.getTask(id);
            String response = gson.toJson(task);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }

    private void createTask(HttpExchange exchange, String body) throws IOException {
        try {
            object = JsonParser.parseString(body).getAsJsonObject();
            String name = object.get(NAME).getAsString();
            String description = object.get(DESCRIPTION).getAsString();
            LocalDateTime startTime = object.get(START_TIME) != null
                    ? LocalDateTime.parse(object.get(START_TIME).getAsString(), FORMATTER)
                    : null;
            long duration = object.get(DURATION) != null ? object.get(DURATION).getAsLong() : 0L;

            UUID taskId = manager.createTask(new Task(name, description, startTime, duration));
            object = new JsonObject();
            object.addProperty(ID, taskId.toString());
            String response = gson.toJson(object);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }

    private void updateTask(HttpExchange exchange, String body) throws IOException {
        try {
            Task task = gson.fromJson(body, Task.class);
            manager.updateTask(task);
            sendText(exchange, "", CREATED_OK);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }

    private void deleteTaskById(HttpExchange exchange) throws IOException {
        boolean result;
        try {
            UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
            result = manager.removeTask(id);
            object = new JsonObject();
            object.addProperty(RESULT, result);
            String response = gson.toJson(object);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }
}
