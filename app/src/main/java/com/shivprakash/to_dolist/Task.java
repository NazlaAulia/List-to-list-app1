package com.shivprakash.to_dolist;

import com.google.gson.annotations.SerializedName;

public class Task {
    private int id;

    @SerializedName("task")
    private String taskName;

    @SerializedName("priority")
    private String priority;

    @SerializedName("category")
    private String category;

    @SerializedName("notes")
    private String notes;

    // JSON: due_date  -> Java: dueDate
    @SerializedName("due_date")
    private String dueDate;

    // JSON: due_time  -> Java: dueTime
    @SerializedName("due_time")
    private String dueTime;

    // di DB 0/1 → simpan sebagai int, tapi sediakan helper boolean untuk UI
    @SerializedName("completed")
    private int completed;

    public Task() {}

    public Task(int id, String taskName, String priority, String category,
                String notes, String dueDate, String dueTime, int completed) {
        this.id = id;
        this.taskName = taskName;
        this.priority = priority;
        this.category = category;
        this.notes = notes;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.completed = completed; // <- INT, bukan boolean
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }

    // Helper buat UI yang butuh boolean
    public boolean isCompleted() { return completed == 1; }
    public void setCompleted(boolean value) { this.completed = value ? 1 : 0; }

    // Kalau butuh akses integer mentahnya:
    public int getCompletedInt() { return completed; }
    public void setCompletedInt(int completed) { this.completed = completed; }
}
