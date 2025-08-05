package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.dto.DashboardDTO;
import com.nhom7.quiz.quizapp.model.dto.QuizPendingDTO;
import com.nhom7.quiz.quizapp.service.AdminService.DashboardService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardDTO getStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/dashboard/pending-quizzes")
    public ResponseEntity<List<QuizPendingDTO>> getPendingQuizzes() {
        List<QuizPendingDTO> pending = dashboardService.getPendingQuizzes();
        return ResponseEntity.ok(pending);
    }

    @PutMapping("/quizzes/{id}/approve")
    public ResponseEntity<?> approveQuiz(@PathVariable Long id) {
        dashboardService.approveQuiz(id); // Gọi service xử lý
        return ResponseEntity.ok().build();
    }

}
