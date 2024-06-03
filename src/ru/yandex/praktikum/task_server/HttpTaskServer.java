package ru.yandex.praktikum.task_server;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.praktikum.task_manager.Managers;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_server.task_handlers.EpicHandler;
import ru.yandex.praktikum.task_server.task_handlers.HistoryHandler;
import ru.yandex.praktikum.task_server.task_handlers.PrioritizedHandler;
import ru.yandex.praktikum.task_server.task_handlers.SubtaskHandler;
import ru.yandex.praktikum.task_server.task_handlers.TaskHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

import static ru.yandex.praktikum.Constants.PORT;

public class HttpTaskServer {

    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) {
        try {
            this.taskManager = taskManager;
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefault());
        taskServer.serverStart();
    }

    public void serverStart() {
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtaskHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
        httpServer.start();
    }

    public void serverStop() {
        httpServer.stop(1);
    }
}
