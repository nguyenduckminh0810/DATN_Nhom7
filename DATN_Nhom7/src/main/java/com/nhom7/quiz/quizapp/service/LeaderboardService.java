package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.dto.LeaderboardEntry;
import com.nhom7.quiz.quizapp.model.dto.Badge;
import com.nhom7.quiz.quizapp.repository.ResultRepo;

@Service
public class LeaderboardService {

    @Autowired
    private ResultRepo resultRepo;

    // Xếp hạng theo quiz cụ thể
    public List<LeaderboardEntry> getQuizLeaderboard(Long quizId, int limit) {
        try {
            System.out.println("🔍 Getting quiz leaderboard for quizId: " + quizId + ", limit: " + limit);

            List<Result> results = resultRepo.findTopByQuizIdOrderByScoreDescTimeTakenAsc(
                    quizId, PageRequest.of(0, limit));

            System.out.println("✅ Found " + results.size() + " results for quiz " + quizId);

            return results.stream()
                    .map(this::convertToLeaderboardEntry)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error in getQuizLeaderboard: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Trả về list rỗng thay vì null
        }
    }

    // ✅ Xếp hạng theo quiz cụ thể với pagination thực sự
    public org.springframework.data.domain.Page<LeaderboardEntry> getQuizLeaderboardPaginated(Long quizId, int page, int size) {
        try {
            System.out.println("🔍 Getting paginated quiz leaderboard for quizId: " + quizId + ", page: " + page + ", size: " + size);

            // Sử dụng Pageable để phân trang
            org.springframework.data.domain.Pageable pageable = PageRequest.of(page, size);
            org.springframework.data.domain.Page<Result> resultsPage = resultRepo.findByQuizIdOrderByScoreDescTimeTakenAsc(quizId, pageable);

            System.out.println("✅ Found " + resultsPage.getContent().size() + " results for page " + page + 
                             ", total: " + resultsPage.getTotalElements() + 
                             ", pages: " + resultsPage.getTotalPages());

            // Convert Page<Result> thành Page<LeaderboardEntry>
            org.springframework.data.domain.Page<LeaderboardEntry> leaderboardPage = resultsPage.map(this::convertToLeaderboardEntry);
            
            return leaderboardPage;
        } catch (Exception e) {
            System.err.println("❌ Error in getQuizLeaderboardPaginated: " + e.getMessage());
            e.printStackTrace();
            // Trả về empty page
            return org.springframework.data.domain.Page.empty(PageRequest.of(page, size));
        }
    }

