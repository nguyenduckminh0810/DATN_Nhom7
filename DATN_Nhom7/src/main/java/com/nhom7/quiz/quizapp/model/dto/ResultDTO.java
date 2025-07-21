package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;

import com.nhom7.quiz.quizapp.model.Result;

public class ResultDTO {
    private Long id;
    private String quizTitle;
    private int score;
    private LocalDateTime completedAt;

    public ResultDTO(Result result) {
        this.id = result.getId();
        this.quizTitle = result.getQuiz().getTitle();
        this.score = result.getScore();
        this.completedAt = result.getCompletedAt();
    }

    // 🟢 Bắt buộc cần getter nếu muốn trả về JSON
    public Long getId() {
        return id;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
