package com.nhom7.quiz.quizapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "quiz_attempt_progress")
public class QuizAttemptProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnore
    private QuizAttempt attempt;

    @Column(name = "current_question_index", nullable = false)
    private Integer currentQuestionIndex = 0;

    @Column(name = "time_remaining", nullable = false)
    private Integer timeRemaining;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "last_saved_at")
    private LocalDateTime lastSavedAt = LocalDateTime.now();

    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public QuizAttemptProgress() {
    }

    public QuizAttemptProgress(QuizAttempt attempt, Integer timeRemaining) {
        this.attempt = attempt;
        this.timeRemaining = timeRemaining;
        this.startedAt = LocalDateTime.now();
        this.lastSavedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(QuizAttempt attempt) {
        this.attempt = attempt;
    }

    public Integer getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(Integer currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public Integer getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(Integer timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = LocalDateTime.now();
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Helper methods
    public void updateProgress(Integer questionIndex, Integer timeRemaining) {
        this.currentQuestionIndex = questionIndex;
        this.timeRemaining = timeRemaining;
        this.lastSavedAt = LocalDateTime.now();
    }

    public void markAsInactive() {
        this.isActive = false;
        this.lastSavedAt = LocalDateTime.now();
    }
}
