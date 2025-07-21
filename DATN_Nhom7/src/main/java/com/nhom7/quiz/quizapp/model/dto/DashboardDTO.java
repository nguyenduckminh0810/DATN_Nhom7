package com.nhom7.quiz.quizapp.model.dto;

public class DashboardDTO {
    private long totalUsers;
    private long totalQuizzes;
    private long totalQuizAttempts;
    private long totalCategories;
    private long pendingApproval;
    private long totalReports;

    public DashboardDTO(long totalUsers, long totalQuizzes, long totalQuizAttempts, long totalCategories,
            long pendingApproval, long totalReports) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.totalQuizAttempts = totalQuizAttempts;
        this.totalCategories = totalCategories;
        this.pendingApproval = pendingApproval;
        this.totalReports = totalReports;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public long getTotalQuizAttempts() {
        return totalQuizAttempts;
    }

    public void setTotalQuizAttempts(long totalQuizAttempts) {
        this.totalQuizAttempts = totalQuizAttempts;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getPendingApproval() {
        return pendingApproval;
    }

    public void setPendingApproval(long pendingApproval) {
        this.pendingApproval = pendingApproval;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

}
