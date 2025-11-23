package com.nhom7.quiz.quizapp.model;

public enum AttemptStatus {
    IN_PROGRESS,    // Đang làm dở
    SUBMITTED,      // Đã nộp bài
    COMPLETED,      // Hoàn thành (đã chấm điểm)
    ABANDONED,      // Bỏ dở (user chọn làm lại)
    CANCELLED,      // Hủy bỏ
    EXPIRED         // Hết hạn
}


