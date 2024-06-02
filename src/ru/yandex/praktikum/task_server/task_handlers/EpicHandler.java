package ru.yandex.praktikum.task_server.task_handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.praktikum.exceptions.NotFoundException;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ru.yandex.praktikum.Constants.BAD_REQUEST;
import static ru.yandex.praktikum.Constants.DELETE;
import static ru.yandex.praktikum.Constants.DESCRIPTION;
import static ru.yandex.praktikum.Constants.EPICS;
import static ru.yandex.praktikum.Constants.ERROR_DESCRIPTION;
import static ru.yandex.praktikum.Constants.ERROR_MESSAGE;
import static ru.yandex.praktikum.Constants.GET;
import static ru.yandex.praktikum.Constants.ID;
import static ru.yandex.praktikum.Constants.INTERNAL_ERROR;
import static ru.yandex.praktikum.Constants.NAME;
import static ru.yandex.praktikum.Constants.NOT_FOUND;
import static ru.yandex.praktikum.Constants.POST;
import static ru.yandex.praktikum.Constants.RESULT;
import static ru.yandex.praktikum.Constants.SUBTASKS;
import static ru.yandex.praktikum.Constants.SUCCESS;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager manager) {
        super(manager);
    }

    enum Endpoint {
        GET_EPICS,
        GET_EPIC_BY_ID,
        GET_EPIC_SUBTASK,
        CREATE_EPIC,
        DELETE_EPIC,
        UNKNOWN
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Endpoint endpoint = getEndpoint(exchange, body);

            switch (endpoint) {
                case GET_EPICS:
                    getEpics(exchange);
                    break;
                case GET_EPIC_BY_ID:
                    getEpicById(exchange);
                    break;
                case GET_EPIC_SUBTASK:
                    getEpicSubtasks(exchange);
                    break;
                case CREATE_EPIC:
                    createEpic(exchange, body);
                case DELETE_EPIC:
                    deleteEpicById(exchange);
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

        if (pathParts.length == 2 && pathParts[1].equals(EPICS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals(POST) && !body.isEmpty()) {
                return Endpoint.CREATE_EPIC;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals(EPICS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals(DELETE)) {
                return Endpoint.DELETE_EPIC;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals(EPICS) && pathParts[3].equals(SUBTASKS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_EPIC_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void getEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = manager.getEpicTasks();
        object = new JsonObject();
        object.add(EPICS, gson.fromJson(gson.toJson(epics), JsonElement.class));
        String response = gson.toJson(object);
        sendText(exchange, response, SUCCESS);
    }

    private void getEpicById(HttpExchange exchange) throws IOException {
        try {
            UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
            Epic epic = manager.getEpic(id);
            String response = gson.toJson(epic);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }

    private void getEpicSubtasks(HttpExchange exchange) throws IOException {
        try {
            UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
            List<Subtask> subtasks = manager.getEpicSubtask(id);
            object = new JsonObject();
            object.add(SUBTASKS, gson.fromJson(gson.toJson(subtasks), JsonElement.class));
            String response = gson.toJson(object);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }

    private void createEpic(HttpExchange exchange, String body) throws IOException {
        try {
            object = JsonParser.parseString(body).getAsJsonObject();
            String name = object.get(NAME).getAsString();
            String description = object.get(DESCRIPTION).getAsString();

            UUID epicId = manager.createEpic(new Epic(name, description));
            object = new JsonObject();
            object.addProperty(ID, epicId.toString());
            String response = gson.toJson(object);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }

    private void deleteEpicById(HttpExchange exchange) throws IOException {
        boolean result;
        try {
            UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
            result = manager.removeEpic(id);
            object = new JsonObject();
            object.addProperty(RESULT, result);
            String response = gson.toJson(object);
            sendText(exchange, response, SUCCESS);
        } catch (NotFoundException e) {
            sendError(exchange, e);
        }
    }
}
