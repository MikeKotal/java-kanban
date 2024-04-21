package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_collections.LinkedTaskList;
import ru.yandex.praktikum.task_collections.Node;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedTaskList<Task> history = new LinkedTaskList<>();
    private final Map<UUID, Node<Task>> taskMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task != null) {
            if (taskMap.containsKey(task.getId())) {
                history.removeNode(taskMap.get(task.getId()));
                taskMap.remove(task.getId());
            }
            history.linkLast(task);
            taskMap.put(task.getId(), history.getLastTask());
        }
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void remove(UUID id) {
        if (taskMap.containsKey(id)) {
            history.removeNode(taskMap.get(id));
            taskMap.remove(id);
        }
    }
}
