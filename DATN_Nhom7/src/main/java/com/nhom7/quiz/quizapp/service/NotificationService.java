package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.Notification;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.Report;
import com.nhom7.quiz.quizapp.model.dto.NotificationDTO;
import com.nhom7.quiz.quizapp.repository.NotificationRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // NOTIFICATION TYPES
    public static final String QUIZ_RESULT_READY = "QUIZ_RESULT_READY";
    public static final String QUIZ_APPROVED = "QUIZ_APPROVED";
    public static final String QUIZ_REJECTED = "QUIZ_REJECTED";
    public static final String ACCOUNT_STATUS_CHANGED = "ACCOUNT_STATUS_CHANGED";
    public static final String REVIEW_RECEIVED = "REVIEW_RECEIVED";
    public static final String NEW_QUIZ_SUBMITTED = "NEW_QUIZ_SUBMITTED";
    public static final String QUIZ_COMPLETED = "QUIZ_COMPLETED";
    public static final String NEW_USER_REGISTERED = "NEW_USER_REGISTERED";
    public static final String REPORT_SUBMITTED = "REPORT_SUBMITTED";
    public static final String SYSTEM_ACTIVITY = "SYSTEM_ACTIVITY";

    // PRIORITY LEVELS
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";

    // TẠO VÀ GỬI NOTIFICATION
    public void sendNotification(Long userId, String type, String title, String message,
            String priority, Long relatedEntityId, String relatedEntityType, String actionUrl) {
        try {
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isEmpty()) {
                System.out.println("User không tồn tại: " + userId);
                return;
            }

            User user = userOpt.get();

            // TẠO NOTIFICATION ENTITY
            Notification notification = new Notification(
                    user, message, type, title, priority, relatedEntityId, relatedEntityType, actionUrl);

            // LƯU VÀO DATABASE
            Notification savedNotification = notificationRepo.save(notification);

            // TẠO DTO CHO WEBSOCKET
            NotificationDTO notificationDTO = new NotificationDTO(
                    savedNotification.getId(), type, userId, title, message, priority,
                    relatedEntityId, relatedEntityType, actionUrl, false, savedNotification.getCreatedAt());

            // GỬI QUA WEBSOCKET
            System.out.println("🔌 Sending WebSocket message to user: " + user.getUsername());
            System.out.println("🔌 Message content: " + notificationDTO);

            messagingTemplate.convertAndSendToUser(
                    user.getUsername(), // DESTINATION USER
                    "/queue/notifications", // QUEUE PATH
                    notificationDTO // MESSAGE
            );

            System.out.println("Đã gửi notification: " + type + " cho user: " + user.getUsername());

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // GỬI NOTIFICATION CHO ADMIN
    public void sendNotificationToAdmins(String type, String title, String message,
            String priority, Long relatedEntityId, String relatedEntityType, String actionUrl) {
        try {
            // TÌM TẤT CẢ ADMIN (SỬ DỤNG PAGINATION)
            Page<User> adminPage = userRepo.findByRole("ADMIN", PageRequest.of(0, 1000));
            List<User> admins = adminPage.getContent();

            for (User admin : admins) {
                sendNotification(admin.getId(), type, title, message, priority, relatedEntityId, relatedEntityType,
                        actionUrl);
            }

            System.out.println("Đã gửi notification cho " + admins.size() + " admin");

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi notification cho admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // LẤY NOTIFICATIONS CỦA USER
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // LẤY NOTIFICATIONS CHƯA ĐỌC CỦA USER
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepo.findUnreadByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ĐÁNH DẤU NOTIFICATION ĐÃ ĐỌC
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepo.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notificationRepo.save(notification);
            System.out.println("Marked notification " + notificationId + " as read");
        } else {
            System.err.println("Notification " + notificationId + " not found");
        }
    }

    // ĐÁNH DẤU TẤT CẢ NOTIFICATIONS CỦA USER ĐÃ ĐỌC
    @Transactional
    public int markAllAsRead(Long userId, boolean isAdmin) {
        if (isAdmin) {
            // Optional: nếu bạn muốn admin có endpoint mark-all riêng
            // Tạo method dưới đây trong repo (nếu chưa có):
            // @Modifying
            // @Query("UPDATE Notification n SET n.isRead = true WHERE n.audience IN
            // ('ADMIN','ALL')")
            // int markAllAsReadForAdmin();
            int updated = isAdmin ? notificationRepo.markAllAsReadForAdmin()
                    : notificationRepo.markAllAsReadForUser(userId);
            System.out.println("Mark-all updated rows = " + updated);
            return notificationRepo.markAllAsReadForAdmin();
        } else {
            int updated = isAdmin ? notificationRepo.markAllAsReadForAdmin()
                    : notificationRepo.markAllAsReadForUser(userId);
            System.out.println("Mark-all updated rows = " + updated);
            return notificationRepo.markAllAsReadForUser(userId);
        }
    }

    // ĐẾM NOTIFICATIONS CHƯA ĐỌC
    public Long getUnreadCount(Long userId) {
        return notificationRepo.countUnreadByUserId(userId);
    }

    // XÓA NOTIFICATION
    public void deleteNotification(Long notificationId) {
        notificationRepo.deleteById(notificationId);
    }

    // CONVERT ENTITY TO DTO
    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getNotificationType(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getPriority(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.getActionUrl(),
                notification.isRead(),
                notification.getCreatedAt());
    }

    // SPECIFIC NOTIFICATION METHODS

    // QUIZ RESULT READY
    public void sendQuizResultNotification(Long userId, Long quizId, String quizTitle, int score) {
        String title = "Kết quả Quiz: " + quizTitle;
        String message = "Bạn đã đạt " + score + " điểm trong quiz " + quizTitle;
        String actionUrl = "/quiz/result/" + quizId;

        sendNotification(userId, QUIZ_RESULT_READY, title, message, PRIORITY_NORMAL, quizId, "QUIZ", actionUrl);
    }

    // QUIZ APPROVED
    public void sendQuizApprovedNotification(Long userId, Long quizId, String quizTitle) {
        String title = "Quiz được phê duyệt";
        String message = "Quiz '" + quizTitle + "' của bạn đã được phê duyệt";
        String actionUrl = "/quiz/" + quizId;

        sendNotification(userId, QUIZ_APPROVED, title, message, PRIORITY_HIGH, quizId, "QUIZ", actionUrl);
    }

    // QUIZ REJECTED
    public void sendQuizRejectedNotification(Long userId, Long quizId, String quizTitle) {
        String title = "Quiz bị từ chối";
        String message = "Quiz '" + quizTitle + "' của bạn đã bị từ chối";
        String actionUrl = "/quiz/edit/" + quizId;

        sendNotification(userId, QUIZ_REJECTED, title, message, PRIORITY_HIGH, quizId, "QUIZ", actionUrl);
    }

    // ACCOUNT STATUS CHANGED
    public void sendAccountStatusNotification(Long userId, boolean isBanned) {
        String title = isBanned ? "Tài khoản bị khóa" : "Tài khoản được mở khóa";
        String message = isBanned ? "Tài khoản của bạn đã bị khóa" : "Tài khoản của bạn đã được mở khóa";
        String actionUrl = "/profile";

        sendNotification(userId, ACCOUNT_STATUS_CHANGED, title, message, PRIORITY_URGENT, userId, "USER", actionUrl);
    }

    // NEW QUIZ SUBMITTED (CHO ADMIN)
    public void sendNewQuizSubmittedNotification(Long quizId, String quizTitle, String creatorName) {
        // Hệ thống đang cho phép tạo quiz công khai không cần duyệt → không gửi thông
        // báo cho admin
        System.out.println("Skipped admin notification NEW_QUIZ_SUBMITTED because quizzes are auto-public.");
    }

    // QUIZ COMPLETED (CHO ADMIN)
    public void sendQuizCompletedNotification(Long quizId, String quizTitle, String userName, int score) {
        // Tránh spam admin: sự kiện hoàn thành quiz chỉ dành cho user
        System.out.println("Skipped admin notification QUIZ_COMPLETED for user activity.");
    }

    // NEW REPORT NOTIFICATION (CHO ADMIN)
    public void sendNewReportNotification(Report report) {
        try {
            System.out.println("NotificationService.sendNewReportNotification called:");
            System.out.println("Report ID: " + report.getId());
            System.out.println("Reporter: " + report.getUser().getFullName());
            System.out.println("Quiz: " + report.getQuiz().getTitle());
            System.out.println("Reason: " + report.getReason());

            String title = "Báo cáo mới cần xử lý";
            String content = "User " + report.getUser().getFullName() + " đã báo cáo quiz '" +
                    report.getQuiz().getTitle() + "' với lý do: " + report.getReason();
            String actionUrl = "/admin/reports";

            System.out.println("Admin notification details:");
            System.out.println("Title: " + title);
            System.out.println("Content: " + content);
            System.out.println("Action URL: " + actionUrl);

            // GỬI NOTIFICATION CHO TẤT CẢ ADMIN
            sendNotificationToAdmins(
                    "REPORT_SUBMITTED",
                    title,
                    content,
                    "HIGH",
                    report.getId(),
                    "REPORT",
                    actionUrl);

            System.out.println("Đã gửi notification cho admin về report mới");

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi notification cho admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // REPORT ACTION NOTIFICATION (CHO USER ĐÃ BÁO CÁO)
    public void sendReportActionNotification(Report report, User admin) {
        try {
            System.out.println("NotificationService.sendReportActionNotification called:");
            System.out.println("Report ID: " + report.getId());
            System.out.println("Report Status: " + report.getStatus());
            System.out.println("Admin: " + admin.getFullName());

            User reporter = report.getUser(); // User đã gửi báo cáo
            String action = report.getStatus();

            System.out.println("🔧 Reporter: " + reporter.getUsername() + " (ID: " + reporter.getId() + ")");

            // SỬA LOGIC: MAP RESOLVED THÀNH APPROVED CHO NOTIFICATION
            boolean isApproved = action.equals("APPROVED") || action.equals("RESOLVED");

            String title = isApproved ? "Báo cáo của bạn đã được chấp nhận" : "Báo cáo của bạn đã bị từ chối";

            String content = isApproved
                    ? "Admin " + admin.getFullName() + " đã chấp nhận báo cáo về quiz: " + report.getQuiz().getTitle()
                    : "Admin " + admin.getFullName() + " đã từ chối báo cáo về quiz: " + report.getQuiz().getTitle() +
                            ". Lý do: " + report.getAdminResponse();

            String priority = isApproved ? "HIGH" : "NORMAL";
            String actionUrl = "/quiz/" + report.getQuiz().getId();

            System.out.println("Notification details:");
            System.out.println("Title: " + title);
            System.out.println("Content: " + content);
            System.out.println("Priority: " + priority);
            System.out.println("Action URL: " + actionUrl);

            // GỬI NOTIFICATION QUA WEBSOCKET
            sendNotification(
                    reporter.getId(),
                    "REPORT_ACTION",
                    title,
                    content,
                    priority,
                    report.getQuiz().getId(),
                    "QUIZ",
                    actionUrl);

            System.out.println("Đã gửi report action notification cho user: " + reporter.getUsername());

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi report action notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}