package com.nhom7.quiz.quizapp.repository;

import com.nhom7.quiz.quizapp.model.AttemptStatus;
import com.nhom7.quiz.quizapp.model.QuizAttempt;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepo extends JpaRepository<QuizAttempt, Long> {

    // ====== BASIC PAGINATION QUERIES ======
    Page<QuizAttempt> findByUser_Id(Long userId, Pageable pageable);

    Page<QuizAttempt> findByQuiz_Id(Long quizId, Pageable pageable);

    Page<QuizAttempt> findByUser_IdAndQuiz_Id(Long userId, Long quizId, Pageable pageable);

    // Tất cả attempts (phân trang) sắp xếp theo thời gian
    @Query("SELECT qa FROM QuizAttempt qa ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findAllOrderByAttemptedAtDesc(Pageable pageable);

    // Theo user sắp xếp mới → cũ
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Theo quiz sắp xếp mới → cũ (dùng List cho các màn tổng hợp/summary)
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findByQuizIdOrderByAttemptedAtDesc(@Param("quizId") Long quizId);

    // ====== TIME RANGE / AGGREGATION ======
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.attemptedAt >= :from AND qa.attemptedAt < :to")
    List<QuizAttempt> findByAttemptedAtBetween(@Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Group by hour (SQL Server). Nếu dùng MySQL/Postgres, đổi hàm DATEPART tương
    // ứng.
    @Query(value = "SELECT DATEPART(HOUR, attempted_at) AS h, COUNT(*) AS c " +
            "FROM quiz_attempts " +
            "WHERE attempted_at >= :from AND attempted_at < :to " +
            "GROUP BY DATEPART(HOUR, attempted_at)", nativeQuery = true)
    List<Object[]> countAttemptsGroupedByHour(@Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // ====== RESUME QUIZ (IN_PROGRESS) ======
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user.id = :userId " +
            "AND qa.quiz.id = :quizId " +
            "AND qa.status = 'IN_PROGRESS' " +
            "ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findInProgressAttemptsByUserAndQuiz(@Param("userId") Long userId,
            @Param("quizId") Long quizId);

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user.id = :userId " +
            "AND qa.status = 'IN_PROGRESS' " +
            "ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findInProgressAttemptsByUser(@Param("userId") Long userId);

    // Bảo mật: kiểm tra attempt thuộc về user
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.id = :attemptId " +
            "AND qa.user.id = :userId")
    Optional<QuizAttempt> findByIdAndUserId(@Param("attemptId") Long attemptId,
            @Param("userId") Long userId);

    // ====== FILTER BY STATUS ======
    List<QuizAttempt> findByStatus(AttemptStatus status);

    List<QuizAttempt> findByUser_IdAndStatus(Long userId, AttemptStatus status);

    List<QuizAttempt> findByQuiz_IdAndStatus(Long quizId, AttemptStatus status);

    // ====== HISTORY: ONLY SUBMITTED ======
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user.id = :userId AND qa.status = 'SUBMITTED' " +
            "ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findCompletedByUserIdOrderByAttemptedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.quiz.id = :quizId AND qa.status = 'SUBMITTED' " +
            "ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findCompletedByQuizIdOrderByAttemptedAtDesc(@Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user.id = :userId AND qa.quiz.id = :quizId AND qa.status = 'SUBMITTED' " +
            "ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findCompletedByUserIdAndQuizIdOrderByAttemptedAtDesc(@Param("userId") Long userId,
            @Param("quizId") Long quizId,
            Pageable pageable);

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.status = 'SUBMITTED' " +
            "ORDER BY qa.attemptedAt DESC")
    Page<QuizAttempt> findAllCompletedOrderByAttemptedAtDesc(Pageable pageable);

    // Cho summary (không phân trang)
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.quiz.id = :quizId AND qa.status = 'SUBMITTED' " +
            "ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findCompletedByQuizIdOrderByAttemptedAtDesc(@Param("quizId") Long quizId);

    Page<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId, Pageable pageable);
}
