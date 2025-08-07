package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LeaderboardEntry {
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private int score;
    private int timeTaken; // giây
    private int rank;
    private List<String> badges; // huy hiệu
    private LocalDateTime completedAt;
    private Long quizId;
    private String quizTitle;

    // Constructor
    public LeaderboardEntry() {}

    public LeaderboardEntry(Long userId, String username, String fullName, String avatarUrl, 
                          int score, int timeTaken, int rank, List<String> badges, 
                          LocalDateTime completedAt, Long quizId, String quizTitle) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.score = score;
        this.timeTaken = timeTaken;
        this.rank = rank;
        this.badges = badges;
        this.completedAt = completedAt;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
}
