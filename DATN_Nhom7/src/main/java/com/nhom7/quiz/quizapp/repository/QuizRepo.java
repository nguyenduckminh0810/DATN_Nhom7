package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom7.quiz.quizapp.model.Quiz;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByUserId(Long userId, Pageable pageable);

    Page<Quiz> findByIsPublic(boolean isPublic, Pageable pageable);

    @Query("""
                SELECT DISTINCT q FROM Quiz q
                LEFT JOIN q.category c
                LEFT JOIN q.quizTags qt
                LEFT JOIN qt.tag t
                WHERE (:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:tagId IS NULL OR t.id = :tagId)
                  AND (:isPublic IS NULL OR q.isPublic = :isPublic)
            """)
    Page<Quiz> searchQuizzes(
            @Param("keyword") String keyword,
            @Param("tagId") Long tagId,
            @Param("isPublic") Boolean isPublic,
            Pageable pageable);

}
