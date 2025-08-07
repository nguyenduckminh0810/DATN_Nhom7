package com.nhom7.quiz.quizapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.QuizReview;
import com.nhom7.quiz.quizapp.model.dto.ReviewRequestDTO;
import com.nhom7.quiz.quizapp.model.dto.ReviewDTO;
import com.nhom7.quiz.quizapp.service.QuizReviewService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizReviewController {
    @Autowired
    private QuizReviewService reviewService;

    // Lấy danh sách đánh giá - ai cũng có thể xem
    @GetMapping("/{quizId}/reviews")
    public ResponseEntity<?> getReviewsForQuiz(@PathVariable Long quizId) {
        try {
            System.out.println("📋 Getting reviews for quiz ID: " + quizId);
            List<QuizReview> reviews = reviewService.getReviewsForQuiz(quizId);
            
            // ✅ CONVERT TO DTO ĐỂ TRÁNH LAZY LOADING ISSUES
            List<ReviewDTO> reviewDTOs = reviews.stream()
                    .map(ReviewDTO::new)
                    .collect(Collectors.toList());
            
            System.out.println("✅ Found " + reviewDTOs.size() + " reviews, converted to DTOs");
            return ResponseEntity.ok(reviewDTOs);
        } catch (Exception e) {
            System.err.println("❌ Error getting reviews for quiz " + quizId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<ReviewDTO>());
        }
    }

    // Gửi đánh giá quiz - chỉ user đã đăng nhập
    @PostMapping("/{quizId}/review")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> submitReview(@PathVariable Long quizId,
            @RequestBody ReviewRequestDTO request) {
        Long userId = request.getUserId();
        QuizReview review = reviewService.submitReview(quizId, userId, request.getRating(), request.getReviewText());
        return ResponseEntity.ok(review);
    }

    // Lấy điểm trung bình - ai cũng có thể xem
    @GetMapping("/{quizId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long quizId) {
        Double avg = reviewService.getAverageRating(quizId);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
    }
}
