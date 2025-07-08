package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.Image;

public interface ImageRepo extends JpaRepository<Image, Long> {
    // Custom query methods can be defined here if needed
    Image findByQuestionId(Long id);

    Image findByQuizId(Long id);
}
