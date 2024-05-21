package ru.yandex.praktikum.task_tracker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.yandex.praktikum.task_tracker.TaskTypes.EPIC;

public class Epic extends Task {
    private final List<UUID> idSubtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, null, 0L);
    }

    public Epic(String name, String description, UUID id, Statuses status, LocalDateTime startTime,
                Long durationInMinutes, LocalDateTime endTime) {
        super(name, description, startTime, durationInMinutes);
        this.id = id;
        this.status = status;
        this.endTime = endTime;
    }

    public boolean addSubtask(UUID idSubtask) {
        if (id.equals(idSubtask)) {
            return false;
        }
        idSubtasks.add(idSubtask);
        return true;
    }

    public List<UUID> getIdSubtasks() {
        return idSubtasks;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toStringFile() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", id, EPIC, name, status, description, startTime,
                duration.toMinutes(), endTime);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "idSubtasks=" + idSubtasks +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", startTime=" + startTime +
                ", duration=" + duration.toMinutes() +
                ", endTime=" + endTime +
                '}';
    }
}
