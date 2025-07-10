package com.nhom7.quiz.quizapp.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.UserDTO;
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

}
