-- ✅ KIỂM TRA CẤU TRÚC BẢNG NOTIFICATIONS
-- Chạy script này để xem cấu trúc bảng notifications hiện tại

USE QuizApp;
GO

-- ✅ KIỂM TRA BẢNG CÓ TỒN TẠI KHÔNG
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'notifications')
BEGIN
    PRINT '✅ Bảng notifications tồn tại';
    
    -- ✅ HIỂN THỊ CẤU TRÚC BẢNG
    SELECT 
        COLUMN_NAME,
        DATA_TYPE,
        IS_NULLABLE,
        COLUMN_DEFAULT,
        CHARACTER_MAXIMUM_LENGTH
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'notifications'
    ORDER BY ORDINAL_POSITION;
    
    -- ✅ HIỂN THỊ SỐ LƯỢNG RECORD
    SELECT COUNT(*) as TotalNotifications FROM notifications;
    
    -- ✅ HIỂN THỊ 5 RECORD MỚI NHẤT
    SELECT TOP 5 * FROM notifications ORDER BY created_at DESC;
    
    -- ✅ KIỂM TRA INDEX
    SELECT 
        i.name as IndexName,
        i.type_desc as IndexType,
        c.name as ColumnName
    FROM sys.indexes i
    INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
    INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
    WHERE i.object_id = OBJECT_ID('notifications')
    ORDER BY i.name, ic.key_ordinal;
    
END
ELSE
BEGIN
    PRINT '❌ Bảng notifications KHÔNG tồn tại!';
    PRINT 'Vui lòng tạo bảng notifications trước khi chạy script cập nhật.';
    
    -- ✅ HIỂN THỊ DANH SÁCH CÁC BẢNG CÓ SẴN
    PRINT 'Các bảng có sẵn trong database:';
    SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';
END 