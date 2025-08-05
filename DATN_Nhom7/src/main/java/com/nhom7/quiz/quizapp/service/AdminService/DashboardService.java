package com.nhom7.quiz.quizapp.service.AdminService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Kiểm tra quyền admin
    private void checkAdminPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Chỉ admin mới có quyền truy cập dashboard");
        }
    }

    public DashboardDTO getDashboardStats() {
        checkAdminPermission();
        
        long totalUsers = userRepo.count();
        // ✅ Cập nhật để chỉ đếm quiz chưa bị xóa
        long totalQuizzes = quizRepo.countByIsPublicFalseAndDeletedFalse() + quizRepo.countByIsPublicTrueAndDeletedFalse();
        long totalAttempts = attemptRepo.count();
        long totalCategories = categoryRepo.count();
        long totalReports = reportRepo.count();

        // Số lượng quiz chờ duyệt = isPublic = false và chưa bị xóa
        long pendingApproval = quizRepo.countByIsPublicFalseAndDeletedFalse();

        return new DashboardDTO(
                totalUsers,
                totalQuizzes,
                totalAttempts,
                totalCategories,
                pendingApproval,
                totalReports);
    }

    public List<QuizPendingDTO> getPendingQuizzes() {
        checkAdminPermission();
        
        // ✅ Cập nhật để chỉ lấy quiz chưa bị xóa
        return quizRepo.findByIsPublicFalseAndDeletedFalseOrderByCreatedAtDesc().stream()
                .map(quiz -> new QuizPendingDTO(
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getUser() != null ? quiz.getUser().getFullName() : "Không rõ",
                        quiz.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Chức năng duyệt quiz trong modal tại AdminDashboard
    public void approveQuiz(Long quizId) {
        checkAdminPermission();
        
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz"));

        quiz.setPublic(true); // hoặc trạng thái là "approved"
        quizRepo.save(quiz);
    }

}
