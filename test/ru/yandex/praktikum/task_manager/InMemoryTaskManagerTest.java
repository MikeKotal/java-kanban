package ru.yandex.praktikum.task_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private final Task task1 = new Task("Позвонить другу", "Уточнить место встречи");
    private final Task task2 = new Task("Поставить чайник", "Гостям нужен чайок");
    private final Epic epic1 = new Epic("Поход в магазин", "Встречаем гостей");
    private final Epic epic2 = new Epic("Сделать уроки", "Уроки на понедельник");
    public static final int EXPECTED_TASK_COUNT = 2;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

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
    void whenAddedTwoSubtasksThenReturnSubtaskList() {
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", epic1));
        taskManager.createSubtask(new Subtask("Сделать английский", "Present Simple", epic2));

        assertEquals(EXPECTED_TASK_COUNT, taskManager.getSubtasks().size(),
                "Некорректное количество подзадач в списке");
    }

    @Test
    void whenEpicListClearedThenEpicAndSubtaskShouldBeEmpty() {
        taskManager.createEpic(epic1);
        taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", epic1));
        taskManager.clearEpicTasks();

        assertTrue(taskManager.getEpicTasks().isEmpty(), "Список епиков не пустой");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач епика не пустой");
    }

    @Test
    void whenTaskListClearedThenTaskListShouldBeEmpty() {
        taskManager.createTask(task1);
        taskManager.clearTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Список задач не пустой");
    }

    @Test
    void whenSubtaskListClearedThenSubtaskListShouldBeEmpty() {
        taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки", epic1));
        taskManager.clearSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой");
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
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
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
    void whenCreatedTwoSimilarTasksThenReturnExistTaskId() {
        UUID taskId = taskManager.createTask(task1);
        UUID anotherTaskId = taskManager.createTask(task1);

        assertEquals(taskId, anotherTaskId, "Было создано 2 одинаковых задачи с разными Id");
    }

    @Test
    void whenSubtaskIsCreatedThenShouldReturnId() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);

        assertNotNull(subtaskId, "После создания подзадачи должен был присвоится Id");
        assertEquals(subtaskId, subtask.getId(), "Подзадаче присвоен некорректный Id");
    }

    @Test
    void whenCreatedTwoSimilarSubtasksThenReturnExistSubtaskId() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
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
    void whenUpdateStatusSubtaskThenUpdatedOnlyStatus() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
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
    void whenSubtaskChangedStatusOnDoneThenEpicHasDone() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        taskManager.createSubtask(subtask);
        subtask.setStatus(Statuses.DONE);
        taskManager.updateSubtask(subtask);
        Epic updatedEpic = taskManager.getEpic(epic1.getId());

        assertEquals(Statuses.DONE, updatedEpic.getStatus(), "Некорректный статус эпика");
    }

    @Test
    void whenOneOfSubtaskChangedStatusInProgressThenEpicInProgress() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        Subtask subtask2 = new Subtask("Взять сливу", "Для радости", epic1);
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
    void whenSubtaskHasStatusNewThenEpicHasStatusNew() {
        taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        taskManager.createSubtask(subtask);
        Epic epic = taskManager.getEpic(epic1.getId());

        assertEquals(Statuses.NEW, epic.getStatus(), "Некорректный статус эпика");
    }

    @Test
    void whenEpicRemoveThenReturnTrue() {
        UUID epicId = taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        taskManager.createSubtask(subtask);

        assertTrue(taskManager.removeEpic(epicId), "Эпик не был удален");
    }

    @Test
    void whenTaskRemoveByIdThenReturnTrue() {
        UUID taskId = taskManager.createTask(task1);

        assertTrue(taskManager.removeTask(taskId), "Задача не была удалена");
    }

    @Test
    void whenSubtaskRemoveByIdThenReturnTrue() {
        UUID epicId = taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        UUID subtaskId = taskManager.createSubtask(subtask);
        Epic linkedEpic = taskManager.getEpic(epicId);

        assertTrue(taskManager.removeSubtask(subtaskId), "Подзадача не была удалена");
        assertTrue(linkedEpic.getIdSubtasks().isEmpty(),
                "Подзадача не была удалена из списка в связанном эпике");
    }

    @Test
    void whenAddSubtaskIntoEpicThenSubtaskListIsNotEmpty() {
        int expectedSubtaskCount = 1;
        UUID epicId = taskManager.createEpic(epic1);
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
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
        Subtask subtask = new Subtask("Взять молоко", "Для кашки", epic1);
        taskManager.getSubtask(taskManager.createSubtask(subtask));
        List<Task> history = taskManager.getTaskHistory();

        assertEquals(expectedTaskCount, history.size(), "Некорректное количество задач в истории просмотра");
    }

    @Test
    void whenGetTaskByUnknownIdThenReturnEmptyHistory() {
        taskManager.getEpic(UUID.randomUUID());
        List<Task> history = taskManager.getTaskHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой");
    }
}