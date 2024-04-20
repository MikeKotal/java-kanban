package ru.yandex.praktikum.task_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Epic epic;
    private Task task;
    private Subtask subtask;
    private List<Task> history;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        epic = new Epic("Поход в магазин", "Встречаем гостей");
        task = new Task("Позвонить другу", "Уточнить место встречи");
        subtask = new Subtask("Взять молоко", "Для кашки", epic);
        epic.setId(UUID.randomUUID());
        task.setId(UUID.randomUUID());
        subtask.setId(UUID.randomUUID());
    }

    @Test
    void whenAdded3TasksThenReturn3ElementsFromHistory() {
        int expectedTaskCount = 3;
        historyManager.add(epic);
        historyManager.add(task);
        historyManager.add(subtask);
        history = historyManager.getHistory();

        assertEquals(expectedTaskCount, history.size(), "Некорректное количество задач в истории просмотра");
        assertEquals(epic, history.getFirst(), "Эпик должен быть первым в списке");
        assertEquals(task, history.get(1), "Задача должна быть второй в списке");
        assertEquals(subtask, history.getLast(), "Подзадача должна быть последней в списке");
    }

    @Test
    void whenAdded3SimilarTasks3TimesThenReturn3ElementsFromHistory() {
        int expectedHistorySize = 3;
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < expectedHistorySize; k++) {
                historyManager.add(epic);
                historyManager.add(task);
                historyManager.add(subtask);
            }
        }
        history = historyManager.getHistory();

        assertEquals(expectedHistorySize, history.size(), "В истории просмотра задач некорректное число задач");
        assertEquals("Поход в магазин", history.getFirst().getName(), "Первая задача некорректная");
        assertEquals("Взять молоко", history.getLast().getName(), "Последняя задача некорректная");
    }

    @Test
    void whenAddedEmptyTaskThenReturnEmptyHistory() {
        historyManager.add(null);
        history = historyManager.getHistory();

        assertNull(history, "История просмотра должна быть пустой");
    }

    @Test
    void whenRemoveTaskByIdThenHistoryDecrease() {
        int sizeBeforeRemoving = 3;
        int sizeAfterRemoving = 2;
        String expectedErrorMessage = "Некорректное количество элементов в истории";

        historyManager.add(epic);
        historyManager.add(task);
        historyManager.add(subtask);

        assertEquals(sizeBeforeRemoving, historyManager.getHistory().size(), expectedErrorMessage);

        historyManager.remove(task.getId());

        assertEquals(sizeAfterRemoving, historyManager.getHistory().size(), expectedErrorMessage);
    }
}