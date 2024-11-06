package com.example.lolimobilee;

public class JournalEntry {

    private String title;
    private String date;
    private String preview; // Added preview field

    public JournalEntry(String title, String date, String preview) {
        this.title = title;
        this.date = date;
        this.preview = preview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }
}
