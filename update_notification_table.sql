-- ✅ UPDATE NOTIFICATIONS TABLE FOR WEBSOCKET NOTIFICATIONS
-- Chạy script này để cập nhật bảng notifications với các field mới

USE QuizApp;
GO

-- ✅ THÊM CÁC COLUMN MỚI VÀO BẢNG NOTIFICATIONS
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'notification_type')
BEGIN
    ALTER TABLE notifications ADD notification_type NVARCHAR(50) NOT NULL DEFAULT 'SYSTEM_ACTIVITY';
    PRINT '✅ Added notification_type column';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'title')
BEGIN
    ALTER TABLE notifications ADD title NVARCHAR(255) NULL;
    PRINT '✅ Added title column';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'priority')
BEGIN
    ALTER TABLE notifications ADD priority NVARCHAR(20) NOT NULL DEFAULT 'NORMAL';
    PRINT '✅ Added priority column';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'related_entity_id')
BEGIN
    ALTER TABLE notifications ADD related_entity_id BIGINT NULL;
    PRINT '✅ Added related_entity_id column';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'related_entity_type')
BEGIN
    ALTER TABLE notifications ADD related_entity_type NVARCHAR(50) NULL;
    PRINT '✅ Added related_entity_type column';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'action_url')
BEGIN
    ALTER TABLE notifications ADD action_url NVARCHAR(255) NULL;
    PRINT '✅ Added action_url column';
END

-- ✅ CẬP NHẬT DỮ LIỆU MẪU CHO CÁC NOTIFICATIONS HIỆN CÓ
UPDATE notifications 
SET 
    notification_type = 'SYSTEM_ACTIVITY',
    title = 'Thông báo hệ thống',
    priority = 'NORMAL'
WHERE notification_type IS NULL OR title IS NULL OR priority IS NULL;

PRINT '✅ Updated existing notifications with default values';

-- ✅ TẠO INDEX CHO PERFORMANCE
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notifications_user_id')
BEGIN
    CREATE INDEX IX_notifications_user_id ON notifications(user_id);
    PRINT '✅ Created index on user_id';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notifications_created_at')
BEGIN
    CREATE INDEX IX_notifications_created_at ON notifications(created_at);
    PRINT '✅ Created index on created_at';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notifications_is_read')
BEGIN
    CREATE INDEX IX_notifications_is_read ON notifications(is_read);
    PRINT '✅ Created index on is_read';
END

-- ✅ INSERT DỮ LIỆU MẪU (TÙY CHỌN)
-- INSERT INTO notifications (user_id, content, notification_type, title, priority, created_at, is_read)
-- VALUES 
-- (1, 'Chào mừng bạn đến với QuizApp!', 'SYSTEM_ACTIVITY', 'Chào mừng', 'NORMAL', GETDATE(), 0),
-- (1, 'Bạn đã hoàn thành quiz Toán học cơ bản với 85 điểm', 'QUIZ_RESULT_READY', 'Kết quả Quiz', 'NORMAL', GETDATE(), 0);

PRINT '✅ Notification table updated successfully!';
PRINT '✅ Ready for WebSocket notifications!'; 