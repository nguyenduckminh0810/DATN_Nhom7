package com.nhom7.quiz.quizapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.dto.LeaderboardEntry;
import com.nhom7.quiz.quizapp.service.LeaderboardService;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    // ✅ MAIN ENDPOINT - Xử lý request chính từ frontend
    @GetMapping("")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "global") String type,
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) Long quizId) {

        try {
            List<LeaderboardEntry> leaderboard;

            if ("quiz".equals(type) && quizId != null) {
                leaderboard = leaderboardService.getQuizLeaderboard(quizId, limit);
            } else {
                leaderboard = leaderboardService.getGlobalLeaderboard(period, limit, offset);
            }

            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            System.err.println("❌ Error in getLeaderboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // Trả về list rỗng thay vì lỗi
        }
    }

    // 🔍 TEST ENDPOINT - Để debug
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "Leaderboard test endpoint working!",
                "timestamp", System.currentTimeMillis(),
                "status", "SUCCESS",
                "endpoint", "/api/leaderboard/test"));
    }

    // 🔍 TEST ENDPOINT - Kiểm tra kết nối database
    @GetMapping("/test-db")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        try {
            // Test kết nối database đơn giản
            int totalUsers = leaderboardService.getTotalUsers();
            int totalQuizzes = leaderboardService.getTotalQuizzes();

            return ResponseEntity.ok(Map.of(
                    "message", "Database connection test successful!",
                    "totalUsers", totalUsers,
                    "totalQuizzes", totalQuizzes,
                    "status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "message", "Database connection test failed",
                    "error", e.getMessage(),
                    "status", "ERROR"));
        }
    }

    // 🔍 TEST ENDPOINT 2 - Test global
    @GetMapping("/test-global")
    public ResponseEntity<Map<String, Object>> testGlobalEndpoint() {
        try {
            List<LeaderboardEntry> leaderboard = leaderboardService.getGlobalLeaderboard("all", 5);
            return ResponseEntity.ok(Map.of(
                    "message", "Global leaderboard test working!",
                    "count", leaderboard.size(),
                    "data", leaderboard,
                    "status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "message", "Global leaderboard test failed",
                    "error", e.getMessage(),
                    "status", "ERROR"));
        }
    }

    // 🔍 TEST ENDPOINT 3 - Dữ liệu mẫu
    @GetMapping("/test-sample")
    public ResponseEntity<Map<String, Object>> testSampleData() {
        List<LeaderboardEntry> sampleData = List.of(
                new LeaderboardEntry(1L, "user1", "Nguyễn Văn A", "/avatar1.jpg", 95, 120, 1, List.of("🥇 Top 1"),
                        LocalDateTime.now(), null, null),
                new LeaderboardEntry(2L, "user2", "Trần Thị B", "/avatar2.jpg", 88, 150, 2, List.of("🥈 Top 3"),
                        LocalDateTime.now(), null, null),
                new LeaderboardEntry(3L, "user3", "Lê Văn C", "/avatar3.jpg", 82, 180, 3, List.of("🥉 Top 3"),
                        LocalDateTime.now(), null, null));

        return ResponseEntity.ok(Map.of(
                "message", "Sample leaderboard data",
                "count", sampleData.size(),
                "data", sampleData,
                "status", "SUCCESS"));
    }

    // 🔍 TEST ENDPOINT 4 - Test quiz leaderboard
    @GetMapping("/test-quiz/{quizId}")
    public ResponseEntity<Map<String, Object>> testQuizLeaderboard(@PathVariable Long quizId) {
        try {
            List<LeaderboardEntry> leaderboard = leaderboardService.getQuizLeaderboard(quizId, 5);
            return ResponseEntity.ok(Map.of(
                    "message", "Quiz leaderboard test working!",
                    "quizId", quizId,
                    "count", leaderboard.size(),
                    "data", leaderboard,
                    "status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "message", "Quiz leaderboard test failed",
                    "quizId", quizId,
                    "error", e.getMessage(),
                    "status", "ERROR"));
        }
    }

    // ✅ USER ENDPOINTS - Xếp hạng công khai cho user
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<LeaderboardEntry>> getQuizLeaderboard(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<LeaderboardEntry> leaderboard = leaderboardService.getQuizLeaderboard(quizId, limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            System.err.println("❌ Error in getQuizLeaderboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // Trả về list rỗng thay vì lỗi
        }
    }

    // ✅ USER ENDPOINTS - Xếp hạng tổng điểm cho user
    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardEntry>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        try {
            List<LeaderboardEntry> leaderboard = leaderboardService.getGlobalLeaderboard(period, limit, offset);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            System.err.println("❌ Error in getGlobalLeaderboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // Trả về list rỗng thay vì lỗi
        }
    }

    // ✅ USER ENDPOINTS - Xếp hạng theo lớp cho user
    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LeaderboardEntry>> getClassLeaderboard(
            @PathVariable String className,
            @RequestParam(defaultValue = "10") int limit) {

        List<LeaderboardEntry> leaderboard = leaderboardService.getClassLeaderboard(className, limit);
        return ResponseEntity.ok(leaderboard);
    }

    // ✅ USER ENDPOINTS - Tìm vị trí của user
    @GetMapping("/user/{userId}/rank")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Integer> getUserRank(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "weekly") String period) {

        int rank = leaderboardService.getUserRank(userId, period);
        return ResponseEntity.ok(rank);
    }

    // ✅ USER ENDPOINTS - Thông tin leaderboard cho user cụ thể
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LeaderboardEntry>> getUserLeaderboardInfo(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(defaultValue = "10") int limit) {

        List<LeaderboardEntry> globalLeaderboard = leaderboardService.getGlobalLeaderboard(period, limit);
        LeaderboardEntry userEntry = globalLeaderboard.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (userEntry != null) {
            return ResponseEntity.ok(List.of(userEntry));
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    // 🔐 ADMIN-ONLY ENDPOINTS - Thống kê chi tiết cho admin
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLeaderboardStatistics(
            @RequestParam(defaultValue = "weekly") String period) {

        // TODO: Implement admin statistics
        return ResponseEntity.ok(Map.of(
                "totalUsers", leaderboardService.getTotalUsers(),
                "totalQuizzes", leaderboardService.getTotalQuizzes(),
                "averageScore", leaderboardService.getAverageScore(period),
                "topPerformers", leaderboardService.getTopPerformers(period)));
    }

    // 🔐 ADMIN-ONLY ENDPOINTS - Export leaderboard data
    @GetMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportLeaderboardData(
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(defaultValue = "json") String format) {

        // TODO: Implement export functionality
        return ResponseEntity.ok("Export functionality coming soon");
    }

    // 🔐 ADMIN-ONLY ENDPOINTS - Quản lý leaderboard settings
    @GetMapping("/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLeaderboardSettings() {

        // TODO: Implement settings management
        return ResponseEntity.ok(Map.of(
                "bonusPointsEnabled", true,
                "streakBonusEnabled", true,
                "timeBonusEnabled", true,
                "maxLeaderboardEntries", 100));
    }
}
