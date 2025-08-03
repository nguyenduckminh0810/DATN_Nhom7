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
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Report;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.ReportRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class ReportService {

    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private QuizRepo quizRepo;

    @Autowired
    private UserRepo userRepo;

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

        return reportRepo.save(report);
    }

    // Lấy tất cả báo cáo với phân trang
    public Page<Report> getAllReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportRepo.findAll(pageable);
    }

    public Page<Report> getReportsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportRepo.findByStatus(status, pageable);
    }

    // Cập nhật trạng thái báo cáo
    public Report updateReportStatus(Long reportId, String newStatus) {
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

                System.out.println("🔒 Quiz ID " + reportedQuiz.getId() + " đã được ẩn do báo cáo được phê duyệt");
            }
        }

        return reportRepo.save(report);
    }

    // Lấy báo cáo theo ID
    public Optional<Report> getReportById(Long id) {
        return reportRepo.findById(id);
    }

    public List<Report> getReportsByUserId(Long userId) {
        return reportRepo.findByUser_Id(userId);
    }

    // Lấy báo cáo của quiz
    public List<Report> getReportsByQuizId(Long quizId) {
        return reportRepo.findByQuiz_Id(quizId);
    }

    // Thống kê báo cáo
    public Map<String, Object> getReportStats() {
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

    // Xóa báo cáo
    public boolean deleteReport(Long reportId) {
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
        return status.equals("PENDING") || status.equals("RESOLVED") || status.equals("REJECTED");
    }
}