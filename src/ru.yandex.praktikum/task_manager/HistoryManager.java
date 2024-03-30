package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}