    // Xếp hạng tổng điểm theo tuần/tháng/all-time (hỗ trợ offset)
    public List<LeaderboardEntry> getGlobalLeaderboard(String period, int limit, int offset) {
        try {
            LocalDateTime startDate = getStartDate(period);
            int page = limit > 0 ? offset / limit : 0;
            List<Object[]> rawResults = resultRepo.getGlobalLeaderboard(startDate, PageRequest.of(page, limit));

            List<LeaderboardEntry> entries = new ArrayList<>();
            int rank = 1;

            for (Object[] row : rawResults) {
                try {
                    Long userId = (Long) row[0];
                    String username = (String) row[1];
                    String fullName = (String) row[2];
                    String avatarUrl = (String) row[3];
                    // ✅ Sửa lỗi cast - chuyển Long thành Integer
                    Long totalScoreLong = (Long) row[4];
                    Integer totalScore = totalScoreLong != null ? totalScoreLong.intValue() : 0;
                    Long attemptCount = (Long) row[5];

                    List<String> badges = calculateBadges(totalScore, attemptCount);

                    LeaderboardEntry entry = new LeaderboardEntry(
                            userId, username, fullName, avatarUrl,
                            totalScore, 0, rank, badges,
                            LocalDateTime.now(), null, null);

                    entries.add(entry);
                    rank++;
                } catch (Exception e) {
                    System.err.println("❌ Error processing row " + rank + ": " + e.getMessage());
                    // Tiếp tục với row tiếp theo thay vì dừng toàn bộ
                    continue;
                }
            }

            return entries;
        } catch (Exception e) {
            System.err.println("❌ Error in getGlobalLeaderboard: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Trả về list rỗng thay vì null
        }
    }

    // Overload để tương thích cũ
    public List<LeaderboardEntry> getGlobalLeaderboard(String period, int limit) {
        return getGlobalLeaderboard(period, limit, 0);
    }

    // Xếp hạng theo lớp (placeholder - có thể mở rộng sau)
    public List<LeaderboardEntry> getClassLeaderboard(String className, int limit) {
        // TODO: Implement khi có thông tin lớp
        return new ArrayList<>();
    }

    // Tìm vị trí của user trong leaderboard
    public int getUserRank(Long userId, String period) {
        LocalDateTime startDate = getStartDate(period);
        List<Object[]> allResults = resultRepo.getGlobalLeaderboard(startDate, PageRequest.of(0, 1000));

        for (int i = 0; i < allResults.size(); i++) {
            if (allResults.get(i)[0].equals(userId)) {
                return i + 1;
            }
        }
        return -1; // Không tìm thấy
    }

    // Tính toán huy hiệu
    private List<String> calculateBadges(int totalScore, Long attemptCount) {
        List<String> badges = new ArrayList<>();

        try {
            if (totalScore >= 1000) {
                badges.add("🏆 Master");
            } else if (totalScore >= 500) {
                badges.add("🥇 Expert");
            } else if (totalScore >= 200) {
                badges.add("🥈 Advanced");
            }

            if (attemptCount != null && attemptCount >= 50) {
                badges.add("📚 Dedicated");
            } else if (attemptCount != null && attemptCount >= 20) {
                badges.add("📖 Active");
            }
        } catch (Exception e) {
            System.err.println("❌ Error calculating badges: " + e.getMessage());
            // Trả về badge mặc định nếu có lỗi
            badges.add("🎯 New Player");
        }

        return badges;
    }

    // Tính toán huy hiệu cho quiz cụ thể
    private List<String> calculateQuizBadges(Result result, List<Result> allQuizResults) {
        List<String> badges = new ArrayList<>();

        try {
            // 100% chính xác
            if (result.getScore() >= 100) {
                badges.add("💯 Perfect Score");
            }

            // Nhanh nhất (có timeTaken)
            if (result.getTimeTaken() != null) {
                List<Result> fastestResults = allQuizResults.stream()
                        .filter(r -> r.getTimeTaken() != null)
                        .sorted((r1, r2) -> Integer.compare(r1.getTimeTaken(), r2.getTimeTaken()))
                        .limit(3)
                        .toList();

                if (!fastestResults.isEmpty() && fastestResults.get(0).getId().equals(result.getId())) {
                    badges.add("⚡ Speed Demon");
                }
            }

            // Top 3 theo điểm
            List<Result> top3ByScore = allQuizResults.stream()
                    .sorted((r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()))
                    .limit(3)
                    .toList();

            if (top3ByScore.stream().anyMatch(r -> r.getId().equals(result.getId()))) {
                badges.add("🥉 Top 3");
            }

            // Streak badge (3+ quizzes today)
            long todayAttempts = resultRepo.countByUserIdAndCompletedAtToday(result.getUser().getId());
            if (todayAttempts >= 3) {
                badges.add("🔥 Streak Master");
            }
        } catch (Exception e) {
            System.err.println("❌ Error calculating quiz badges: " + e.getMessage());
            // Trả về badge mặc định nếu có lỗi
            badges.add("🎯 Quiz Player");
        }

        return badges;
    }

    // Chuyển đổi Result thành LeaderboardEntry
    private LeaderboardEntry convertToLeaderboardEntry(Result result) {
        try {
            System.out.println("🔍 Converting result for user: " + result.getUser().getUsername() + ", score: "
                    + result.getScore());

            List<Result> allQuizResults = resultRepo.findByQuiz_Id(result.getQuiz().getId());
            List<String> badges = calculateQuizBadges(result, allQuizResults);

            LeaderboardEntry entry = new LeaderboardEntry(
                    result.getUser().getId(),
                    result.getUser().getUsername(),
                    result.getUser().getFullName(),
                    result.getUser().getAvatarUrl(),
                    result.getScore(),
                    result.getTimeTaken() != null ? result.getTimeTaken() : 0, // Sử dụng timeTaken thực tế
                    0, // rank sẽ được set sau
                    badges,
                    result.getCompletedAt(),
                    result.getQuiz().getId(),
                    result.getQuiz().getTitle());

            System.out.println("✅ Converted to LeaderboardEntry: " + entry.getUsername() + " - " + entry.getScore());
            return entry;
        } catch (Exception e) {
            System.err.println("❌ Error converting result to LeaderboardEntry: " + e.getMessage());
            e.printStackTrace();

            // Trả về entry mặc định nếu có lỗi
            return new LeaderboardEntry(
                    result.getId(), // Fallback to result ID
                    "Unknown User",
                    "Unknown User",
                    null,
                    0,
                    0,
                    0,
                    List.of("🎯 Player"),
                    LocalDateTime.now(),
                    null,
                    "Unknown Quiz");
        }
    }

    // Lấy ngày bắt đầu theo period
    private LocalDateTime getStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "weekly":
                return now.minus(7, ChronoUnit.DAYS);
            case "monthly":
                return now.minus(30, ChronoUnit.DAYS);
            case "all-time":
                return LocalDateTime.of(2020, 1, 1, 0, 0); // Từ năm 2020
            default:
                return now.minus(7, ChronoUnit.DAYS); // Default weekly
        }
    }

    // 🔐 ADMIN METHODS - Thống kê cho admin
    public int getTotalUsers() {
        // TODO: Implement actual user count
        return 150; // Placeholder
    }

    public int getTotalQuizzes() {
        // TODO: Implement actual quiz count
        return 25; // Placeholder
    }

    public double getAverageScore(String period) {
        LocalDateTime startDate = getStartDate(period);
        List<Object[]> results = resultRepo.getGlobalLeaderboard(startDate, PageRequest.of(0, 1000));

        if (results.isEmpty()) {
            return 0.0;
        }

        double totalScore = results.stream()
                .mapToDouble(row -> {
                    Long scoreLong = (Long) row[4];
                    return scoreLong != null ? scoreLong.doubleValue() : 0.0;
                })
                .sum();

        return totalScore / results.size();
    }

    public List<LeaderboardEntry> getTopPerformers(String period) {
        return getGlobalLeaderboard(period, 10);
    }
}
