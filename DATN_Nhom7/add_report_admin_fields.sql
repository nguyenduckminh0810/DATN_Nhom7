-- ✅ THÊM FIELDS MỚI CHO REPORT TABLE
-- Chạy script này để cập nhật database schema

USE QuizApp;

-- ✅ THÊM COLUMN admin_response
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'reports' AND COLUMN_NAME = 'admin_response')
BEGIN
    ALTER TABLE reports ADD admin_response TEXT;
    PRINT N'✅ Đã thêm column admin_response';
END
ELSE
BEGIN
    PRINT N'ℹ️ Column admin_response đã tồn tại';
END

-- ✅ THÊM COLUMN resolved_at
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'reports' AND COLUMN_NAME = 'resolved_at')
BEGIN
    ALTER TABLE reports ADD resolved_at DATETIME2;
    PRINT N'✅ Đã thêm column resolved_at';
END
ELSE
BEGIN
    PRINT N'ℹ️ Column resolved_at đã tồn tại';
END

-- ✅ THÊM COLUMN resolved_by
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'reports' AND COLUMN_NAME = 'resolved_by')
BEGIN
    ALTER TABLE reports ADD resolved_by BIGINT;
    PRINT N'✅ Đã thêm column resolved_by';
END
ELSE
BEGIN
    PRINT N'ℹ️ Column resolved_by đã tồn tại';
END

-- ✅ THÊM FOREIGN KEY CHO resolved_by
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
               WHERE TABLE_NAME = 'reports' AND COLUMN_NAME = 'resolved_by' AND CONSTRAINT_NAME LIKE '%FK%')
BEGIN
    ALTER TABLE reports ADD CONSTRAINT FK_reports_resolved_by 
    FOREIGN KEY (resolved_by) REFERENCES users(id);
    PRINT N'✅ Đã thêm foreign key cho resolved_by';
END
ELSE
BEGIN
    PRINT N'ℹ️ Foreign key cho resolved_by đã tồn tại';
END

PRINT N'🎉 Hoàn thành cập nhật database schema cho reports!';
