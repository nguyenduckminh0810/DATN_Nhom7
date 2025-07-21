package com.nhom7.quiz.quizapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.dto.LoginRequest;
import com.nhom7.quiz.quizapp.model.dto.QuizDTO;
import com.nhom7.quiz.quizapp.model.dto.UserDTO;
import com.nhom7.quiz.quizapp.model.dto.ResultDTO;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
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
        private CategoryRepo categoryRepo;
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
        public ResponseEntity<Page<QuizDTO>> searchQuizzes(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long tagId,
                        @RequestParam(required = false) Boolean isPublic,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<QuizDTO> results = adminService.searchAndFilterQuizzes(keyword, tagId, isPublic, page, size);
                return ResponseEntity.ok(results);
        }

        // API lấy tất cả quiz attempts (lịch sử làm quiz) cho admin
        @GetMapping("/all-attempts")
        public ResponseEntity<Page<ResultDTO>> getAllQuizAttempts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) Long quizId) {
                Page<ResultDTO> result = adminService.getAllQuizAttempts(page, size, userId, quizId);
                return ResponseEntity.ok(result);
        }

        @Autowired
        private QuizRepo quizRepo;

        @PutMapping("/quizzes/{id}")
        public ResponseEntity<?> updateQuiz(@PathVariable Long id, @RequestBody QuizDTO quizDTO) {
                Optional<Quiz> optionalQuiz = quizRepo.findById(id);
                if (optionalQuiz.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }

                Quiz quiz = optionalQuiz.get();
                quiz.setTitle(quizDTO.getTitle());
                quiz.setPublic(quizDTO.isPublic());
                Category category = categoryRepo.findById(quizDTO.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
                quiz.setCategory(category);

                quizRepo.save(quiz);

                return ResponseEntity.ok().build();
        }

        // Phương thúc dùng để tạo quiz mới
        @PostMapping("/quizzes")
        public ResponseEntity<?> createQuiz(@RequestBody QuizDTO dto) {
                try {
                        // Tìm thể loại
                        Category category = categoryRepo.findById(dto.getCategoryId())
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
                        // Tạo quiz mới
                        Quiz quiz = new Quiz();
                        quiz.setTitle(dto.getTitle());
                        quiz.setPublic(dto.isPublic());
                        quiz.setCategory(category);
                        quiz.setCreatedAt(LocalDateTime.now());

                        quizRepo.save(quiz);

                        return ResponseEntity.ok("Quiz đã được tạo thành công.");
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi tạo quiz: " + e.getMessage());
                }
        }

        // Phương thức dùng để xóa quiz
        @DeleteMapping("/quizzes/{id}")
        public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
                if (!quizRepo.existsById(id)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz không tồn tại.");
                }

                try {
                        quizRepo.deleteById(id);
                        return ResponseEntity.ok("Đã xoá quiz thành công.");
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi xoá quiz: " + e.getMessage());
                }
        }

        // Lấy danh sách tất cả danh mục
        @GetMapping("/categories")
        public List<Category> getAllCategories() {
                return categoryRepo.findAll();
        }

}
