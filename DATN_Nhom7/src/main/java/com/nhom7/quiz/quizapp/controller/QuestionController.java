package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Question;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.service.AnswerService;
import com.nhom7.quiz.quizapp.service.QuestionService;
import com.nhom7.quiz.quizapp.service.QuizService;
import com.nhom7.quiz.quizapp.service.userService.LoginService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.parameters.P;

@RestController
@RequestMapping("/api/question")

public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private LoginService loginService;

    // Xem câu hỏi để chỉnh sửa - chỉ chủ sở hữu quiz
    @GetMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN') or @quizService.isOwner(#quizId, authentication.name)")
    public ResponseEntity<List<Question>> getQuestionsByQuizId(@PathVariable Long quizId,
            Authentication authentication) {
        System.out.println("Requesting questions for quiz ID: " + quizId);

        try {
            // Kiểm tra xem người dùng có quyền xem câu hỏi không
            if (authentication == null || authentication.getName() == null) {
                System.out.println("Không có thông tin authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            String username = authentication.getName();
            User currentUser = loginService.findByUsername(username);

            if (currentUser == null) {
                System.out.println("Không tìm thấy user: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            // Lấy thông tin quiz
            Quiz quiz = quizService.getQuizById(quizId).orElse(null);
            if (quiz == null) {
                System.out.println("Không tìm thấy quiz ID: " + quizId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Kiểm tra xem người dùng có phải là người tạo quiz không
            System.out.println("Checking quiz creator:");
            System.out.println("  Current user ID: " + currentUser.getId() + " (type: "
                    + currentUser.getId().getClass().getSimpleName() + ")");
            System.out.println("  Current username: " + currentUser.getUsername());
            System.out.println("  Quiz creator ID: " + quiz.getUser().getId() + " (type: "
                    + quiz.getUser().getId().getClass().getSimpleName() + ")");
            System.out.println("  Quiz creator name: " + quiz.getUser().getUsername());
            System.out.println("  Quiz ID: " + quizId);

            boolean isQuizCreator = quiz.getUser().getId().equals(currentUser.getId());
            System.out.println("  Is creator: " + isQuizCreator);
            System.out.println("  IDs equal: " + quiz.getUser().getId().equals(currentUser.getId()));
            System.out.println("  IDs == comparison: " + (quiz.getUser().getId() == currentUser.getId()));

            if (!isQuizCreator) {
                System.out.println("User " + username + " không phải là người tạo quiz " + quizId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            List<Question> questions = questionService.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                System.out.println("Không tìm thấy questions cho quiz ID: " + quizId);
                return ResponseEntity.ok(questions);
            }
            System.out.println("Tìm thấy " + questions.size() + " questions cho quiz ID: " + quizId);

            // DEBUG: In ra timeLimit của từng question
            for (Question q : questions) {
                System.out.println("Question ID: " + q.getId() + ", TimeLimit: " + q.getTimeLimit());
            }

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy questions cho quiz ID " + quizId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ENDPOINT MỚI CHO VIỆC CHƠI QUIZ - CHO PHÉP TẤT CẢ NGƯỜI DÙNG TRUY CẬP
    @GetMapping("/play/{quizId}")
    public ResponseEntity<List<Question>> getQuestionsForPlay(@PathVariable Long quizId) {
        System.out.println("🎮 Requesting questions for playing quiz ID: " + quizId);

        try {
            // Lấy thông tin quiz
            Quiz quiz = quizService.getQuizById(quizId).orElse(null);
            if (quiz == null) {
                System.out.println("Không tìm thấy quiz ID: " + quizId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            List<Question> questions = questionService.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                System.out.println("Không tìm thấy questions cho quiz ID: " + quizId);
                return ResponseEntity.ok(questions);
            }
            System.out.println("Tìm thấy " + questions.size() + " questions cho quiz ID: " + quizId);

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy questions cho quiz ID " + quizId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Autowired
    private AnswerService answerService;

    // Xóa câu hỏi - chỉ chủ sở hữu quiz hoặc admin
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or @questionService.isOwner(#id, authentication.name)")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            answerService.deleteByQuestionId(id);
            questionService.deleteQuestion(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xoá câu hỏi: " + e.getMessage());
        }
    }

    // Cập nhật câu hỏi - chỉ chủ sở hữu quiz hoặc admin
    @PutMapping("/update/{questionId}")
    @PreAuthorize("hasRole('ADMIN') or @questionService.isOwner(#questionId, authentication.name)")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @RequestBody Question question) {
        Question updatedQuestion = questionService.updateQuestion(questionId, question);
        if (updatedQuestion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedQuestion);
    }

    // Tạo câu hỏi - chỉ chủ sở hữu quiz hoặc admin
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or @quizService.isOwner(#question.quiz.id, authentication.name)")
    public ResponseEntity<Question> createQuestion(@RequestBody @P("question") Question question) {
        Question created = questionService.createQuestion(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
