package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class QuestionImportDto {
    private String content;
    private int point;
    private int timeLimit; // ✅ THÊM FIELD THỜI GIAN
    private List<AnswerImportDto> answers;

    // Constructors
    public QuestionImportDto() {
    }

    public QuestionImportDto(String content, int point, List<AnswerImportDto> answers) {
        this.content = content;
        this.point = point;
        this.timeLimit = 30; // Mặc định 30 giây
        this.answers = answers;
    }

    public QuestionImportDto(String content, int point, int timeLimit, List<AnswerImportDto> answers) {
        this.content = content;
        this.point = point;
        this.timeLimit = timeLimit;
        this.answers = answers;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<AnswerImportDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerImportDto> answers) {
        this.answers = answers;
    }
}