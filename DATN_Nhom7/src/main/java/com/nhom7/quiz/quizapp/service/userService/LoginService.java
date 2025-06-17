package com.nhom7.quiz.quizapp.service.userService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;

import org.springframework.stereotype.Service;
@Service
public class LoginService {
    @Autowired
	private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;
	// Enum để xác định trạng thái đăng nhập
    public enum LoginStatus {
        SUCCESS, USER_NOT_FOUND, WRONG_PASSWORD
    }
	// Kết quả đăng nhập
    public record LoginResult(LoginStatus status, User user) {}
	// Phương thức kiểm tra đăng nhập
    public LoginResult checkLogin(String username, String rawPassword) {
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new LoginResult(LoginStatus.USER_NOT_FOUND, null);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return new LoginResult(LoginStatus.WRONG_PASSWORD, null);
        }

        return new LoginResult(LoginStatus.SUCCESS, user);
    }

}
