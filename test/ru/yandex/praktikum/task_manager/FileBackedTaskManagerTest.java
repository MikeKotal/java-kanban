package ru.yandex.praktikum.task_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.praktikum.exceptions.ManagerSaveException;
import ru.yandex.praktikum.exceptions.ManagerUploadException;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;
import ru.yandex.praktikum.task_tracker.TaskTypes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = File.createTempFile("taskFile", ".csv");
        super.taskManager = new FileBackedTaskManager(testFile);
    }

    @Test
    public void whenCreatedThreeTasksThenSaveThreeTasksIntoFile() throws IOException {
        taskManager.createEpic(epic1);
        taskManager.createTask(task1);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки",
                current.plusHours(1), durationInMinutes, epic1));
        Subtask subtask = taskManager.getSubtask(subtaskId);

        String expectedSubtask = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", subtask.getId(), TaskTypes.SUBTASK,
                subtask.getName(), subtask.getStatus(), subtask.getDescription(), subtask.getStartTime(),
                subtask.getDuration().toMinutes(), subtask.getEndTime(), subtask.getEpicId());
        String expectedFirstFileLine = "id,type,name,status,description,startTime,durationInMinutes,endTime,epic";
        int expectedLinesIntoFile = 4;

        try (BufferedReader fileReader = new BufferedReader(new FileReader(testFile, StandardCharsets.UTF_8))) {
            List<String> result = fileReader.lines().toList();

            assertEquals(expectedLinesIntoFile, result.size(), "Некорректное количество записей в файле");
            assertEquals(expectedFirstFileLine, result.getFirst(), "Самая первая запись в файле некорректна");
            assertEquals(expectedSubtask, result.getLast(), "Самая последняя запись в файле некорректна");
        }
    }

    @Test
    public void whenDeletedOneOfThreeTasksThenShouldLeftTwoTasksIntoFile() throws IOException {
        taskManager.createEpic(epic1);
        UUID taskId = taskManager.createTask(task1);
        UUID subtaskId = taskManager.createSubtask(new Subtask("Взять молоко", "Для кашки",
                current.plusHours(1), durationInMinutes, epic1));
        taskManager.removeSubtask(subtaskId);
        Task createdTask = taskManager.getTask(taskId);

        String expectedTask = String.format("%s,%s,%s,%s,%s,%s,%s,%s", createdTask.getId(), TaskTypes.TASK,
                createdTask.getName(), createdTask.getStatus(), createdTask.getDescription(),
                createdTask.getStartTime(), createdTask.getDuration().toMinutes(), createdTask.getEndTime());
        int expectedLinesIntoFile = 3;

        try (BufferedReader fileReader = new BufferedReader(new FileReader(testFile, StandardCharsets.UTF_8))) {
            List<String> result = fileReader.lines().toList();

            assertEquals(expectedLinesIntoFile, result.size(), "Некорректное количество записей в файле");
            assertEquals(expectedTask, result.getLast(), "Самая последняя запись в файле некорректна");
        }
    }

    @Test
    public void whenBackupThreeTasksThenReturnThisThreeTasks() {
        List<String> tasksForFile = new ArrayList<>();
        UUID epicId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID subtaskId = UUID.randomUUID();

        String stringEpic = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                epicId, TaskTypes.EPIC, "Поход в магазин", Statuses.NEW, "Встречаем гостей", current.minusDays(2),
                durationInMinutes, current.plusMinutes(durationInMinutes));
        String stringTask = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                taskId, TaskTypes.TASK, "Позвонить другу", Statuses.NEW, "Уточнить место встречи", current.plusHours(1),
                durationInMinutes, current.plusHours(1).plusMinutes(durationInMinutes));
        String stringSubtask = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                subtaskId, TaskTypes.SUBTASK, "Взять молоко", Statuses.NEW, "Для кашки", current.minusHours(1),
                durationInMinutes, current.minusHours(1).plusMinutes(durationInMinutes), epicId);
        tasksForFile.add("id,type,name,status,description,startTime,durationInMinutes,endTime,epic");

        tasksForFile.add(stringEpic);
        tasksForFile.add(stringTask);
        tasksForFile.add(stringSubtask);

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile))) {
            tasksForFile.forEach(task -> {
                try {
                    outputStream.write(task.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new ManagerSaveException("Произошла ошибка при сохранении задач в файл taskFile.csv");
                }
            });
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при сохранении задач в файл taskFile.csv");
        }

        taskManager = FileBackedTaskManager.loadFromFile(testFile);
        int expectedCountTasks = 1;
        long expectedDuration = 15L;

        List<Epic> epics = taskManager.getEpicTasks();
        List<Task> tasks = taskManager.getTasks();
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(expectedCountTasks, epics.size(), "Некорректное количество эпиков в списке");
        assertEquals(expectedCountTasks, tasks.size(), "Некорректное количество задач в списке");
        assertEquals(expectedCountTasks, subtasks.size(), "Некорректное количество подзадач в списке");

        assertEquals(epicId, epics.getFirst().getId(), "Некорректный эпик");
        assertEquals(stringTask, tasks.getFirst().toStringFile(), "Некорректная задача");
        assertEquals(stringSubtask, subtasks.getFirst().toStringFile(), "Некорректная подзадача");
        assertEquals(epics.getFirst().getIdSubtasks().getFirst(), subtasks.getFirst().getId(),
                "В эпике некорректный id подзадачи");

        assertEquals(current.minusHours(1), epics.getFirst().getStartTime(),
                "Произошел некорректный перерасчет начала выполнения для эпика");
        assertEquals(current.minusHours(1).plusMinutes(durationInMinutes), epics.getFirst().getEndTime(),
                "Произошел некорректный перерасчет окончания времени выполнения для эпика");
        assertEquals(expectedDuration, epics.getFirst().getDuration().toMinutes(),
                "Некорректная продолжительность выполнения эпика в минутах");
    }

    @Test
    public void whenBackupEmptyFilesThenReturnEmptyListOfTask() {
        taskManager = FileBackedTaskManager.loadFromFile(testFile);

        List<Epic> epics = taskManager.getEpicTasks();
        List<Task> tasks = taskManager.getTasks();
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertTrue(epics.isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(tasks.isEmpty(), "Список задач должен быть пустым");
        assertTrue(subtasks.isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    public void checkManagerUploadException() {
        assertThrows(ManagerUploadException.class,
                () -> FileBackedTaskManager.loadFromFile(Path.of("Test").toFile()),
                "Выброшена некорректная ошибка");
    }
}
