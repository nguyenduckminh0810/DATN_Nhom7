package com.nhom7.quiz.quizapp.service.userService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;
@Service
public class ReginService {
    // Enum để xác định trạng thái đăng ký
    public enum RegisterStatus {
        SUCCESS,
        USERNAME_EXISTS,
        EMAIL_EXISTS,
        FULL_NAME_REQUIRED,
    }
    // Kết quả đăng ký
    public record RegisterResult(RegisterStatus status, User user) {
    }

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Phương thức đăng ký người dùng mới
    public RegisterResult register(User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            return new RegisterResult(RegisterStatus.EMAIL_EXISTS, null);
        }

        if (userRepo.existsByUsername(user.getUsername())) {
            return new RegisterResult(RegisterStatus.USERNAME_EXISTS, null);
        }
        if(user.getFullName() == null || user.getFullName().isEmpty()) {
            return new RegisterResult(RegisterStatus.FULL_NAME_REQUIRED, null);
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepo.save(user);

        return new RegisterResult(RegisterStatus.SUCCESS, savedUser);
    }
}
