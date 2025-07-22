package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

public class QuizPendingDTO {
    private Long id;
    private String title;
    private String creatorName;
    private LocalDateTime createdAt;

    public QuizPendingDTO(Long id, String title, String creatorName, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.creatorName = creatorName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
