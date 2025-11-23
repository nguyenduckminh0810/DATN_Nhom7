package com.nhom7.quiz.quizapp.repository;

import com.nhom7.quiz.quizapp.model.QuizAttemptProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptProgressRepo extends JpaRepository<QuizAttemptProgress, Long> {

       // Tìm progress theo attempt ID
       Optional<QuizAttemptProgress> findByAttemptId(Long attemptId);

       // Tìm progress đang active theo user và quiz
       @Query("SELECT p FROM QuizAttemptProgress p " +
                     "JOIN p.attempt a " +
                     "WHERE a.user.id = :userId " +
                     "AND a.quiz.id = :quizId " +
                     "AND a.status = 'IN_PROGRESS' " +
                     "AND p.isActive = true " +
                     "ORDER BY p.lastSavedAt DESC")
       List<QuizAttemptProgress> findActiveProgressByUserAndQuiz(@Param("userId") Long userId,
                     @Param("quizId") Long quizId);

       // Tìm progress đang active theo user
       @Query("SELECT p FROM QuizAttemptProgress p " +
                     "JOIN p.attempt a " +
                     "WHERE a.user.id = :userId " +
                     "AND a.status = 'IN_PROGRESS' " +
                     "AND p.isActive = true " +
                     "ORDER BY p.lastSavedAt DESC")
       List<QuizAttemptProgress> findActiveProgressByUser(@Param("userId") Long userId);

       // Tìm progress theo attempt ID và user ID (bảo mật)
       @Query("SELECT p FROM QuizAttemptProgress p " +
                     "JOIN p.attempt a " +
                     "WHERE p.attempt.id = :attemptId " +
                     "AND a.user.id = :userId")
       Optional<QuizAttemptProgress> findByAttemptIdAndUserId(@Param("attemptId") Long attemptId,
                     @Param("userId") Long userId);

       // Xóa tất cả progress không active
       void deleteByIsActiveFalse();
}
