package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager manager = Managers.getDefaultHistory();
    private final Map<UUID, Epic> epicTasks;
    private final Map<UUID, Task> tasks;
    private final Map<UUID, Subtask> subtasks;
    private final Set<Task> sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager() {
        epicTasks = new HashMap<>();
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public InMemoryTaskManager(Map<UUID, Epic> epicTasks, Map<UUID, Task> tasks, Map<UUID, Subtask> subtasks) {
        this.epicTasks = epicTasks;
        this.tasks = tasks;
        this.subtasks = subtasks;
        sortedTasks.addAll(tasks.values().stream().filter(task -> (task.getStartTime() != null &&
                !task.getDuration().isZero())).toList());
        sortedTasks.addAll(subtasks.values().stream().filter(subtask -> (subtask.getStartTime()
                != null && !subtask.getDuration().isZero())).toList());
        epicTasks.values().forEach(this::changerEpicDuration);
    }

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
        epicTasks.values().forEach(epic -> manager.remove(epic.getId()));
        subtasks.values().forEach(subtask -> manager.remove(subtask.getId()));
        epicTasks.clear();
        subtasks.clear();
    }

    @Override
    public void clearTasks() {
        tasks.values().forEach(task -> {
            manager.remove(task.getId());
            sortedTasks.remove(task);
        });
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.values().forEach(subtask -> {
            manager.remove(subtask.getId());
            sortedTasks.remove(subtask);
        });
        subtasks.clear();
        epicTasks.values().forEach(epic -> {
            epic.getIdSubtasks().clear();
            changerEpicStatus(epic);
            changerEpicDuration(epic);
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
        if (getTasks().stream().noneMatch(sortedTask -> checkTimeIntersections(task, sortedTask))) {
            task.setId(UUID.randomUUID());
            if (task.getStartTime() != null && !task.getDuration().isZero()) {
                sortedTasks.add(task);
            }
            tasks.put(task.getId(), task);
            return task.getId();
        }
        return null;
    }

    @Override
    public UUID createSubtask(Subtask subtask) {
        if (!epicTasks.containsKey(subtask.getEpicId())) {
            return subtask.getId();
        }
        if (subtask.getId() != null && subtasks.containsKey(subtask.getId())) {
            return subtask.getId();
        }
        if (getSubtasks().stream().noneMatch(sortedSubtask -> checkTimeIntersections(subtask, sortedSubtask))) {
            subtask.setId(UUID.randomUUID());
            Epic epic = epicTasks.get(subtask.getEpicId());
            if (subtask.getStartTime() != null && !subtask.getDuration().isZero()) {
                sortedTasks.add(subtask);
            }
            epic.addSubtask(subtask.getId());
            subtasks.put(subtask.getId(), subtask);
            changerEpicStatus(epic);
            changerEpicDuration(epic);
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
            if (getTasks().stream().noneMatch(sortedTask -> checkTimeIntersections(task, sortedTask))) {
                tasks.put(task.getId(), task);
                sortedTasks.remove(getTask(task.getId()));
                if (task.getStartTime() != null && !task.getDuration().isZero()) {
                    sortedTasks.add(task);
                }
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            if (getSubtasks().stream().noneMatch(sortedSubtask -> checkTimeIntersections(subtask, sortedSubtask))) {
                Epic epic = epicTasks.get(subtask.getEpicId());
                subtasks.put(subtask.getId(), subtask);
                sortedTasks.remove(getSubtask(subtask.getId()));
                if (subtask.getStartTime() != null && !subtask.getDuration().isZero()) {
                    sortedTasks.add(subtask);
                }
                changerEpicStatus(epic);
                changerEpicDuration(epic);
            }
        }
    }

    @Override
    public boolean removeEpic(UUID id) {
        Epic deleteEpic = epicTasks.remove(id);
        if (deleteEpic != null) {
            deleteEpic.getIdSubtasks().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                manager.remove(subtaskId);
            });
            manager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTask(UUID id) {
        Task task = tasks.remove(id);
        if (task != null) {
            manager.remove(id);
            sortedTasks.remove(task);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSubtask(UUID id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic linkedEpic = epicTasks.get(subtask.getEpicId());
            linkedEpic.getIdSubtasks().remove(id);
            sortedTasks.remove(subtask);
            changerEpicStatus(linkedEpic);
            changerEpicDuration(linkedEpic);
            manager.remove(id);
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

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(sortedTasks);
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

    private void changerEpicDuration(Epic epic) {
        List<Subtask> subtasksList = epic.getIdSubtasks().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getStartTime() != null)
                .filter(sortedTasks::contains)
                .toList();
        if (!subtasksList.isEmpty()) {
            Optional<Subtask> startTime = subtasksList.stream().min(Comparator.comparing(Task::getStartTime));
            startTime.ifPresent(subtask -> epic.setStartTime(subtask.getStartTime()));
            List<Subtask> endTimes = subtasksList.stream().filter(subtask -> subtask.getEndTime() != null).toList();
            if (!endTimes.isEmpty()) {
                Optional<Subtask> endTime = endTimes.stream().max(Comparator.comparing(Task::getEndTime));
                endTime.ifPresent(subtask -> epic.setEndTime(subtask.getEndTime()));
                epic.setDuration(endTimes.stream().mapToLong(subtask -> subtask.getDuration().toMinutes()).sum());
            }
        } else {
            epic.setStartTime(null);
            epic.setDuration(0L);
        }
    }

    private boolean checkTimeIntersections(Task task1, Task task2) {
        if (task1.equals(task2)) {
            return false;
        }
        if (task1.getStartTime() != null && task1.getEndTime() != null
                && task2.getStartTime() != null && task2.getEndTime() != null) {
            return (task1.getStartTime().isBefore(task2.getEndTime()) && task1.getEndTime().isAfter(task2.getStartTime()));
        }
        return false;
    }
}