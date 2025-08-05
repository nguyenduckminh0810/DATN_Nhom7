package com.nhom7.quiz.quizapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Question;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.QuestionRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private QuizRepo quizRepo;

    @Autowired
    private UserRepo userRepo;

    // Kiểm tra quyền admin
    private void checkAdminPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Chỉ admin mới có quyền thực hiện thao tác này");
        }
    }

    // Kiểm tra quyền sở hữu quiz
    private void checkQuizOwnership(Long quizId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Không có quyền truy cập");
        }
        
        // Admin có thể quản lý tất cả
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // User thường chỉ có thể quản lý question của quiz của mình
            String currentUsername = authentication.getName();
            User currentUser = userRepo.findByUsername(currentUsername).orElse(null);
            
            if (currentUser == null) {
                throw new AccessDeniedException("Không tìm thấy thông tin người dùng");
            }
            
            Quiz quiz = quizRepo.findById(quizId).orElse(null);
            if (quiz == null || !quiz.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn chỉ có thể quản lý câu hỏi của quiz của mình");
            }
        }
    }

    // Method để hỗ trợ @PreAuthorize
    public boolean isOwner(Long questionId, String username) {
        try {
            Optional<Question> questionOpt = questionRepo.findById(questionId);
            if (questionOpt.isPresent()) {
                Question question = questionOpt.get();
                User user = userRepo.findByUsername(username).orElse(null);
                return user != null && question.getQuiz() != null && question.getQuiz().getUser() != null &&
                       user.getId().equals(question.getQuiz().getUser().getId());
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error checking question ownership: " + e.getMessage());
            return false;
        }
    }

    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepo.findByQuizId(quizId);
    }

    public Question updateQuestion(Long id, Question updatedQuestion) {
        Optional<Question> optional = questionRepo.findById(id);
        if (optional.isEmpty()) {
            return null;
        }

        Question existing = optional.get();
        checkQuizOwnership(existing.getQuiz().getId());
        
        System.out.println("Cập nhật question ID: " + id);
        System.out.println("TimeLimit cũ: " + existing.getTimeLimit());
        System.out.println("TimeLimit mới: " + updatedQuestion.getTimeLimit());

        existing.setContent(updatedQuestion.getContent());
        existing.setPoint(updatedQuestion.getPoint());
        // ✅ THÊM CẬP NHẬT TIMELIMIT
        existing.setTimeLimit(updatedQuestion.getTimeLimit());

        Question saved = questionRepo.save(existing);
        System.out.println("TimeLimit sau khi lưu: " + saved.getTimeLimit());
        return saved;
    }

    public Question createQuestion(Question question) {
        checkQuizOwnership(question.getQuiz().getId());
        return questionRepo.save(question);
    }

    public boolean deleteQuestion(Long questionId) {
        Optional<Question> optional = questionRepo.findById(questionId);
        if (optional.isEmpty()) {
            return false;
        }
        
        Question question = optional.get();
        checkQuizOwnership(question.getQuiz().getId());
        
        questionRepo.delete(question);
        return true;
    }
}
