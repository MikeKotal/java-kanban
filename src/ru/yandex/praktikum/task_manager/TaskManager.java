package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TaskManager {
    private HashMap<UUID, Epic> epicTasks = new HashMap<>();
    private HashMap<UUID, Task> tasks = new HashMap<>();
    private HashMap<UUID, Subtask> subtasks = new HashMap<>();

    public HashMap<UUID, Epic> getEpicTasks() {
        return epicTasks;
    }

    public HashMap<UUID, Task> getTasks() {
        return tasks;
    }

    public HashMap<UUID, Subtask> getSubtasks() {
        return subtasks;
    }

    public void clearEpicTasks() {
        epicTasks.clear();
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public Epic getEpic(UUID id) {
        if (!epicTasks.containsKey(id)) {
            System.out.printf("Эпик с тикетом '%s' отсутствет\n", id);
            return null;
        }
        return epicTasks.get(id);
    }

    public Task getTask(UUID id) {
        if (!tasks.containsKey(id)) {
            System.out.printf("Задача с тикетом '%s' отсутствет\n", id);
            return null;
        }
        return tasks.get(id);
    }

    public Subtask getSubtask(UUID id) {
        if (!subtasks.containsKey(id)) {
            System.out.printf("Подзадача с тикетом '%s' отсутствет\n", id);
            return null;
        }
        return subtasks.get(id);
    }

    public String createEpic(Epic epic) {
        epic.setId(UUID.randomUUID());
        if (epicTasks.containsKey(epic.getId())) {
            return "Такой эпик уже существует!";
        }
        epicTasks.put(epic.getId(), epic);
        return String.format("Задача типа 'Epic' с идентификатором '%s' создана\n", epic.getId());
    }

    public String createTask(Task task) {
        task.setId(UUID.randomUUID());
        if (tasks.containsKey(task.getId())) {
            return "Такая задача уже существует!";
        }
        tasks.put(task.getId(), task);
        return String.format("Задача типа 'Task' с идентификатором '%s' создана\n", task.getId());

    }

    public String createSubtask(Subtask subtask, Epic epic) {
        subtask.setId(UUID.randomUUID());
        if (subtasks.containsKey(subtask.getId())) {
            return "Такая подзадача уже существует!";
        }
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        return String.format("Задача типа 'Subtask' с идентификатором '%s' создана\n", subtask.getId());

    }

    public String updateEpic(Epic epic) {
        if (epicTasks.containsKey(epic.getId())) {
            epicTasks.put(epic.getId(), epic);
            return String.format("Задача типа 'Epic' с идентификатором '%s' обновлена\n", epic.getId());
        }
        return String.format("Задачи типа 'Epic' с идентификатором '%s' нет в списке", epic.getId());
    }

    public String updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return String.format("Задача типа 'Task' с идентификатором '%s' обновлена\n", task.getId());
        }
        return String.format("Задачи типа 'Task' с идентификатором '%s' нет в списке\n", task.getId());
    }

    public String updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            for (Subtask task : subtask.getEpic().getSubtasks()) {
                if (task.getStatus() == Statuses.DONE) {
                    subtask.getEpic().setStatus(Statuses.DONE);
                }
                if (task.getStatus() == Statuses.IN_PROGRESS) {
                    subtask.getEpic().setStatus(Statuses.IN_PROGRESS);
                }
            }
            return String.format("Задача типа 'Subtask' с идентификатором '%s' обновлена\n", subtask.getId());
        }
        return String.format("Задачи типа 'Subtask' с идентификатором '%s' нет в списке\n", subtask.getId());
    }

    public boolean removeEpic(UUID id) {
        if (epicTasks.containsKey(id)) {
            if (subtasks.containsKey(id)) {
                if (epicTasks.get(id).equals(subtasks.get(id).getEpic())) {
                    subtasks.remove(id);
                }
            }
            return epicTasks.remove(id) != null;
        }
        System.out.printf("Эпик с тикетом '%s' отсутствует\n", id);
        return false;
    }

    public boolean removeTask(UUID id) {
        return tasks.remove(id) != null;
    }

    public boolean removeSubtask(UUID id) {
        if (subtasks.containsKey(id)) {
            Epic epic = subtasks.get(id).getEpic();
            if (epic.getSubtasks().size() == 1) {
                epic.setStatus(Statuses.NEW);
            }
            return subtasks.remove(id) != null;
        }
        System.out.printf("Подзадачи с тикетом '%s' нет в списке", id);
        return false;
    }

    public List<Subtask> getEpicSubtask(Epic epic) {
        if (!epicTasks.containsKey(epic.getId())) {
            System.out.printf("Эпик с тикетом '%s' отсутствет\n", epic.getId());
            return null;
        }
        return epicTasks.get(epic.getId()).getSubtasks();
    }

}