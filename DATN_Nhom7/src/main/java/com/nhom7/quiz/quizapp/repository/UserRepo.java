package com.nhom7.quiz.quizapp.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.nhom7.quiz.quizapp.model.User;

public interface UserRepo extends JpaRepository<User, Long> {
<<<<<<< HEAD
        // Trả về người dùng theo tên đăng nhập hoặc email
        // Nếu không tìm thấy thì trả về Optional.empty()
        Optional<User> findByRole(String role);
=======
    // Trả về người dùng theo tên đăng nhập hoặc email
    // Nếu không tìm thấy thì trả về Optional.empty()
    Page<User> findByRole(String role, Pageable pageable);
>>>>>>> 49598303905d5152a96eeba8beaad1a5b8511c4c

        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

<<<<<<< HEAD
        boolean existsByUsername(String username);

        boolean existsByEmail(String email);
        // ✅ 1. Quản lý người dùng
        // Xem danh sách người dùng

        // Tìm kiếm, lọc người dùng (theo tên, email, trạng thái,…)

        // Khóa/Mở khóa tài khoản

        // Xóa người dùng vi phạm

        // Gán/quản lý vai trò người dùng (USER / ADMIN)

        @Query("SELECT u FROM User u " +
                        "WHERE (:name IS NULL OR u.fullName LIKE %:name%) " + // ✅ fullName
                        "AND (:email IS NULL OR u.email LIKE %:email%) " +
                        "AND (:status IS NULL OR u.role = :status)") // ✅ dùng role nếu chưa có trường status
        List<User> searchUsers(@Param("name") String name,
                        @Param("email") String email,
                        @Param("status") String status);

=======
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
>>>>>>> 49598303905d5152a96eeba8beaad1a5b8511c4c
}
