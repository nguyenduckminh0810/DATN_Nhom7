package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;

    // Kiểm tra quyền admin
    private void checkAdminPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Chỉ admin mới có quyền thực hiện thao tác này");
        }
    }

    public List<Category> getAll() {
        // ✅ CHỈ LẤY CATEGORIES CHƯA BỊ XÓA
        return categoryRepo.findAllActive();
    }

    public Category create(Category category) {
        checkAdminPermission();
        category.setCreatedAt(LocalDateTime.now());
        category.setDeleted(false); // ✅ ĐẢM BẢO KHÔNG BỊ XÓA
        return categoryRepo.save(category);
    }

    public Category update(Long id, Category category) {
        checkAdminPermission();
        Category existingCategory = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        // ✅ CHỈ CẬP NHẬT NẾU CHƯA BỊ XÓA
        if (existingCategory.getDeleted() != null && existingCategory.getDeleted()) {
            throw new RuntimeException("Không thể cập nhật danh mục đã bị xóa");
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return categoryRepo.save(existingCategory);
    }

    // ✅ SOFT DELETE VỚI KIỂM TRA QUIZ
    public String delete(Long id) {
        checkAdminPermission();
        
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        // ✅ KIỂM TRA XEM CÓ QUIZ NÀO KHÔNG
        Long quizCount = categoryRepo.countActiveQuizzesByCategory(id);
        if (quizCount > 0) {
            return "Không thể xóa danh mục vì có " + quizCount + " quiz đang sử dụng. Vui lòng di chuyển quiz sang danh mục khác trước khi xóa.";
        }
        
        // ✅ SOFT DELETE
        category.setDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        
        // ✅ LẤY USER HIỆN TẠI
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Có thể cần implement để lấy User entity từ authentication
            // category.setDeletedBy(currentUser);
        }
        
        categoryRepo.save(category);
        return "Đã xóa danh mục thành công (soft delete)";
    }

    // ✅ HARD DELETE (CHỈ DÀNH CHO ADMIN)
    public String hardDelete(Long id) {
        checkAdminPermission();
        
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        // ✅ KIỂM TRA QUIZ
        Long quizCount = categoryRepo.countActiveQuizzesByCategory(id);
        if (quizCount > 0) {
            return "Không thể xóa hoàn toàn danh mục vì có " + quizCount + " quiz đang sử dụng.";
        }
        
        categoryRepo.deleteById(id);
        return "Đã xóa hoàn toàn danh mục";
    }

    // ✅ RESTORE CATEGORY
    public String restore(Long id) {
        checkAdminPermission();
        
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        category.setDeleted(false);
        category.setDeletedAt(null);
        category.setDeletedBy(null);
        
        categoryRepo.save(category);
        return "Đã khôi phục danh mục thành công";
    }

    // ✅ LẤY DANH SÁCH CATEGORIES ĐÃ XÓA
    public List<Category> getDeletedCategories() {
        checkAdminPermission();
        return categoryRepo.findAll().stream()
                .filter(category -> category.getDeleted() != null && category.getDeleted())
                .toList();
    }
}
