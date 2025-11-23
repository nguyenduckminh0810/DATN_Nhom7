package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.AttemptStatus;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.QuizAttempt;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptDTO;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptSummaryDTO;
import com.nhom7.quiz.quizapp.repository.QuizAttemptRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
     * Tìm tất cả attempts (bao gồm IN_PROGRESS) - CHỈ CHO ADMIN HOẶC RESUME QUIZ
     *
     * @param userId null nếu xem tất cả (admin)
     * @param quizId null nếu không lọc theo quiz
     * @param page   trang hiện tại (0-based)
     * @param size   số item/trang
     */
    public Page<QuizAttemptDTO> findAllQuizAttempts(Long userId, Long quizId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttempt> attempts;

        System.out.println("🔍 QuizAttemptService.findAllQuizAttempts (ALL STATUSES):");
        System.out.println(
                "  📋 Params - userId: " + userId + ", quizId: " + quizId + ", page: " + page + ", size: " + size);

        if (userId != null && quizId != null) {
            System.out.println("  🔍 Query: findByUser_IdAndQuiz_Id(" + userId + ", " + quizId + ")");
            attempts = quizAttemptRepo.findByUser_IdAndQuiz_Id(userId, quizId, pageable);
        } else if (userId != null) {
            // Có bản có order sẵn
            System.out.println("  🔍 Query: findByUserIdOrderByAttemptedAtDesc(" + userId + ")");
            attempts = quizAttemptRepo.findByUserIdOrderByAttemptedAtDesc(userId, pageable);
        } else if (quizId != null) {
            // Lấy theo quiz (nếu cần order, có thể set sort trong Pageable từ Controller)
            System.out.println("  🔍 Query: findByQuiz_Id(" + quizId + ")");
            attempts = quizAttemptRepo.findByQuiz_Id(quizId, pageable);
        } else {
            System.out.println("  🔍 Query: findAllOrderByAttemptedAtDesc()");
            attempts = quizAttemptRepo.findAllOrderByAttemptedAtDesc(pageable);
        }

        System.out.println("  📊 Raw query result (ALL STATUSES): " +
                attempts.getTotalElements() + " total elements, " + attempts.getContent().size() + " in current page");

        Page<QuizAttemptDTO> result = attempts.map(this::convertToDTO);
        System.out.println("  ✅ Converted to DTO: " + result.getTotalElements() + " total elements");

        return result;
    }

    /**
     * Tìm lịch sử làm quiz với phân quyền - CHỈ LẤY ATTEMPT ĐÃ HOÀN THÀNH
     *
     * @param userId null nếu xem tất cả (admin)
     * @param quizId null nếu không lọc theo quiz
     * @param page   trang hiện tại (0-based)
     * @param size   số item/trang
     */
    public Page<QuizAttemptDTO> findQuizAttempts(Long userId, Long quizId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttempt> attempts;

        System.out.println("🔍 QuizAttemptService.findQuizAttempts (COMPLETED ONLY):");
        System.out.println(
                "  📋 Params - userId: " + userId + ", quizId: " + quizId + ", page: " + page + ", size: " + size);

        if (userId != null && quizId != null) {
            System.out.println(
                    "  🔍 Query: findCompletedByUserIdAndQuizIdOrderByAttemptedAtDesc(" + userId + ", " + quizId + ")");
            attempts = quizAttemptRepo.findCompletedByUserIdAndQuizIdOrderByAttemptedAtDesc(userId, quizId, pageable);
        } else if (userId != null) {
            System.out.println("  🔍 Query: findCompletedByUserIdOrderByAttemptedAtDesc(" + userId + ")");
            attempts = quizAttemptRepo.findCompletedByUserIdOrderByAttemptedAtDesc(userId, pageable);
        } else if (quizId != null) {
            System.out.println("  🔍 Query: findCompletedByQuizIdOrderByAttemptedAtDesc(" + quizId + ")");
            attempts = quizAttemptRepo.findCompletedByQuizIdOrderByAttemptedAtDesc(quizId, pageable);
        } else {
            System.out.println("  🔍 Query: findAllCompletedOrderByAttemptedAtDesc()");
            attempts = quizAttemptRepo.findAllCompletedOrderByAttemptedAtDesc(pageable);
        }

        System.out.println("  📊 Raw query result (COMPLETED ONLY): " +
                attempts.getTotalElements() + " total elements, " + attempts.getContent().size() + " in current page");

        Page<QuizAttemptDTO> result = attempts.map(this::convertToDTO);
        System.out.println("  ✅ Converted to DTO: " + result.getTotalElements() + " total elements");
        return result;
    }

    /**
     * Bắt đầu một quiz attempt mới
     */
    public Long startAttempt(Long quizId, Long userId) {
        System.out.println("QuizAttemptService.startAttempt() - Quiz ID: " + quizId + ", User ID: " + userId);

        User user = userRepo.findById(userId).orElse(null);
        System.out.println("Found user: " + (user != null ? user.getUsername() : "null"));

        Quiz quiz = quizRepo.findById(quizId).orElse(null);
        System.out.println("Found quiz: " + (quiz != null ? quiz.getTitle() : "null"));

        if (user == null || quiz == null) {
            System.err.println("User or quiz not found");
            return null;
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(0);
        attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setTimeTaken(0);
        attempt.setStatus(AttemptStatus.IN_PROGRESS); // ✅ đặt trạng thái rõ ràng

        System.out.println("🔍 Creating attempt with data:");
        System.out.println("  - User: " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("  - Quiz: " + quiz.getTitle() + " (ID: " + quiz.getId() + ")");
        System.out.println("  - Score: " + attempt.getScore());
        System.out.println("  - AttemptedAt: " + attempt.getAttemptedAt());
        System.out.println("  - TimeTaken: " + attempt.getTimeTaken());
        System.out.println("  - Status: " + attempt.getStatus());

        QuizAttempt saved = quizAttemptRepo.save(attempt);
        System.out.println("Successfully saved attempt with ID: " + saved.getId());
        return saved.getId();
    }

    /**
     * Trả về trạng thái attempt
     */
    public String getAttemptStatus(Long attemptId) {
        System.out.println("QuizAttemptService.getAttemptStatus() - Attempt ID: " + attemptId);

        QuizAttempt attempt = quizAttemptRepo.findById(attemptId).orElse(null);
        if (attempt == null) {
            System.err.println("Attempt not found");
            return null;
        }

        System.out.println("Found attempt:");
        System.out.println("  - ID: " + attempt.getId());
        System.out.println("  - Score: " + attempt.getScore());
        System.out.println("  - AttemptedAt: " + attempt.getAttemptedAt());
        System.out.println("  - TimeTaken: " + attempt.getTimeTaken());
        System.out.println("  - Status: " + attempt.getStatus());

        // Ưu tiên đọc từ trường status (enum)
        return attempt.getStatus() != null ? attempt.getStatus().name() : "UNKNOWN";
    }

    /**
     * Chuyển đổi QuizAttempt thành QuizAttemptDTO
     */
    private QuizAttemptDTO convertToDTO(QuizAttempt attempt) {
        String username = attempt.getUser() != null ? attempt.getUser().getUsername() : "Unknown";
        String quizTitle = attempt.getQuiz() != null ? attempt.getQuiz().getTitle() : "Unknown Quiz";
        return new QuizAttemptDTO(
                attempt.getId(),
                username,
                quizTitle,
                attempt.getScore(),
                attempt.getAttemptedAt(),
                attempt.getTimeTaken(),
                attempt.getStatus() // ✅ giữ status
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

    /**
     * Lấy 5 attempt gần nhất (đã hoàn thành) cho 1 quiz
     */
    public List<QuizAttemptSummaryDTO> getRecentAttemptsForQuiz(Long quizId) {
        try {
            System.out.println("QuizAttemptService.getRecentAttemptsForQuiz() - Quiz ID: " + quizId);

            if (quizId == null) {
                System.err.println("Quiz ID is null!");
                return new ArrayList<>();
            }

            System.out.println("📊 Fetching COMPLETED attempts from repository...");
            List<QuizAttempt> attempts = quizAttemptRepo.findCompletedByQuizIdOrderByAttemptedAtDesc(quizId);
            System.out.println("📊 Found " + attempts.size() + " COMPLETED attempts for quiz " + quizId);

            if (attempts.isEmpty()) {
                System.out.println("ℹ️ No COMPLETED attempts found for quiz " + quizId + ". Returning empty list.");
                return new ArrayList<>();
            }

            System.out.println("🔄 Converting COMPLETED attempts to DTOs...");
            List<QuizAttemptSummaryDTO> result = attempts.stream()
                    .limit(5)
                    .map(a -> {
                        try {
                            System.out.println("🔄 Converting COMPLETED attempt ID: " + a.getId() + " (Status: "
                                    + a.getStatus() + ")");
                            return convertToSummaryDTO(a);
                        } catch (Exception e) {
                            System.err.println("Error converting attempt " + a.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            System.out.println("✅ Successfully converted " + result.size() + " COMPLETED DTOs");
            return result;

        } catch (Exception e) {
            System.err.println("Error in getRecentAttemptsForQuiz: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Chuyển đổi QuizAttempt thành QuizAttemptSummaryDTO
     */
    private QuizAttemptSummaryDTO convertToSummaryDTO(QuizAttempt attempt) {
        try {
            System.out.println("🔄 Converting attempt ID: " + attempt.getId());

            if (attempt == null) {
                System.err.println("Attempt is null!");
                return null;
            }
            if (attempt.getUser() == null) {
                System.err.println("User is null for attempt ID: " + attempt.getId());
                return null;
            }

            String displayName = attempt.getUser().getFullName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = attempt.getUser().getUsername();
            }
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "Unknown User";
            }

            System.out.println("Converted attempt ID: " + attempt.getId() + " for user: " + displayName);

            return new QuizAttemptSummaryDTO(
                    attempt.getId(),
                    displayName,
                    attempt.getScore(),
                    attempt.getTimeTaken(),
                    attempt.getAttemptedAt());

        } catch (Exception e) {
            System.err.println("Error in convertToSummaryDTO for attempt ID "
                    + (attempt != null ? attempt.getId() : "null") + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Đếm tổng số quiz attempts trong database (debug)
     */
    public long getTotalCount() {
        long count = quizAttemptRepo.count();
        System.out.println("🔍 QuizAttemptService.getTotalCount(): " + count);
        return count;
    }

    /**
     * Tạo sample data cho quiz attempts (debug)
     */
    public String createSampleData() {
        try {
            System.out.println("Creating sample quiz attempt data...");

            List<User> users = userRepo.findAll();
            List<Quiz> quizzes = quizRepo.findAll();

            if (users.isEmpty()) {
                return "No users found in database. Cannot create sample data.";
            }
            if (quizzes.isEmpty()) {
                return "No quizzes found in database. Cannot create sample data.";
            }

            System.out.println("Found " + users.size() + " users and " + quizzes.size() + " quizzes");

            int createdCount = 0;

            for (User user : users) {
                for (int i = 0; i < Math.min(3, quizzes.size()); i++) {
                    Quiz quiz = quizzes.get(i);

                    QuizAttempt attempt = new QuizAttempt();
                    attempt.setUser(user);
                    attempt.setQuiz(quiz);
                    attempt.setScore(60 + (int) (Math.random() * 40)); // 60-100
                    attempt.setTimeTaken(120 + (int) (Math.random() * 300)); // 2-7 phút
                    attempt.setAttemptedAt(LocalDateTime.now().minusDays((int) (Math.random() * 30)));
                    attempt.setStatus(AttemptStatus.SUBMITTED);

                    quizAttemptRepo.save(attempt);
                    createdCount++;

                    System.out.println("Created attempt: User " + user.getUsername() +
                            " -> Quiz " + quiz.getTitle() + " (Score: " + attempt.getScore() + ")");
                }
            }

            return "Successfully created " + createdCount + " sample quiz attempts!";

        } catch (Exception e) {
            System.err.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
