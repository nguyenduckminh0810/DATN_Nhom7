package com.nhom7.quiz.quizapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.Result;

public interface ResultRepo extends JpaRepository<Result, Long> {
    // Các phương thức truy vấn tùy chỉnh nếu cần
    List<Result> findAllByUser_Id(Long userId);

    List<Result> findByUser_Id(Long userId);
    
    // Thêm method để xóa results theo quiz_id
    List<Result> findByQuiz_Id(Long quizId);
    
    void deleteByQuiz_Id(Long quizId);
}
