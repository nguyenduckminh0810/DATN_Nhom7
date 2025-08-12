package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

public class ActivityDTO {
    private Long userId;
    private String fullName;
    private String activityType;
    private LocalDateTime activityTime;

    public ActivityDTO(Long userId, String fullName, String activityType, LocalDateTime activityTime) {
        this.userId = userId;
        this.fullName = fullName;
        this.activityType = activityType;
        this.activityTime = activityTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public LocalDateTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }

}
