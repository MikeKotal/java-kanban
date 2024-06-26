package ru.yandex.praktikum.task_manager;

import ru.yandex.praktikum.exceptions.ManagerSaveException;
import ru.yandex.praktikum.exceptions.ManagerUploadException;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;
import ru.yandex.praktikum.task_tracker.TaskTypes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File taskFile;

    public FileBackedTaskManager(File taskFile) {
        this.taskFile = taskFile;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager;
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            taskManager = new FileBackedTaskManager(file);
            fileReader.readLine(); //скипаем строку с наименованием полей
            while (fileReader.ready()) {
                String[] taskInfo = fileReader.readLine().split(",");
                UUID id = UUID.fromString(taskInfo[0]);
                TaskTypes taskTypes = TaskTypes.valueOf(taskInfo[1]);
                String name = taskInfo[2];
                Statuses status = Statuses.valueOf(taskInfo[3]);
                String description = taskInfo[4];
                LocalDateTime startTime = LocalDateTime.parse(taskInfo[5]);
                Long durationInMinutes = Long.parseLong(taskInfo[6]);
                LocalDateTime endTime = LocalDateTime.parse(taskInfo[7]);
                UUID epicId = taskInfo.length == 9 ? UUID.fromString(taskInfo[8]) : null;
                switch (taskTypes) {
                    case EPIC:
                        taskManager.epicTasks.put(id, new Epic(name, description, id, status, startTime, durationInMinutes, endTime));
                        break;
                    case TASK:
                        Task task = new Task(name, description, id, status, startTime, durationInMinutes);
                        taskManager.tasks.put(id, task);
                        taskManager.sortedTasks.add(task);
                        break;
                    case SUBTASK:
                        Subtask subtask = new Subtask(name, description, id, status, startTime, durationInMinutes, epicId);
                        taskManager.subtasks.put(id, subtask);
                        taskManager.epicTasks.get(epicId).addSubtask(id);
                        taskManager.sortedTasks.add(subtask);
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerUploadException("Произошла ошибка при восстановении задач из файла taskFile.csv");
        }
        taskManager.epicTasks.values().forEach(taskManager::changerEpicDuration);
        return taskManager;
    }

    private void save() {
        List<Epic> epics = getEpicTasks();
        List<Task> tasks = getTasks();
        List<Subtask> subtasks = getSubtasks();

        List<String> tasksForFile = new ArrayList<>();
        tasksForFile.add("id,type,name,status,description,startTime,durationInMinutes,endTime,epic");

        tasksForFile.addAll(epics.stream().map(Epic::toStringFile).toList());
        tasksForFile.addAll(tasks.stream().map(Task::toStringFile).toList());
        tasksForFile.addAll(subtasks.stream().map(Subtask::toStringFile).toList());
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(taskFile))) {
            tasksForFile.forEach(task -> {
                try {
                    outputStream.write(task.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new ManagerSaveException("Произошла ошибка при сохранении задач в файл taskFile.csv");
                }
            });
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при сохранении задач в файл taskFile.csv");
        }
    }

    @Override
    public void clearEpicTasks() {
        super.clearEpicTasks();
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public UUID createEpic(Epic epic) {
        UUID epicId = super.createEpic(epic);
        save();
        return epicId;
    }

    @Override
    public UUID createTask(Task task) {
        UUID taskId = super.createTask(task);
        save();
        return taskId;
    }

    @Override
    public UUID createSubtask(Subtask subtask) {
        UUID subtaskId = super.createSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public boolean removeEpic(UUID id) {
        boolean result = super.removeEpic(id);
        save();
        return result;
    }

    @Override
    public boolean removeTask(UUID id) {
        boolean result = super.removeTask(id);
        save();
        return result;
    }

    @Override
    public boolean removeSubtask(UUID id) {
        boolean result = super.removeSubtask(id);
        save();
        return result;
    }
}
