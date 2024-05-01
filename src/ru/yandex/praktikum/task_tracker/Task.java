package ru.yandex.praktikum.task_tracker;

import java.util.Objects;
import java.util.UUID;

import static ru.yandex.praktikum.task_tracker.TaskTypes.TASK;

public class Task {
    protected String name;
    protected String description;
    protected UUID id;
    protected Statuses status;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = Statuses.NEW;
    }

    public Task(String name, String description, UUID id, Statuses status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Statuses getStatus() {
        return status;
    }

    public void setStatus(Statuses status) {
        this.status = status;
    }

    public String toStringFile() {
        return String.format("%s,%s,%s,%s,%s", id, TASK, name, status, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
