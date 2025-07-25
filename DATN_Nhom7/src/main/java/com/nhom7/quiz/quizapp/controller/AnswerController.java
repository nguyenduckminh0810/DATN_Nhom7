package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.service.AnswerService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {
    @Autowired
    private AnswerService answerService;

    @GetMapping("/{questionId}")
    public List<Answer> getAnswersByQuestionId(@PathVariable Long questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateAnswers(@RequestBody List<Answer> answers) {
        List<Answer> updated = answerService.updateAnswers(answers);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/create-multiple")
    public ResponseEntity<?> createMultipleAnswers(@RequestBody List<Answer> answers) {
        List<Answer> saved = answerService.createMultipleAnswers(answers);
        return ResponseEntity.ok(saved);
    }
}
