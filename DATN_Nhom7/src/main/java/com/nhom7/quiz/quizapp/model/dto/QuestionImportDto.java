package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class QuestionImportDto {
    private String content;
    private int point;
    private List<AnswerImportDto> answers;
    
    // Constructors
    public QuestionImportDto() {}
    
    public QuestionImportDto(String content, int point, List<AnswerImportDto> answers) {
        this.content = content;
        this.point = point;
        this.answers = answers;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public int getPoint() { return point; }
    public void setPoint(int point) { this.point = point; }
    
    public List<AnswerImportDto> getAnswers() { return answers; }
    public void setAnswers(List<AnswerImportDto> answers) { this.answers = answers; }
}