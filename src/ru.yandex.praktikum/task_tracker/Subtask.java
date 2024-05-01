package ru.yandex.praktikum.task_tracker;

import java.util.UUID;

import static ru.yandex.praktikum.task_tracker.TaskTypes.SUBTASK;

public class Subtask extends Task {
    private final UUID epicId;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        epicId = epic.getId();
    }

    public Subtask(String name, String description, UUID id, Statuses status, UUID epicId) {
        super(name, description);
        this.id = id;
        this.status = status;
        this.epicId = epicId;
    }

    public UUID getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s", id, SUBTASK, name, status, description, epicId);
    }
}
