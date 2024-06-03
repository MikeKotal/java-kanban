package ru.yandex.praktikum.task_server.task_handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_tracker.Task;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static ru.yandex.praktikum.Constants.ERROR_DESCRIPTION;
import static ru.yandex.praktikum.Constants.ERROR_MESSAGE;
import static ru.yandex.praktikum.Constants.GET;
import static ru.yandex.praktikum.Constants.INTERNAL_ERROR;
import static ru.yandex.praktikum.Constants.NOT_FOUND;
import static ru.yandex.praktikum.Constants.PRIORITIZED;
import static ru.yandex.praktikum.Constants.SUCCESS;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            String requestMethod = exchange.getRequestMethod();

            if (requestMethod.equals(GET) && pathParts.length == 2 && pathParts[1].equals(PRIORITIZED)) {
                getPrioritized(exchange);
            } else {
                object = new JsonObject();
                object.addProperty(ERROR_MESSAGE, "Некорректно вызван метод");
                String error = gson.toJson(object);
                sendText(exchange, error, NOT_FOUND);
            }
        } catch (Exception e) {
            object = new JsonObject();
            object.addProperty(ERROR_MESSAGE, e.getMessage());
            object.addProperty(ERROR_DESCRIPTION, Arrays.toString(e.getStackTrace()));
            String error = gson.toJson(object);
            sendText(exchange, error, INTERNAL_ERROR);
        }
    }

    private void getPrioritized(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getPrioritizedTasks();
        object = new JsonObject();
        object.add(PRIORITIZED, gson.fromJson(gson.toJson(tasks), JsonElement.class));
        String response = gson.toJson(object);
        sendText(exchange, response, SUCCESS);
    }
}
