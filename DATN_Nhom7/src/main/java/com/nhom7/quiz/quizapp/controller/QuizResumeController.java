package com.nhom7.quiz.quizapp.controller;

import com.nhom7.quiz.quizapp.model.QuizAttempt;
import com.nhom7.quiz.quizapp.model.dto.QuizResumeDTO;
import com.nhom7.quiz.quizapp.service.QuizResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz-resume")
public class QuizResumeController {
    
    @Autowired
    private QuizResumeService quizResumeService;
    
    @Autowired
    private com.nhom7.quiz.quizapp.repository.UserRepo userRepo;
    
    /**
     * ✅ Kiểm tra xem user có attempt dở nào cho quiz không
     */
    @GetMapping("/check/{quizId}")
    public ResponseEntity<?> checkInProgressAttempt(@PathVariable Long quizId) {
        try {
            Long userId = getCurrentUserId();
            var attemptOpt = quizResumeService.checkInProgressAttempt(userId, quizId);
            
            if (attemptOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "hasInProgressAttempt", false,
                    "message", "Không có attempt dở nào"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "hasInProgressAttempt", true,
                "attemptData", attemptOpt.get()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi kiểm tra attempt: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ Lấy thông tin chi tiết để resume quiz
     */
    @GetMapping("/resume/{attemptId}")
    public ResponseEntity<?> resumeAttempt(@PathVariable Long attemptId) {
        try {
            Long userId = getCurrentUserId();
            QuizResumeDTO resumeData = quizResumeService.resumeAttempt(attemptId, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", resumeData
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi resume attempt: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ Lưu tiến độ làm quiz (auto-save)
     */
    @PostMapping("/save-progress/{attemptId}")
    public ResponseEntity<?> saveProgress(
            @PathVariable Long attemptId,
            @RequestBody Map<String, Object> request) {
        
        try {
            Long userId = getCurrentUserId();
            
            Integer questionIndex = (Integer) request.get("questionIndex");
            Integer timeRemaining = (Integer) request.get("timeRemaining");
            @SuppressWarnings("unchecked")
            Map<Long, String> answers = (Map<Long, String>) request.get("answers");
            
            if (questionIndex == null || timeRemaining == null || answers == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Thiếu thông tin cần thiết"
                ));
            }
            
            quizResumeService.saveProgress(attemptId, userId, questionIndex, timeRemaining, answers);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã lưu tiến độ thành công"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi lưu tiến độ: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ Tạo attempt mới (khi user chọn làm lại)
     */
    @PostMapping("/new-attempt/{quizId}")
    public ResponseEntity<?> createNewAttempt(@PathVariable Long quizId) {
        try {
            Long userId = getCurrentUserId();
            QuizAttempt newAttempt = quizResumeService.createNewAttempt(userId, quizId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã tạo attempt mới thành công",
                "attemptId", newAttempt.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi tạo attempt mới: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ Lấy đáp án đã chọn từ JSON
     */
    @GetMapping("/answers/{attemptId}")
    public ResponseEntity<?> getAnswers(@PathVariable Long attemptId) {
        try {
            Long userId = getCurrentUserId();
            QuizResumeDTO resumeData = quizResumeService.resumeAttempt(attemptId, userId);
            
            Map<Long, String> answers = quizResumeService.getAnswersFromJson(resumeData.getAnswersJson());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "answers", answers
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi lấy đáp án: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ Cleanup progress không active (admin only)
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupInactiveProgress() {
        try {
            // TODO: Kiểm tra quyền admin
            quizResumeService.cleanupInactiveProgress();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cleanup progress không active thành công"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi cleanup: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ Lấy user ID từ security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập");
        }
        
        String username = authentication.getName();
        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("Không thể xác định user");
        }
        
        // Tìm user ID từ username
        var userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy user với username: " + username);
        }
        
        return userOpt.get().getId();
    }
}
