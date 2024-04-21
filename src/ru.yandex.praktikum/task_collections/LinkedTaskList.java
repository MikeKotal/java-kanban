package ru.yandex.praktikum.task_collections;

import java.util.ArrayList;

public class LinkedTaskList<T> {
    private Node<T> firstTask;
    private Node<T> lastTask;

    public void linkLast(T task) {
        final Node<T> preLastTask = lastTask;
        final Node<T> newNode = new Node<>(preLastTask, task, null);
        lastTask = newNode;
        if (preLastTask == null) {
            firstTask = newNode;
        } else {
            preLastTask.next = newNode;
        }
    }

    public ArrayList<T> getTasks() {
        ArrayList<T> tasks = new ArrayList<>();
        Node<T> currentNode = firstTask;
        while (currentNode != null) {
            tasks.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    public Node<T> getLastTask() {
        return lastTask;
    }

    public void removeNode(Node<T> taskNode) {
        final Node<T> next = taskNode.next;
        final Node<T> prev = taskNode.prev;

        if (prev == null) {
            firstTask = next;
        } else {
            prev.next = next;
            taskNode.prev = null;
        }

        if (next == null) {
            lastTask = prev;
        } else {
            next.prev = prev;
            taskNode.next = null;
        }

        taskNode.data = null;
    }
}
