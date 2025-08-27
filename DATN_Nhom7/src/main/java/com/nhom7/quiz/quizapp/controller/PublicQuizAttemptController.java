package com.nhom7.quiz.quizapp.controller;

import com.nhom7.quiz.quizapp.model.dto.QuizAttemptSummaryDTO;
import com.nhom7.quiz.quizapp.service.QuizAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/quiz-attempts")
@CrossOrigin(origins = "*")
public class PublicQuizAttemptController {

    @Autowired
    private QuizAttemptService quizAttemptService;

    /**
     * Test endpoint để kiểm tra public access
     * GET /api/public/quiz-attempts/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        System.out.println("Test public endpoint accessed successfully!");
        return ResponseEntity.ok("Public endpoint works!");
    }

    /**
     * API lấy recent attempts cho một quiz cụ thể (Public endpoint)
     * GET /api/public/quiz-attempts/recent/{quizId}
     */
    @GetMapping("/recent/{quizId}")
    public ResponseEntity<List<QuizAttemptSummaryDTO>> getPublicRecentAttempts(@PathVariable Long quizId) {
        System.out.println("Requesting PUBLIC recent attempts for quiz ID: " + quizId);
        try {
            // VALIDATE QUIZ ID
            if (quizId == null || quizId <= 0) {
                System.err.println("Invalid quiz ID: " + quizId);
                return ResponseEntity.badRequest().build();
            }

            List<QuizAttemptSummaryDTO> recentAttempts = quizAttemptService.getRecentAttemptsForQuiz(quizId);
            System.out.println("Found " + recentAttempts.size() + " recent attempts for quiz " + quizId);
            return ResponseEntity.ok(recentAttempts);
        } catch (Exception e) {
            System.err.println("Error getting recent attempts for quiz " + quizId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}