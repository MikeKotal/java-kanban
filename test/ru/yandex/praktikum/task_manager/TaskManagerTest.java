package ru.yandex.praktikum.task_manager;

import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected final LocalDateTime current = LocalDateTime.now();
    protected final long durationInMinutes = 15L;
    protected final Task task1 = new Task("Позвонить другу", "Уточнить место встречи", current,
            durationInMinutes);
    protected final Task task2 = new Task("Поставить чайник", "Гостям нужен чайок", current.plusHours(1),
            durationInMinutes);
    protected final Epic epic1 = new Epic("Поход в магазин", "Встречаем гостей");
    protected final Epic epic2 = new Epic("Сделать уроки", "Уроки на понедельник");
    public static final int EXPECTED_TASK_COUNT = 2;

    @Test
    void whenAddedTwoEpicsThenReturnEpicList() {
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        assertEquals(EXPECTED_TASK_COUNT, taskManager.getEpicTasks().size(),
                "Некорректное количество епиков в списке");
    }

    @Test
    void whenAddedTwoTasksThenReturnTaskList() {
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        assertEquals(EXPECTED_TASK_COUNT, taskManager.getTasks().size(),
                "Некорректное колчество задач в списке");
    }

    @Test
    void whenAddedTwoIntersectionsTasksThenReturnOnlyOne() {
        taskManager.createTask(task1);
        task2.setStartTime(current.plusMinutes(14));
        taskManager.createTask(task2);

        int expectedTasksCount = 1;
        assertEquals(expectedTasksCount, taskManager.getTasks().size(),
                "В случае пересечения 2-х задач, должна остаться только одна");
    }

    @Test
    void whenAddedTwoSubtasksThenReturnSubtaskList() {
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current, durationInMinutes,
                epic1));
        taskManager.createSubtask(new Subtask("Сделать английский", "Present Simple",
                current.plusHours(1), durationInMinutes, epic2));

        assertEquals(EXPECTED_TASK_COUNT, taskManager.getSubtasks().size(),
                "Некорректное количество подзадач в списке");
    }

    @Test
    void whenAddedTwoIntersectionsSubtasksThenReturnOnlyOne() {
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current, durationInMinutes,
                epic1));
        taskManager.createSubtask(new Subtask("Сделать английский", "Present Simple",
                current.plusMinutes(14), durationInMinutes, epic2));

        int expectedTasksCount = 1;
        assertEquals(expectedTasksCount, taskManager.getSubtasks().size(),
                "В случае пересечения 2-х подзадач, должна остаться только одна");
    }

    @Test
    void whenEpicListClearedThenEpicAndSubtaskShouldBeEmpty() {
        taskManager.createEpic(epic1);
        taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", current, durationInMinutes,
                epic1));
        taskManager.clearEpicTasks();

        assertTrue(taskManager.getEpicTasks().isEmpty(), "Список епиков не пустой");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач епика не пустой");
    }

    @Test
    void whenTaskListClearedThenTaskListShouldBeEmpty() {
        taskManager.createTask(task1);
        taskManager.clearTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Список задач не пустой");
        assertFalse(taskManager.getPrioritizedTasks().contains(task1),
                "Из сортированного списка не была удалена задача");
    }

    @Test
    void whenSubtaskListClearedThenSubtaskListShouldBeEmpty() {
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes,
                epic1);
        taskManager.createSubtask(subtask);
        taskManager.clearSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой");
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask),
                "Из сортированного списка не была удалена подзадача");
        assertNull(epic1.getStartTime(), "Дата начала эпика должна быть пустой");
        assertNull(epic1.getEndTime(), "Дата окончания эпика должна быть пустой");
    }

    @Test
    void whenGetEpicByIdThenEpicNotEmpty() {
        Epic createdEpic = taskManager.getEpic(taskManager.createEpic(epic1));

        assertNotNull(createdEpic, "При попытке получить эпик вернулся null");
        assertEquals(epic1, createdEpic, "Епики должны быть идентичными");
    }

    @Test
    void whenGetEpicByUnknownIdThenEpicIsEmpty() {
        Epic createdEpic = taskManager.getEpic(UUID.randomUUID());

        assertNull(createdEpic, "При попытке получить неизвестный эпик вернулся не null");
    }

    @Test
    void whenGetTaskByIdThenTaskNotEmpty() {
        Task createdTask = taskManager.getTask(taskManager.createTask(task1));

        assertNotNull(createdTask, "При попытке получить задачу вернулся null");
        assertEquals(task1, createdTask, "Задачи должны быть идентичными");
    }

    @Test
    void whenGetTaskByUnknownIdThenTaskIsEmpty() {
        Task createdTask = taskManager.getTask(UUID.randomUUID());

        assertNull(createdTask, "При попытке получить неизвестную задачу вернулся не null");
    }

    @Test
    void whenGetSubtaskByIdThenSubtaskNotEmpty() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask createdSubtask = taskManager.getSubtask(taskManager.createSubtask(subtask));

        assertNotNull(createdSubtask, "При попытке получить подзадачу вернулся null");
        assertEquals(subtask, createdSubtask, "Подадачи должны быть идентичными");
    }

    @Test
    void whenGetSubtaskByUnknownIdThenSubtaskIsEmpty() {
        Subtask createdSubtask = taskManager.getSubtask(UUID.randomUUID());

        assertNull(createdSubtask, "При попытке получить неизвестную подзадачу вернулся не null");
    }

    @Test
    void whenEpicIsCreatedThenShouldReturnId() {
        UUID epicId = taskManager.createEpic(epic1);

        assertNotNull(epicId, "После создания эпика должен был присвоится Id задачи");
        assertEquals(epicId, epic1.getId(), "Эпику присвоен некорректный Id");
    }

    @Test
    void whenCreatedTwoSimilarEpicsThenReturnExistEpicId() {
        UUID epicId = taskManager.createEpic(epic1);
        UUID anotherEpicId = taskManager.createEpic(epic1);

        assertEquals(epicId, anotherEpicId, "Было создано 2 одинаковых эпика с разными Id");
    }

    @Test
    void whenTaskIsCreatedThenShouldReturnId() {
        UUID taskId = taskManager.createTask(task1);

        assertNotNull(taskId, "После создания задачи должен был присвоится Id");
        assertEquals(taskId, task1.getId(), "Задаче присвоен некорректный Id");
    }

    @Test
    void whenAddedTwoTasksWithoutTimeThenReturnTwoTasks() {
        task1.setStartTime(null);
        task2.setDuration(0L);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        assertEquals(EXPECTED_TASK_COUNT, taskManager.getTasks().size(),
                "Некорректное количесство задач в списке");
    }

    @Test
    void whenCreatedTwoSimilarTasksThenReturnExistTaskId() {
        UUID taskId = taskManager.createTask(task1);
        UUID anotherTaskId = taskManager.createTask(task1);

        assertEquals(taskId, anotherTaskId, "Было создано 2 одинаковых задачи с разными Id");
    }

    @Test
    void whenSubtaskIsCreatedThenShouldReturnId() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);

        assertNotNull(subtaskId, "После создания подзадачи должен был присвоится Id");
        assertEquals(subtaskId, subtask.getId(), "Подзадаче присвоен некорректный Id");
    }

    @Test
    void whenCreatedTwoSimilarSubtasksThenReturnExistSubtaskId() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);
        UUID anotherSubtaskId = taskManager.createSubtask(subtask);

        assertEquals(subtaskId, anotherSubtaskId, "Были созданы 2 одинаковых подзадачи с разными Id");
    }

    @Test
    void whenUpdateEpicThenUpdateOnlyNameAndDescription() {
        String newName = "Тест";
        String newDescription = "Тестович";
        UUID epicId = taskManager.createEpic(epic1);
        epic1.setName(newName);
        epic1.setDescription(newDescription);
        taskManager.updateEpic(epic1);
        Epic updatedEpic = taskManager.getEpic(epicId);

        assertEquals(newName, updatedEpic.getName(), "Имя эпика не было изменено");
        assertEquals(newDescription, updatedEpic.getDescription(), "Описание эпика не было измненено");
        assertEquals(epicId, updatedEpic.getId(), "Id эпиков должно совпадать");
    }

    @Test
    void whenUpdateStatusTaskThenUpdatedOnlyStatus() {
        UUID taskId = taskManager.createTask(task1);
        String expectedName = task1.getName();
        String expectedDescription = task1.getDescription();
        task1.setStatus(Statuses.IN_PROGRESS);
        taskManager.updateTask(task1);
        Task updatedTask = taskManager.getTask(taskId);

        assertEquals(expectedName, updatedTask.getName(), "Имя задачи не должно быть изменено");
        assertEquals(expectedDescription, updatedTask.getDescription(), "Описание задачи не должно быть изменено");
        assertEquals(taskId, updatedTask.getId(), "Id задачи не должно быть изменено");
        assertEquals(Statuses.IN_PROGRESS, updatedTask.getStatus(), "У задачи не был изменен статус");
    }

    @Test
    void whenUpdateTimeTaskThenTimeIsUpdated() {
        task1.setStartTime(null);
        task1.setDuration(0L);
        UUID taskId = taskManager.createTask(task1);

        assertNotNull(taskId, "Задание не было успешно создано");
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(),
                "Задание с пустым временем не должно было попасть в отсортированный список");

        task1.setStartTime(current);
        task1.setDuration(durationInMinutes);
        taskManager.updateTask(task1);
        Task updatedTask = taskManager.getTask(taskId);
        int expectedTasksCount = 1;
        LocalDateTime expectedEndTime = current.plusMinutes(durationInMinutes);

        assertEquals(expectedTasksCount, taskManager.getPrioritizedTasks().size(),
                "Задача не была добавлена в сортированный список");
        assertEquals(expectedEndTime, updatedTask.getEndTime(), "Время окончания задачи не было обновлено");
    }

    @Test
    void whenUpdateStatusSubtaskThenUpdatedOnlyStatus() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);
        String expectedName = subtask.getName();
        String expectedDescription = subtask.getDescription();
        subtask.setStatus(Statuses.IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        Subtask updatedSubtask = taskManager.getSubtask(subtaskId);

        assertEquals(expectedName, updatedSubtask.getName(), "Имя подзадачи не должно быть изменено");
        assertEquals(expectedDescription, updatedSubtask.getDescription(), "Описание подзадачи не должно быть изменено");
        assertEquals(subtaskId, updatedSubtask.getId(), "Id подзадачи не должно быть изменено");
        assertEquals(Statuses.IN_PROGRESS, updatedSubtask.getStatus(), "У подзадачи не был изменен статус");
    }

    @Test
    void whenUpdateTimeSubtaskThenTimeIsUpdated() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", null, 0L, epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);

        assertNotNull(subtaskId, "Задание не было успешно создано");
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(),
                "Подзадача с пустым временем не должна была попасть в отсортированный список");

        subtask.setStartTime(current);
        subtask.setDuration(durationInMinutes);
        taskManager.updateSubtask(subtask);
        Subtask updatedSubtask = taskManager.getSubtask(subtaskId);
        int expectedTasksCount = 1;
        LocalDateTime expectedEndTime = current.plusMinutes(durationInMinutes);

        assertEquals(expectedTasksCount, taskManager.getPrioritizedTasks().size(),
                "Подзадача не была добавлена в сортированный список");
        assertEquals(expectedEndTime, updatedSubtask.getEndTime(), "Время окончания подзадачи не было обновлено");
    }

    @Test
    void whenSubtaskUpdatedStatusInProgressThenEpicInProgress() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask2 = new Subtask("Взять сливу", "Для радости", current.plusHours(1),
                durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        subtask.setStatus(Statuses.DONE);
        subtask2.setStatus(Statuses.IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        taskManager.updateSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpic(epic1.getId());

        assertEquals(Statuses.IN_PROGRESS, updatedEpic.getStatus(), "Некорректный статус эпика");
    }

    @Test
    void whenAddedTwoSubtaskWithTimeThenEpicTimeShouldBeReturned() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливу", "Для радости", current.plusHours(1),
                durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        Epic epic = taskManager.getEpic(epic1.getId());
        LocalDateTime expectedEndTime = current.plusHours(1).plusMinutes(durationInMinutes);
        long expectedDuration = 30L;

        assertEquals(current, epic.getStartTime(), "Некорректное время старта решения епика");
        assertEquals(expectedDuration, epic.getDuration().toMinutes(), "Некорректная продолжительность эпика");
        assertEquals(expectedEndTime, epic.getEndTime(), "Некорректное время завершения эпика");
    }

    @Test
    void checkChangeEpicDurationAfterDeleteAndUpdateSubtasks() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливу", "Для радости", current.plusHours(1),
                0L, epic1);
        Subtask subtask2 = new Subtask("Взять масло", "Для хлебушка", null,
                0L, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        Epic epic = taskManager.getEpic(epic1.getId());

        assertEquals(current, epic.getStartTime(), "Некорректное время начала эпика");
        assertEquals(durationInMinutes, epic.getDuration().toMinutes(), "Некорректная продолжительность эпика");
        assertEquals(current.plusMinutes(durationInMinutes), epic.getEndTime(),
                "Некорретное время завершения эпика");

        subtask.setDuration(120L);
        subtask1.setDuration(30L);
        subtask2.setStartTime(current.minusHours(1));
        subtask2.setDuration(55L);
        taskManager.updateSubtask(subtask);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        Epic epicUpdated = taskManager.getEpic(epic1.getId());
        LocalDateTime expectedStartTime = current.minusHours(1);
        LocalDateTime expectedEndTime = current.plusHours(2);
        long expectedDuration = 175L;

        assertEquals(expectedStartTime, epicUpdated.getStartTime(),
                "Был произведен некорректный перерасчет старта эпика");
        assertEquals(expectedDuration, epic.getDuration().toMinutes(), "Некорректная продолжительность эпика");
        assertEquals(expectedEndTime, epicUpdated.getEndTime(),
                "Был произведен некорректный перерасчет окончания эпика");
    }

    @Test
    void whenEpicRemoveThenReturnTrue() {
        UUID epicId = taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        taskManager.createSubtask(subtask);

        assertTrue(taskManager.removeEpic(epicId), "Эпик не был удален");
    }

    @Test
    void whenTaskRemoveByIdThenReturnTrue() {
        UUID taskId = taskManager.createTask(task1);

        assertTrue(taskManager.removeTask(taskId), "Задача не была удалена");
        assertFalse(taskManager.getPrioritizedTasks().contains(task1),
                "В сортированном списке после удаления задачи быть не должно");
    }

    @Test
    void whenSubtaskRemoveByIdThenReturnTrue() {
        UUID epicId = taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);
        Epic linkedEpic = taskManager.getEpic(epicId);

        assertTrue(taskManager.removeSubtask(subtaskId), "Подзадача не была удалена");
        assertTrue(linkedEpic.getIdSubtasks().isEmpty(),
                "Подзадача не была удалена из списка в связанном эпике");
    }

    @Test
    void whenSubtaskWithTimeRemoveThenEpicShouldChangeTime() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливу", "Для радости", current.plusHours(1),
                0L, epic1);
        Subtask subtask2 = new Subtask("Взять масло", "Для хлебушка", current.plusHours(1),
                durationInMinutes, epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.removeSubtask(subtaskId);
        Epic epic = taskManager.getEpic(epic1.getId());
        LocalDateTime expectedStartTime = current.plusHours(1);
        LocalDateTime expectedEndDate = expectedStartTime.plusMinutes(durationInMinutes);

        assertEquals(expectedStartTime, epic.getStartTime(), "Некорректный перерасчет времени начала эпика");
        assertEquals(expectedEndDate, epic.getEndTime(), "Некорректный перерасчет времени окончания эпика");
    }

    @Test
    void whenAddSubtaskIntoEpicThenSubtaskListIsNotEmpty() {
        int expectedSubtaskCount = 1;
        UUID epicId = taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        List<Subtask> epicSubtask = taskManager.getEpicSubtask(epicId);

        assertEquals(expectedSubtaskCount, epicSubtask.size(), "Некорректнное количество подзадач в эпике");
        assertNotNull(epicSubtask, "Список подзадач не должен быть пустым");
        assertEquals(subtask, epicSubtask.getFirst(), "Подзадача некорректна");
    }

    @Test
    void whenViewed3TasksThenReturn3ElementsFromHistory() {
        int expectedTaskCount = 3;
        taskManager.getEpic(taskManager.createEpic(epic1));
        taskManager.getTask(taskManager.createTask(task1));
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current.plusHours(1), durationInMinutes, epic1);
        taskManager.getSubtask(taskManager.createSubtask(subtask));
        List<Task> history = taskManager.getTaskHistory();

        assertEquals(expectedTaskCount, history.size(), "Некорректное количество задач в истории просмотра");
    }

    @Test
    void whenRemovedEpicThenSubtasksShouldBeDeletedFromHistory() {
        int expectedSizeBeforeRemoving = 4;
        List<Task> history;
        UUID epicId = taskManager.createEpic(epic1);

        taskManager.getEpic(epicId);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливки", "Для кофе", current.plusHours(1),
                durationInMinutes, epic1);
        Subtask subtask2 = new Subtask("Взять масло", "Для хлебушка", current.plusHours(2),
                durationInMinutes, epic1);
        taskManager.getSubtask(taskManager.createSubtask(subtask));
        taskManager.getSubtask(taskManager.createSubtask(subtask1));
        taskManager.getSubtask(taskManager.createSubtask(subtask2));
        history = taskManager.getTaskHistory();

        assertEquals(expectedSizeBeforeRemoving, history.size(),
                "Некорректное количество задач в истории просмотра");

        taskManager.removeEpic(epicId);
        history = taskManager.getTaskHistory();

        assertTrue(history.isEmpty(), "Подзадачи должны быть удалены из истории вместе в эпиком");
    }

    @Test
    void whenGetTaskByUnknownIdThenReturnEmptyHistory() {
        taskManager.getEpic(UUID.randomUUID());
        List<Task> history = taskManager.getTaskHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void whenClearEpicsThenEpicsAndSubtasksShouldBeDeletedFromHistory() {
        int expectedSizeBeforeCleared = 5;
        List<Task> history;

        taskManager.getEpic(taskManager.createEpic(epic1));
        taskManager.getEpic(taskManager.createEpic(epic2));
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливки", "Для кофе", current.plusHours(1),
                durationInMinutes, epic2);
        Subtask subtask2 = new Subtask("Взять масло", "Для хлебушка", current.plusHours(2),
                durationInMinutes, epic2);
        taskManager.getSubtask(taskManager.createSubtask(subtask));
        taskManager.getSubtask(taskManager.createSubtask(subtask1));
        taskManager.getSubtask(taskManager.createSubtask(subtask2));
        history = taskManager.getTaskHistory();

        assertEquals(expectedSizeBeforeCleared, history.size(),
                "Некорректное количество задач в истории просмотра");

        taskManager.clearEpicTasks();
        history = taskManager.getTaskHistory();

        assertTrue(history.isEmpty(), "Все эпики и связанные с ними подзадачи должны быть удалены из истории");
    }

    @Test
    void whenClearTasksThenTasksShouldBeDeletedFromHistory() {
        int expectedSizeBeforeCleared = 2;
        List<Task> history;

        taskManager.getTask(taskManager.createTask(task1));
        taskManager.getTask(taskManager.createTask(task2));
        history = taskManager.getTaskHistory();

        assertEquals(expectedSizeBeforeCleared, history.size(),
                "Некорректное количество задач в истории просмотра");

        taskManager.clearTasks();
        history = taskManager.getTaskHistory();

        assertTrue(history.isEmpty(), "Все задачи должны быть удалены из истории");
    }

    @Test
    void whenClearSubtasksThenSubtasksShouldBeDeletedFromHistory() {
        int expectedSizeBeforeCleared = 2;
        List<Task> history;

        taskManager.getEpic(taskManager.createEpic(epic1));
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current, durationInMinutes, epic1);
        taskManager.getSubtask(taskManager.createSubtask(subtask));
        history = taskManager.getTaskHistory();

        assertEquals(expectedSizeBeforeCleared, history.size(),
                "Некорректное количество задач в истории просмотра");

        taskManager.clearSubtasks();
        history = taskManager.getTaskHistory();

        assertEquals(expectedSizeBeforeCleared - 1, history.size(),
                "Все подзадачи должны быть удалены из истории");
        assertEquals("Поход в магазин", history.getFirst().getName(),
                "В истории должен остаться только эпик");
    }

    @Test
    void whenAddedTasksWithTimeThenReturnSortedListOfTasks() {
        taskManager.createEpic(epic1);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", current.minusHours(1), durationInMinutes, epic1);
        Subtask subtask1 = new Subtask("Взять сливу", "Для радости", current.plusHours(3),
                durationInMinutes, epic1);
        Subtask subtask2 = new Subtask("Взять масло", "Для хлебушка", current.minusMinutes(30),
                durationInMinutes, epic1);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        List<Task> tasks = taskManager.getPrioritizedTasks();

        assertEquals(subtask, tasks.getFirst(), "Некорректная задача вначале сортированного списка");
        assertEquals(subtask1, tasks.getLast(), "Некорректная задача в конце сортированного списка");
    }
}
