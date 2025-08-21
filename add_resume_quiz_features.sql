-- ✅ SCRIPT THÊM TÍNH NĂNG RESUME QUIZ
-- Chạy script này để cập nhật database

USE QuizApp;
GO

-- ✅ 1. Thêm cột status vào bảng quiz_attempts
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'quiz_attempts' AND COLUMN_NAME = 'status')
BEGIN
    ALTER TABLE quiz_attempts 
    ADD status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS';
    
    PRINT '✅ Đã thêm cột status vào bảng quiz_attempts';
END
ELSE
BEGIN
    PRINT 'ℹ️ Cột status đã tồn tại trong bảng quiz_attempts';
END

-- ✅ 2. Tạo bảng quiz_attempt_progress
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'quiz_attempt_progress')
BEGIN
    CREATE TABLE quiz_attempt_progress (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        attempt_id BIGINT NOT NULL,
        current_question_index INT NOT NULL DEFAULT 0,
        time_remaining INT NOT NULL,
        started_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        last_saved_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        answers_json NVARCHAR(MAX),
        is_active BIT NOT NULL DEFAULT 1,
        
        CONSTRAINT FK_quiz_attempt_progress_attempt 
        FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE
    );
    
    PRINT '✅ Đã tạo bảng quiz_attempt_progress';
END
ELSE
BEGIN
    PRINT 'ℹ️ Bảng quiz_attempt_progress đã tồn tại';
END

-- ✅ 3. Tạo index để tối ưu performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_quiz_attempt_progress_attempt_id')
BEGIN
    CREATE INDEX IX_quiz_attempt_progress_attempt_id 
    ON quiz_attempt_progress(attempt_id);
    
    PRINT '✅ Đã tạo index cho attempt_id';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_quiz_attempt_progress_user_quiz')
BEGIN
    CREATE INDEX IX_quiz_attempt_progress_user_quiz 
    ON quiz_attempt_progress(attempt_id) 
    INCLUDE (is_active, last_saved_at);
    
    PRINT '✅ Đã tạo index cho user_quiz lookup';
END

-- ✅ 4. Cập nhật dữ liệu hiện có
-- Đánh dấu tất cả attempt cũ là COMPLETED nếu có score > 0
UPDATE quiz_attempts 
SET status = 'COMPLETED' 
WHERE score > 0 AND (status IS NULL OR status = 'IN_PROGRESS');

-- Đánh dấu tất cả attempt cũ là SUBMITTED nếu có timeTaken > 0
UPDATE quiz_attempts 
SET status = 'SUBMITTED' 
WHERE timeTaken > 0 AND score = 0 AND (status IS NULL OR status = 'IN_PROGRESS');

PRINT '✅ Đã cập nhật trạng thái cho các attempt cũ';

-- ✅ 5. Tạo view để dễ dàng query
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = 'vw_quiz_attempts_with_progress')
BEGIN
    EXEC('CREATE VIEW vw_quiz_attempts_with_progress AS
    SELECT 
        qa.id,
        qa.user_id,
        qa.quiz_id,
        qa.score,
        qa.attempted_at,
        qa.time_taken,
        qa.status,
        qap.current_question_index,
        qap.time_remaining,
        qap.started_at,
        qap.last_saved_at,
        qap.answers_json,
        qap.is_active
    FROM quiz_attempts qa
    LEFT JOIN quiz_attempt_progress qap ON qa.id = qap.attempt_id');
    
    PRINT '✅ Đã tạo view vw_quiz_attempts_with_progress';
END

-- ✅ 6. Tạo stored procedure để cleanup progress không active
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_NAME = 'sp_cleanup_inactive_progress')
BEGIN
    EXEC('CREATE PROCEDURE sp_cleanup_inactive_progress
    AS
    BEGIN
        SET NOCOUNT ON;
        
        -- Xóa progress không active
        DELETE FROM quiz_attempt_progress 
        WHERE is_active = 0;
        
        -- Xóa progress cũ hơn 7 ngày
        DELETE FROM quiz_attempt_progress 
        WHERE last_saved_at < DATEADD(day, -7, GETDATE());
        
        PRINT ''Đã cleanup progress thành công'';
    END');
    
    PRINT '✅ Đã tạo stored procedure sp_cleanup_inactive_progress';
END

-- ✅ 7. Tạo trigger để tự động cập nhật last_saved_at
IF NOT EXISTS (SELECT * FROM sys.triggers WHERE name = 'tr_quiz_attempt_progress_update')
BEGIN
    EXEC('CREATE TRIGGER tr_quiz_attempt_progress_update
    ON quiz_attempt_progress
    AFTER UPDATE
    AS
    BEGIN
        SET NOCOUNT ON;
        
        UPDATE quiz_attempt_progress
        SET last_saved_at = GETDATE()
        FROM quiz_attempt_progress qap
        INNER JOIN inserted i ON qap.id = i.id;
    END');
    
    PRINT '✅ Đã tạo trigger tr_quiz_attempt_progress_update';
END

-- ✅ 8. Kiểm tra kết quả
PRINT '';
PRINT '=== KẾT QUẢ CẬP NHẬT DATABASE ===';
PRINT '';

-- Đếm số attempt theo trạng thái
SELECT 
    status,
    COUNT(*) as count
FROM quiz_attempts 
GROUP BY status
ORDER BY status;

-- Kiểm tra bảng progress
SELECT 
    COUNT(*) as total_progress,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_progress,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_progress
FROM quiz_attempt_progress;

PRINT '';
PRINT '✅ HOÀN THÀNH CẬP NHẬT DATABASE CHO TÍNH NĂNG RESUME QUIZ!';
PRINT 'Bây giờ bạn có thể sử dụng các API mới:';
PRINT '- GET /api/quiz-resume/check/{quizId}';
PRINT '- GET /api/quiz-resume/resume/{attemptId}';
PRINT '- POST /api/quiz-resume/save-progress/{attemptId}';
PRINT '- POST /api/quiz-resume/new-attempt/{quizId}';
PRINT '- GET /api/quiz-resume/answers/{attemptId}';
PRINT '- DELETE /api/quiz-resume/cleanup';
