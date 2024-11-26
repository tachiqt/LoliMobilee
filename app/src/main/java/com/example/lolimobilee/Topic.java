package com.example.lolimobilee;

import java.util.List;

public class Topic {
    private String title;
    private List<String> questions;

    public Topic(String title, List<String> questions) {
        this.title = title;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }
}
