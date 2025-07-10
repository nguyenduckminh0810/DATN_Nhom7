package com.nhom7.quiz.quizapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.dto.UserDTO;
import com.nhom7.quiz.quizapp.service.AdminService.adminservice;
import com.nhom7.quiz.quizapp.service.userService.LoginService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
        @Autowired
        private LoginService loginService;

        // Tạo đăng nhập cho admin
        @PostMapping("/login")
        public ResponseEntity<?> adminLogin(@RequestBody String username, @RequestBody String password) {
                // Phương thức để xác thực người dùng
                LoginService.LoginResultForAdmin result = loginService.authenticateAdmin(username, password);
                return switch (result.status()) {
                        case SUCCESS -> ResponseEntity.ok(Map.of(
                                        "status", "SUCCESS",
                                        "message", "Đăng nhập thành công",
                                        "user", result.user()));
                        case USER_NOT_FOUND -> ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Map.of(
                                                        "status", "USER_NOT_FOUND",
                                                        "message", "Người dùng không tồn tại"));
                        case WRONG_PASSWORD -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of(
                                                        "status", "WRONG_PASSWORD",
                                                        "message", "Mật khẩu không đúng"));
                        case NOT_ADMIN -> ResponseEntity
                                        .status(HttpStatus.FORBIDDEN)
                                        .body(Map.of(
                                                        "status", "NOT_ADMIN",
                                                        "message", "Không phải người dùng quản trị"));
                };
        }

        @Autowired
        private adminservice adminService;

        @GetMapping("/all-users")
        public ResponseEntity<Page<UserDTO>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String role

        ) {
                return ResponseEntity.ok(adminService.getAllUsers(page, size, search, role));
        }

}
