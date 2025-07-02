package com.nhom7.quiz.quizapp.service.AdminService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class adminservice {
    @Autowired
    private UserRepo userRepo;

    // Phương thức để lấy danh sách tất cả người dùng
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
