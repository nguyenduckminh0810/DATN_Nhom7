package com.nhom7.quiz.quizapp.model.composite;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class QuizTagID implements Serializable {

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "tag_id")
    private Long tagId;

    // Constructors
    public QuizTagID() {}

    public QuizTagID(Long quizId, Long tagId) {
        this.quizId = quizId;
        this.tagId = tagId;
    }

    // Getters and Setters
    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    // equals() và hashCode() rất quan trọng cho composite key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizTagID)) return false;
        QuizTagID that = (QuizTagID) o;
        return quizId.equals(that.quizId) && tagId.equals(that.tagId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(quizId, tagId);
    }
}

