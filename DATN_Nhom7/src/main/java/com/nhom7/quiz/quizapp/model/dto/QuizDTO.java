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

    public QuizDTO(Long id, String title, boolean isPublic, LocalDateTime createdAt, List<String> tags,
            String categoryName) {
        this.id = id;
        this.title = title;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.tags = tags;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getCategoryName() {
        return categoryName;
    }

}
