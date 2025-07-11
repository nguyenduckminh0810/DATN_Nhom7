package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom7.quiz.quizapp.model.Quiz;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByUserId(Long userId, Pageable pageable);

    Page<Quiz> findByIsPublic(boolean isPublic, Pageable pageable);
}
