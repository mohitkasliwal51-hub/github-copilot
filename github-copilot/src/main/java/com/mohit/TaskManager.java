package com.mohit;

import java.util.ArrayList;
import java.util.List;

class Task {
    private String description;
    private boolean done;

    public Task(String description) {
        this.description = description;
        this.done = false;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}

public class TaskManager {
    private List<Task> tasks;

    public TaskManager() {
        this.tasks = new ArrayList<>();
    }

    public void addTask(String description) {
        tasks.add(new Task(description));
    }

    public List<Task> listTasks() {
        return new ArrayList<>(tasks);
    }

    public void markTaskAsDone(String description) {
        for (Task task : tasks) {
            if (task.getDescription().equals(description)) {
                task.setDone(true);
                break;
            }
        }
    }

    public boolean removeTask(String description) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getDescription().equals(description)) {
                tasks.remove(i); // removes first matching task
                return true;
            }
        }
        return false;
    }
}