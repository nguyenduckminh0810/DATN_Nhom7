package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDetailDTO {
    private Long id;
    private String title;
    private String image;
    private boolean isPublic;
    private LocalDateTime createdAt;

    // Creator info
    private String creatorName;
    private String creatorAvatar;
    private Long creatorId;

    // Category info
    private String categoryName;
    private List<String> tags;

    // Statistics
    private int totalQuestions;
    private int totalPoints;
    private int totalTime;
    private int totalPlays;
    private double averageScore;
    private int uniqueParticipants;
    private double completionRate;
    private int averageTime;

    // Recent attempts
    private List<QuizAttemptSummaryDTO> recentAttempts;

    public QuizDetailDTO() {
    }

    public QuizDetailDTO(Long id, String title, String image, boolean isPublic, LocalDateTime createdAt,
            String creatorName, String creatorAvatar, Long creatorId, String categoryName, List<String> tags,
            int totalQuestions, int totalPoints, int totalTime, int totalPlays, double averageScore,
            int uniqueParticipants, double completionRate, int averageTime) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.creatorName = creatorName;
        this.creatorAvatar = creatorAvatar;
        this.creatorId = creatorId;
        this.categoryName = categoryName;
        this.tags = tags;
        this.totalQuestions = totalQuestions;
        this.totalPoints = totalPoints;
        this.totalTime = totalTime;
        this.totalPlays = totalPlays;
        this.averageScore = averageScore;
        this.uniqueParticipants = uniqueParticipants;
        this.completionRate = completionRate;
        this.averageTime = averageTime;
    }

    // Getters and Setters
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public void setCreatorAvatar(String creatorAvatar) {
        this.creatorAvatar = creatorAvatar;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(int totalPlays) {
        this.totalPlays = totalPlays;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getUniqueParticipants() {
        return uniqueParticipants;
    }

    public void setUniqueParticipants(int uniqueParticipants) {
        this.uniqueParticipants = uniqueParticipants;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public int getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(int averageTime) {
        this.averageTime = averageTime;
    }

    public List<QuizAttemptSummaryDTO> getRecentAttempts() {
        return recentAttempts;
    }

    public void setRecentAttempts(List<QuizAttemptSummaryDTO> recentAttempts) {
        this.recentAttempts = recentAttempts;
    }
}