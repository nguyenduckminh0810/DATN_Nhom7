package com.nhom7.quiz.quizapp.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.User;

public interface UserRepo extends JpaRepository<User, Long> {
        long countByRole(String role);

        // Trả về người dùng theo tên đăng nhập hoặc email
        // Nếu không tìm thấy thì trả về Optional.empty()
        Page<User> findByRole(String role, Pageable pageable);

        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

        // Tìm kiếm người dùng theo tên, vai trò hoặc email chứa từ khóa,
        // không phân
        // biệt hoa thường, có phân trang (dạng search)
        Page<User> findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        String role1, String fullName,
                        String role2, String email,
                        Pageable pageable);

        // Tìm kiếm người dùng theo tên đăng nhập hoặc email chứa từ khóa, không phân
        // biệt hoa thường, có phân trang
        Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        String fullName, String email, Pageable pageable);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);
}
