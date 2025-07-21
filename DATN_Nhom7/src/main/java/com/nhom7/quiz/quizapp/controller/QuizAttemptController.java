package com.nhom7.quiz.quizapp.controller;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptDTO;
import com.nhom7.quiz.quizapp.service.QuizAttemptService;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz-attempts")
@CrossOrigin(origins = "*")
public class QuizAttemptController {
    
    @Autowired
    private QuizAttemptService quizAttemptService;
    
    @Autowired
    private LoginService loginService;
    
    /**
     * API lấy lịch sử làm quiz - dùng chung cho admin và user
     * GET /api/quiz-attempts?userId={userId}&quizId={quizId}&page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<Page<QuizAttemptDTO>> getQuizAttempts(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long quizId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Lấy thông tin user hiện tại từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = loginService.findByUsername(currentUsername);
        
        // Phân quyền: User thường chỉ xem được lịch sử của chính mình
        if (!"ADMIN".equals(currentUser.getRole())) {
            userId = currentUser.getId(); // Ghi đè userId bằng ID của user hiện tại
        }
        
        // Gọi service để lấy dữ liệu
        Page<QuizAttemptDTO> attempts = quizAttemptService.findQuizAttempts(userId, quizId, page, size);
        
        return ResponseEntity.ok(attempts);
    }
    
    /**
     * API lấy lịch sử làm quiz của user hiện tại (dành cho user thường)
     * GET /api/quiz-attempts/my-history
     */
    @GetMapping("/my-history")
    public ResponseEntity<Page<QuizAttemptDTO>> getMyQuizHistory(
            @RequestParam(required = false) Long quizId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Lấy thông tin user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = loginService.findByUsername(currentUsername);
        
        // Chỉ lấy lịch sử của user hiện tại
        Page<QuizAttemptDTO> attempts = quizAttemptService.findQuizAttempts(currentUser.getId(), quizId, page, size);
        
        return ResponseEntity.ok(attempts);
    }
} 