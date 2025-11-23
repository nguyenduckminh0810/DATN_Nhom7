package com.nhom7.quiz.quizapp.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

public class QuizResumeDTO {
    
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer currentQuestionIndex;
    private Integer timeRemaining;
    private String answersJson;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSavedAt;
    
    private Boolean hasTimeLimit;
    private Integer totalQuestions;
    
    // Constructor
    public QuizResumeDTO() {}
    
    // Getters and Setters
    public Long getAttemptId() {
        return attemptId;
    }
    
    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }
    
    public Long getQuizId() {
        return quizId;
    }
    
    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }
    
    public String getQuizTitle() {
        return quizTitle;
    }
    
    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
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
    
    public String getAnswersJson() {
        return answersJson;
    }
    
    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
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
        this.lastSavedAt = lastSavedAt;
    }
    
    public Boolean getHasTimeLimit() {
        return hasTimeLimit;
    }
    
    public void setHasTimeLimit(Boolean hasTimeLimit) {
        this.hasTimeLimit = hasTimeLimit;
    }
    
    public Integer getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}
