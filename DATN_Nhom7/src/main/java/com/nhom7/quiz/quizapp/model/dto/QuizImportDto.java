package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class QuizImportDto {
    private String title;
    private String description;
    private Long categoryId;
    private String topicImageUrl;
    private List<QuestionImportDto> questions;

    // Constructors
    public QuizImportDto() {
    }

    public QuizImportDto(String title, String description, Long categoryId, List<QuestionImportDto> questions) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.questions = questions;
    }

    public QuizImportDto(String title, String description, Long categoryId, String topicImageUrl,
            List<QuestionImportDto> questions) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.topicImageUrl = topicImageUrl;
        this.questions = questions;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTopicImageUrl() {
        return topicImageUrl;
    }

    public void setTopicImageUrl(String topicImageUrl) {
        this.topicImageUrl = topicImageUrl;
    }

    public List<QuestionImportDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionImportDto> questions) {
        this.questions = questions;
    }
}