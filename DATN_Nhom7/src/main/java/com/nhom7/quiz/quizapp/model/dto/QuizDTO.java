package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDTO {
    private Long id;
    private String title;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private List<String> tags;
    private String categoryName;
    private Long categoryId;
    private String creatorName;

    public QuizDTO(Long id, String title, boolean isPublic, LocalDateTime createdAt, List<String> tags,
            String categoryName, Long categoryId, String creatorName) {
        this.id = id;
        this.title = title;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.tags = tags;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.creatorName = creatorName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

}
