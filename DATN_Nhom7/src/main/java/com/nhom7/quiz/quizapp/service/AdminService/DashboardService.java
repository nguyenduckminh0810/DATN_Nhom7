package com.nhom7.quiz.quizapp.service.AdminService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.dto.DashboardDTO;
import com.nhom7.quiz.quizapp.model.dto.QuizPendingDTO;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;
import com.nhom7.quiz.quizapp.repository.QuizAttemptRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.ReportRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class DashboardService {

    private final UserRepo userRepo;
    private final QuizRepo quizRepo;
    private final QuizAttemptRepo attemptRepo;
    private final CategoryRepo categoryRepo;
    private final ReportRepo reportRepo;

    public DashboardService(
            UserRepo userRepo,
            QuizRepo quizRepo,
            QuizAttemptRepo attemptRepo,
            CategoryRepo categoryRepo,
            ReportRepo reportRepo) {
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.attemptRepo = attemptRepo;
        this.categoryRepo = categoryRepo;
        this.reportRepo = reportRepo;
    }

    public DashboardDTO getDashboardStats() {
        long totalUsers = userRepo.count();
        long totalQuizzes = quizRepo.count();
        long totalAttempts = attemptRepo.count();
        long totalCategories = categoryRepo.count();
        long totalReports = reportRepo.count();

        // Số lượng quiz chờ duyệt = isPublic = false
        long pendingApproval = quizRepo.countByIsPublicFalse();

        return new DashboardDTO(
                totalUsers,
                totalQuizzes,
                totalAttempts,
                totalCategories,
                pendingApproval,
                totalReports);
    }

    public List<QuizPendingDTO> getPendingQuizzes() {
        return quizRepo.findByIsPublicFalseOrderByCreatedAtDesc().stream()
                .map(quiz -> new QuizPendingDTO(
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getUser() != null ? quiz.getUser().getFullName() : "Không rõ",
                        quiz.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Chức năng duyệt quiz trong modal tại AdminDashboard
    public void approveQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz"));

        quiz.setPublic(true); // hoặc trạng thái là "approved"
        quizRepo.save(quiz);
    }

}
