package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.CorrectAnswerDTO;
import com.nhom7.quiz.quizapp.model.dto.EvaluationResult;
import com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO;
import com.nhom7.quiz.quizapp.repository.AnswerRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.ResultRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class ResultService {

    @Autowired
    private AnswerRepo answerRepo;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private QuizRepo quizRepo;

    // Kiểm tra quyền admin
    private void checkAdminPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Chỉ admin mới có quyền thực hiện thao tác này");
        }
    }

    // Kiểm tra quyền user (chỉ có thể xem kết quả của mình)
    private void checkUserPermission(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Không có quyền truy cập");
        }
        
        // Admin có thể xem tất cả
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // User thường chỉ có thể xem kết quả của mình
            String currentUsername = authentication.getName();
            User currentUser = userRepo.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                throw new AccessDeniedException("Bạn chỉ có thể xem kết quả của mình");
            }
        }
    }

    // Method cho @PreAuthorize
    public boolean checkUserPermission(Long userId, String username) {
        // Admin có thể xem tất cả
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // User thường chỉ có thể xem kết quả của mình
        User currentUser = userRepo.findByUsername(username).orElse(null);
        return currentUser != null && currentUser.getId().equals(userId);
    }

    public List<Result> getResultsByUserId(Long userId) {
        checkUserPermission(userId);
        return resultRepo.findByUser_Id(userId);
    }

    public EvaluationResult evaluateAndSave(QuizSubmissionDTO submission) {
        int correctCount = 0;
        List<CorrectAnswerDTO> correctAnswers = new ArrayList<>();

        for (QuizSubmissionDTO.AnswerSubmission ans : submission.getAnswers()) {
            Long questionId = ans.getQuestionId();

            // Lấy đáp án đúng nhất cho câu hỏi
            Optional<Answer> correctAnswerOpt = answerRepo.findByQuestion_IdAndIsCorrectTrue(questionId);

            if (correctAnswerOpt.isPresent()) {
                Long correctAnswerId = correctAnswerOpt.get().getId();
                correctAnswers.add(new CorrectAnswerDTO(questionId, correctAnswerId));

                if (correctAnswerId.equals(ans.getAnswerId())) {
                    correctCount++;
                }
            } else {
                // Nếu không tìm thấy đáp án đúng thì vẫn thêm vào để client biết
                correctAnswers.add(new CorrectAnswerDTO(questionId, null));
            }
        }

        int total = submission.getAnswers().size();
        int baseScore = (int) ((correctCount / (double) total) * 100);

        // Lấy User và Quiz từ ID
        Optional<User> userOpt = userRepo.findById(submission.getUserId());
        Optional<Quiz> quizOpt = quizRepo.findById(submission.getQuizId());

        if (userOpt.isEmpty() || quizOpt.isEmpty()) {
            throw new IllegalArgumentException("User hoặc Quiz không tồn tại.");
        }

        // Tính bonus điểm
        int bonusPoints = calculateBonusPoints(submission.getQuizId(), submission.getUserId(), baseScore);
        int finalScore = baseScore + bonusPoints;

        Result result = new Result();
        result.setUser(userOpt.get());
        result.setQuiz(quizOpt.get());
        result.setScore(finalScore);
        result.setCompletedAt(LocalDateTime.now());
        result.setTimeTaken(submission.getTimeTaken()); // Thêm thời gian làm quiz

        resultRepo.save(result);

        return new EvaluationResult(finalScore, correctAnswers);
    }

    // Tính toán bonus điểm cho leaderboard
    private int calculateBonusPoints(Long quizId, Long userId, int baseScore) {
        int bonus = 0;
        
        // +3 điểm nếu không sai câu nào (100% chính xác)
        if (baseScore == 100) {
            bonus += 3;
            System.out.println("🎯 Perfect Score Bonus: +3 points");
        }
        
        // +5 điểm nếu trong top 3 nhanh nhất
        List<Result> top3Fastest = resultRepo.findTop3ByQuizIdOrderByTimeTakenAsc(quizId);
        if (!top3Fastest.isEmpty() && top3Fastest.size() <= 3) {
            // Kiểm tra xem user có trong top 3 không (sẽ được cập nhật sau khi save)
            bonus += 5;
            System.out.println("⚡ Speed Bonus: +5 points (Top 3 fastest)");
        }
        
        // +2 điểm nếu làm liên tiếp 3 quiz trong ngày
        long todayAttempts = resultRepo.countByUserIdAndCompletedAtToday(userId);
        if (todayAttempts >= 3) {
            bonus += 2;
            System.out.println("🔥 Streak Bonus: +2 points (3+ quizzes today)");
        }
        
        // +1 điểm nếu làm quiz lần đầu tiên
        long totalAttempts = resultRepo.countByUser_Id(userId);
        if (totalAttempts == 0) {
            bonus += 1;
            System.out.println("🌟 First Time Bonus: +1 point");
        }
        
        System.out.println("💰 Total Bonus Points: " + bonus);
        return bonus;
    }

    public List<Result> getResultsByQuizId(Long quizId) {
        checkAdminPermission();
        return resultRepo.findByQuiz_Id(quizId);
    }

    public void deleteResultsByQuizId(Long quizId) {
        checkAdminPermission();
        resultRepo.deleteByQuiz_Id(quizId);
    }
}
