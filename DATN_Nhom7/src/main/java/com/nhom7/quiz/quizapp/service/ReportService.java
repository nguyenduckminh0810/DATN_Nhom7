package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Report;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.ReportRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import com.nhom7.quiz.quizapp.service.NotificationService;

@Service
public class ReportService {

    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private QuizRepo quizRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NotificationService notificationService;

    // Kiểm tra quyền admin
    private void checkAdminPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Chỉ admin mới có quyền thực hiện thao tác này");
        }
    }

    // Kiểm tra quyền user (chỉ có thể xem báo cáo của mình)
    private void checkUserPermission(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Không có quyền truy cập");
        }

        // Admin có thể xem tất cả
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // User thường chỉ có thể xem báo cáo của mình
            String currentUsername = authentication.getName();
            User currentUser = userRepo.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                throw new AccessDeniedException("Bạn chỉ có thể xem báo cáo của mình");
            }
        }
    }

    // Method để hỗ trợ @PreAuthorize
    public boolean isCurrentUser(Long userId, String username) {
        try {
            User user = userRepo.findByUsername(username).orElse(null);
            return user != null && user.getId().equals(userId);
        } catch (Exception e) {
            System.err.println("Error checking user permission: " + e.getMessage());
            return false;
        }
    }

    // Tạo báo cáo quiz mới
    public Report createQuizReport(Long userId, Long quizId, String reason) {
        // Kiểm tra user tồn tại
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User không tồn tại!");
        }

        // Kiểm tra quiz tồn tại
        Optional<Quiz> quizOpt = quizRepo.findById(quizId);
        if (quizOpt.isEmpty()) {
            throw new IllegalArgumentException("Quiz không tồn tại!");
        }

        // Kiểm tra user đã báo cáo quiz này chưa
        List<Report> existingReports = reportRepo.findByQuizIdAndUserId(quizId, userId);
        if (!existingReports.isEmpty()) {
            throw new IllegalArgumentException("Bạn đã báo cáo quiz này rồi!");
        }

        // Tạo báo cáo mới
        Report report = new Report();
        report.setUser(userOpt.get());
        report.setQuiz(quizOpt.get());
        report.setComment(null);
        report.setReason(reason);
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        Quiz quiz = quizOpt.get();

        report.setReportedUser(quiz.getUser());

        Report savedReport = reportRepo.save(report);

        // GỬI NOTIFICATION CHO ADMIN KHI CÓ REPORT MỚI
        try {
            notificationService.sendNewReportNotification(savedReport);
            System.out.println(" Đã gửi notification cho admin về report mới: " + savedReport.getId());
        } catch (Exception e) {
            System.err.println(" Lỗi khi gửi notification cho admin: " + e.getMessage());
            e.printStackTrace();
        }

        return savedReport;
    }

    // Lấy tất cả báo cáo với phân trang (chỉ admin)
    public Page<Report> getAllReports(int page, int size) {
        checkAdminPermission();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportRepo.findAll(pageable);
    }

    public Page<Report> getReportsByStatus(String status, int page, int size) {
        checkAdminPermission();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportRepo.findByStatus(status, pageable);
    }

    // Cập nhật trạng thái báo cáo (chỉ admin)
    public Report updateReportStatus(Long reportId, String newStatus) {
        checkAdminPermission();

        Optional<Report> reportOpt = reportRepo.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Báo cáo không tồn tại!");
        }

        // Validate status
        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận: PENDING, RESOLVED, REJECTED");
        }

        Report report = reportOpt.get();
        String oldStatus = report.getStatus();
        report.setStatus(newStatus);

        // Xử lý quiz khi báo cáo được phê duyệt
        if ("RESOLVED".equals(newStatus) && !"RESOLVED".equals(oldStatus)) {
            Quiz reportedQuiz = report.getQuiz();
            if (reportedQuiz != null) {
                reportedQuiz.setPublic(false);
                quizRepo.save(reportedQuiz);

                System.out.println(" Quiz ID " + reportedQuiz.getId() + " đã được ẩn do báo cáo được phê duyệt");
            }
        }

        return reportRepo.save(report);
    }

    // Lấy báo cáo theo ID (admin hoặc user sở hữu)
    public Optional<Report> getReportById(Long id) {
        Optional<Report> reportOpt = reportRepo.findById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            checkUserPermission(report.getUser().getId());
        }
        return reportOpt;
    }

    public List<Report> getReportsByUserId(Long userId) {
        checkUserPermission(userId);
        return reportRepo.findByUser_Id(userId);
    }

    // Lấy báo cáo của quiz (chỉ admin)
    public List<Report> getReportsByQuizId(Long quizId) {
        checkAdminPermission();
        return reportRepo.findByQuiz_Id(quizId);
    }

    // Thống kê báo cáo (chỉ admin)
    public Map<String, Object> getReportStats() {
        checkAdminPermission();

        Map<String, Object> stats = new HashMap<>();

        // Tổng số báo cáo
        long totalReports = reportRepo.count();
        stats.put("totalReports", totalReports);

        // Số báo cáo theo trạng thái
        long pendingReports = reportRepo.countByStatus("PENDING");
        long resolvedReports = reportRepo.countByStatus("RESOLVED");
        long rejectedReports = reportRepo.countByStatus("REJECTED");

        stats.put("pendingReports", pendingReports);
        stats.put("resolvedReports", resolvedReports);
        stats.put("rejectedReports", rejectedReports);

        // THÊM APPROVED STATS (MAP TỪ RESOLVED)
        stats.put("approvedReports", resolvedReports);

        // Phần trăm
        if (totalReports > 0) {
            stats.put("pendingPercentage", Math.round((pendingReports * 100.0) / totalReports * 10.0) / 10.0);
            stats.put("resolvedPercentage", Math.round((resolvedReports * 100.0) / totalReports * 10.0) / 10.0);
            stats.put("rejectedPercentage", Math.round((rejectedReports * 100.0) / totalReports * 10.0) / 10.0);
        } else {
            stats.put("pendingPercentage", 0.0);
            stats.put("resolvedPercentage", 0.0);
            stats.put("rejectedPercentage", 0.0);
        }

        return stats;
    }

    // Xóa báo cáo (chỉ admin)
    public boolean deleteReport(Long reportId) {
        checkAdminPermission();

        if (reportRepo.existsById(reportId)) {
            reportRepo.deleteById(reportId);
            return true;
        }
        return false;
    }

    // Kiểm tra user đã báo cáo quiz này chưa
    public boolean hasUserReportedQuiz(Long userId, Long quizId) {
        List<Report> reports = reportRepo.findByQuizIdAndUserId(quizId, userId);
        return !reports.isEmpty();
    }

    private boolean isValidStatus(String status) {
        return status.equals("PENDING") || status.equals("RESOLVED") || status.equals("REJECTED") ||
                status.equals("APPROVED"); // THÊM APPROVED
    }

    // METHOD MỚI: XỬ LÝ ADMIN ACTION VỚI NOTIFICATION
    public Report handleReportAction(Long reportId, String action, String adminResponse, User admin) {
        checkAdminPermission();

        System.out.println(" ReportService.handleReportAction called:");
        System.out.println(" Report ID: " + reportId);
        System.out.println(" Action: " + action);
        System.out.println(" Admin Response: " + adminResponse);
        System.out.println(" Admin: " + admin.getFullName());

        Optional<Report> reportOpt = reportRepo.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Báo cáo không tồn tại!");
        }

        Report report = reportOpt.get();
        String oldStatus = report.getStatus();

        System.out.println(" Found report: " + report.getId() + ", old status: " + oldStatus);

        // CẬP NHẬT REPORT
        // MAP APPROVED THÀNH RESOLVED ĐỂ TƯƠNG THÍCH VỚI DATABASE
        String dbStatus = "APPROVED".equals(action) ? "RESOLVED" : action;
        report.setStatus(dbStatus);
        report.setAdminResponse(adminResponse);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(admin);

        // XỬ LÝ QUIZ KHI APPROVED
        System.out.println(" Checking quiz hiding logic:");
        System.out.println(" Action: " + action);
        System.out.println(" Old Status: " + oldStatus);
        System.out.println(" Should hide quiz: " + ("APPROVED".equals(action) && !"RESOLVED".equals(oldStatus)));

        if ("APPROVED".equals(action) && !"RESOLVED".equals(oldStatus)) {
            Quiz reportedQuiz = report.getQuiz();
            if (reportedQuiz != null) {
                System.out.println(
                        " Quiz before hiding - ID: " + reportedQuiz.getId() + ", isPublic: " + reportedQuiz.isPublic());
                reportedQuiz.setPublic(false);
                quizRepo.save(reportedQuiz);
                System.out.println(" Quiz ID " + reportedQuiz.getId() + " đã được ẩn do báo cáo được chấp nhận");
                System.out.println(
                        " Quiz after hiding - ID: " + reportedQuiz.getId() + ", isPublic: " + reportedQuiz.isPublic());
            } else {
                System.out.println(" Quiz is null, cannot hide");
            }
        } else {
            System.out.println("Quiz hiding condition not met - Action: " + action + ", Old Status: " + oldStatus);
        }

        Report savedReport = reportRepo.save(report);

        // GỬI NOTIFICATION CHO USER ĐÃ BÁO CÁO
        try {
            notificationService.sendReportActionNotification(savedReport, admin);
            System.out.println(" Đã gửi notification cho user: " + savedReport.getUser().getUsername());
        } catch (Exception e) {
            System.err.println(" Lỗi khi gửi notification: " + e.getMessage());
            e.printStackTrace();
        }

        return savedReport;
    }
}