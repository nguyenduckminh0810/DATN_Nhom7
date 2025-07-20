package com.nhom7.quiz.quizapp.repository;

import com.nhom7.quiz.quizapp.model.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
} 