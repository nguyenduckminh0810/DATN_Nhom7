package com.nhom7.quiz.quizapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Report;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.ReportDTO;
import com.nhom7.quiz.quizapp.model.dto.ReportActionDTO;
import com.nhom7.quiz.quizapp.repository.ReportRepo;
import com.nhom7.quiz.quizapp.service.QuizService;
import com.nhom7.quiz.quizapp.service.ReportService;
import com.nhom7.quiz.quizapp.service.AdminService.adminservice;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private LoginService userService;
    @Autowired
    private QuizService quizService;

    private User authUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    // Tạo báo cáo - ai cũng có thể tạo
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReport(@RequestBody ReportDTO reportDTO, Authentication authentication) {
        try {
            // Lấy user đang đăng nhập (reporter)
            User reporter = authUser(authentication);

            // Lấy quiz và owner
            Quiz quiz = quizService.findById(reportDTO.getQuizId());

            // ❌ Không cho tự report quiz của mình
            if (quiz.getUser().getId().equals(reporter.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("status", "ERROR", "message", "Bạn không thể báo cáo quiz do chính bạn tạo."));
            }

            // (Khuyến nghị) chặn report trùng
            if (reportService.hasUserReportedQuiz(reporter.getId(), quiz.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("status", "ERROR", "message", "Bạn đã báo cáo quiz này rồi."));
            }

            // Tạo report – ép dùng reporterId từ BE để tránh giả mạo
            Report report = reportService.createQuizReport(
                    reporter.getId(),
                    quiz.getId(),
                    reportDTO.getReason());

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Báo cáo đã được gửi thành công",
                    "reportId", report.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Có lỗi xảy ra khi tạo báo cáo"));
        }
    }

    // Lấy tất cả báo cáo - chỉ admin
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Report> reportPage = reportService.getAllReports(page, size);

        List<ReportDTO> reportDTOs = reportPage.getContent().stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("reports", reportDTOs);
        response.put("currentPage", reportPage.getNumber());
        response.put("totalPages", reportPage.getTotalPages());
        response.put("totalItems", reportPage.getTotalElements());
        response.put("pageSize", reportPage.getSize());

        return ResponseEntity.ok(response);
    }

    // Lấy báo cáo theo trạng thái - chỉ admin
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReportsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Report> reportPage = reportService.getReportsByStatus(status, page, size);

        List<ReportDTO> reportDTOs = reportPage.getContent().stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("reports", reportDTOs);
        response.put("currentPage", reportPage.getNumber());
        response.put("totalPages", reportPage.getTotalPages());
        response.put("totalItems", reportPage.getTotalElements());
        response.put("pageSize", reportPage.getSize());
        response.put("status", status);

        return ResponseEntity.ok(response);
    }

    // Lấy chi tiết báo cáo theo ID - admin hoặc user sở hữu
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        Optional<Report> reportOpt = reportService.getReportById(id);

        if (reportOpt.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Không tìm thấy báo cáo");
            return ResponseEntity.notFound().build();
        }

        ReportDTO reportDTO = new ReportDTO(reportOpt.get());
        return ResponseEntity.ok(reportDTO);
    }

    // Cập nhật trạng thái báo cáo - chỉ admin
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Long id,
            @RequestBody ReportDTO reportDTO) {

        try {
            Report updatedReport = reportService.updateReportStatus(id, reportDTO.getStatus());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Cập nhật trạng thái báo cáo thành công");
            response.put("report", new ReportDTO(updatedReport));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Lấy báo cáo của user - admin hoặc user sở hữu
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<List<ReportDTO>> getReportsByUserId(@PathVariable Long userId) {
        List<Report> reports = reportService.getReportsByUserId(userId);
        List<ReportDTO> reportDTOs = reports.stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reportDTOs);
    }

    // Lấy báo cáo của quiz - chỉ admin
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportDTO>> getReportsByQuizId(@PathVariable Long quizId) {
        List<Report> reports = reportService.getReportsByQuizId(quizId);
        List<ReportDTO> reportDTOs = reports.stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reportDTOs);
    }

    // Thống kê báo cáo - chỉ admin
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReportStats() {
        Map<String, Object> stats = reportService.getReportStats();
        return ResponseEntity.ok(stats);
    }

    // Kiểm tra user đã báo cáo quiz chưa - ai cũng có thể kiểm tra
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkUserReported(
            @RequestParam Long userId,
            @RequestParam Long quizId) {

        boolean hasReported = reportService.hasUserReportedQuiz(userId, quizId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasReported", hasReported);
        response.put("message", hasReported ? "Bạn đã báo cáo quiz này rồi" : "Chưa báo cáo quiz này");

        return ResponseEntity.ok(response);
    }

    // Xóa báo cáo - chỉ admin
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        boolean deleted = reportService.deleteReport(id);

        if (deleted) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Xóa báo cáo thành công");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Không tìm thấy báo cáo để xóa");
            return ResponseEntity.notFound().build();
        }
    }

    // Xử lí user bị report - chỉ admin
    @Autowired
    private ReportRepo reportRepo;
    @Autowired
    private adminservice adminService;

    @PutMapping("/reports/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolveReport(@PathVariable Long id) {
        Report report = reportRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy report"));

        report.setStatus("RESOLVED");
        reportRepo.save(report);

        if (report.getReportedUser() != null) {
            adminService.checkAndBanUser(report.getReportedUser().getId());
        }

        return ResponseEntity.ok("Đã xử lý report và kiểm tra user bị report");
    }

    // ✅ ENDPOINT MỚI: ADMIN ACTION VỚI REPORT
    @PutMapping("/{reportId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> handleReportAction(
            @PathVariable Long reportId,
            @RequestBody ReportActionDTO actionDTO,
            Authentication authentication) {
        
        System.out.println("🔧 ===== REPORT ACTION ENDPOINT CALLED =====");
        System.out.println("🔧 Report ID: " + reportId);
        System.out.println("🔧 Action DTO: " + actionDTO);
        
        try {
            // ✅ LẤY ADMIN ĐANG THỰC HIỆN ACTION
            User admin = authUser(authentication);
            
            // ✅ DEBUG: In ra thông tin action
            System.out.println("🔧 Admin " + admin.getFullName() + " thực hiện action: " + actionDTO.getAction());
            System.out.println("🔧 Report ID: " + reportId);
            System.out.println("🔧 Admin Response: " + actionDTO.getAdminResponse());
            System.out.println("🔧 Admin Note: " + actionDTO.getAdminNote());
            System.out.println("🔧 Full ActionDTO: " + actionDTO);
            
            // ✅ XỬ LÝ REPORT ACTION
            Report updatedReport = reportService.handleReportAction(
                reportId, 
                actionDTO.getAction(), 
                actionDTO.getAdminResponse(), 
                admin
            );
            
            // ✅ TRẢ VỀ RESPONSE THÀNH CÔNG
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Đã xử lý report thành công");
            response.put("report", new ReportDTO(updatedReport));
            response.put("action", actionDTO.getAction());
            response.put("adminName", admin.getFullName());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý report action: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Có lỗi xảy ra khi xử lý report");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}