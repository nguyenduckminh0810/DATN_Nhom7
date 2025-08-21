package com.nhom7.quiz.quizapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom7.quiz.quizapp.model.*;
import com.nhom7.quiz.quizapp.model.dto.QuizResumeDTO;
import com.nhom7.quiz.quizapp.repository.QuizAttemptProgressRepo;
import com.nhom7.quiz.quizapp.repository.QuizAttemptRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizResumeService {
    
    @Autowired
    private QuizAttemptRepo quizAttemptRepo;
    
    @Autowired
    private QuizAttemptProgressRepo progressRepo;
    
    @Autowired
    private QuizRepo quizRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * ✅ Kiểm tra xem user có attempt dở nào cho quiz không
     */
    public Optional<QuizResumeDTO> checkInProgressAttempt(Long userId, Long quizId) {
        List<QuizAttempt> inProgressAttempts = quizAttemptRepo
            .findInProgressAttemptsByUserAndQuiz(userId, quizId);
        
        if (inProgressAttempts.isEmpty()) {
            return Optional.empty();
        }
        
        // Lấy attempt mới nhất
        QuizAttempt latestAttempt = inProgressAttempts.get(0);
        
        // ✅ KIỂM TRA THÊM: Chỉ cho phép resume nếu attempt thực sự đang IN_PROGRESS
        if (latestAttempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            System.out.println("⚠️ Attempt ID " + latestAttempt.getId() + " có status: " + latestAttempt.getStatus() + " - không thể resume");
            return Optional.empty();
        }
        
        Optional<QuizAttemptProgress> progressOpt = progressRepo.findByAttemptId(latestAttempt.getId());
        
        if (progressOpt.isEmpty()) {
            return Optional.empty();
        }
        
        QuizAttemptProgress progress = progressOpt.get();
        return Optional.of(convertToResumeDTO(latestAttempt, progress));
    }
    
    /**
     * ✅ Lấy thông tin chi tiết để resume quiz
     */
    public QuizResumeDTO getResumeDetails(Long attemptId, Long userId) {
        Optional<QuizAttempt> attemptOpt = quizAttemptRepo.findByIdAndUserId(attemptId, userId);
        if (attemptOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy attempt hoặc không có quyền truy cập");
        }
        
        QuizAttempt attempt = attemptOpt.get();
        if (!attempt.isInProgress()) {
            throw new RuntimeException("Attempt này không thể resume");
        }
        
        Optional<QuizAttemptProgress> progressOpt = progressRepo.findByAttemptId(attemptId);
        if (progressOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông tin tiến độ");
        }
        
        return convertToResumeDTO(attempt, progressOpt.get());
    }
    
    /**
     * ✅ Lưu tiến độ làm quiz (auto-save)
     */
    @Transactional
    public void saveProgress(Long attemptId, Long userId, Integer questionIndex, 
                           Integer timeRemaining, Map<Long, String> answers) {
        
        Optional<QuizAttempt> attemptOpt = quizAttemptRepo.findByIdAndUserId(attemptId, userId);
        if (attemptOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy attempt hoặc không có quyền truy cập");
        }
        
        QuizAttempt attempt = attemptOpt.get();
        if (!attempt.isInProgress()) {
            throw new RuntimeException("Attempt này không thể cập nhật tiến độ");
        }
        
        Optional<QuizAttemptProgress> progressOpt = progressRepo.findByAttemptId(attemptId);
        QuizAttemptProgress progress;
        
        if (progressOpt.isEmpty()) {
            // Tạo mới progress nếu chưa có
            progress = new QuizAttemptProgress(attempt, timeRemaining);
        } else {
            progress = progressOpt.get();
        }
        
        // Cập nhật tiến độ
        progress.updateProgress(questionIndex, timeRemaining);
        
        // Lưu đáp án dưới dạng JSON
        try {
            String answersJson = objectMapper.writeValueAsString(answers);
            progress.setAnswersJson(answersJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi lưu đáp án", e);
        }
        
        progressRepo.save(progress);
    }
    
    /**
     * ✅ Đánh dấu attempt cũ là ABANDONED và tạo attempt mới
     */
    @Transactional
    public QuizAttempt createNewAttempt(Long userId, Long quizId) {
        // Đánh dấu tất cả attempt cũ là ABANDONED
        List<QuizAttempt> oldAttempts = quizAttemptRepo.findInProgressAttemptsByUserAndQuiz(userId, quizId);
        for (QuizAttempt oldAttempt : oldAttempts) {
            oldAttempt.markAsAbandoned();
            quizAttemptRepo.save(oldAttempt);
        }
        
        // Tạo attempt mới
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        Quiz quiz = quizRepo.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz"));
        
        QuizAttempt newAttempt = new QuizAttempt();
        newAttempt.setUser(user);
        newAttempt.setQuiz(quiz);
        newAttempt.setScore(0);
        newAttempt.setTimeTaken(0);
        newAttempt.setStatus(AttemptStatus.IN_PROGRESS);
        
        return quizAttemptRepo.save(newAttempt);
    }
    
    /**
     * ✅ Resume attempt hiện tại
     */
    @Transactional
    public QuizResumeDTO resumeAttempt(Long attemptId, Long userId) {
        return getResumeDetails(attemptId, userId);
    }
    
    /**
     * ✅ Chuyển đổi từ model sang DTO
     */
    private QuizResumeDTO convertToResumeDTO(QuizAttempt attempt, QuizAttemptProgress progress) {
        QuizResumeDTO dto = new QuizResumeDTO();
        dto.setAttemptId(attempt.getId());
        dto.setQuizId(attempt.getQuiz().getId());
        dto.setQuizTitle(attempt.getQuiz().getTitle());
        dto.setCurrentQuestionIndex(progress.getCurrentQuestionIndex());
        dto.setTimeRemaining(progress.getTimeRemaining());
        dto.setAnswersJson(progress.getAnswersJson());
        dto.setStartedAt(progress.getStartedAt());
        dto.setLastSavedAt(progress.getLastSavedAt());
        
        // Kiểm tra quiz có giới hạn thời gian không
        Quiz quiz = attempt.getQuiz();
        // TODO: Thêm field timeLimit vào Quiz model nếu cần
        dto.setHasTimeLimit(false); // Tạm thời set false
        dto.setTotalQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);
        
        return dto;
    }
    
    /**
     * ✅ Lấy đáp án đã chọn từ JSON
     */
    public Map<Long, String> getAnswersFromJson(String answersJson) {
        if (answersJson == null || answersJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(answersJson, new TypeReference<Map<Long, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * ✅ Xóa progress không active (cleanup)
     */
    @Transactional
    public void cleanupInactiveProgress() {
        progressRepo.deleteByIsActiveFalse();
    }
}
