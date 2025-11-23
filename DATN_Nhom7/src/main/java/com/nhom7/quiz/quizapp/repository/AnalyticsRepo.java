package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom7.quiz.quizapp.model.Result;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepo extends JpaRepository<Result, Long> {

    // Attempts per day from quiz_attempts
    @Query(value = "SELECT CAST(attempted_at AS date) AS d, COUNT(*) AS c " +
            "FROM quiz_attempts WHERE attempted_at >= :from AND attempted_at < :to " +
            "GROUP BY CAST(attempted_at AS date) ORDER BY d",
            nativeQuery = true)
    List<Object[]> attemptsSeries(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Active users per day (distinct users attempting quizzes)
    @Query(value = "SELECT CAST(attempted_at AS date) AS d, COUNT(DISTINCT user_id) AS c " +
            "FROM quiz_attempts WHERE attempted_at >= :from AND attempted_at < :to " +
            "GROUP BY CAST(attempted_at AS date) ORDER BY d",
            nativeQuery = true)
    List<Object[]> activeUsersSeries(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // New users per day
    @Query(value = "SELECT CAST(created_at AS date) AS d, COUNT(*) AS c " +
            "FROM users WHERE created_at >= :from AND created_at < :to " +
            "GROUP BY CAST(created_at AS date) ORDER BY d",
            nativeQuery = true)
    List<Object[]> newUsersSeries(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Average score per day (completed results)
    @Query(value = "SELECT CAST(completed_at AS date) AS d, AVG(CAST(score AS float)) AS avgScore " +
            "FROM results WHERE completed_at >= :from AND completed_at < :to " +
            "GROUP BY CAST(completed_at AS date) ORDER BY d",
            nativeQuery = true)
    List<Object[]> avgScoreSeries(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Score histogram: counts per score
    @Query(value = "SELECT score, COUNT(*) AS c FROM results " +
            "WHERE completed_at >= :from AND completed_at < :to " +
            "GROUP BY score ORDER BY score",
            nativeQuery = true)
    List<Object[]> scoreCounts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Completion numbers
    @Query(value = "SELECT COUNT(*) FROM quiz_attempts WHERE attempted_at >= :from AND attempted_at < :to",
            nativeQuery = true)
    long countAttempts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(*) FROM results WHERE completed_at >= :from AND completed_at < :to",
            nativeQuery = true)
    long countCompleted(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Category distribution (top N)
    @Query(value = "SELECT TOP (:limit) c.id, c.name, COUNT(*) AS cnt " +
            "FROM quiz_attempts qa " +
            "JOIN quizzes q ON q.id = qa.quiz_id " +
            "JOIN categories c ON c.id = q.category_id " +
            "WHERE qa.attempted_at >= :from AND qa.attempted_at < :to " +
            "GROUP BY c.id, c.name ORDER BY cnt DESC",
            nativeQuery = true)
    List<Object[]> categoryDistributionTop(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to,
                                           @Param("limit") int limit);

    // Heatmap (weekday, hour)
    @Query(value = "SELECT DATEPART(WEEKDAY, attempted_at) AS wd, DATEPART(HOUR, attempted_at) AS h, COUNT(*) AS c " +
            "FROM quiz_attempts WHERE attempted_at >= :from AND attempted_at < :to " +
            "GROUP BY DATEPART(WEEKDAY, attempted_at), DATEPART(HOUR, attempted_at)",
            nativeQuery = true)
    List<Object[]> heatmap(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Top performers
    @Query(value = "SELECT TOP (:limit) u.id, u.username, u.full_name, COUNT(r.id) AS attempts, AVG(CAST(r.score AS float)) AS avgScore " +
            "FROM results r JOIN users u ON u.id = r.user_id " +
            "WHERE r.completed_at >= :from AND r.completed_at < :to " +
            "GROUP BY u.id, u.username, u.full_name HAVING COUNT(r.id) >= :minAttempts " +
            "ORDER BY avgScore DESC",
            nativeQuery = true)
    List<Object[]> topPerformers(@Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to,
                                 @Param("limit") int limit,
                                 @Param("minAttempts") int minAttempts);

    // Attempts per quiz
    @Query(value = "SELECT TOP (:limit) q.id, q.title, COUNT(*) AS attempts " +
            "FROM quiz_attempts qa JOIN quizzes q ON q.id = qa.quiz_id " +
            "WHERE qa.attempted_at >= :from AND qa.attempted_at < :to " +
            "GROUP BY q.id, q.title ORDER BY attempts DESC",
            nativeQuery = true)
    List<Object[]> topQuizzesByAttempts(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        @Param("limit") int limit);

    // Completed per quiz
    @Query(value = "SELECT q.id, COUNT(*) AS completed, AVG(CAST(r.score AS float)) AS avgScore " +
            "FROM results r JOIN quizzes q ON q.id = r.quiz_id " +
            "WHERE r.completed_at >= :from AND r.completed_at < :to " +
            "GROUP BY q.id",
            nativeQuery = true)
    List<Object[]> completedAndAvgScorePerQuiz(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);
}


