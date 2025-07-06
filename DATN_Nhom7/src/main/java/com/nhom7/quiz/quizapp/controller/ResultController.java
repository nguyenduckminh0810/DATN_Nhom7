package com.nhom7.quiz.quizapp.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO;
import com.nhom7.quiz.quizapp.model.dto.ResultDTO;
import com.nhom7.quiz.quizapp.service.ResultService;

@RestController
@RequestMapping("/api/result")
public class ResultController {
    @Autowired
    private ResultService resultService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitResult(@RequestBody QuizSubmissionDTO submission) {
        int score = resultService.evaluateAndSave(submission);
        return ResponseEntity.ok(Map.of("score", score));
    }

    @GetMapping("/user/{userId}")
    public List<ResultDTO> getResultsByUserId(@PathVariable Long userId) {
        List<Result> results = resultService.getResultsByUserId(userId);
        return results.stream()
                .map(ResultDTO::new)
                .collect(Collectors.toList());
    }

}