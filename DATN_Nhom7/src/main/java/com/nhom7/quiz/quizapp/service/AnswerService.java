package com.nhom7.quiz.quizapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.model.Question;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.AnswerRepo;
import com.nhom7.quiz.quizapp.repository.QuestionRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class AnswerService {
    @Autowired
    private AnswerRepo answerRepo;

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

    // Kiểm tra quyền sở hữu question
    private void checkQuestionOwnership(Long questionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Không có quyền truy cập");
        }

        // Admin có thể quản lý tất cả
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // User thường chỉ có thể quản lý answer của question của mình
            String currentUsername = authentication.getName();
            User currentUser = userRepo.findByUsername(currentUsername).orElse(null);

            if (currentUser == null) {
                throw new AccessDeniedException("Không tìm thấy thông tin người dùng");
            }

            Question question = questionRepo.findById(questionId).orElse(null);
            if (question == null) {
                throw new AccessDeniedException("Không tìm thấy câu hỏi");
            }

            Quiz quiz = question.getQuiz();
            if (quiz == null || !quiz.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn chỉ có thể quản lý đáp án của câu hỏi của mình");
            }
        }
    }

    // Method để hỗ trợ @PreAuthorize
    public boolean isOwner(Long questionId, String username) {
        try {
            Question question = questionRepo.findById(questionId).orElse(null);
            if (question != null && question.getQuiz() != null) {
                User user = userRepo.findByUsername(username).orElse(null);
                return user != null && question.getQuiz().getUser() != null &&
                        user.getId().equals(question.getQuiz().getUser().getId());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking answer ownership: " + e.getMessage());
            return false;
        }
    }

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepo.findByQuestionId(questionId);
    }

    public List<Answer> createMultipleAnswers(List<Answer> answers) {
        if (answers != null && !answers.isEmpty()) {
            // Kiểm tra quyền sở hữu cho question đầu tiên
            checkQuestionOwnership(answers.get(0).getQuestion().getId());
        }
        return answerRepo.saveAll(answers);
    }

    @Transactional
    public List<Answer> updateAnswers(List<Answer> updatedAnswers) {
        List<Answer> result = new ArrayList<>();

        for (Answer updated : updatedAnswers) {
            Optional<Answer> optional = answerRepo.findById(updated.getId());
            if (optional.isEmpty()) {
                throw new RuntimeException("Không tìm thấy câu trả lời với ID: " + updated.getId());
            }

            Answer existing = optional.get();
            checkQuestionOwnership(existing.getQuestion().getId());

            existing.setContent(updated.getContent());
            existing.setCorrect(updated.isCorrect());

            result.add(answerRepo.save(existing));
        }

        return result;
    }

    public void deleteByQuestionId(Long questionId) {
        checkQuestionOwnership(questionId);

        List<Answer> answers = answerRepo.findByQuestionId(questionId);
        if (answers != null && !answers.isEmpty()) {
            answerRepo.deleteAll(answers);
        }
    }

    // Method cho hard delete - không kiểm tra ownership
    public void forceDeleteByQuestionId(Long questionId) {
        List<Answer> answers = answerRepo.findByQuestionId(questionId);
        if (answers != null && !answers.isEmpty()) {
            answerRepo.deleteAll(answers);
        }
    }
}
