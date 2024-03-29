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
    private HashMap<UUID, Epic> epicTasks = new HashMap<>();
    private HashMap<UUID, Task> tasks = new HashMap<>();
    private HashMap<UUID, Subtask> subtasks = new HashMap<>();

    public List<Epic> getEpicTasks() {
        epicTasks.values().forEach(manager::add);
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<Task> getTasks() {
        tasks.values().forEach(manager::add);
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        subtasks.values().forEach(manager::add);
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
        if (!epicTasks.containsKey(id)) {
            System.out.printf("Эпик с тикетом '%s' отсутствет\n", id);
            return null;
        }
        manager.add(epicTasks.get(id));
        return epicTasks.get(id);
    }

    @Override
    public Task getTask(UUID id) {
        if (!tasks.containsKey(id)) {
            System.out.printf("Задача с тикетом '%s' отсутствет\n", id);
            return null;
        }
        manager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Subtask getSubtask(UUID id) {
        if (!subtasks.containsKey(id)) {
            System.out.printf("Подзадача с тикетом '%s' отсутствет\n", id);
            return null;
        }
        manager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public UUID createEpic(Epic epic) {
        if (epic.getId() != null && epicTasks.containsKey(epic.getId())) {
            System.out.println("Такой эпик уже существует!");
            return epic.getId();
        }
        epic.setId(UUID.randomUUID());
        epicTasks.put(epic.getId(), epic);
        System.out.println("Эпик создан");
        return epic.getId();
    }

    @Override
    public UUID createTask(Task task) {
        if (task.getId() != null && tasks.containsKey(task.getId())) {
            System.out.println("Такая задача уже существует!");
            return task.getId();
        }
        task.setId(UUID.randomUUID());
        tasks.put(task.getId(), task);
        System.out.println("Задача создана");
        return task.getId();
    }

    @Override
    public UUID createSubtask(Subtask subtask) {
        if (!epicTasks.containsKey(subtask.getEpicId())) {
            System.out.println("При создании подзадачи эпик не был определен!");
            return subtask.getId();
        }
        if (subtask.getId() != null && subtasks.containsKey(subtask.getId())) {
            System.out.println("Такая подзадача уже существует!");
            return subtask.getId();
        }
        subtask.setId(UUID.randomUUID());
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic.addSubtask(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            changerEpicStatus(epic);
            System.out.println("Подзадача создана");
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
            System.out.printf("Задача типа 'Epic' с идентификатором '%s' обновлена\n", epic.getId());
        } else {
            System.out.printf("Задачи типа 'Epic' с идентификатором '%s' нет в списке\n", epic.getId());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            System.out.printf("Задача типа 'Task' с идентификатором '%s' обновлена\n", task.getId());
        } else {
            System.out.printf("Задачи типа 'Task' с идентификатором '%s' нет в списке\n", task.getId());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            changerEpicStatus(epicTasks.get(subtask.getEpicId()));
            System.out.printf("Задача типа 'Subtask' с идентификатором '%s' обновлена\n", subtask.getId());
        } else {
            System.out.printf("Задачи типа 'Subtask' с идентификатором '%s' нет в списке\n", subtask.getId());
        }
    }

    @Override
    public boolean removeEpic(UUID id) {
        Epic deleteEpic = epicTasks.remove(id);
        if (deleteEpic != null) {
            deleteEpic.getIdSubtasks().forEach(taskId -> subtasks.remove(taskId));
            return true;
        }
        System.out.printf("Эпик с тикетом '%s' отсутствует\n", id);
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
        System.out.printf("Подзадачи с тикетом '%s' нет в списке", id);
        return false;
    }

    @Override
    public List<Subtask> getEpicSubtask(UUID id) {
        if (!epicTasks.containsKey(id)) {
            System.out.printf("Эпик с тикетом '%s' отсутствет\n", id);
            return null;
        }
        return epicTasks.get(id).getIdSubtasks().stream().map(subtaskId -> subtasks.get(subtaskId)).toList();
    }

    @Override
    public HistoryManager getManager() {
        return manager;
    }

    private void changerEpicStatus(Epic epic) {
        List<Subtask> epicSubtask = epic.getIdSubtasks().stream().map(subtaskId -> subtasks.get(subtaskId)).toList();
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