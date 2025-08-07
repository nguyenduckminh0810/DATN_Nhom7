package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String type;
    private Long userId;
    private String title;
    private String message;
    private String priority;
    private Long relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;
    private boolean isRead;
    private LocalDateTime createdAt;

    // ✅ CONSTRUCTOR CHO WEBSOCKET MESSAGE
    public NotificationDTO(String type, Long userId, String title, String message, 
                          String priority, Long relatedEntityId, String relatedEntityType, String actionUrl) {
        this.type = type;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.actionUrl = actionUrl;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ CONSTRUCTOR TỪ ENTITY
    public NotificationDTO(Long id, String type, Long userId, String title, String message, 
                          String priority, Long relatedEntityId, String relatedEntityType, 
                          String actionUrl, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.actionUrl = actionUrl;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 