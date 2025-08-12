package com.nhom7.quiz.quizapp.controller;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    // === API PUBLIC ===
    // Lấy danh sách danh mục chưa bị xoá (public)
    @GetMapping
    public List<Category> getAll() {
        System.out.println("📋 Getting all categories (public access)");
        return categoryService.getAll();
    }

    // === API ADMIN ===
    // Tạo danh mục mới (admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        System.out.println("➕ Creating category (ADMIN only)");
        return ResponseEntity.ok(categoryService.create(category));
    }

    // Cập nhật danh mục (admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category) {
        System.out.println("✏️ Updating category " + id + " (ADMIN only)");
        return ResponseEntity.ok(categoryService.update(id, category));
    }

    // Xoá mềm danh mục (admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        System.out.println("🗑️ Deleting category " + id + " (ADMIN only)");
        categoryService.delete(id);
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách danh mục đã xoá (admin)
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Category> getDeletedCategories() {
        System.out.println("📋 Getting deleted categories (ADMIN only)");
        return categoryService.getDeletedCategories();
    }

    // Phục hồi danh mục đã xoá (admin)
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restore(@PathVariable Long id) {
        System.out.println("🔄 Restoring category " + id + " (ADMIN only)");
        String result = categoryService.restore(id);
        return ResponseEntity.ok(result);
    }
}
