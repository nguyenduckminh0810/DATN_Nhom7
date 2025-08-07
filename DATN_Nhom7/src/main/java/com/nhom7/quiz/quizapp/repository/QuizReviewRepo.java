package com.nhom7.quiz.quizapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.QuizReview;
import com.nhom7.quiz.quizapp.model.User;

public interface QuizReviewRepo extends JpaRepository<QuizReview, Long> {
    Optional<QuizReview> findByUserAndQuiz(User user, Quiz quiz);

    List<QuizReview> findAllByQuiz(Quiz quiz);

    @Query("SELECT AVG(qr.rating) FROM QuizReview qr WHERE qr.quiz.id = :quizId")
    Double getAverageRating(Long quizId);

    // ✅ EAGER FETCH để tránh lazy loading issues
    @Query("SELECT qr FROM QuizReview qr JOIN FETCH qr.user WHERE qr.quiz = :quiz ORDER BY qr.createdAt DESC")
    List<QuizReview> findByQuizOrderByCreatedAtDesc(@Param("quiz") Quiz quiz);

}
