package com.nhom7.quiz.quizapp.controller;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.QuizAttempt;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptDTO;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptSummaryDTO;
import com.nhom7.quiz.quizapp.model.dto.SubmitAttemptRequest;
import java.util.List;
import com.nhom7.quiz.quizapp.service.QuizAttemptService;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    private com.nhom7.quiz.quizapp.service.ResultService resultService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private QuestionService questionService;

    /**
     * Bắt đầu một attempt mới (public/practice cũng hỗ trợ)
     * POST /api/quiz-attempts/start?quizId=123
     */
    @PostMapping("/start")
    public ResponseEntity<?> startAttempt(@RequestParam Long quizId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null && !"anonymousUser".equals(authentication.getName())) {
                currentUser = loginService.findByUsername(authentication.getName());
            }
            Long userId = currentUser != null ? currentUser.getId() : null;
            Long attemptId = quizAttemptService.startAttempt(quizId, userId);
            return ResponseEntity.ok(java.util.Map.of("attemptId", attemptId, "status", com.nhom7.quiz.quizapp.model.AttemptStatus.IN_PROGRESS.name()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy trạng thái attempt
     * GET /api/quiz-attempts/{attemptId}/status
     */
    @GetMapping("/{attemptId}/status")
    public ResponseEntity<?> getAttemptStatus(@PathVariable Long attemptId) {
        String status = quizAttemptService.getAttemptStatus(attemptId);
        if (status == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(java.util.Map.of("status", status));
    }

    /**
     * Lấy câu hỏi theo attemptId (để chơi theo attempt)
     * GET /api/quiz-attempts/{attemptId}/questions
     */
    @GetMapping("/{attemptId}/questions")
    public ResponseEntity<?> getAttemptQuestions(@PathVariable Long attemptId) {
        QuizAttempt attempt = quizAttemptService.findById(attemptId);
        if (attempt == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        // Kiểm tra quyền: user hiện tại phải là chủ sở hữu hoặc admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = loginService.findByUsername(authentication.getName());
        boolean isAdmin = currentUser != null && currentUser.getRole() != null && ("ADMIN".equalsIgnoreCase(currentUser.getRole()) || "admin".equalsIgnoreCase(currentUser.getRole()));
        if (!isAdmin && (attempt.getUser() == null || !attempt.getUser().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long quizId = attempt.getQuiz().getId();
        String quizTitle = attempt.getQuiz().getTitle();
        java.util.List<com.nhom7.quiz.quizapp.model.Question> questions = questionService.getQuestionsByQuizId(quizId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("quizId", quizId);
        resp.put("quizTitle", quizTitle);
        resp.put("questions", questions);
        return ResponseEntity.ok(resp);
    }

    /**
     * Nộp bài theo attemptId
     * POST /api/quiz-attempts/{attemptId}/submit
     */
    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<?> submitAttempt(@PathVariable Long attemptId, @RequestBody SubmitAttemptRequest body) {
        try {
            // Tái sử dụng ResultService evaluateAndSave hiện có theo quizId/userId
            // Lấy attempt -> từ đó lấy quizId, userId
            QuizAttempt attempt = quizAttemptService.findById(attemptId);
            if (attempt == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (attempt.getStatus() != com.nhom7.quiz.quizapp.model.AttemptStatus.IN_PROGRESS) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error","Attempt not in progress"));
            }

            // Build QuizSubmissionDTO từ attempt
            com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO dto = new com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO();
            dto.setQuizId(attempt.getQuiz().getId());
            dto.setUserId(attempt.getUser().getId());
            dto.setAnswers(body.getAnswers());
            dto.setTimeTaken(body.getTimeTaken());

            com.nhom7.quiz.quizapp.model.dto.EvaluationResult eval = resultService.evaluateAndSave(dto);

            // Update trạng thái attempt
            attempt.setStatus(com.nhom7.quiz.quizapp.model.AttemptStatus.COMPLETED);
            attempt.setAttemptedAt(java.time.LocalDateTime.now());
            quizAttemptService.saveQuizAttempt(attempt);

            return ResponseEntity.ok(java.util.Map.of("resultId", eval.getResultId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * API lấy lịch sử làm quiz - dùng chung cho admin và user
     * GET
     * /api/quiz-attempts?userId={userId}&quizId={quizId}&page={page}&size={size}
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
        
        System.out.println("🔍 QuizAttempt Request:");
        System.out.println("  📋 Current user: " + currentUsername + " (ID: " + currentUser.getId() + ", Role: " + currentUser.getRole() + ")");
        System.out.println("  📋 Request params - userId: " + userId + ", quizId: " + quizId + ", page: " + page + ", size: " + size);

        // Phân quyền: User thường chỉ xem được lịch sử của chính mình
        // Fix: Handle both "admin" and "ADMIN" role formats
        String userRole = currentUser.getRole();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole) || "admin".equalsIgnoreCase(userRole);
        
        if (!isAdmin) {
            userId = currentUser.getId(); // Ghi đè userId bằng ID của user hiện tại
            System.out.println("  🔒 Non-admin user, filtering to own attempts only (userId: " + userId + ")");
        } else {
            System.out.println("  👑 Admin user, can view all attempts (role: " + userRole + ")");
        }

        // Gọi service để lấy dữ liệu
        Page<QuizAttemptDTO> attempts = quizAttemptService.findQuizAttempts(userId, quizId, page, size);
        
        System.out.println("  📊 Query result: " + attempts.getTotalElements() + " total attempts, " + attempts.getContent().size() + " in current page");

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

    /**
     * Test endpoint để kiểm tra public access
     * GET /api/quiz-attempts/public/test
     */
    @GetMapping("/public/test")
    public ResponseEntity<String> testPublicEndpoint() {
        System.out.println("✅ Test public endpoint accessed successfully!");
        return ResponseEntity.ok("Public endpoint works!");
    }

    /**
     * Debug endpoint để kiểm tra database
     * GET /api/quiz-attempts/debug/count
     */
    @GetMapping("/debug/count")
    public ResponseEntity<String> debugDatabaseCount() {
        try {
            long totalCount = quizAttemptService.getTotalCount();
            System.out.println("🔍 Total quiz attempts in database: " + totalCount);
            return ResponseEntity.ok("Total quiz attempts in database: " + totalCount);
        } catch (Exception e) {
            System.err.println("❌ Error checking database: " + e.getMessage());
            return ResponseEntity.ok("Error checking database: " + e.getMessage());
        }
    }

    /**
     * Debug endpoint để tạo sample data
     * POST /api/quiz-attempts/debug/create-sample
     */
    @PostMapping("/debug/create-sample")
    public ResponseEntity<String> createSampleData() {
        try {
            String result = quizAttemptService.createSampleData();
            System.out.println("✅ Sample data creation result: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Error creating sample data: " + e.getMessage());
            return ResponseEntity.ok("Error creating sample data: " + e.getMessage());
        }
    }

    /**
     * API lấy recent attempts cho một quiz cụ thể (Public endpoint - final)
     * GET /api/quiz-attempts/public/recent/{quizId}
     */
    @GetMapping("/public/recent/{quizId}")
    public ResponseEntity<List<QuizAttemptSummaryDTO>> getPublicRecentAttemptsFinal(@PathVariable Long quizId) {
        System.out.println("🔍 Requesting PUBLIC recent attempts (final) for quiz ID: " + quizId);
        try {
            // ✅ VALIDATE QUIZ ID
            if (quizId == null || quizId <= 0) {
                System.err.println("❌ Invalid quiz ID: " + quizId);
                return ResponseEntity.badRequest().build();
            }

            List<QuizAttemptSummaryDTO> recentAttempts = quizAttemptService.getRecentAttemptsForQuiz(quizId);
            System.out.println("✅ Found " + recentAttempts.size() + " recent attempts for quiz " + quizId);
            return ResponseEntity.ok(recentAttempts);
        } catch (Exception e) {
            System.err.println("❌ Error getting recent attempts for quiz " + quizId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}