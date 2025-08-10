-- Test Leaderboard với dữ liệu mẫu
-- Chạy từng phần để tránh lỗi khóa ngoại

-- 1. Kiểm tra bảng results
SELECT * FROM results;

-- 2. Kiểm tra bảng users
SELECT * FROM users;

-- 3. Kiểm tra bảng quizzes
SELECT * FROM quizzes;

-- 4. Tạo dữ liệu mẫu cho leaderboard (nếu chưa có)
-- Thêm users mẫu
IF NOT EXISTS (SELECT * FROM users WHERE username = 'user1')
BEGIN
    INSERT INTO users (username, email, password, full_name, avatar_url, bio, role, created_at) 
    VALUES ('user1', 'user1@gmail.com', '123456', N'Nguyễn Văn A', NULL, N'Sinh viên CNTT', 'USER', GETDATE());
END

IF NOT EXISTS (SELECT * FROM users WHERE username = 'user2')
BEGIN
    INSERT INTO users (username, email, password, full_name, avatar_url, bio, role, created_at) 
    VALUES ('user2', 'user2@gmail.com', '123456', N'Trần Thị B', NULL, N'Giáo viên Toán', 'USER', GETDATE());
END

IF NOT EXISTS (SELECT * FROM users WHERE username = 'user3')
BEGIN
    INSERT INTO users (username, email, password, full_name, avatar_url, bio, role, created_at) 
    VALUES ('user3', 'user3@gmail.com', '123456', N'Lê Văn C', NULL, N'Sinh viên Kinh tế', 'USER', GETDATE());
END

-- Thêm quiz mẫu
IF NOT EXISTS (SELECT * FROM quizzes WHERE title = N'Quiz Toán cơ bản')
BEGIN
    INSERT INTO quizzes (title, user_id, category_id, is_public, created_at) 
    VALUES (N'Quiz Toán cơ bản', 1, 1, 1, GETDATE());
END

-- Thêm results mẫu cho leaderboard
IF NOT EXISTS (SELECT * FROM results WHERE user_id = 1 AND quiz_id = 1)
BEGIN
    INSERT INTO results (user_id, quiz_id, score, completed_at, time_taken) 
    VALUES (1, 1, 95, GETDATE(), 120);
END

IF NOT EXISTS (SELECT * FROM results WHERE user_id = 2 AND quiz_id = 1)
BEGIN
    INSERT INTO results (user_id, quiz_id, score, completed_at, time_taken) 
    VALUES (2, 1, 88, GETDATE(), 150);
END

IF NOT EXISTS (SELECT * FROM results WHERE user_id = 3 AND quiz_id = 1)
BEGIN
    INSERT INTO results (user_id, quiz_id, score, completed_at, time_taken) 
    VALUES (3, 1, 82, GETDATE(), 180);
END

-- Thêm thêm results để test global leaderboard
IF NOT EXISTS (SELECT * FROM results WHERE user_id = 1 AND quiz_id = 2)
BEGIN
    INSERT INTO results (user_id, quiz_id, score, completed_at, time_taken) 
    VALUES (1, 2, 90, DATEADD(day, -1, GETDATE()), 90);
END

IF NOT EXISTS (SELECT * FROM results WHERE user_id = 2 AND quiz_id = 2)
BEGIN
    INSERT INTO results (user_id, quiz_id, score, completed_at, time_taken) 
    VALUES (2, 2, 85, DATEADD(day, -1, GETDATE()), 110);
END

-- 5. Test query leaderboard
-- Test global leaderboard
SELECT 
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

-- Test quiz leaderboard
SELECT 
    r.user_id,
    u.username,
    u.full_name,
    u.avatar_url,
    r.score,
    r.time_taken,
    r.completed_at
FROM results r
JOIN users u ON r.user_id = u.id
WHERE r.quiz_id = 1
ORDER BY r.score DESC, r.completed_at ASC;
