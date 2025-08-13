package com.nhom7.quiz.quizapp.repository;

import com.nhom7.quiz.quizapp.model.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizAttemptRepo extends JpaRepository<QuizAttempt, Long> {

    // Tìm theo user
    Page<QuizAttempt> findByUserId(Long userId, Pageable pageable);

    // Tìm theo quiz
    Page<QuizAttempt> findByQuizId(Long quizId, Pageable pageable);

    // Tìm theo user và quiz
    Page<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId, Pageable pageable);

    // Tìm tất cả với phân trang
    @Query("SELECT qa FROM QuizAttempt qa ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findAllOrderByAttemptedAtDesc(Pageable pageable);

    // Tìm theo user với sắp xếp theo thời gian giảm dần
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Tìm theo quiz với sắp xếp theo thời gian giảm dần
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findByQuizIdOrderByAttemptedAtDesc(@Param("quizId") Long quizId);

    // Attempts within time range
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.attemptedAt >= :from AND qa.attemptedAt < :to")
    List<QuizAttempt> findByAttemptedAtBetween(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    // Group by hour using native SQL (SQL Server compatible)
    @Query(value = "SELECT DATEPART(HOUR, attempted_at) AS h, COUNT(*) AS c " +
                   "FROM quiz_attempts " +
                   "WHERE attempted_at >= :from AND attempted_at < :to " +
                   "GROUP BY DATEPART(HOUR, attempted_at)",
           nativeQuery = true)
    List<Object[]> countAttemptsGroupedByHour(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}