package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

public class Badge {
    private String id;
    private String name;
    private String description;
    private String icon;
    private BadgeType type;
    private int requirement;
    private LocalDateTime earnedAt;
    private boolean isEarned;

    public enum BadgeType {
        PERFECT_SCORE("💯 Perfect Score", "Đạt 100% chính xác"),
        FASTEST_TIME("⚡ Speed Demon", "Hoàn thành nhanh nhất"),
        TOP_3("🥉 Top 3", "Nằm trong top 3"),
        DAILY_STREAK("🔥 Daily Streak", "Làm quiz liên tiếp 3 ngày"),
        MASTER_QUIZ("🏆 Master", "Tổng điểm >= 1000"),
        EXPERT_QUIZ("🥇 Expert", "Tổng điểm >= 500"),
        DEDICATED_LEARNER("📚 Dedicated", "Làm >= 50 quiz"),
        ACTIVE_LEARNER("📖 Active", "Làm >= 20 quiz");

        private final String displayName;
        private final String description;

        BadgeType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public Badge() {}

    public Badge(String id, String name, String description, String icon, BadgeType type, int requirement) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.requirement = requirement;
        this.isEarned = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public BadgeType getType() {
        return type;
    }

    public void setType(BadgeType type) {
        this.type = type;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }

    public boolean isEarned() {
        return isEarned;
    }

    public void setEarned(boolean earned) {
        isEarned = earned;
    }
}
