package com.nhom7.quiz.quizapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Tag;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.LoginRequest;
import com.nhom7.quiz.quizapp.model.dto.QuizDTO;
import com.nhom7.quiz.quizapp.model.dto.TagDTO;
import com.nhom7.quiz.quizapp.model.dto.UserDTO;
import com.nhom7.quiz.quizapp.model.dto.ResultDTO;
import com.nhom7.quiz.quizapp.model.dto.AttemptsByHourDTO;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.TagRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import com.nhom7.quiz.quizapp.service.AdminService.adminservice;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.config.JwtUtil;
import com.nhom7.quiz.quizapp.service.CategoryService;
import com.nhom7.quiz.quizapp.service.NotificationService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/admin")

public class AdminController {
        @Autowired
        private CategoryRepo categoryRepo;
        @Autowired
        private LoginService loginService;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private NotificationService notificationService;

        // ADMIN LOGIN - PUBLIC (không cần PreAuthorize)
        @PostMapping("/login")
        public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
                // Phương thức để xác thực người dùng
                LoginService.LoginResultForAdmin result = loginService.authenticateAdmin(loginRequest.getUsername(),
                                loginRequest.getPassword());
                return switch (result.status()) {
                        case SUCCESS -> {
                                String token = jwtUtil.generateToken(result.user().getUsername(),
                                                result.user().getRole());
                                yield ResponseEntity.ok(Map.of(
                                                "status", "SUCCESS",
                                                "message", "Đăng nhập thành công",
                                                "token", token,
                                                "user", result.user()));
                        }
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

        @Autowired
        private com.nhom7.quiz.quizapp.service.AdminService.DashboardService dashboardService;

        // ADMIN ONLY - Lấy danh sách người dùng
        @GetMapping("/all-users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<UserDTO>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String role

        ) {
                return ResponseEntity.ok(adminService.getAllUsers(page, size, search, role));
        }

        // ADMIN ONLY - Cập nhật user
        @PutMapping("/users/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateUser(@PathVariable Long id,
                        @RequestBody UserDTO dto,
                        Authentication authentication) {
                String currentUsername = authentication.getName();
                User currentUser = userRepo.findByUsername(currentUsername)
                                .orElseThrow(() -> new UsernameNotFoundException("Not found"));

                // Chặn tự ĐỔI ROLE của chính mình
                if (currentUser.getId().equals(id)
                                && dto.getRole() != null
                                && !dto.getRole().equalsIgnoreCase(currentUser.getRole())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("status", "FORBIDDEN", "message",
                                                        "Bạn không thể đổi vai trò của chính mình"));
                }

                // Không đặt check “tự xoá” ở đây (để ở DELETE)
                UserDTO updated = adminService.updateUser(id, dto);
                return ResponseEntity.ok(updated);
        }

        // ADMIN ONLY - Thêm user
        @Autowired
        private PasswordEncoder passwordEncoder;

        @PostMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> createUser(@RequestBody User user) {
                if (userRepo.existsByEmail(user.getEmail()) || userRepo.existsByUsername(user.getUsername())) {
                        return ResponseEntity.badRequest().body("Email hoặc Username đã tồn tại.");
                }

                // Hash password nếu cần
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setCreatedAt(LocalDateTime.now());
                User saved = userRepo.save(user);
                return ResponseEntity.ok(saved);
        }

        // ADMIN ONLY - Xoá user
        @DeleteMapping("/users/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> deleteUser(@PathVariable Long id) {
                adminService.deleteUser(id);
                return ResponseEntity.ok("Đã xoá người dùng thành công");
        }

        // Ban user vi phạm
        @Autowired
        private UserRepo userRepo;

        @PutMapping("/users/{id}/ban")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> banUser(@PathVariable Long id, Authentication authentication) {
                User user = userRepo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                String currentUsername = authentication.getName();
                User currentUser = userRepo.findByUsername(currentUsername)
                                .orElseThrow(() -> new UsernameNotFoundException("Not found"));

                if (currentUser.getId().equals(id)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("status", "FORBIDDEN", "message",
                                                        "Bạn không thể tự ban tài khoản của mình"));
                }
                user.setBanned(true);
                user.setRole("BANNED");
                userRepo.save(user);

                // GỬI NOTIFICATION CHO USER
                try {
                        notificationService.sendAccountStatusNotification(user.getId(), true);
                        System.out.println("Sent ban notification to user: " + user.getUsername());
                } catch (Exception e) {
                        System.err.println("Error sending ban notification: " + e.getMessage());
                }

