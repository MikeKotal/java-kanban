package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_COUNT_HISTORY = 10;
    private List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() < MAX_COUNT_HISTORY) {
            history.add(task);
        } else {
            history.removeFirst();
            history.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
