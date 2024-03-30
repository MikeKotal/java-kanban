package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager manager = Managers.getDefaultHistory();
    private final HashMap<UUID, Epic> epicTasks = new HashMap<>();
    private final HashMap<UUID, Task> tasks = new HashMap<>();
    private final HashMap<UUID, Subtask> subtasks = new HashMap<>();

    public List<Epic> getEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearEpicTasks() {
        epicTasks.clear();
        subtasks.clear();
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear();
        epicTasks.values().forEach(epic -> {
            epic.getIdSubtasks().clear();
            changerEpicStatus(epic);
        });
    }

    @Override
    public Epic getEpic(UUID id) {
        manager.add(epicTasks.get(id));
        return epicTasks.get(id);
    }

    @Override
    public Task getTask(UUID id) {
        manager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Subtask getSubtask(UUID id) {
        manager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public UUID createEpic(Epic epic) {
        if (epic.getId() != null && epicTasks.containsKey(epic.getId())) {
            return epic.getId();
        }
        epic.setId(UUID.randomUUID());
        epicTasks.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public UUID createTask(Task task) {
        if (task.getId() != null && tasks.containsKey(task.getId())) {
            return task.getId();
        }
        task.setId(UUID.randomUUID());
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public UUID createSubtask(Subtask subtask) {
        if (!epicTasks.containsKey(subtask.getEpicId())) {
            return subtask.getId();
        }
        if (subtask.getId() != null && subtasks.containsKey(subtask.getId())) {
            return subtask.getId();
        }
        subtask.setId(UUID.randomUUID());
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic.addSubtask(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            changerEpicStatus(epic);
            return subtask.getId();
        }
        return null;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epicTasks.containsKey(epic.getId())) {
            Epic newEpic = epicTasks.get(epic.getId());
            newEpic.setName(epic.getName());
            newEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            changerEpicStatus(epicTasks.get(subtask.getEpicId()));
        }
    }

    @Override
    public boolean removeEpic(UUID id) {
        Epic deleteEpic = epicTasks.remove(id);
        if (deleteEpic != null) {
            deleteEpic.getIdSubtasks().forEach(subtasks::remove);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTask(UUID id) {
        return tasks.remove(id) != null;
    }

    @Override
    public boolean removeSubtask(UUID id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic linkedEpic = epicTasks.get(subtask.getEpicId());
            linkedEpic.getIdSubtasks().remove(id);
            changerEpicStatus(linkedEpic);
            return true;
        }
        return false;
    }

    @Override
    public List<Subtask> getEpicSubtask(UUID id) {
        if (!epicTasks.containsKey(id)) {
            return null;
        }
        return epicTasks.get(id).getIdSubtasks().stream().map(subtasks::get).toList();
    }

    @Override
    public List<Task> getTaskHistory() {
        return manager.getHistory();
    }

    private void changerEpicStatus(Epic epic) {
        List<Subtask> epicSubtask = epic.getIdSubtasks().stream().map(subtasks::get).toList();
        if (!epicSubtask.isEmpty()) {
            Statuses finalStatus = null;
            for (Subtask subtask : epicSubtask) {
                if (subtask.getStatus() == Statuses.DONE && (finalStatus == Statuses.DONE || finalStatus == null)) {
                    finalStatus = Statuses.DONE;
                } else if (subtask.getStatus() == Statuses.NEW && (finalStatus == Statuses.NEW || finalStatus == null)) {
                    finalStatus = Statuses.NEW;
                } else {
                    finalStatus = Statuses.IN_PROGRESS;
                    break;
                }
            }
            epic.setStatus(finalStatus);
        } else {
            epic.setStatus(Statuses.NEW);
        }
    }
}