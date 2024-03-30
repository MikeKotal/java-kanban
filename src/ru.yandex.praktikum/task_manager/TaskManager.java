package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.List;
import java.util.UUID;

public interface TaskManager {
    List<Epic> getEpicTasks();

    List<Task> getTasks();

    List<Subtask> getSubtasks();

    void clearEpicTasks();

    void clearTasks();

    void clearSubtasks();

    Epic getEpic(UUID id);

    Task getTask(UUID id);

    Subtask getSubtask(UUID id);

    UUID createEpic(Epic epic);

    UUID createTask(Task task);

    UUID createSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    boolean removeEpic(UUID id);

    boolean removeTask(UUID id);

    boolean removeSubtask(UUID id);

    List<Subtask> getEpicSubtask(UUID id);

    List<Task> getTaskHistory();
}