                return ResponseEntity.ok("Người dùng đã bị ban.");
        }

        // ADMIN ONLY - Test ban user
        @PostMapping("/test-ban/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<String> testBan(@PathVariable Long id) {
                adminService.checkAndBanUser(id);
                return ResponseEntity.ok("Đã kiểm tra và xử lý ban nếu đủ report");
        }

        // Attempts today by hour for dashboard chart
        @GetMapping("/stats/attempts-today")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<AttemptsByHourDTO> getAttemptsTodayByHour(
                        @RequestParam(required = false, name = "tz") String timezone) {
                AttemptsByHourDTO dto = dashboardService.getAttemptsTodayByHour(timezone);
                return ResponseEntity.ok(dto);
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

        // Tạo danh mục mới
        @PostMapping("/categories")
        public ResponseEntity<?> createCategory(@RequestBody Category category) {
                try {
                        category.setCreatedAt(LocalDateTime.now());
                        Category savedCategory = categoryRepo.save(category);
                        return ResponseEntity.ok(savedCategory);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi tạo danh mục: " + e.getMessage());
                }
        }

        // Cập nhật danh mục
        @PutMapping("/categories/{id}")
        public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
                if (!categoryRepo.existsById(id)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Danh mục không tồn tại.");
                }

                try {
                        Category existingCategory = categoryRepo.findById(id).orElse(null);
                        if (existingCategory != null) {
                                existingCategory.setName(category.getName());
                                existingCategory.setDescription(category.getDescription());
                                Category updatedCategory = categoryRepo.save(existingCategory);
                                return ResponseEntity.ok(updatedCategory);
                        }
                        return ResponseEntity.notFound().build();
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi cập nhật danh mục: " + e.getMessage());
                }
        }

        @Autowired
        private CategoryService categoryService;

        // Xoá mềm danh mục
        @DeleteMapping("/categories/{id}")
        public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
                try {
                        String result = categoryService.delete(id);
                        return ResponseEntity.ok(result);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi xóa danh mục: " + e.getMessage());
                }
        }

        // Xoá hoàn toàn danh mục(cứng)
        @DeleteMapping("/categories/{id}/hard")
        public ResponseEntity<?> hardDeleteCategory(@PathVariable Long id) {
                try {
                        String result = categoryService.hardDelete(id);
                        return ResponseEntity.ok(result);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi xóa hoàn toàn danh mục: " + e.getMessage());
                }
        }

        // RESTORE CATEGORY
        @PutMapping("/categories/{id}/restore")
        public ResponseEntity<?> restoreCategory(@PathVariable Long id) {
                try {
                        String result = categoryService.restore(id);
                        return ResponseEntity.ok(result);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi khôi phục danh mục: " + e.getMessage());
                }
        }

        // GET DELETED CATEGORIES
        @GetMapping("/categories/deleted")
        public ResponseEntity<?> getDeletedCategories() {
                try {
                        List<Category> deletedCategories = categoryService.getDeletedCategories();
                        return ResponseEntity.ok(deletedCategories);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Lỗi khi lấy danh sách danh mục đã xóa: " + e.getMessage());
                }
        }

        @Autowired
        private TagRepo tagRepo;

        // Lấy danh sách tất cả các tag
        @GetMapping("/tags")
        public ResponseEntity<List<TagDTO>> getAllTags() {
                List<Tag> tags = tagRepo.findAll();
                List<TagDTO> tagDTOs = tags.stream()
                                .map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.getDescription()))
                                .toList();
                return ResponseEntity.ok(tagDTOs);
        }

        @Autowired
        private com.nhom7.quiz.quizapp.service.AdminService.AnalyticsService analyticsService;
        @Autowired
        private com.nhom7.quiz.quizapp.service.AdminService.AnalyticsExportService analyticsExportService;

        @GetMapping("/analytics/stats/attempts-series")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getAttemptsSeries(@RequestParam String from, @RequestParam String to,
                        @RequestParam(required = false) String tz) {
                return ResponseEntity.ok(analyticsService.attemptsSeries(from, to, tz));
        }

        @GetMapping("/analytics/stats/users-series")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getUsersSeries(@RequestParam String from, @RequestParam String to,
                        @RequestParam(required = false) String tz) {
                return ResponseEntity.ok(analyticsService.usersSeries(from, to, tz));
        }

        @GetMapping("/analytics/stats/quality-series")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getQualitySeries(@RequestParam String from, @RequestParam String to,
                        @RequestParam(required = false) String tz) {
                return ResponseEntity.ok(analyticsService.qualitySeries(from, to, tz));
        }

        @GetMapping("/analytics/stats/score-histogram")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getScoreHistogram(@RequestParam String from, @RequestParam String to,
                        @RequestParam(defaultValue = "20") int bins) {
                return ResponseEntity.ok(analyticsService.scoreHistogram(from, to, bins));
        }

        @GetMapping("/analytics/stats/completion")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getCompletion(@RequestParam String from, @RequestParam String to) {
                return ResponseEntity.ok(analyticsService.completion(from, to));
        }

        @GetMapping("/analytics/stats/category-distribution")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getCategoryDistribution(@RequestParam String from, @RequestParam String to,
                        @RequestParam(defaultValue = "10") int limit) {
                return ResponseEntity.ok(analyticsService.categoryDistribution(from, to, limit));
        }

        @GetMapping("/analytics/stats/heatmap")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getHeatmap(@RequestParam String from, @RequestParam String to,
                        @RequestParam(required = false) String tz) {
                return ResponseEntity.ok(analyticsService.heatmap(from, to, tz));
        }

        @GetMapping("/analytics/top-quizzes")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getTopQuizzes(@RequestParam String from, @RequestParam String to,
                        @RequestParam(defaultValue = "10") int limit) {
                return ResponseEntity.ok(analyticsService.topQuizzes(from, to, limit));
        }

        @GetMapping("/analytics/top-performers")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getTopPerformers(@RequestParam String from, @RequestParam String to,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(defaultValue = "5") int minAttempts) {
                return ResponseEntity.ok(analyticsService.topPerformers(from, to, limit, minAttempts));
        }

        // Export Excel tổng hợp analytics
        @GetMapping("/analytics/export/xlsx")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<byte[]> exportAnalyticsXlsx(
                        @RequestParam String from,
                        @RequestParam String to,
                        @RequestParam(required = false) String tz,
                        @RequestParam(defaultValue = "20") int bins,
                        @RequestParam(defaultValue = "10") int topLimit,
                        @RequestParam(defaultValue = "5") int minAttempts) {
                byte[] bytes = analyticsExportService.exportAnalyticsXlsx(from, to, tz, bins, topLimit, minAttempts);
                return ResponseEntity.ok()
                                .header("Content-Disposition",
                                                "attachment; filename=analytics-" + from + "_" + to + ".xlsx")
                                .header("Content-Type",
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .body(bytes);
        }

}
