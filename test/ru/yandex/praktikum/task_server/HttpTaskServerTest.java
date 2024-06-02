package ru.yandex.praktikum.task_server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.task_manager.InMemoryTaskManager;
import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_server.task_serializers.TaskDurationAdapter;
import ru.yandex.praktikum.task_server.task_serializers.TaskStartTimeDeserializer;
import ru.yandex.praktikum.task_server.task_serializers.TaskStartTimeSerializer;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.praktikum.Constants.CREATED_OK;
import static ru.yandex.praktikum.Constants.EPICS;
import static ru.yandex.praktikum.Constants.ERROR_MESSAGE;
import static ru.yandex.praktikum.Constants.HISTORY;
import static ru.yandex.praktikum.Constants.NOT_FOUND;
import static ru.yandex.praktikum.Constants.PRIORITIZED;
import static ru.yandex.praktikum.Constants.RESULT;
import static ru.yandex.praktikum.Constants.SUBTASKS;
import static ru.yandex.praktikum.Constants.SUCCESS;
import static ru.yandex.praktikum.Constants.TASKS;

public class HttpTaskServerTest {

    private final TaskManager taskManager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    private HttpClient client;
    private final LocalDateTime current = LocalDateTime.now();
    private final long durationInMinutes = 15L;
    private final Epic epic = new Epic("Поход в магазин", "Встречаем гостей");
    private final Task task = new Task("Позвонить другу", "Уточнить место встречи", current,
            durationInMinutes);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TaskStartTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new TaskStartTimeDeserializer())
            .registerTypeAdapter(Duration.class, new TaskDurationAdapter())
            .setPrettyPrinting()
            .create();

    @BeforeEach
    public void setUp() {
        taskManager.clearTasks();
        taskManager.clearSubtasks();
        taskManager.clearEpicTasks();
        taskServer.serverStart();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void shutDown() {
        taskServer.serverStop();
    }

    @Test
    void whenSendCreateTaskRequestThenTaskIsCreated() throws IOException, InterruptedException {
        String body = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        List<Task> tasks = taskManager.getTasks();
        int expectedTasksCount = 1;

        assertEquals(expectedTasksCount, tasks.size(), "В списке задач некорректное количество задач");
        assertEquals(task.getName(), tasks.getFirst().getName(), "Задача была добавлена некорректно");
    }

    @Test
    void whenSendGetTasksRequestThenFilledTasksIsNotEmpty() throws IOException, InterruptedException {
        UUID taskId = taskManager.createTask(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(TASKS);
        int expectedCountTasks = 1;
        assertEquals(expectedCountTasks, jsonArray.size(), "Некорректное количество элементов в массиве");

        String id = jsonArray.get(0).getAsJsonObject().get("id").getAsString();

        assertEquals(taskId.toString(), id, "Была получена некорректная задача");
    }

    @Test
    void whenSendGetTasksByIdRequestThenTaskIsReturned() throws IOException, InterruptedException {
        UUID taskId = taskManager.createTask(task);
        URI url = URI.create(String.format("http://localhost:8080/tasks/%s", taskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String id = jsonObject.get("id").getAsString();

        assertEquals(taskId.toString(), id, "Была получена некорректная задача");
    }

    @Test
    void whenSendUpdateTaskThenReturnedTaskIsUpdated() throws IOException, InterruptedException {
        String expectedName = "Test";
        String expectedDescription = "TestNew";
        UUID taskId = taskManager.createTask(task);
        Task requestTask = taskManager.getTask(taskId);
        requestTask.setName(expectedName);
        requestTask.setDescription(expectedDescription);
        String body = gson.toJson(requestTask);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(CREATED_OK, response.statusCode(), "Некорректный статус код ответа");

        Task updatedTask = taskManager.getTask(taskId);
        assertEquals(expectedName, updatedTask.getName(), "Имя не было изменено");
        assertEquals(expectedDescription, updatedTask.getDescription(), "Описание не было изменено");
    }

    @Test
    void whenSendDeleteTaskThenTaskMustDeletedFromList() throws IOException, InterruptedException {
        UUID taskId = taskManager.createTask(task);
        URI url = URI.create(String.format("http://localhost:8080/tasks/%s", taskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boolean result = jsonObject.get(RESULT).getAsBoolean();
        List<Task> tasks = taskManager.getTasks();

        assertTrue(result, "Некорректное значение результата удаления задачи");
        assertTrue(tasks.isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    void whenSendInvalidRequestThenReturnError() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(NOT_FOUND, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get(ERROR_MESSAGE).getAsString();
        String expectedMessage = "Некорректно вызван метод";

        assertEquals(expectedMessage, errorMessage, "Некорректное сообщение об ошибке");
    }

    @Test
    void whenSendCreateSubtaskRequestThenSubtaskIsCreated() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic);
        String body = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        List<Subtask> subtasks = taskManager.getSubtasks();
        int expectedTasksCount = 1;

        assertEquals(expectedTasksCount, subtasks.size(), "В списке подзадач некорректное количество задач");
        assertEquals(subtask.getName(), subtasks.getFirst().getName(), "Подадача была добавлена некорректно");
    }

    @Test
    void whenSendGetSubtasksRequestThenFilledSubtasksIsNotEmpty() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current,
                durationInMinutes, epic));
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(SUBTASKS);
        int expectedCountTasks = 1;
        assertEquals(expectedCountTasks, jsonArray.size(), "Некорректное количество элементов в массиве");

        String id = jsonArray.get(0).getAsJsonObject().get("id").getAsString();

        assertEquals(subtaskId.toString(), id, "Была получена некорректная подзадача");
    }

    @Test
    void whenSendGetSubtasksByIdRequestThenSubtaskIsReturned() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current,
                durationInMinutes, epic));
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%s", subtaskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String id = jsonObject.get("id").getAsString();

        assertEquals(subtaskId.toString(), id, "Была получена некорректная подзадача");
    }

    @Test
    void whenSendUpdateSubtaskThenReturnedSubtaskIsUpdated() throws IOException, InterruptedException {
        String expectedName = "Test";
        String expectedDescription = "TestNew";
        taskManager.createEpic(epic);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current,
                durationInMinutes, epic));
        Subtask requestSubtask = taskManager.getSubtask(subtaskId);
        requestSubtask.setName(expectedName);
        requestSubtask.setDescription(expectedDescription);
        String body = gson.toJson(requestSubtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(CREATED_OK, response.statusCode(), "Некорректный статус код ответа");

        Subtask updatedSubtask = taskManager.getSubtask(subtaskId);
        assertEquals(expectedName, updatedSubtask.getName(), "Имя не было изменено");
        assertEquals(expectedDescription, updatedSubtask.getDescription(), "Описание не было изменено");
    }

    @Test
    void whenSendDeleteSubtaskThenSubtaskMustDeletedFromList() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current,
                durationInMinutes, epic));
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%s", subtaskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boolean result = jsonObject.get(RESULT).getAsBoolean();
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertTrue(result, "Некорректное значение результата удаления подзадачи");
        assertTrue(subtasks.isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void whenSendCreateEpicRequestThenEpicIsCreated() throws IOException, InterruptedException {
        String body = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        List<Epic> epics = taskManager.getEpicTasks();
        int expectedTasksCount = 1;

        assertEquals(expectedTasksCount, epics.size(), "В списке эпиков некорректное количество задач");
        assertEquals(epic.getName(), epics.getFirst().getName(), "Эпик был добавлен некорректно");
    }

    @Test
    void whenSendGetEpicsRequestThenFilledEpicsIsNotEmpty() throws IOException, InterruptedException {
        UUID epicId = taskManager.createEpic(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(EPICS);
        int expectedCountTasks = 1;
        assertEquals(expectedCountTasks, jsonArray.size(), "Некорректное количество элементов в массиве");

        String id = jsonArray.get(0).getAsJsonObject().get("id").getAsString();

        assertEquals(epicId.toString(), id, "Был получен некорректный эпик");
    }

    @Test
    void whenSendGetEpicsByIdRequestThenEpicIsReturned() throws IOException, InterruptedException {
        UUID epicId = taskManager.createEpic(epic);
        URI url = URI.create(String.format("http://localhost:8080/epics/%s", epicId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String id = jsonObject.get("id").getAsString();

        assertEquals(epicId.toString(), id, "Был получен некорректный эпик");
    }

    @Test
    void whenSendGetEpicSubtasksRequestThenSubtasksIsReturned() throws IOException, InterruptedException {
        UUID epicId = taskManager.createEpic(epic);
        UUID taskId1 = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current,
                durationInMinutes, epic));
        UUID taskId2 = taskManager.createSubtask(new Subtask("Взять маслечко", "Для картошки",
                current.plusMinutes(durationInMinutes), durationInMinutes, epic));
        URI url = URI.create(String.format("http://localhost:8080/epics/%s/subtasks", epicId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(SUBTASKS);
        int expectedCountTasks = 2;
        assertEquals(expectedCountTasks, jsonArray.size(), "Некорректное количество элементов в массиве");

        String firstId = jsonArray.get(0).getAsJsonObject().get("id").getAsString();
        String secondId = jsonArray.get(1).getAsJsonObject().get("id").getAsString();

        assertEquals(taskId1.toString(), firstId, "Первая подзадача в эпике некорректна");
        assertEquals(taskId2.toString(), secondId, "Вторая подзадача в эпике некорректна");
    }

    @Test
    void whenSendDeleteEpicThenEpicMustDeletedFromList() throws IOException, InterruptedException {
        UUID epicId = taskManager.createEpic(epic);
        URI url = URI.create(String.format("http://localhost:8080/epics/%s", epicId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boolean result = jsonObject.get(RESULT).getAsBoolean();
        List<Epic> epics = taskManager.getEpicTasks();

        assertTrue(result, "Некорректное значение результата удаления эпика");
        assertTrue(epics.isEmpty(), "Список эпиков должен быть пустым");
    }

    @Test
    void whenSendGetHistoryFor3ViewedTasksThenReturn3Tasks() throws IOException, InterruptedException {
        UUID taskId = taskManager.createTask(task);
        UUID epicId = taskManager.createEpic(epic);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки",
                current.plusMinutes(durationInMinutes), durationInMinutes, epic));
        taskManager.getTask(taskId);
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(HISTORY);
        int expectedCountTasks = 3;
        assertEquals(expectedCountTasks, jsonArray.size(), "Некорректное количество элементов в массиве");

        String firstTaskId = jsonArray.get(0).getAsJsonObject().get("id").getAsString();
        String secondEpicId = jsonArray.get(1).getAsJsonObject().get("id").getAsString();
        String thirdSubtaskId = jsonArray.get(2).getAsJsonObject().get("id").getAsString();

        assertEquals(taskId.toString(), firstTaskId, "Из истории была получена некорректная задача");
        assertEquals(epicId.toString(), secondEpicId, "Из истории был получен некорректный эпик");
        assertEquals(subtaskId.toString(), thirdSubtaskId, "Из истории была получена некорректная подзадача");
    }

    @Test
    void whenSendGetPrioritizedThenReturnSortedListOfTasks() throws IOException, InterruptedException {
        UUID taskId = taskManager.createTask(task);
        taskManager.createEpic(epic);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки",
                current.minusMinutes(durationInMinutes), durationInMinutes, epic));

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(SUCCESS, response.statusCode(), "Некорректный статус код ответа");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(PRIORITIZED);
        int expectedCountTasks = 2;
        assertEquals(expectedCountTasks, jsonArray.size(), "Некорректное количество элементов в массиве");

        String firstSubtaskId = jsonArray.get(0).getAsJsonObject().get("id").getAsString();
        String secondTaskId = jsonArray.get(1).getAsJsonObject().get("id").getAsString();

        assertEquals(subtaskId.toString(), firstSubtaskId, "Первой в списке должна идти подзадача");
        assertEquals(taskId.toString(), secondTaskId, "Второй в списке должна идти задача");
    }
}
