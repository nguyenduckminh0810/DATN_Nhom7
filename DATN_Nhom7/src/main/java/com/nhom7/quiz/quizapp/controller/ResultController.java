package com.nhom7.quiz.quizapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.dto.EvaluationResult;
import com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO;
import com.nhom7.quiz.quizapp.model.dto.ResultDTO;
import com.nhom7.quiz.quizapp.service.ResultService;

@RestController
@RequestMapping("/api/result")
public class ResultController {
    @Autowired
    private ResultService resultService;

    // Nộp kết quả quiz - ai cũng có thể nộp
    @PostMapping("/submit")
    public ResponseEntity<EvaluationResult> submitResult(@RequestBody QuizSubmissionDTO submission) {
        try {
            System.out.println("📝 Submitting quiz result for user: " + submission.getUserId());
            System.out.println("📝 Quiz ID: " + submission.getQuizId());
            System.out.println("📝 Time taken: " + submission.getTimeTaken() + " seconds");
            
            EvaluationResult result = resultService.evaluateAndSave(submission);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Error submitting quiz result: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Xem kết quả của user - chỉ admin hoặc user sở hữu
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @resultService.checkUserPermission(#userId, authentication.principal)")
    public List<ResultDTO> getResultsByUserId(@PathVariable Long userId) {
        List<Result> results = resultService.getResultsByUserId(userId);
        return results.stream()
                .map(ResultDTO::new)
                .collect(Collectors.toList());
    }

}