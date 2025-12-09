package com.shivprakash.to_dolist;

public class RiwayatModel {
    private int id;
    private String task, category, priority, due_date, due_time, status;

    public RiwayatModel(int id,String task, String category, String priority, String due_date, String due_time, String status) {
        this.id = id;
        this.task = task;
        this.category = category;
        this.priority = priority;
        this.due_date = due_date;
        this.due_time = due_time;
        this.status = status;
    }
    public int getId() {return id;}
    public String getTask() {
        return task;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getDue_date() {
        return due_date;
    }

    public String getDue_time() {
        return due_time;
    }

    public String getStatus() {
        return status;
    }
}
