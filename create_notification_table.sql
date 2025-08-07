-- ✅ TẠO BẢNG NOTIFICATIONS (NẾU CHƯA TỒN TẠI)
-- Chạy script này để tạo bảng notifications với đầy đủ các field cần thiết

USE QuizApp;
GO

-- ✅ KIỂM TRA VÀ TẠO BẢNG NOTIFICATIONS
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'notifications')
BEGIN
    PRINT '📝 Đang tạo bảng notifications...';
    
    CREATE TABLE notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        content NVARCHAR(MAX) NOT NULL,
        notification_type NVARCHAR(50) NOT NULL DEFAULT 'SYSTEM_ACTIVITY',
        title NVARCHAR(255) NULL,
        priority NVARCHAR(20) NOT NULL DEFAULT 'NORMAL',
        related_entity_id BIGINT NULL,
        related_entity_type NVARCHAR(50) NULL,
        action_url NVARCHAR(255) NULL,
        is_read BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL
    );
    
    PRINT '✅ Bảng notifications đã được tạo thành công!';
    
    -- ✅ TẠO FOREIGN KEY (NẾU BẢNG USERS TỒN TẠI)
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'users')
    BEGIN
        BEGIN TRY
            ALTER TABLE notifications 
            ADD CONSTRAINT FK_notifications_user_id 
            FOREIGN KEY (user_id) REFERENCES users(id);
            PRINT '✅ Foreign key FK_notifications_user_id đã được tạo';
        END TRY
        BEGIN CATCH
            PRINT '⚠️ Không thể tạo foreign key: ' + ERROR_MESSAGE();
        END CATCH
    END
    ELSE
    BEGIN
        PRINT '⚠️ Bảng users không tồn tại, bỏ qua tạo foreign key';
    END
    
    -- ✅ TẠO INDEX CHO PERFORMANCE
    CREATE INDEX IX_notifications_user_id ON notifications(user_id);
    CREATE INDEX IX_notifications_created_at ON notifications(created_at);
    CREATE INDEX IX_notifications_is_read ON notifications(is_read);
    CREATE INDEX IX_notifications_type ON notifications(notification_type);
    CREATE INDEX IX_notifications_priority ON notifications(priority);
    
    PRINT '✅ Các index đã được tạo';
    
    -- ✅ INSERT DỮ LIỆU MẪU
    INSERT INTO notifications (user_id, content, notification_type, title, priority, created_at, is_read)
    VALUES 
    (1, 'Chào mừng bạn đến với QuizApp!', 'SYSTEM_ACTIVITY', 'Chào mừng', 'NORMAL', GETDATE(), 0),
    (1, 'Bạn đã hoàn thành quiz Toán học cơ bản với 85 điểm', 'QUIZ_RESULT_READY', 'Kết quả Quiz', 'NORMAL', GETDATE(), 0);
    
    PRINT '✅ Dữ liệu mẫu đã được thêm';
    
END
ELSE
BEGIN
    PRINT 'ℹ️ Bảng notifications đã tồn tại';
    PRINT 'Vui lòng chạy script update_notification_table_safe.sql để cập nhật các field mới';
END

PRINT '✅ Hoàn thành!'; 