-- ✅ UPDATE NOTIFICATIONS TABLE FOR WEBSOCKET NOTIFICATIONS (FIXED VERSION)
-- Chạy script này để cập nhật bảng notifications với các field mới

USE QuizApp;
GO

-- ✅ KIỂM TRA BẢNG NOTIFICATIONS CÓ TỒN TẠI KHÔNG
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'notifications')
BEGIN
    PRINT '❌ Bảng notifications không tồn tại! Vui lòng tạo bảng trước.';
    RETURN;
END

PRINT '✅ Bảng notifications đã tồn tại, tiếp tục cập nhật...';

-- ✅ THÊM CÁC COLUMN MỚI VÀO BẢNG NOTIFICATIONS
-- Bước 1: Thêm notification_type
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'notification_type')
BEGIN
    BEGIN TRY
        ALTER TABLE notifications ADD notification_type NVARCHAR(50) NOT NULL DEFAULT 'SYSTEM_ACTIVITY';
        PRINT '✅ Added notification_type column';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi thêm notification_type column: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Column notification_type đã tồn tại';
END

-- Bước 2: Thêm title
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'title')
BEGIN
    BEGIN TRY
        ALTER TABLE notifications ADD title NVARCHAR(255) NULL;
        PRINT '✅ Added title column';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi thêm title column: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Column title đã tồn tại';
END

-- Bước 3: Thêm priority
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'priority')
BEGIN
    BEGIN TRY
        ALTER TABLE notifications ADD priority NVARCHAR(20) NOT NULL DEFAULT 'NORMAL';
        PRINT '✅ Added priority column';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi thêm priority column: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Column priority đã tồn tại';
END

-- Bước 4: Thêm related_entity_id
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'related_entity_id')
BEGIN
    BEGIN TRY
        ALTER TABLE notifications ADD related_entity_id BIGINT NULL;
        PRINT '✅ Added related_entity_id column';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi thêm related_entity_id column: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Column related_entity_id đã tồn tại';
END

-- Bước 5: Thêm related_entity_type
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'related_entity_type')
BEGIN
    BEGIN TRY
        ALTER TABLE notifications ADD related_entity_type NVARCHAR(50) NULL;
        PRINT '✅ Added related_entity_type column';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi thêm related_entity_type column: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Column related_entity_type đã tồn tại';
END

-- Bước 6: Thêm action_url
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'notifications' AND COLUMN_NAME = 'action_url')
BEGIN
    BEGIN TRY
        ALTER TABLE notifications ADD action_url NVARCHAR(255) NULL;
        PRINT '✅ Added action_url column';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi thêm action_url column: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Column action_url đã tồn tại';
END

GO

-- ✅ CẬP NHẬT DỮ LIỆU MẪU CHO CÁC NOTIFICATIONS HIỆN CÓ
-- Tách riêng phần UPDATE để tránh lỗi "Invalid column name"
BEGIN TRY
    UPDATE notifications 
    SET 
        notification_type = 'SYSTEM_ACTIVITY',
        title = 'Thông báo hệ thống',
        priority = 'NORMAL'
    WHERE notification_type IS NULL OR title IS NULL OR priority IS NULL;
    
    PRINT '✅ Updated existing notifications with default values';
END TRY
BEGIN CATCH
    PRINT '❌ Lỗi khi cập nhật dữ liệu: ' + ERROR_MESSAGE();
END CATCH

GO

-- ✅ TẠO INDEX CHO PERFORMANCE
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notifications_user_id')
BEGIN
    BEGIN TRY
        CREATE INDEX IX_notifications_user_id ON notifications(user_id);
        PRINT '✅ Created index on user_id';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi tạo index user_id: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Index IX_notifications_user_id đã tồn tại';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notifications_created_at')
BEGIN
    BEGIN TRY
        CREATE INDEX IX_notifications_created_at ON notifications(created_at);
        PRINT '✅ Created index on created_at';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi tạo index created_at: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Index IX_notifications_created_at đã tồn tại';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notifications_is_read')
BEGIN
    BEGIN TRY
        CREATE INDEX IX_notifications_is_read ON notifications(is_read);
        PRINT '✅ Created index on is_read';
    END TRY
    BEGIN CATCH
        PRINT '❌ Lỗi khi tạo index is_read: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'ℹ️ Index IX_notifications_is_read đã tồn tại';
END

PRINT '✅ Notification table updated successfully!';
PRINT '✅ Ready for WebSocket notifications!'; 