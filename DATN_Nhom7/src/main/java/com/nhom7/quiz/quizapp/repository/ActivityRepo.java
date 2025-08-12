package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nhom7.quiz.quizapp.model.User;

public interface ActivityRepo extends JpaRepository<User, Long> {

    @Query(value = """
            SELECT u.id AS userId, u.full_name AS fullName, 'User Registered' AS activityType, u.created_at AS activityTime
            FROM users u
            WHERE u.created_at >= DATEADD(DAY, -30, GETDATE())

            UNION ALL

            SELECT q.user_id AS userId, u.full_name AS fullName, CONCAT('Created Quiz: ', q.title) AS activityType, q.created_at AS activityTime
            FROM quizzes q
            JOIN users u ON u.id = q.user_id
            WHERE q.created_at >= DATEADD(DAY, -30, GETDATE())

            UNION ALL

            SELECT qa.user_id AS userId, u.full_name AS fullName, CONCAT('Played Quiz: ', q.title) AS activityType, qa.attempted_at AS activityTime
            FROM quiz_attempts qa
            JOIN users u ON u.id = qa.user_id
            JOIN quizzes q ON q.id = qa.quiz_id
            WHERE qa.attempted_at >= DATEADD(DAY, -30, GETDATE())

            UNION ALL

            SELECT r.user_id AS userId, u.full_name AS fullName, CONCAT('Reported: ', r.reason) AS activityType, r.created_at AS activityTime
            FROM reports r
            JOIN users u ON u.id = r.user_id
            WHERE r.created_at >= DATEADD(DAY, -30, GETDATE())

            ORDER BY activityTime DESC
            """, countQuery = """
            SELECT COUNT(*) FROM (
                SELECT u.id
                FROM users u
                WHERE u.created_at >= DATEADD(DAY, -30, GETDATE())

                UNION ALL

                SELECT q.user_id
                FROM quizzes q
                WHERE q.created_at >= DATEADD(DAY, -30, GETDATE())

                UNION ALL

                SELECT qa.user_id
                FROM quiz_attempts qa
                WHERE qa.attempted_at >= DATEADD(DAY, -30, GETDATE())

                UNION ALL

                SELECT r.user_id
                FROM reports r
                WHERE r.created_at >= DATEADD(DAY, -30, GETDATE())
            ) AS total
            """, nativeQuery = true)
    Page<Object[]> getRecentActivities(Pageable pageable);
}
