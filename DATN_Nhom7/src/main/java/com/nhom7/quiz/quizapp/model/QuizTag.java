package com.nhom7.quiz.quizapp.model;

import com.nhom7.quiz.quizapp.model.composite.QuizTagID;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_tags")
public class QuizTag {

    @EmbeddedId
    private QuizTagID id;

    // Quan hệ nhiều - một với Quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("quizId")  // ánh xạ quizId trong khóa chính
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // Quan hệ nhiều - một với Tag
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")  // ánh xạ tagId trong khóa chính
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // Constructors
    public QuizTag() {}

    public QuizTag(Quiz quiz, Tag tag) {
        this.quiz = quiz;
        this.tag = tag;
        this.id = new QuizTagID(quiz.getId(), tag.getId());
    }

    // Getters và setters
    public QuizTagID getId() {
        return id;
    }

    public void setId(QuizTagID id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}

