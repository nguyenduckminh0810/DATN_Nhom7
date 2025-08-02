package com.nhom7.quiz.quizapp.model.dto;

public class AnswerImportDto {
    private String content;
    private boolean isCorrect;
    
    // Constructors
    public AnswerImportDto() {}
    
    public AnswerImportDto(String content, boolean isCorrect) {
        this.content = content;
        this.isCorrect = isCorrect;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}