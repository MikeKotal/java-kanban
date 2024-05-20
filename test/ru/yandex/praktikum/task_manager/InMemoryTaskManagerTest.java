package ru.yandex.praktikum.task_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void setUp() {
        super.taskManager = (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    void whenEpicHasDoneSubtaskThenEpicIsDone() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        subtask.setStatus(Statuses.DONE);
        taskManager.changerEpicStatus(epic1);

        assertEquals(Statuses.DONE, epic1.getStatus(), "Некорректный статус эпика");
    }

    @Test
    void whenEpicHasSubtaskInProgressThenEpicInProgress() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask2 = new Subtask("Взять сливу", "Для радости", current.plusHours(1),
                durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        subtask.setStatus(Statuses.DONE);
        subtask2.setStatus(Statuses.IN_PROGRESS);
        taskManager.changerEpicStatus(epic1);

        assertEquals(Statuses.IN_PROGRESS, epic1.getStatus(), "Некорректный статус эпика");
    }

    @Test
    void whenEpicHasSubtaskInNewStatusThenEpicInNew() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.changerEpicStatus(epic1);

        assertEquals(Statuses.NEW, epic1.getStatus(), "Некорректный статус эпика");
    }

    @Test
    void whenIntersectionTasksEqualsThenReturnFalse() {
        task1.setId(UUID.randomUUID());
        assertFalse(taskManager.checkTimeIntersections(task1, task1),
                "Некорректный результат вычсления пересечений для одинковых объектов");
    }

    @Test
    void whenStartTask1BeforeEndTask2ThenReturnTrue() {
        task1.setId(UUID.randomUUID());
        task2.setId(UUID.randomUUID());
        task2.setStartTime(current.plusMinutes(5));
        task2.setDuration(5L);
        assertTrue(taskManager.checkTimeIntersections(task1, task2),
                "Объекты пересекаются, некорректный результат");
    }

    @Test
    void whenTask1BeforeTask2ThenReturnFalse() {
        task1.setId(UUID.randomUUID());
        task2.setId(UUID.randomUUID());
        assertFalse(taskManager.checkTimeIntersections(task1, task2),
                "Объекты не пересекаются, некорректный результат");
    }

    @Test
    void whenSubtasksWithoutTimeThenEpicTimeIsEmpty() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливу", "Для радости", current.plusHours(1),
                durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        subtask.setStartTime(null);
        subtask.setDuration(0L);
        subtask1.setStartTime(null);
        subtask1.setDuration(0L);
        taskManager.changerEpicDuration(epic1);

        assertNull(epic1.getStartTime(), "Время старта епика должно быть пустым");
        assertTrue(epic1.getDuration().isZero(), "Продолжительность выполнения епика должно быть пустым");
        assertNull(epic1.getEndTime(), "Время завершения епика должно быть пустым");
    }

    @Test
    void whenSubtasksHasTimeThenEpicShouldReturnTime() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливу", "Для радости", current.plusMinutes(30),
                durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        subtask.setStartTime(current.minusHours(1));
        subtask.setDuration(durationInMinutes);
        subtask1.setStartTime(current.plusHours(1));
        subtask1.setDuration(durationInMinutes);
        taskManager.changerEpicDuration(epic1);
        LocalDateTime expectedEndTime = current.plusHours(1).plusMinutes(durationInMinutes);
        long expectedDuration = 30L;

        assertEquals(current.minusHours(1), epic1.getStartTime(), "Некорректное время старта решения епика");
        assertEquals(expectedDuration, epic1.getDuration().toMinutes(), "Некорректная продолжительность эпика");
        assertEquals(expectedEndTime, epic1.getEndTime(), "Некорректное время завершения эпика");
    }

    @Test
    void whenEpicHasTimeWithoutSubtasksThenEpicShouldReturnEmptyTime() {
        epic1.setStartTime(current);
        epic1.setDuration(durationInMinutes);
        epic1.setEndTime(current.plusHours(1));
        taskManager.changerEpicDuration(epic1);
        long expectedDuration = 0;

        assertNull(epic1.getStartTime(), "Время старта решения эпика должно быть пустым");
        assertEquals(expectedDuration, epic1.getDuration().toMinutes(), "Продолжительность эпика должна быть 0");
        assertNull(epic1.getEndTime(), "Ввремя завершения эпика должно быть пустым");
    }
}