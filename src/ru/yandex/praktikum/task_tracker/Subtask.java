package ru.yandex.praktikum.task_tracker;

import java.time.LocalDateTime;
import java.util.UUID;

import static ru.yandex.praktikum.task_tracker.TaskTypes.SUBTASK;

public class Subtask extends Task {
    private final UUID epicId;

    public Subtask(String name, String description, LocalDateTime startTime, Long durationInMinutes, Epic epic) {
        super(name, description, startTime, durationInMinutes);
        epicId = epic.getId();
    }

    public Subtask(String name, String description, UUID id, Statuses status, LocalDateTime startTime,
                   Long durationInMinutes, UUID epicId) {
        super(name, description, startTime, durationInMinutes);
        this.id = id;
        this.status = status;
        this.epicId = epicId;
    }

    public UUID getEpicId() {
        return epicId;
    }

    @Override
    public String toStringFile() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", id, SUBTASK, name, status, description, startTime,
                duration.toMinutes(), epicId);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
