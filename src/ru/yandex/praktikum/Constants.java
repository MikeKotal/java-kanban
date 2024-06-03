package ru.yandex.praktikum;

import java.time.format.DateTimeFormatter;

public interface Constants {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    int PORT = 8080;

    int SUCCESS = 200;
    int CREATED_OK = 201;
    int BAD_REQUEST = 400;
    int NOT_FOUND = 404;
    int NOT_ACCEPTABLE = 406;
    int INTERNAL_ERROR = 500;

    String POST = "POST";
    String GET = "GET";
    String DELETE = "DELETE";

    String EPICS = "epics";
    String TASKS = "tasks";
    String SUBTASKS = "subtasks";
    String HISTORY = "history";
    String PRIORITIZED = "prioritized";
    String RESULT = "result";

    String NAME = "name";
    String DESCRIPTION = "description";
    String ID = "id";
    String START_TIME = "startTime";
    String DURATION = "duration";
    String EPIC_ID = "epicId";

    String ERROR_MESSAGE = "errorMessage";
    String ERROR_DESCRIPTION = "errorDescription";
}
