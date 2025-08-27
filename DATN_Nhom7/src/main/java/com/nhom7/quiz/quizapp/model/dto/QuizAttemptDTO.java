package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;
import com.nhom7.quiz.quizapp.model.AttemptStatus;

public class QuizAttemptDTO {
    private Long id;
    private String username;
    private String quizTitle;
    private int score;
    private LocalDateTime attemptedAt;
    private int timeTaken; // thời gian làm bài (giây)
    private AttemptStatus status; // ✅ THÊM TRẠNG THÁI

    public QuizAttemptDTO(Long id, String username, String quizTitle, int score, LocalDateTime attemptedAt, int timeTaken, AttemptStatus status) {
        this.id = id;
        this.username = username;
        this.quizTitle = quizTitle;
        this.score = score;
        this.attemptedAt = attemptedAt;
        this.timeTaken = timeTaken;
        this.status = status;
    }
    
    // ✅ CONSTRUCTOR CŨ ĐỂ TƯƠNG THÍCH NGƯỢC
    public QuizAttemptDTO(Long id, String username, String quizTitle, int score, LocalDateTime attemptedAt, int timeTaken) {
        this(id, username, quizTitle, score, attemptedAt, timeTaken, AttemptStatus.SUBMITTED);
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
    
    // ✅ GETTER/SETTER CHO STATUS
    public AttemptStatus getStatus() {
        return status;
    }
    
    public void setStatus(AttemptStatus status) {
        this.status = status;
    }
} 