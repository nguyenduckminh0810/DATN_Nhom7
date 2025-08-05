package com.nhom7.quiz.quizapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Kiểm tra quyền user (chỉ có thể quản lý review của mình)
    private void checkUserPermission(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Không có quyền truy cập");
        }
        
        // Admin có thể quản lý tất cả
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // User thường chỉ có thể quản lý review của mình
            String currentUsername = authentication.getName();
            User currentUser = userRepo.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                throw new AccessDeniedException("Bạn chỉ có thể quản lý đánh giá của mình");
            }
        }
    }

    public QuizReview submitReview(Long quizId, Long userId, int rating, String reviewText) {
        checkUserPermission(userId);
        
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

    // Phương thức trả về danh sách đánh giá
    public List<QuizReview> getReviewsForQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId).orElseThrow();
        return reviewRepo.findByQuizOrderByCreatedAtDesc(quiz);
    }

}
