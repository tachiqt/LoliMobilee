package com.example.lolimobilee;

public class Task {
    private String id;
    private String title;
    private String date;
    private String description;
    private boolean isPriority;
    private boolean isChecked;
    private String status;
    public Task() {
        this.status = "incomplete";
        this.isChecked = false;
    }

    public Task(String id, String title, String date, String description, boolean isPriority) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.isPriority = isPriority;
        this.status = "incomplete";
        this.isChecked = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPriority() { return isPriority; }
    public void setPriority(boolean priority) { isPriority = priority; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { this.isChecked = checked; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isComplete() {
        return "done".equalsIgnoreCase(status);
    }
}
