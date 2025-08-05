package com.nhom7.quiz.quizapp.model.dto;

public class ReviewRequestDTO {
    private Long userId;
    private int rating;
    private String reviewText;

    public ReviewRequestDTO(Long userId, int rating, String reviewText) {
        this.userId = userId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

}
