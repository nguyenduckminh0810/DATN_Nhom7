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

    // ✅ PUBLIC - Không cần annotation
    @GetMapping
    public List<Category> getAll() {
        System.out.println("📋 Getting all categories (public access)");
        return categoryService.getAll();
    }

    // ✅ ADMIN ONLY
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        System.out.println("➕ Creating category (ADMIN only)");
        return ResponseEntity.ok(categoryService.create(category));
    }

    // ✅ ADMIN ONLY
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category) {
        System.out.println("✏️ Updating category " + id + " (ADMIN only)");
        return ResponseEntity.ok(categoryService.update(id, category));
    }

    // ✅ ADMIN ONLY
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        System.out.println("🗑️ Deleting category " + id + " (ADMIN only)");
        categoryService.delete(id);
        return ResponseEntity.ok().build();
    }
}
