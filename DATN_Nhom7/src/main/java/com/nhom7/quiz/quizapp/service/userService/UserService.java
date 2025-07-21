package com.nhom7.quiz.quizapp.service.userService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.UserResponse;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public List<UserResponse> searchUsers(String name, String email, String status) {
        List<User> users = userRepo.searchUsers(name, email, status);
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

}
