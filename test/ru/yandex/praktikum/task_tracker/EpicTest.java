package ru.yandex.praktikum.task_tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {
    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic("Тест", "Тестович");
    }

    @Test
    void whenAddSubtaskThenSizeSubtasksIncrease() {
        int expectedSubtaskCount = 1;
        UUID subtaskId = UUID.randomUUID();
        epic.setId(UUID.randomUUID());

        assertTrue(epic.addSubtask(subtaskId), "Значение id подзадачи не было добавлено в список");
        assertEquals(expectedSubtaskCount, epic.getIdSubtasks().size(), "Некорректное количество id в списке");
        assertEquals(subtaskId, epic.getIdSubtasks().getFirst(), "Некорректное значение в списке подзадач");
    }

    @Test
    void whenAddEpicIdIntoSubtaskIdThenFalse() {
        UUID epicId = UUID.randomUUID();
        epic.setId(epicId);

        assertFalse(epic.addSubtask(epicId), "Сам епик не может быть добавлен в список подзадач");
    }
}