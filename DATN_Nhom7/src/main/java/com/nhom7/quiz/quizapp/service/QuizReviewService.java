package com.nhom7.quiz.quizapp.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.QuizReview;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.QuizReviewRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class QuizReviewService {
    private final QuizReviewRepo reviewRepo;
    private final UserRepo userRepo;
    private final QuizRepo quizRepo;

    public QuizReviewService(QuizReviewRepo reviewRepo, UserRepo userRepo, QuizRepo quizRepo) {
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
    }

    public QuizReview submitReview(Long quizId, Long userId, int rating, String reviewText) {
        User user = userRepo.findById(userId).orElseThrow();
        Quiz quiz = quizRepo.findById(quizId).orElseThrow();

        Optional<QuizReview> existing = reviewRepo.findByUserAndQuiz(user, quiz);

        QuizReview review = existing.orElse(new QuizReview());
        review.setUser(user);
        review.setQuiz(quiz);
        review.setRating(rating);
        review.setReviewText(reviewText);

        return reviewRepo.save(review);
    }

    public Double getAverageRating(Long quizId) {
        return reviewRepo.getAverageRating(quizId);
    }
}
