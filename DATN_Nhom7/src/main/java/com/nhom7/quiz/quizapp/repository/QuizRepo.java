package com.nhom7.quiz.quizapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom7.quiz.quizapp.model.Quiz;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Long> {
      boolean existsByIdAndUser_Username(Long id, String username);

      // ✅ CÁC METHOD HIỆN TẠI - CHỈ LẤY QUIZ CHƯA BỊ XÓA
      @Query("SELECT q FROM Quiz q WHERE q.user.id = :userId AND (q.deleted IS NULL OR q.deleted = false)")
      Page<Quiz> findByUserIdAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);

      // ✅ SỬA: Query để xử lý đúng trường hợp deleted = null
      @Query("SELECT q FROM Quiz q WHERE q.isPublic = :isPublic AND (q.deleted IS NULL OR q.deleted = false)")
      Page<Quiz> findByIsPublicAndDeletedFalse(@Param("isPublic") Boolean isPublic, Pageable pageable);

      long countByIsPublicFalseAndDeletedFalse();

      List<Quiz> findByIsPublicFalseAndDeletedFalseOrderByCreatedAtDesc();

      // ✅ THÊM CÁC METHOD CHO DASHBOARD
      @Query("SELECT COUNT(q) FROM Quiz q WHERE q.isPublic = true AND (q.deleted IS NULL OR q.deleted = false)")
      long countByIsPublicTrueAndDeletedFalse();

      // ✅ THÊM CÁC METHOD CHO DEBUG
      long countByUserId(Long userId);

      long countByUserIdAndDeletedFalse(Long userId);

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
                        AND (q.deleted IS NULL OR q.deleted = false)
                  """)
      Page<Quiz> searchQuizzes(
                  @Param("keyword") String keyword,
                  @Param("tagId") Long tagId,
                  @Param("isPublic") Boolean isPublic,
                  Pageable pageable);

      // ✅ THÊM CÁC METHOD CHO SOFT DELETE
      // Lấy tất cả quiz (kể cả đã xóa) - cho admin
      Page<Quiz> findByUserId(Long userId, Pageable pageable);

      // Lấy quiz đã bị xóa mềm
      Page<Quiz> findByUserIdAndDeletedTrue(Long userId, Pageable pageable);

      // Kiểm tra quiz có tồn tại và chưa bị xóa
      boolean existsByIdAndDeletedFalse(Long id);

      // Lấy quiz theo ID (chưa bị xóa)
      Quiz findByIdAndDeletedFalse(Long id);

      // Restore quiz (đặt lại deleted = false)
      @Modifying
      @Query("UPDATE Quiz q SET q.deleted = false, q.deletedAt = null, q.deletedBy = null WHERE q.id = :id")
      void restoreQuiz(@Param("id") Long id);

      // ✅ THÊM METHOD TÌM QUIZ THEO CODE
      @Query("SELECT q FROM Quiz q WHERE q.quizCode = :quizCode AND (q.deleted IS NULL OR q.deleted = false)")
      Optional<Quiz> findByQuizCode(@Param("quizCode") String quizCode);

      // ✅ THÊM METHOD KIỂM TRA CODE ĐÃ TỒN TẠI
      boolean existsByQuizCode(String quizCode);

      // Tìm kiếm quiz theo trạng thái public và theo category
      @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.category.id = :categoryId AND (q.deleted IS NULL OR q.deleted = false)")
      Page<Quiz> findPublicByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
}
