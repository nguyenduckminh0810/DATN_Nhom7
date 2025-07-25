package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;

    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    public Category create(Category category) {
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepo.save(category);
    }

    public Category update(Long id, Category category) {
        Category existing = categoryRepo.findById(id).orElseThrow();
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepo.save(existing);
    }

    public void delete(Long id) {
        categoryRepo.deleteById(id);
    }
}
