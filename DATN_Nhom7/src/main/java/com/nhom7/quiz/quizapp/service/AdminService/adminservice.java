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
import com.nhom7.quiz.quizapp.model.dto.QuizDTO;
import com.nhom7.quiz.quizapp.model.dto.UserDTO;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class adminservice {
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
            userPage = userRepo.findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(role,
                    search, role, search, pageable);
        } else if (hasSearch) {
            // Chỉ tìm kiếm
            userPage = userRepo.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
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

    @Autowired
    private QuizRepo quizRepo;

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
                quiz.getCategory() != null ? quiz.getCategory().getName() : null);
    }

}
