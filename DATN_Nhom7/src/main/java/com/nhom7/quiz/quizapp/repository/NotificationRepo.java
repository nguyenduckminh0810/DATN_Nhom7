package com.nhom7.quiz.quizapp.repository;

import com.nhom7.quiz.quizapp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    // ✅ TÌM NOTIFICATIONS CỦA USER
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // ✅ TÌM NOTIFICATIONS CHƯA ĐỌC CỦA USER
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // ✅ ĐẾM SỐ NOTIFICATIONS CHƯA ĐỌC CỦA USER
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    // ✅ TÌM NOTIFICATIONS THEO TYPE
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.notificationType = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(@Param("userId") Long userId,
            @Param("type") String type);

    // ✅ TÌM NOTIFICATIONS THEO PRIORITY
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.priority = :priority ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(@Param("userId") Long userId,
            @Param("priority") String priority);

    // ✅ XÓA NOTIFICATIONS CŨ (30 NGÀY TRỞ LÊN)
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    void deleteOldNotifications(@Param("date") java.time.LocalDateTime date);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true
            WHERE
              (n.audience = 'USER' AND n.user.id = :userId)
              OR (n.audience = 'ALL')
            """)
    int markAllAsReadForUser(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.audience IN ('ADMIN','ALL')")
    int markAllAsReadForAdmin();
}