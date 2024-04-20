package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Task;

import java.util.List;
import java.util.UUID;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();

    void remove(UUID id);
}
