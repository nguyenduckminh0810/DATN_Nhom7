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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Report;
import com.nhom7.quiz.quizapp.model.dto.ReportDTO;
import com.nhom7.quiz.quizapp.repository.ReportRepo;
import com.nhom7.quiz.quizapp.service.ReportService;
import com.nhom7.quiz.quizapp.service.AdminService.adminservice;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Tạo báo cáo
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportDTO reportDTO) {
        try {
            Report report = reportService.createQuizReport(
                    reportDTO.getUserId(),
                    reportDTO.getQuizId(),
                    reportDTO.getReason());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Báo cáo đã được gửi thành công");
            response.put("reportId", report.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Có lỗi xảy ra khi tạo báo cáo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Lấy tất cả báo cáo
    @GetMapping
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

    // Lấy báo cáo theo trạng thái
    @GetMapping("/status/{status}")
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

    // Lấy chi tiết báo cáo theo ID
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

    // Cập nhật trạng thái báo cáo
    @PutMapping("/{id}/status")
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

    // Lấy báo cáo của user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportDTO>> getReportsByUserId(@PathVariable Long userId) {
        List<Report> reports = reportService.getReportsByUserId(userId);
        List<ReportDTO> reportDTOs = reports.stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reportDTOs);
    }

    // Lấy báo cáo của quiz
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<ReportDTO>> getReportsByQuizId(@PathVariable Long quizId) {
        List<Report> reports = reportService.getReportsByQuizId(quizId);
        List<ReportDTO> reportDTOs = reports.stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reportDTOs);
    }

    // Thống kê báo cáo
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReportStats() {
        Map<String, Object> stats = reportService.getReportStats();
        return ResponseEntity.ok(stats);
    }

    // Kiểm tra user đã báo cáo quiz chưa
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

    // Xóa báo cáo
    @DeleteMapping("/{id}")
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

    // Xử lí user bị report
    @Autowired
    private ReportRepo reportRepo;
    @Autowired
    private adminservice adminService;

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id) {
        Report report = reportRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy report"));

        report.setStatus("RESOLVED");
        reportRepo.save(report);

        adminService.checkAndBanUser(report.getReportedUser().getId());

        return ResponseEntity.ok("Đã xử lý report và kiểm tra user bị report");
    }

}