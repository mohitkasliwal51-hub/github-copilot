package com.mohit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new TaskManager();
    }

    @Test
    void addTask_shouldAddTaskWithNotDoneStatus() {
        taskManager.addTask("Buy milk");

        List<Task> tasks = listTasks();
        assertEquals(1, tasks.size());
        assertEquals("Buy milk", tasks.get(0).getDescription());
        assertFalse(tasks.get(0).isDone());
    }

    @Test
    void addTask_shouldAddMultipleTasks() {
        addTasks("Task 1", "Task 2", "Task 3");

        assertEquals(3, listTasks().size());
    }

    @Test
    void listTasks_shouldReturnEmptyListInitially() {
        assertTrue(listTasks().isEmpty());
    }

    @Test
    void listTasks_shouldReturnDefensiveCopy() {
        taskManager.addTask("Original task");

        List<Task> tasks = listTasks();
        tasks.clear();

        assertEquals(1, listTasks().size());
    }

    @Test
    void markTaskAsDone_shouldMarkMatchingTask() {
        taskManager.addTask("Complete task");

        taskManager.markTaskAsDone("Complete task");

        assertTrue(listTasks().get(0).isDone());
    }

    @Test
    void markTaskAsDone_shouldDoNothingForNonExistentTask() {
        taskManager.addTask("Task 1");

        taskManager.markTaskAsDone("Non-existent task");

        assertFalse(listTasks().get(0).isDone());
    }

    @Test
    void markTaskAsDone_shouldMarkBothWhenCalledForEachTask() {
        addTasks("Task 1", "Task 2");

        taskManager.markTaskAsDone("Task 1");
        taskManager.markTaskAsDone("Task 2");

        List<Task> tasks = listTasks();
        assertTrue(tasks.get(0).isDone());
        assertTrue(tasks.get(1).isDone());
    }

    @Test
    void markTaskAsDone_shouldMarkOnlyFirstMatchingTask() {
        addTasks("Duplicate", "Duplicate");

        taskManager.markTaskAsDone("Duplicate");

        List<Task> tasks = listTasks();
        assertTrue(tasks.get(0).isDone());
        assertFalse(tasks.get(1).isDone());
    }

    @Test
    void removeTask_shouldRemoveExistingTaskAndReturnTrue() {
        taskManager.addTask("Remove me");

        boolean removed = taskManager.removeTask("Remove me");

        assertTrue(removed);
        assertTrue(listTasks().isEmpty());
    }

    @Test
    void removeTask_shouldReturnFalseWhenTaskDoesNotExist() {
        taskManager.addTask("Task 1");

        boolean removed = taskManager.removeTask("Non-existent task");

        assertFalse(removed);
        assertEquals(1, listTasks().size());
    }

    @Test
    void removeTask_shouldRemoveOnlyFirstMatchingTask() {
        addTasks("Duplicate", "Duplicate");

        boolean removed = taskManager.removeTask("Duplicate");

        assertTrue(removed);
        assertEquals(1, listTasks().size());
        assertEquals("Duplicate", listTasks().get(0).getDescription());
    }

    @Test
    void removeTask_shouldSupportMultipleRemovals() {
        addTasks("Task 1", "Task 2", "Task 3");

        taskManager.removeTask("Task 1");
        taskManager.removeTask("Task 3");

        List<Task> tasks = listTasks();
        assertEquals(1, tasks.size());
        assertEquals("Task 2", tasks.get(0).getDescription());
    }

    private void addTasks(String... descriptions) {
        for (String description : descriptions) {
            taskManager.addTask(description);
        }
    }

    private List<Task> listTasks() {
        return taskManager.listTasks();
    }
}
