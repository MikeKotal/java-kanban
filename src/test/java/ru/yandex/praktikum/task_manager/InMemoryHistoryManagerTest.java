package ru.yandex.praktikum.task_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Epic epic;
    private Task task;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        epic = new Epic("Поход в магазин", "Встречаем гостей");
        task = new Task("Позвонить другу", "Уточнить место встречи");
        subtask = new Subtask("Взять молоко", "Для кашки", epic);
    }

    @Test
    void whenAdded3TasksThenReturn3ElementsFromHistory() {
        int expectedTaskCount = 3;
        historyManager.add(epic);
        historyManager.add(task);
        historyManager.add(subtask);
        List<Task> history = historyManager.getHistory();

        assertEquals(expectedTaskCount, history.size(), "Некорректное количество задач в истории просмотра");
        assertEquals(epic, history.getFirst(), "Эпик должен быть первым в списке");
        assertEquals(task, history.get(1), "Задача должна быть второй в списке");
        assertEquals(subtask, history.getLast(), "Подзадача должна быть последней в списке");
    }

    @Test
    void whenAdded11TasksThenReturn10ElementsFromHistory() {
        int expectedHistorySize = 10;
        historyManager.add(epic);
        for (int i = 0; i < expectedHistorySize; i++) {
            historyManager.add(new Task(String.format("Задача №%s", i + 1), "Тест"));
        }
        List<Task> history = historyManager.getHistory();

        assertEquals(expectedHistorySize, history.size(), "В истории просмотра задач некорректное число задач");
        assertEquals("Задача №1", history.getFirst().getName(), "Первая задача некорректная");
        assertEquals("Задача №10", history.getLast().getName(), "последняя задача некорректная");
    }
}