package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.dto.Badge;
import com.nhom7.quiz.quizapp.service.BadgeService;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    // Lấy tất cả badge có thể đạt được
    @GetMapping
    public ResponseEntity<List<Badge>> getAllBadges() {
        List<Badge> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    // Lấy badge của user cụ thể
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Badge>> getUserBadges(@PathVariable Long userId) {
        List<Badge> userBadges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(userBadges);
    }

    // Lấy badge theo loại
    @GetMapping("/type/{badgeType}")
    public ResponseEntity<Badge> getBadgeByType(@PathVariable String badgeType) {
        Badge badge = badgeService.getBadgeByType(badgeType);
        return ResponseEntity.ok(badge);
    }

    // Kiểm tra xem user có đạt được badge mới không
    @GetMapping("/user/{userId}/check-new")
    public ResponseEntity<List<Badge>> checkNewBadges(@PathVariable Long userId) {
        List<Badge> newBadges = badgeService.checkNewBadges(userId);
        return ResponseEntity.ok(newBadges);
    }
}
