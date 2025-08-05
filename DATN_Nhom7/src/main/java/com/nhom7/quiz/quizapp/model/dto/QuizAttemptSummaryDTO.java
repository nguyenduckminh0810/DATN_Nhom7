package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

public class QuizAttemptSummaryDTO {
    private Long id;
    private String userName;
    private int score;
    private int timeTaken;
    private LocalDateTime attemptedAt;

    public QuizAttemptSummaryDTO() {
    }

    public QuizAttemptSummaryDTO(Long id, String userName, int score, int timeTaken, LocalDateTime attemptedAt) {
        this.id = id;
        this.userName = userName;
        this.score = score;
        this.timeTaken = timeTaken;
        this.attemptedAt = attemptedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
}