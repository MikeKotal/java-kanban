package ru.yandex.praktikum.task_tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Epic extends Task {
    private final List<UUID> idSubtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public void addSubtask(UUID idSubtask) {
        idSubtasks.add(idSubtask);
    }

    public List<UUID> getIdSubtasks() {
        return idSubtasks;
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
