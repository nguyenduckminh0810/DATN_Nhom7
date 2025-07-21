package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

public class QuizAttemptDTO {
    private Long id;
    private String username;
    private String quizTitle;
    private int score;
    private LocalDateTime attemptedAt;
    private int timeTaken; // thời gian làm bài (giây)

    public QuizAttemptDTO(Long id, String username, String quizTitle, int score, LocalDateTime attemptedAt, int timeTaken) {
        this.id = id;
        this.username = username;
        this.quizTitle = quizTitle;
        this.score = score;
        this.attemptedAt = attemptedAt;
        this.timeTaken = timeTaken;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }
} 