package com.nhom7.quiz.quizapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nhom7.quiz.quizapp.model.Result;

public interface ResultRepo extends JpaRepository<Result, Long> {
    // Các phương thức truy vấn tùy chỉnh nếu cần
    List<Result> findAllByUser_Id(Long userId);

    List<Result> findByUser_Id(Long userId);

    // Thêm method để xóa results theo quiz_id
    List<Result> findByQuiz_Id(Long quizId);

    void deleteByQuiz_Id(Long quizId);

    // Leaderboard queries
    @Query("SELECT r FROM Result r WHERE r.quiz.id = :quizId ORDER BY r.score DESC, r.completedAt ASC")
    List<Result> findTopByQuizIdOrderByScoreDescTimeTakenAsc(@Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT r FROM Result r WHERE r.completedAt >= :startDate ORDER BY r.score DESC")
    List<Result> findTopByPeriodOrderByTotalScoreDesc(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Result r WHERE r.user.id = :userId AND CAST(r.completedAt AS date) = CAST(CURRENT_TIMESTAMP AS date)")
    long countByUserIdAndCompletedAtToday(@Param("userId") Long userId);

    @Query("SELECT r FROM Result r WHERE r.quiz.id = :quizId ORDER BY r.completedAt ASC")
    List<Result> findTop3ByQuizIdOrderByTimeTakenAsc(@Param("quizId") Long quizId, Pageable pageable);

    // Count total attempts by user
    @Query("SELECT COUNT(r) FROM Result r WHERE r.user.id = :userId")
    long countByUser_Id(@Param("userId") Long userId);

    // Global leaderboard - tổng điểm theo user
    @Query("SELECT r.user.id, r.user.username, r.user.fullName, r.user.avatarUrl, " +
            "SUM(r.score) as totalScore, COUNT(r) as attemptCount " +
            "FROM Result r " +
            "WHERE r.completedAt >= :startDate " +
            "GROUP BY r.user.id, r.user.username, r.user.fullName, r.user.avatarUrl " +
            "ORDER BY totalScore DESC")
    List<Object[]> getGlobalLeaderboard(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}
