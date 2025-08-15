package com.nhom7.quiz.quizapp.model.dto;

public class ReportActionDTO {
    private Long reportId;
    private String action; // "APPROVED" hoặc "REJECTED"
    private String adminResponse;
    private String adminNote;

    // Constructor rỗng
    public ReportActionDTO() {}

    // Constructor đầy đủ
    public ReportActionDTO(Long reportId, String action, String adminResponse, String adminNote) {
        this.reportId = reportId;
        this.action = action;
        this.adminResponse = adminResponse;
        this.adminNote = adminNote;
    }

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    @Override
    public String toString() {
        return "ReportActionDTO{" +
                "reportId=" + reportId +
                ", action='" + action + '\'' +
                ", adminResponse='" + adminResponse + '\'' +
                ", adminNote='" + adminNote + '\'' +
                '}';
    }
}
