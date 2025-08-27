package com.nhom7.quiz.quizapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "priority", length = 20, nullable = false)
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    // Audience: USER | ADMIN | ALL (mặc định USER)
    @Column(name = "audience", length = 10, nullable = false)
    private String audience = "USER";

    @Column(name = "related_entity_id")
    private Long relatedEntityId; // ID của quiz, user, etc.

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // QUIZ, USER, RESULT, etc.

    @Column(name = "action_url", length = 255)
    private String actionUrl; // URL để navigate khi click notification

    public Notification() {
    }

    public Notification(User user, String content, String notificationType, String title) {
        this.user = user;
        this.content = content;
        this.notificationType = notificationType;
        this.title = title;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.priority = "NORMAL";
    }

    public Notification(User user, String content, String notificationType, String title,
            String priority, Long relatedEntityId, String relatedEntityType, String actionUrl) {
        this.user = user;
        this.content = content;
        this.notificationType = notificationType;
        this.title = title;
        this.priority = priority;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.actionUrl = actionUrl;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.audience = "USER";
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    // ✅ GETTERS/SETTERS MỚI
    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
