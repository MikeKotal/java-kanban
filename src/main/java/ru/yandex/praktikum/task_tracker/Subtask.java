package ru.yandex.praktikum.task_tracker;

import java.util.UUID;

public class Subtask extends Task {
    private final UUID epicId;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        epicId = epic.getId();
    }

    public UUID getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
