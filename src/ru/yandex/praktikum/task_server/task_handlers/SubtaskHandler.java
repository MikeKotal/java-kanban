package ru.yandex.praktikum.task_server.task_handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.praktikum.exceptions.NotFoundException;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Subtask;

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
import static ru.yandex.praktikum.Constants.EPIC_ID;
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
import static ru.yandex.praktikum.Constants.SUBTASKS;
import static ru.yandex.praktikum.Constants.SUCCESS;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Endpoint endpoint = getEndpoint(exchange, body);

            switch (endpoint) {
                case GET_TASKS:
                    getSubtasks(exchange);
                    break;
                case GET_TASK_BY_ID:
                    getSubtaskById(exchange);
                    break;
                case CREATE_TASK:
                    createSubtask(exchange, body);
                    break;
                case UPDATE_TASK:
                    updateSubtask(exchange, body);
                    break;
                case DELETE_TASK:
                    deleteSubtaskById(exchange);
                    break;
                default:
                    object = new JsonObject();
                    object.addProperty(ERROR_MESSAGE, "Некорректно вызван метод");
                    String error = gson.toJson(object);
                    sendText(exchange, error, NOT_FOUND);
            }
        } catch (NotFoundException e) {
            sendError(exchange, e);
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

        if (pathParts.length == 2 && pathParts[1].equals(SUBTASKS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals(POST) && !body.isEmpty()) {
                JsonElement element = JsonParser.parseString(body);
                JsonObject object = element.getAsJsonObject();
                if (object.isEmpty()) {
                    throw new NotFoundException("Необходимо передать атрибуты подзадачи", BAD_REQUEST);
                }
                return object.get(ID) == null ? Endpoint.CREATE_TASK : Endpoint.UPDATE_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals(SUBTASKS)) {
            if (requestMethod.equals(GET)) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals(DELETE)) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void getSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = manager.getSubtasks();
        object = new JsonObject();
        object.add(SUBTASKS, gson.fromJson(gson.toJson(subtasks), JsonElement.class));
        String response = gson.toJson(object);
        sendText(exchange, response, SUCCESS);
    }

    private void getSubtaskById(HttpExchange exchange) throws IOException {
        UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
        Subtask subtask = manager.getSubtask(id);
        String response = gson.toJson(subtask);
        sendText(exchange, response, SUCCESS);
    }

    private void createSubtask(HttpExchange exchange, String body) throws IOException {
        object = JsonParser.parseString(body).getAsJsonObject();
        String name = object.get(NAME).getAsString();
        String description = object.get(DESCRIPTION).getAsString();
        LocalDateTime startTime = object.get(START_TIME) != null
                ? LocalDateTime.parse(object.get(START_TIME).getAsString(), FORMATTER)
                : null;
        long duration = object.get(DURATION) != null ? object.get(DURATION).getAsLong() : 0L;
        UUID epicId = UUID.fromString(object.get(EPIC_ID).getAsString());
        Epic epic = manager.getEpic(epicId);

        UUID subtaskId = manager.createSubtask(new Subtask(name, description, startTime, duration, epic));
        object = new JsonObject();
        object.addProperty(ID, subtaskId.toString());
        String response = gson.toJson(object);
        sendText(exchange, response, SUCCESS);
    }

    private void updateSubtask(HttpExchange exchange, String body) throws IOException {
        Subtask subtask = gson.fromJson(body, Subtask.class);
        manager.updateSubtask(subtask);
        sendText(exchange, "", CREATED_OK);
    }

    private void deleteSubtaskById(HttpExchange exchange) throws IOException {
        UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/")[2]);
        boolean result = manager.removeSubtask(id);
        object = new JsonObject();
        object.addProperty(RESULT, result);
        String response = gson.toJson(object);
        sendText(exchange, response, SUCCESS);
    }
}
