package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.QuizAttempt;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptDTO;
import com.nhom7.quiz.quizapp.repository.QuizAttemptRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {
    
    @Autowired
    private QuizAttemptRepo quizAttemptRepo;
    
    /**
     * Tìm lịch sử làm quiz với phân quyền
     * @param userId - null nếu muốn xem tất cả (chỉ admin)
     * @param quizId - null nếu không lọc theo quiz
     * @param page - trang hiện tại (bắt đầu từ 0)
     * @param size - số lượng item trên mỗi trang
     * @return Page<QuizAttemptDTO>
     */
    public Page<QuizAttemptDTO> findQuizAttempts(Long userId, Long quizId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttempt> attempts;
        
        if (userId != null && quizId != null) {
            // Lọc theo cả user và quiz
            attempts = quizAttemptRepo.findByUserIdAndQuizId(userId, quizId, pageable);
        } else if (userId != null) {
            // Chỉ lọc theo user
            attempts = quizAttemptRepo.findByUserIdOrderByAttemptedAtDesc(userId, pageable);
        } else if (quizId != null) {
            // Chỉ lọc theo quiz
            attempts = quizAttemptRepo.findByQuizId(quizId, pageable);
        } else {
            // Xem tất cả (chỉ admin)
            attempts = quizAttemptRepo.findAllOrderByAttemptedAtDesc(pageable);
        }
        
        return attempts.map(this::convertToDTO);
    }
    
    /**
     * Chuyển đổi QuizAttempt thành QuizAttemptDTO
     */
    private QuizAttemptDTO convertToDTO(QuizAttempt attempt) {
        return new QuizAttemptDTO(
            attempt.getId(),
            attempt.getUser().getUsername(),
            attempt.getQuiz().getTitle(),
            attempt.getScore(),
            attempt.getAttemptedAt(),
            attempt.getTimeTaken()
        );
    }
    
    /**
     * Lưu quiz attempt mới
     */
    public QuizAttempt saveQuizAttempt(QuizAttempt quizAttempt) {
        return quizAttemptRepo.save(quizAttempt);
    }
    
    /**
     * Tìm quiz attempt theo ID
     */
    public QuizAttempt findById(Long id) {
        return quizAttemptRepo.findById(id).orElse(null);
    }
} 