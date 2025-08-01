package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Question;
import com.nhom7.quiz.quizapp.service.AnswerService;
import com.nhom7.quiz.quizapp.service.QuestionService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/question")

public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @GetMapping("/{quizId}")
    public ResponseEntity<List<Question>> getQuestionsByQuizId(@PathVariable Long quizId) {
        try {
            List<Question> questions = questionService.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                System.out.println("Không tìm thấy questions cho quiz ID: " + quizId);
                // ✅ LUÔN TRẢ VỀ ARRAY RỖNG THAY VÌ NO_CONTENT
                return ResponseEntity.ok(questions);
            }
            System.out.println("Tìm thấy " + questions.size() + " questions cho quiz ID: " + quizId);

            // ✅ DEBUG: In ra timeLimit của từng question
            for (Question q : questions) {
                System.out.println("Question ID: " + q.getId() + ", TimeLimit: " + q.getTimeLimit());
            }

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy questions cho quiz ID " + quizId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Autowired
    private AnswerService answerService;

    @DeleteMapping("/delete/{id}")
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

    @PutMapping("/update/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @RequestBody Question question) {
        Question updatedQuestion = questionService.updateQuestion(questionId, question);
        if (updatedQuestion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedQuestion);
    }

    @PostMapping("/create")
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        Question createdQuestion = questionService.createQuestion(question);
        return ResponseEntity.status(201).body(createdQuestion);
    }

}
