package com.nhom7.quiz.quizapp.service.AdminService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.dto.QuizDTO;
import com.nhom7.quiz.quizapp.model.dto.UserDTO;
import com.nhom7.quiz.quizapp.model.dto.ResultDTO;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import com.nhom7.quiz.quizapp.repository.ResultRepo;

@Service
public class adminservice {


    @Autowired
    private ResultRepo resultRepo;

    // Lấy danh sách tất cả quiz (kể cả riêng tư), lọc theo tags, có phân trang
    public Page<QuizDTO> searchAndFilterQuizzes(String keyword, Long tagId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Quiz> quizzes = quizRepo.findAll(pageable);

        String keywordLower = keyword != null ? keyword.toLowerCase() : "";

        List<QuizDTO> filtered = quizzes.getContent().stream()
                .filter(quiz -> {
                    // --- Điều kiện tìm kiếm ---
                    boolean matchTitle = quiz.getTitle() != null
                            && quiz.getTitle().toLowerCase().contains(keywordLower);

                    boolean matchCategory = quiz.getCategory() != null &&
                            quiz.getCategory().getName().toLowerCase().contains(keywordLower);

                    boolean matchTagName = quiz.getQuizTags().stream()
                            .anyMatch(qt -> qt.getTag().getName().toLowerCase().contains(keywordLower));

                    boolean keywordMatch = keyword == null || keyword.isBlank() || matchTitle || matchCategory
                            || matchTagName;

                    // --- Điều kiện lọc tagId ---
                    boolean tagMatch = tagId == null || quiz.getQuizTags().stream()
                            .anyMatch(qt -> qt.getTag().getId().equals(tagId));

                    return keywordMatch && tagMatch;
                })
                .map(this::convertToDTO)
                .toList();

        return new PageImpl<>(filtered, pageable, quizzes.getTotalElements());
    }

    // Lấy danh sách tất cả quiz attempts (lịch sử làm quiz) cho admin
    public Page<ResultDTO> getAllQuizAttempts(int page, int size, Long userId, Long quizId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("completedAt").descending());

        Page<Result> resultPage;

        if (userId != null && quizId != null) {
            // Lọc theo cả user và quiz - cần tạo method này trong ResultRepo
            List<Result> results = resultRepo.findAll().stream()
                    .filter(result -> result.getUser().getId().equals(userId) && result.getQuiz().getId().equals(quizId))
                    .toList();
            resultPage = new PageImpl<>(results, pageable, results.size());
        } else if (userId != null) {
            // Chỉ lọc theo user
            List<Result> results = resultRepo.findByUser_Id(userId);
            resultPage = new PageImpl<>(results, pageable, results.size());
        } else if (quizId != null) {
            // Chỉ lọc theo quiz - cần tạo method này trong ResultRepo
            List<Result> results = resultRepo.findAll().stream()
                    .filter(result -> result.getQuiz().getId().equals(quizId))
                    .toList();
            resultPage = new PageImpl<>(results, pageable, results.size());
        } else {
            // Lấy tất cả
            resultPage = resultRepo.findAll(pageable);
        }

        return resultPage.map(result -> new ResultDTO(result));
    }



        @Autowired
        private UserRepo userRepo;

        // Phương thức để lấy danh sách tất cả người dùng
        public Page<UserDTO> getAllUsers(int page, int size, String search, String role) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<User> userPage;

                boolean hasSearch = search != null && !search.isBlank();
                boolean hasRole = role != null && !role.isBlank();

                if (hasSearch && hasRole) {
                        // Tìm kiếm người dùng theo vai trò và tên đăng nhập hoặc email
                        userPage = userRepo.findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                                        role,
                                        search, role, search, pageable);
                } else if (hasSearch) {
                        // Chỉ tìm kiếm
                        userPage = userRepo.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search,
                                        search, pageable);
                } else if (hasRole) {
                        // Chỉ lọc theo vai trò
                        userPage = userRepo.findByRole(role, pageable);
                } else {
                        // Không lọc
                        userPage = userRepo.findAll(pageable);
                }
                return userPage.map(user -> new UserDTO(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole(),
                                user.getCreatedAt()));
        }

        // Phương thức dùng để sửa thông tin người dùng
        public UserDTO updateUser(Long id, UserDTO dto) {
                User user = userRepo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

                user.setFullName(dto.getFullName());
                user.setEmail(dto.getEmail());
                user.setUsername(dto.getUsername());
                user.setRole(dto.getRole());

                userRepo.save(user);

                return new UserDTO(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole(),
                                user.getCreatedAt());
        }

        // Phương thức dùng để xóa người dùng
        public void deleteUser(Long id) {
                if (!userRepo.existsById(id)) {
                        throw new RuntimeException("Người dùng không tồn tại");
                }
                userRepo.deleteById(id);
        }

        @Autowired
        private QuizRepo quizRepo;

        // Lấy danh sách tất cả quiz (kể cả riêng tư), lọc theo tags, có phân trang

        public Page<QuizDTO> searchAndFilterQuizzes(String keyword, Long tagId, Boolean isPublic, int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<Quiz> quizzes = quizRepo.searchQuizzes(
                                keyword != null && !keyword.isBlank() ? keyword : null,
                                tagId,
                                isPublic,
                                pageable);

                List<QuizDTO> dtoList = quizzes.getContent().stream()
                                .map(this::convertToDTO)
                                .toList();

                return new PageImpl<>(dtoList, pageable, quizzes.getTotalElements());
        }

        // Hàm chuyển đổi Quiz sang QuizDTO
        private QuizDTO convertToDTO(Quiz quiz) {
                List<String> tagNames = quiz.getQuizTags().stream()
                                .map(qt -> qt.getTag().getName())
                                .toList();

                return new QuizDTO(
                                quiz.getId(),
                                quiz.getTitle(),
                                quiz.isPublic(),
                                quiz.getCreatedAt(),
                                tagNames,
                                quiz.getCategory() != null ? quiz.getCategory().getName() : null,
                                quiz.getCategory() != null ? quiz.getCategory().getId() : null,
                                quiz.getUser() != null ? quiz.getUser().getFullName() : "Không rõ");
        }


}
