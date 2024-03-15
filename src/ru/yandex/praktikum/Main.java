package ru.yandex.praktikum;

import ru.yandex.praktikum.task_manager.TaskManager;
import ru.yandex.praktikum.task_tracker.Epic;
import ru.yandex.praktikum.task_tracker.Statuses;
import ru.yandex.praktikum.task_tracker.Subtask;
import ru.yandex.praktikum.task_tracker.Task;


public class Main {

    public static void main(String[] args) {
        Task task1 = new Task("Позвонить другу", "Уточнить место встречи");
        Task task2 = new Task("Поставить чайник", "Гостям нужен чайок");

        Epic epic1 = new Epic("Поход в магазин", "Встречаем гостей");

        Subtask subtask1 = new Subtask("Взять молоко", "Для кашки", epic1);
        Subtask subtask2 = new Subtask("Взять сливу", "Для радости", epic1);

        Epic epic2 = new Epic("Сделать уроки", "Уроки на понедельник");

        Subtask subtask3 = new Subtask("Сделать английский", "Present Simple", epic2);

        TaskManager manager = new TaskManager();

        System.out.println("Создаем задачи");

        System.out.println(manager.createTask(task1));
        System.out.println(manager.createTask(task2));

        System.out.println(manager.createEpic(epic1));
        System.out.println(manager.createEpic(epic2));

        System.out.println(manager.createSubtask(subtask1, epic1));
        System.out.println(manager.createSubtask(subtask2, epic1));
        System.out.println(manager.createSubtask(subtask3, epic2));

        System.out.println("Проверка создания задач");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubtasks());

        System.out.println("Апдейтим задания");
        task1.setStatus(Statuses.IN_PROGRESS);
        System.out.println(manager.updateTask(task1));
        task2.setStatus(Statuses.DONE);
        System.out.println(manager.updateTask(task2));

        subtask1.setStatus(Statuses.IN_PROGRESS);
        System.out.println(manager.updateSubtask(subtask1));
        subtask3.setStatus(Statuses.DONE);
        System.out.println(manager.updateSubtask(subtask3));

        System.out.println("Смотрим задачи после изменения");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubtasks());

        System.out.println("Удалаем подзадачу");
        System.out.printf("Подзадача удалена: %s\n", manager.removeSubtask(subtask3.getId()));

        System.out.println("Смотрим удаления подзадач в эпиках и смену статусов самого эпика");
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getEpicSubtask(epic2));

        System.out.println("Удаляем епик и задачу");
        System.out.printf("Задача удалена: %s\n", manager.removeTask(task1.getId()));
        System.out.printf("Епик удален: %s\n", manager.removeEpic(epic1.getId()));

        System.out.println("Смотрим изменения по задачам");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubtasks());

        System.out.println(manager.getEpicSubtask(epic1));
    }
}
