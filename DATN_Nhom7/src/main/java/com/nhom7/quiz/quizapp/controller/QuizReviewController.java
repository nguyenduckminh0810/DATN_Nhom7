package com.nhom7.quiz.quizapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.QuizReview;
import com.nhom7.quiz.quizapp.model.dto.ReviewRequestDTO;
import com.nhom7.quiz.quizapp.service.QuizReviewService;

@RestController
@RequestMapping("/api/quizzes")
public class QuizReviewController {
    @Autowired
    private QuizReviewService reviewService;

    // Lấy danh sách đánh giá
    @GetMapping("/{quizId}/reviews")
    public ResponseEntity<?> getReviewsForQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(reviewService.getReviewsForQuiz(quizId));
    }

    // Gửi đánh giá quiz
    @PostMapping("/{quizId}/review")
    public ResponseEntity<?> submitReview(@PathVariable Long quizId,
            @RequestBody ReviewRequestDTO request) {
        Long userId = request.getUserId();
        QuizReview review = reviewService.submitReview(quizId, userId, request.getRating(), request.getReviewText());
        return ResponseEntity.ok(review);
    }

    @GetMapping("/{quizId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long quizId) {
        Double avg = reviewService.getAverageRating(quizId);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
    }
}
