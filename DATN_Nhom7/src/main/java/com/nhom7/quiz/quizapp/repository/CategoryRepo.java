package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.Category;
public interface CategoryRepo extends JpaRepository<Category, Long> {
}

