package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.User;

public interface UserRepo extends JpaRepository<User, Long>{

}
