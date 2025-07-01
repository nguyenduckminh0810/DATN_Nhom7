package com.nhom7.quiz.quizapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom7.quiz.quizapp.model.Quiz;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Long> {
}
