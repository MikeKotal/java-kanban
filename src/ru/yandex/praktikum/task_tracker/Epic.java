package ru.yandex.praktikum.task_tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.yandex.praktikum.task_tracker.TaskTypes.EPIC;

public class Epic extends Task {
    private final List<UUID> idSubtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, UUID id, Statuses status) {
        super(name, description);
        this.id = id;
        this.status = status;
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
    public String toStringFile() {
        return String.format("%s,%s,%s,%s,%s", id, EPIC, name, status, description);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "idSubtasks=" + idSubtasks +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
