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
    public record LoginResult(LoginStatus status, User user) {
    }

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

    // Phương thức xác thực người dùng quản trị
    public enum LoginStatusForAdmin {
        SUCCESS, USER_NOT_FOUND, WRONG_PASSWORD, NOT_ADMIN
    }

    public record LoginResultForAdmin(LoginStatusForAdmin status, User user) {
    }

    public LoginResultForAdmin authenticateAdmin(String username, String password) {
        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new LoginResultForAdmin(LoginStatusForAdmin.USER_NOT_FOUND, null);
        }

        User user = userOpt.get();

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return new LoginResultForAdmin(LoginStatusForAdmin.NOT_ADMIN, null);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new LoginResultForAdmin(LoginStatusForAdmin.WRONG_PASSWORD, null);
        }

        return new LoginResultForAdmin(LoginStatusForAdmin.SUCCESS, user);
    }

    // Phương thức tìm kiếm người dùng theo tên đăng nhập
    public User findByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    // Lưu người dùng
    public void saveUser(User user) {
        userRepo.save(user);
    }

    // Xóa người dùng
    public void deleteUser(User user) {
        userRepo.delete(user);
    }

    // Tìm kiếm người dùng theo id
    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

}
