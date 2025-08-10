-- 🔍 Test Database Connection và Basic Queries
-- Chạy file này để kiểm tra kết nối database

-- 1. Kiểm tra kết nối cơ bản
SELECT 'Database connection test' as test_name, GETDATE() as current_datetime;

-- 2. Kiểm tra các bảng tồn tại
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE' 
AND TABLE_NAME IN ('users', 'quizzes', 'results');

-- 3. Kiểm tra dữ liệu trong bảng users
SELECT COUNT(*) as user_count FROM users;
SELECT TOP 5 id, username, full_name FROM users;

-- 4. Kiểm tra dữ liệu trong bảng quizzes
SELECT COUNT(*) as quiz_count FROM quizzes;
SELECT TOP 5 id, title FROM quizzes;

-- 5. Kiểm tra dữ liệu trong bảng results
SELECT COUNT(*) as result_count FROM results;
SELECT TOP 5 id, user_id, quiz_id, score, completed_at FROM results;

-- 6. Test query leaderboard cơ bản
SELECT TOP 10
    r.user_id,
    u.username,
    u.full_name,
    u.avatar_url,
    r.score,
    r.completed_at
FROM results r
JOIN users u ON r.user_id = u.id
ORDER BY r.score DESC;

-- 7. Test query global leaderboard (tổng điểm)
SELECT TOP 10
    r.user_id,
    u.username,
    u.full_name,
    u.avatar_url,
    SUM(r.score) as totalScore,
    COUNT(r.id) as attemptCount
FROM results r
JOIN users u ON r.user_id = u.id
WHERE r.completed_at >= DATEADD(day, -7, GETDATE()) -- Tuần này
GROUP BY r.user_id, u.username, u.full_name, u.avatar_url
ORDER BY totalScore DESC;

-- 8. Kiểm tra cấu trúc bảng results
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'results'
ORDER BY ORDINAL_POSITION;
