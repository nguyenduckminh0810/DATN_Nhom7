package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.QuizAttempt;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptDTO;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptSummaryDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import com.nhom7.quiz.quizapp.repository.QuizAttemptRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuizAttemptService {

    @Autowired
    private QuizAttemptRepo quizAttemptRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private QuizRepo quizRepo;

    /**
     * Tìm lịch sử làm quiz với phân quyền
     * 
     * @param userId - null nếu muốn xem tất cả (chỉ admin)
     * @param quizId - null nếu không lọc theo quiz
     * @param page   - trang hiện tại (bắt đầu từ 0)
     * @param size   - số lượng item trên mỗi trang
     * @return Page<QuizAttemptDTO>
     */
    public Page<QuizAttemptDTO> findQuizAttempts(Long userId, Long quizId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttempt> attempts;

        System.out.println("🔍 QuizAttemptService.findQuizAttempts:");
        System.out.println("  📋 Params - userId: " + userId + ", quizId: " + quizId + ", page: " + page + ", size: " + size);

        if (userId != null && quizId != null) {
            // Lọc theo cả user và quiz
            System.out.println("  🔍 Query: findByUserIdAndQuizId(" + userId + ", " + quizId + ")");
            attempts = quizAttemptRepo.findByUserIdAndQuizId(userId, quizId, pageable);
        } else if (userId != null) {
            // Chỉ lọc theo user
            System.out.println("  🔍 Query: findByUserIdOrderByAttemptedAtDesc(" + userId + ")");
            attempts = quizAttemptRepo.findByUserIdOrderByAttemptedAtDesc(userId, pageable);
        } else if (quizId != null) {
            // Chỉ lọc theo quiz
            System.out.println("  🔍 Query: findByQuizId(" + quizId + ")");
            attempts = quizAttemptRepo.findByQuizId(quizId, pageable);
        } else {
            // Xem tất cả (chỉ admin)
            System.out.println("  🔍 Query: findAllOrderByAttemptedAtDesc()");
            attempts = quizAttemptRepo.findAllOrderByAttemptedAtDesc(pageable);
        }

        System.out.println("  📊 Raw query result: " + attempts.getTotalElements() + " total elements, " + attempts.getContent().size() + " in current page");
        
        Page<QuizAttemptDTO> result = attempts.map(this::convertToDTO);
        System.out.println("  ✅ Converted to DTO: " + result.getTotalElements() + " total elements");
        
        return result;
    }

    /**
     * Bắt đầu attempt mới (trả về attemptId). Cho phép userId null đối với public practice
     */
     public Long startAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepo.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        if (userId != null) {
            User user = userRepo.findById(userId).orElse(null);
            attempt.setUser(user);
        } else {
            // Với public/anonymous, có thể gán user null nếu schema cho phép.
            // Trong DB hiện tại user_id nullable=false, nên tạm thời gán user đầu tiên hoặc ném lỗi.
            // Để an toàn: yêu cầu đăng nhập hoặc tạo user guest. Ở đây ném lỗi rõ ràng.
            throw new IllegalArgumentException("Public startAttempt yêu cầu đăng nhập trong schema hiện tại");
        }
        attempt.setScore(0);
         attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setTimeTaken(0);
         attempt.setStatus(com.nhom7.quiz.quizapp.model.AttemptStatus.IN_PROGRESS);
        QuizAttempt saved = quizAttemptRepo.save(attempt);
        return saved.getId();
    }

    /**
     * Trả về trạng thái attempt
     */
     public String getAttemptStatus(Long attemptId) {
         return quizAttemptRepo.findById(attemptId).map(a -> a.getStatus().name()).orElse(null);
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
                attempt.getTimeTaken());
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

    /**
     * Lấy recent attempts cho một quiz cụ thể
     */
    public List<QuizAttemptSummaryDTO> getRecentAttemptsForQuiz(Long quizId) {
        List<QuizAttempt> attempts = quizAttemptRepo.findByQuizIdOrderByAttemptedAtDesc(quizId);

        return attempts.stream()
                .limit(5) // Chỉ lấy 5 attempts gần nhất
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi QuizAttempt thành QuizAttemptSummaryDTO
     */
    private QuizAttemptSummaryDTO convertToSummaryDTO(QuizAttempt attempt) {
        return new QuizAttemptSummaryDTO(
                attempt.getId(),
                attempt.getUser().getFullName() != null ? attempt.getUser().getFullName()
                        : attempt.getUser().getUsername(),
                attempt.getScore(),
                attempt.getTimeTaken(),
                attempt.getAttemptedAt());
    }

    /**
     * Đếm tổng số quiz attempts trong database (để debug)
     */
    public long getTotalCount() {
        long count = quizAttemptRepo.count();
        System.out.println("🔍 QuizAttemptService.getTotalCount(): " + count);
        return count;
    }

    /**
     * Tạo sample data cho quiz attempts (để debug)
     */
    public String createSampleData() {
        try {
            System.out.println("🔧 Creating sample quiz attempt data...");
            
            // Lấy users và quizzes có sẵn
            List<User> users = userRepo.findAll();
            List<Quiz> quizzes = quizRepo.findAll();
            
            if (users.isEmpty()) {
                return "❌ No users found in database. Cannot create sample data.";
            }
            
            if (quizzes.isEmpty()) {
                return "❌ No quizzes found in database. Cannot create sample data.";
            }
            
            System.out.println("📊 Found " + users.size() + " users and " + quizzes.size() + " quizzes");
            
            int createdCount = 0;
            
            // Tạo sample attempts cho từng user với từng quiz
            for (User user : users) {
                for (int i = 0; i < Math.min(3, quizzes.size()); i++) { // Tối đa 3 attempts per user
                    Quiz quiz = quizzes.get(i);
                    
                    QuizAttempt attempt = new QuizAttempt();
                    attempt.setUser(user);
                    attempt.setQuiz(quiz);
                    attempt.setScore(60 + (int)(Math.random() * 40)); // Score từ 60-100
                    attempt.setTimeTaken(120 + (int)(Math.random() * 300)); // 2-7 phút
                    attempt.setAttemptedAt(LocalDateTime.now().minusDays((int)(Math.random() * 30))); // Trong 30 ngày qua
                    
                    quizAttemptRepo.save(attempt);
                    createdCount++;
                    
                    System.out.println("✅ Created attempt: User " + user.getUsername() + 
                                     " -> Quiz " + quiz.getTitle() + " (Score: " + attempt.getScore() + ")");
                }
            }
            
            return "✅ Successfully created " + createdCount + " sample quiz attempts!";
            
        } catch (Exception e) {
            System.err.println("❌ Error creating sample data: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error: " + e.getMessage();
        }
    }
}