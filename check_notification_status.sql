-- ✅ KIỂM TRA TRẠNG THÁI NOTIFICATIONS
USE QuizApp;
GO

-- ✅ KIỂM TRA TẤT CẢ NOTIFICATIONS CỦA ADMIN (USER ID = 2)
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

-- ✅ ĐẾM NOTIFICATIONS CHƯA ĐỌC
SELECT 
    COUNT(*) as unread_count
FROM notifications n
WHERE n.user_id = 2 AND n.is_read = 0;

-- ✅ ĐẾM NOTIFICATIONS ĐÃ ĐỌC
SELECT 
    COUNT(*) as read_count
FROM notifications n
WHERE n.user_id = 2 AND n.is_read = 1;

-- ✅ TỔNG SỐ NOTIFICATIONS
SELECT 
    COUNT(*) as total_count
FROM notifications n
WHERE n.user_id = 2; 