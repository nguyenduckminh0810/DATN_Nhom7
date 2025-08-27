package com.nhom7.quiz.quizapp.controller;

import com.nhom7.quiz.quizapp.model.dto.NotificationDTO;
import com.nhom7.quiz.quizapp.service.NotificationService;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LoginService loginService;

    // LẤY TẤT CẢ NOTIFICATIONS CỦA USER
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = loginService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            List<NotificationDTO> notifications = notificationService.getUserNotifications(user.getId());
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy notifications: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // LẤY NOTIFICATIONS CHƯA ĐỌC
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = loginService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getId());
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy unread notifications: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ĐẾM NOTIFICATIONS CHƯA ĐỌC
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = loginService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            Long count = notificationService.getUnreadCount(user.getId());
            return ResponseEntity.ok(Map.of("count", count));

        } catch (Exception e) {
            System.err.println("Lỗi khi đếm unread notifications: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ĐÁNH DẤU NOTIFICATION ĐÃ ĐỌC
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = loginService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Lỗi khi đánh dấu notification đã đọc: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ĐÁNH DẤU TẤT CẢ NOTIFICATIONS ĐÃ ĐỌC
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = loginService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            // xác định role
            boolean isAdmin = false;
            try {
                isAdmin = user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN");
                // nếu dùng enum: isAdmin = user.getRole() == Role.ADMIN;
            } catch (Exception ignored) {
            }

            int updated = notificationService.markAllAsRead(user.getId(), isAdmin);
            return ResponseEntity.ok(Map.of("updated", (long) updated));

        } catch (Exception e) {
            System.err.println("Lỗi khi đánh dấu tất cả notifications đã đọc: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // XÓA NOTIFICATION
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = loginService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Lỗi khi xóa notification: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}