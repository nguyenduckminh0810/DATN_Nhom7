package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.QuizAttempt;
import com.nhom7.quiz.quizapp.model.AttemptStatus;
import com.nhom7.quiz.quizapp.model.QuizAttemptProgress;
import com.nhom7.quiz.quizapp.model.dto.CorrectAnswerDTO;
import com.nhom7.quiz.quizapp.model.dto.EvaluationResult;
import com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO;
import com.nhom7.quiz.quizapp.repository.AnswerRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.ResultRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import com.nhom7.quiz.quizapp.repository.QuizAttemptRepo;
import com.nhom7.quiz.quizapp.repository.QuizAttemptProgressRepo;
import com.nhom7.quiz.quizapp.service.NotificationService;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // Removed QuizAttemptRepo – no longer creating attempts on submit

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private QuizAttemptRepo quizAttemptRepo;

    @Autowired
    private QuizAttemptProgressRepo quizAttemptProgressRepo;

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
        // Lấy Quiz và User
        User user = userRepo.findById(submission.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        Quiz quiz = quizRepo.findById(submission.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz không tồn tại"));

        // Đếm đúng theo đáp án chuẩn
        int correctCount = 0;
        List<CorrectAnswerDTO> correctAnswers = new ArrayList<>();

        // Lấy danh sách câu hỏi/đáp án đúng thực tế của quiz
        List<Answer> corrects = answerRepo.findAll().stream()
                .filter(a -> a.getQuestion() != null
                        && a.getQuestion().getQuiz().getId().equals(quiz.getId())
                        && a.isCorrect())
                .toList();
        java.util.Map<Long, Long> qidToCorrect = new java.util.HashMap<>();
        for (Answer a : corrects) {
            qidToCorrect.put(a.getQuestion().getId(), a.getId());
        }

        for (QuizSubmissionDTO.AnswerSubmission ans : submission.getAnswers()) {
            Long qid = ans.getQuestionId();
            Long correctId = qidToCorrect.get(qid);
            correctAnswers.add(new CorrectAnswerDTO(qid, correctId));
            if (correctId != null && correctId.equals(ans.getAnswerId()))
                correctCount++;
        }

        int totalQuestions = qidToCorrect.size();
        if (totalQuestions <= 0)
            totalQuestions = submission.getAnswers() != null ? submission.getAnswers().size() : 0;
        int score = (int) Math.round(100.0 * correctCount / Math.max(totalQuestions, 1));
        score = Math.max(0, Math.min(100, score));

        // Time taken an toàn (giây)
        int timeTakenSec = 0;
        if (submission.getTimeTaken() != null && submission.getTimeTaken() >= 0
                && submission.getTimeTaken() <= 24 * 3600) {
            timeTakenSec = submission.getTimeTaken();
        }

        Result result = new Result();
        result.setUser(user);
        result.setQuiz(quiz);
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        result.setTimeTaken(timeTakenSec);
        resultRepo.save(result);

        // CẬP NHẬT STATUS CỦA QUIZ ATTEMPT THÀNH SUBMITTED
        try {
            // Tìm attempt mới nhất của user cho quiz này
            Pageable pageable = PageRequest.of(0, 1, Sort.by("attemptedAt").descending());
            Page<QuizAttempt> attemptsPage = quizAttemptRepo.findByUserIdAndQuizId(user.getId(), quiz.getId(),
                    pageable);
            List<QuizAttempt> attempts = attemptsPage.getContent();
            if (!attempts.isEmpty()) {
                QuizAttempt latestAttempt = attempts.get(0);
                if (latestAttempt.getStatus() == AttemptStatus.IN_PROGRESS) {
                    latestAttempt.setStatus(AttemptStatus.SUBMITTED);
                    latestAttempt.setScore(score);
                    latestAttempt.setTimeTaken(timeTakenSec);
                    quizAttemptRepo.save(latestAttempt);

                    // Xóa progress nếu có
                    Optional<QuizAttemptProgress> progressOpt = quizAttemptProgressRepo
                            .findByAttemptId(latestAttempt.getId());
                    if (progressOpt.isPresent()) {
                        quizAttemptProgressRepo.delete(progressOpt.get());
                    }

                    System.out.println(" Đã cập nhật QuizAttempt ID " + latestAttempt.getId() + " thành SUBMITTED");
                }
            }
        } catch (Exception e) {
            System.err.println(" Lỗi khi cập nhật QuizAttempt status: " + e.getMessage());
            // Không throw error vì đây không phải lỗi nghiêm trọng
        }

        // Thông báo (giữ nguyên)
        try {
            notificationService.sendQuizResultNotification(user.getId(), quiz.getId(), quiz.getTitle(), score);
        } catch (Exception ignore) {
        }
        try {
            notificationService.sendQuizCompletedNotification(quiz.getId(), quiz.getTitle(), user.getUsername(), score);
        } catch (Exception ignore) {
        }

        return new EvaluationResult(result.getId(), score, correctAnswers);
    }

    // Bonus/streak logic removed for now (kept simple 0–100 scoring)

    public List<Result> getResultsByQuizId(Long quizId) {
        checkAdminPermission();
        return resultRepo.findByQuiz_Id(quizId);
    }

    public void deleteResultsByQuizId(Long quizId) {
        checkAdminPermission();
        resultRepo.deleteByQuiz_Id(quizId);
    }

    // Trả chi tiết kết quả an toàn cho FE
    public Optional<java.util.Map<String, Object>> getResultDetail(Long resultId) {
        return resultRepo.findById(resultId).map(r -> {
            java.util.Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("resultId", r.getId());
            dto.put("quizId", r.getQuiz().getId());
            dto.put("quizTitle", r.getQuiz().getTitle());
            dto.put("score", r.getScore());
            dto.put("completedAt", r.getCompletedAt());
            // Không trả correctAnswers ở đây nếu không cần; có thể tính/ghi log riêng
            return dto;
        });
    }

    // Method công khai để lấy thống kê quiz (không cần quyền admin)
    public List<Result> getResultsByQuizIdPublic(Long quizId) {
        // Không cần kiểm tra quyền - ai cũng có thể xem thống kê công khai
        return resultRepo.findByQuiz_Id(quizId);
    }
}
