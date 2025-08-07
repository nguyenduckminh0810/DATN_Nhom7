package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.dto.Badge;
import com.nhom7.quiz.quizapp.repository.ResultRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class BadgeService {

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    // Lấy tất cả badge có thể đạt được
    public List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();
        
        for (Badge.BadgeType type : Badge.BadgeType.values()) {
            badges.add(new Badge(
                type.name(),
                type.getDisplayName(),
                type.getDescription(),
                type.getDisplayName().split(" ")[0], // Lấy icon
                type,
                getRequirementForBadge(type)
            ));
        }
        
        return badges;
    }

    // Lấy badge của user cụ thể
    public List<Badge> getUserBadges(Long userId) {
        List<Badge> allBadges = getAllBadges();
        List<Badge> userBadges = new ArrayList<>();
        
        for (Badge badge : allBadges) {
            if (hasEarnedBadge(userId, badge.getType())) {
                badge.setEarned(true);
                badge.setEarnedAt(LocalDateTime.now());
                userBadges.add(badge);
            }
        }
        
        return userBadges;
    }

    // Lấy badge theo loại
    public Badge getBadgeByType(String badgeType) {
        try {
            Badge.BadgeType type = Badge.BadgeType.valueOf(badgeType);
            return new Badge(
                type.name(),
                type.getDisplayName(),
                type.getDescription(),
                type.getDisplayName().split(" ")[0],
                type,
                getRequirementForBadge(type)
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Kiểm tra xem user có đạt được badge mới không
    public List<Badge> checkNewBadges(Long userId) {
        List<Badge> newBadges = new ArrayList<>();
        List<Badge> allBadges = getAllBadges();
        
        for (Badge badge : allBadges) {
            if (!hasEarnedBadge(userId, badge.getType()) && 
                meetsRequirement(userId, badge.getType())) {
                badge.setEarned(true);
                badge.setEarnedAt(LocalDateTime.now());
                newBadges.add(badge);
            }
        }
        
        return newBadges;
    }

    // Kiểm tra xem user có đạt được badge chưa
    private boolean hasEarnedBadge(Long userId, Badge.BadgeType type) {
        // TODO: Implement badge tracking in database
        // Hiện tại chỉ check requirement
        return meetsRequirement(userId, type);
    }

    // Kiểm tra xem user có đáp ứng yêu cầu của badge không
    private boolean meetsRequirement(Long userId, Badge.BadgeType type) {
        switch (type) {
            case PERFECT_SCORE:
                return hasPerfectScore(userId);
            case FASTEST_TIME:
                return hasFastestTime(userId);
            case TOP_3:
                return isInTop3(userId);
            case DAILY_STREAK:
                return hasDailyStreak(userId);
            case MASTER_QUIZ:
                return getTotalScore(userId) >= 1000;
            case EXPERT_QUIZ:
                return getTotalScore(userId) >= 500;
            case DEDICATED_LEARNER:
                return getAttemptCount(userId) >= 50;
            case ACTIVE_LEARNER:
                return getAttemptCount(userId) >= 20;
            default:
                return false;
        }
    }

    // Lấy requirement cho badge
    private int getRequirementForBadge(Badge.BadgeType type) {
        switch (type) {
            case MASTER_QUIZ:
                return 1000;
            case EXPERT_QUIZ:
                return 500;
            case DEDICATED_LEARNER:
                return 50;
            case ACTIVE_LEARNER:
                return 20;
            default:
                return 0;
        }
    }

    // Helper methods
    private boolean hasPerfectScore(Long userId) {
        // TODO: Implement
        return false;
    }

    private boolean hasFastestTime(Long userId) {
        // TODO: Implement
        return false;
    }

    private boolean isInTop3(Long userId) {
        // TODO: Implement
        return false;
    }

    private boolean hasDailyStreak(Long userId) {
        // TODO: Implement
        return false;
    }

    private int getTotalScore(Long userId) {
        // TODO: Implement
        return 0;
    }

    private int getAttemptCount(Long userId) {
        // TODO: Implement
        return 0;
    }
}
