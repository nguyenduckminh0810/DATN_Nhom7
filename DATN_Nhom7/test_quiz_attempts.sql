-- 🔍 Test Quiz Attempts Table và Related Data
-- Chạy file này để kiểm tra bảng quiz_attempts

-- 1. Kiểm tra bảng quiz_attempts có tồn tại không
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE' 
AND TABLE_NAME = 'quiz_attempts';

-- 2. Kiểm tra cấu trúc bảng quiz_attempts
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'quiz_attempts'
ORDER BY ORDINAL_POSITION;

-- 3. Kiểm tra dữ liệu trong bảng quiz_attempts
SELECT COUNT(*) as quiz_attempts_count FROM quiz_attempts;

-- 4. Xem 5 attempts gần nhất
SELECT TOP 5 
    id, user_id, quiz_id, score, time_taken, 
    attempted_at
FROM quiz_attempts 
ORDER BY attempted_at DESC;

-- 5. Kiểm tra attempts cho quiz ID 6 (quiz đang test)
SELECT 
    qa.id,
    qa.user_id,
    u.username,
    u.full_name,
    qa.quiz_id,
    q.title as quiz_title,
    qa.score,
    qa.time_taken,
    qa.attempted_at
FROM quiz_attempts qa
JOIN users u ON qa.user_id = u.id
JOIN quizzes q ON qa.quiz_id = q.id
WHERE qa.quiz_id = 6
ORDER BY qa.attempted_at DESC;

-- 6. So sánh với bảng results (bảng cũ)
SELECT 'quiz_attempts' as table_name, COUNT(*) as count FROM quiz_attempts
UNION ALL
SELECT 'results' as table_name, COUNT(*) as count FROM results;

-- 7. Kiểm tra xem có user nào đã làm quiz 6 chưa
SELECT DISTINCT
    u.id,
    u.username,
    u.full_name
FROM users u
JOIN quiz_attempts qa ON u.id = qa.user_id
WHERE qa.quiz_id = 6;

-- 8. Tạo sample data cho quiz_attempts nếu bảng trống
-- (Chỉ chạy nếu bảng quiz_attempts trống)
IF NOT EXISTS (SELECT 1 FROM quiz_attempts)
BEGIN
    PRINT 'Bảng quiz_attempts trống. Cần tạo sample data.';
    
    -- Lấy user đầu tiên
    DECLARE @firstUserId INT = (SELECT TOP 1 id FROM users);
    -- Lấy quiz đầu tiên
    DECLARE @firstQuizId INT = (SELECT TOP 1 id FROM quizzes);
    
    PRINT 'Sẽ tạo sample data với user ID: ' + CAST(@firstUserId AS VARCHAR) + ' và quiz ID: ' + CAST(@firstQuizId AS VARCHAR);
    
    -- Tạo sample data
    INSERT INTO quiz_attempts (user_id, quiz_id, score, time_taken, attempted_at)
    SELECT 
        u.id, 
        q.id, 
        60 + (ABS(CHECKSUM(NEWID())) % 40), -- Score 60-100
        120 + (ABS(CHECKSUM(NEWID())) % 300), -- 2-7 phút
        DATEADD(day, -(ABS(CHECKSUM(NEWID())) % 30), GETDATE()) -- Trong 30 ngày qua
    FROM users u, quizzes q
    WHERE u.id <= 3 AND q.id <= 3; -- Tạo cho 3 user đầu và 3 quiz đầu
    
    PRINT '✅ Đã tạo sample data thành công!';
END
ELSE
BEGIN
    PRINT 'Bảng quiz_attempts có dữ liệu.';
END
