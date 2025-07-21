package com.nhom7.quiz.quizapp.model.dto;

public class CorrectAnswerDTO {
    private Long questionId;
    private Long correctAnswerId;

    public CorrectAnswerDTO(Long questionId, Long correctAnswerId) {
        this.questionId = questionId;
        this.correctAnswerId = correctAnswerId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getCorrectAnswerId() {
        return correctAnswerId;
    }
}