package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;
import com.nhom7.quiz.quizapp.model.Report;

public class ReportDTO {
    // Fields cho tạo báo cáo mới (INPUT)
    private Long userId;
    private Long quizId;
    private String reason;
    private String status;
    
    // Fields cho hiển thị báo cáo (OUTPUT)
    private Long id;
    private LocalDateTime createdAt;
    
    // User info (OUTPUT)
    private String userFullName;
    private String userEmail;
    
    // Quiz info (OUTPUT)
    private String quizTitle;
    private String quizCreator;
    
    // ✅ THÊM FIELDS CHO ADMIN RESPONSE
    private String adminResponse;
    private LocalDateTime resolvedAt;
    private String resolvedBy;

    // Constructor rỗng (cho tạo mới từ request)
    public ReportDTO() {}

    // Constructor từ Report Entity (cho response)
    public ReportDTO(Report report) {
        this.id = report.getId();
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
        
        // User info
        if (report.getUser() != null) {
            this.userId = report.getUser().getId();
            this.userFullName = report.getUser().getFullName();
            this.userEmail = report.getUser().getEmail();
        }
        
        // Quiz info
        if (report.getQuiz() != null) {
            this.quizId = report.getQuiz().getId();
            this.quizTitle = report.getQuiz().getTitle();
            if (report.getQuiz().getUser() != null) {
                this.quizCreator = report.getQuiz().getUser().getFullName();
            }
        }
        
        // ✅ THÊM ADMIN RESPONSE INFO
        this.adminResponse = report.getAdminResponse();
        this.resolvedAt = report.getResolvedAt();
        if (report.getResolvedBy() != null) {
            this.resolvedBy = report.getResolvedBy().getFullName();
        }
    }

    // Constructor cho tạo báo cáo mới
    public ReportDTO(Long userId, Long quizId, String reason) {
        this.userId = userId;
        this.quizId = quizId;
        this.reason = reason;
    }

    // Constructor cho cập nhật status
    public ReportDTO(String status) {
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public Long getQuizId() { 
        return quizId; 
    }
    public void setQuizId(Long quizId) { 
        this.quizId = quizId; 
    }

    public String getReason() { 
        return reason; 
    }
    public void setReason(String reason) { 
        this.reason = reason; 
    }

    public String getStatus() { 
        return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public String getUserFullName() { 
        return userFullName; 
    }
    public void setUserFullName(String userFullName) { 
        this.userFullName = userFullName; 
    }

    public String getUserEmail() { 
        return userEmail; 
    }
    public void setUserEmail(String userEmail) { 
        this.userEmail = userEmail; 
    }

    public String getQuizTitle() { 
        return quizTitle; 
    }
    public void setQuizTitle(String quizTitle) { 
        this.quizTitle = quizTitle; 
    }

    public String getQuizCreator() { 
        return quizCreator; 
    }
    public void setQuizCreator(String quizCreator) { 
        this.quizCreator = quizCreator; 
    }
    
    // ✅ GETTERS VÀ SETTERS CHO ADMIN RESPONSE
    public String getAdminResponse() { 
        return adminResponse; 
    }
    public void setAdminResponse(String adminResponse) { 
        this.adminResponse = adminResponse; 
    }
    
    public LocalDateTime getResolvedAt() { 
        return resolvedAt; 
    }
    public void setResolvedAt(LocalDateTime resolvedAt) { 
        this.resolvedAt = resolvedAt; 
    }
    
    public String getResolvedBy() { 
        return resolvedBy; 
    }
    public void setResolvedBy(String resolvedBy) { 
        this.resolvedBy = resolvedBy; 
    }
}