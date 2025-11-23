package com.nhom7.quiz.quizapp.repository;

import com.nhom7.quiz.quizapp.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // SOFT DELETE METHODS
    @Query("SELECT c FROM Category c WHERE c.deleted = false OR c.deleted IS NULL")
    List<Category> findAllActive();

    @Query("SELECT c FROM Category c WHERE (c.deleted = false OR c.deleted IS NULL) AND c.name LIKE %:name%")
    Page<Category> findActiveByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category.id = :categoryId AND (q.deleted = false OR q.deleted IS NULL)")
    Long countActiveQuizzesByCategory(@Param("categoryId") Long categoryId);
}
