package com.nhom7.quiz.quizapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.dto.LoginRequest;
import com.nhom7.quiz.quizapp.model.dto.QuizDTO;
import com.nhom7.quiz.quizapp.model.dto.UserDTO;
import com.nhom7.quiz.quizapp.service.AdminService.adminservice;
import com.nhom7.quiz.quizapp.service.userService.LoginService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
        @Autowired
        private LoginService loginService;

        // Tạo đăng nhập cho admin
        @PostMapping("/login")
        public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
                // Phương thức để xác thực người dùng
                LoginService.LoginResultForAdmin result = loginService.authenticateAdmin(loginRequest.getUsername(),
                                loginRequest.getPassword());
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

        // Lấy danh sách người dùng
        @GetMapping("/all-users")
        public ResponseEntity<Page<UserDTO>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String role

        ) {
                return ResponseEntity.ok(adminService.getAllUsers(page, size, search, role));
        }

        // Cập nhật user
        @PutMapping("/users/{id}")
        public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
                UserDTO updated = adminService.updateUser(id, dto);
                return ResponseEntity.ok(updated);
        }

        // Xoá user
        @DeleteMapping("/users/{id}")
        public ResponseEntity<?> deleteUser(@PathVariable Long id) {
                adminService.deleteUser(id);
                return ResponseEntity.ok("Đã xoá người dùng thành công");
        }

        // Lấy danh sách quiz
        @GetMapping("/all-quizzes/filter")
        public ResponseEntity<Page<QuizDTO>> searchAndFilter(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long tagId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<QuizDTO> result = adminService.searchAndFilterQuizzes(keyword, tagId, page, size);
                return ResponseEntity.ok(result);
        }
}
