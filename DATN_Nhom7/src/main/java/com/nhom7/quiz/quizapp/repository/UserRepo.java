package com.nhom7.quiz.quizapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.User;

public interface UserRepo extends JpaRepository<User, Long>{
    //Trả về người dùng theo tên đăng nhập hoặc email
    //Nếu không tìm thấy thì trả về Optional.empty()
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
