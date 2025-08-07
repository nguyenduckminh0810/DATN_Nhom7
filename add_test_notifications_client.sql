-- ✅ THÊM TEST NOTIFICATIONS CHO CLIENT USER
USE QuizApp;
GO

-- ✅ TÌM USER CLIENT (KHÔNG PHẢI ADMIN)
SELECT id, username, role FROM users WHERE role = 'user' OR role = 'USER' ORDER BY id;

-- ✅ THÊM TEST NOTIFICATIONS CHO CLIENT USER (USER ID = 1 hoặc user đầu tiên)
DECLARE @clientUserId INT;
SELECT @clientUserId = id FROM users WHERE role = 'user' OR role = 'USER' ORDER BY id;

IF @clientUserId IS NULL
BEGIN
    PRINT '❌ Không tìm thấy user client nào';
    PRINT 'Danh sách users hiện có:';
    SELECT id, username, role FROM users;
END
ELSE
BEGIN
    PRINT '✅ Tìm thấy client user với ID: ' + CAST(@clientUserId AS VARCHAR);
    
    -- ✅ XÓA NOTIFICATIONS CŨ (NẾU CÓ)
    DELETE FROM notifications WHERE user_id = @clientUserId;
    PRINT '✅ Đã xóa notifications cũ cho client user';
    
    -- ✅ THÊM TEST NOTIFICATIONS CHO CLIENT USER
    INSERT INTO notifications (user_id, content, notification_type, title, priority, related_entity_id, related_entity_type, action_url, is_read, created_at) VALUES
    (@clientUserId, 'Bạn đã hoàn thành quiz "Toán học cơ bản" với 85 điểm', 'QUIZ_RESULT_READY', 'Kết quả Quiz', 'NORMAL', 1, 'QUIZ', '/quiz/result/1', 0, GETDATE()),
    (@clientUserId, 'Quiz "Lịch sử Việt Nam" của bạn đã được phê duyệt', 'QUIZ_APPROVED', 'Quiz được phê duyệt', 'HIGH', 2, 'QUIZ', '/quiz/2', 0, GETDATE()),
    (@clientUserId, 'Bạn đã nhận được đánh giá 5 sao cho quiz "Tiếng Anh"', 'REVIEW_RECEIVED', 'Đánh giá mới', 'NORMAL', 3, 'QUIZ', '/quiz/3', 0, GETDATE());
    
    PRINT '✅ Đã thêm 3 test notifications cho client user';
    PRINT 'User ID: ' + CAST(@clientUserId AS VARCHAR) + ' (Client)';
    PRINT 'Notifications: Quiz Result, Quiz Approved, Review Received';
    
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
    WHERE n.user_id = @clientUserId
    ORDER BY n.created_at DESC;
    
    -- ✅ ĐẾM NOTIFICATIONS CHƯA ĐỌC
    SELECT 
        COUNT(*) as unread_count
    FROM notifications n
    WHERE n.user_id = @clientUserId AND n.is_read = 0;
END 