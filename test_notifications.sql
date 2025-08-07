-- ✅ TEST NOTIFICATIONS FOR ADMIN
USE QuizApp;
GO

-- ✅ THÊM TEST NOTIFICATIONS CHO ADMIN (USER ID = 2)
INSERT INTO notifications (user_id, content, notification_type, title, priority, related_entity_id, related_entity_type, action_url, is_read, created_at) VALUES
(2, 'Quiz "Toán học cơ bản" đã được tạo bởi user123', 'NEW_QUIZ_SUBMITTED', 'Quiz mới được tạo', 'NORMAL', 1, 'QUIZ', '/admin/quizzes', 0, GETDATE()),
(2, 'Có báo cáo mới về quiz "Lịch sử Việt Nam"', 'REPORT_SUBMITTED', 'Báo cáo vi phạm', 'HIGH', 2, 'QUIZ', '/admin/reports', 0, GETDATE()),
(2, 'User "john_doe" đã đăng ký tài khoản', 'NEW_USER_REGISTERED', 'Người dùng mới', 'NORMAL', 3, 'USER', '/admin/users', 0, GETDATE());

PRINT '✅ Đã thêm 3 test notifications cho admin';
PRINT 'User ID: 2 (Admin)';
PRINT 'Notifications: Quiz mới, Báo cáo, User mới';

-- ✅ KIỂM TRA KẾT QUẢ
SELECT 
    n.id,
    n.title,
    n.content,
    n.notification_type,
    n.priority,
    n.is_read,
    n.created_at,
    u.username as user_name
FROM notifications n
JOIN users u ON n.user_id = u.id
WHERE n.user_id = 2
ORDER BY n.created_at DESC; 