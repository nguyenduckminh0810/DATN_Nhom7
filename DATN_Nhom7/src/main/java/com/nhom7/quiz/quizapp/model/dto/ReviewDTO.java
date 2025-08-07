package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDateTime;
import com.nhom7.quiz.quizapp.model.QuizReview;

public class ReviewDTO {
    private Long id;
    private int rating;
    private String reviewText;
    private LocalDateTime createdAt;
    
    // User info
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(QuizReview review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.reviewText = review.getReviewText();
        this.createdAt = review.getCreatedAt();
        
        // Safely extract user info
        if (review.getUser() != null) {
            this.userId = review.getUser().getId();
            this.username = review.getUser().getUsername();
            this.userFullName = review.getUser().getFullName();
            this.userAvatarUrl = review.getUser().getAvatarUrl();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
}