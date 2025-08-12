-- Test Quiz Attempts Debug
-- Kiểm tra cấu trúc bảng và dữ liệu

-- 1. Kiểm tra cấu trúc bảng quiz_attempts
DESCRIBE quiz_attempts;

-- 2. Kiểm tra dữ liệu trong bảng quiz_attempts
SELECT * FROM quiz_attempts ORDER BY id DESC LIMIT 10;

-- 3. Kiểm tra các quiz có sẵn
SELECT id, title, status FROM quiz WHERE status = 'PUBLIC' LIMIT 5;

-- 4. Kiểm tra các user có sẵn
SELECT id, username, role FROM user LIMIT 5;

-- 5. Kiểm tra attempt gần nhất
SELECT 
    qa.id as attempt_id,
    qa.user_id,
    qa.quiz_id,
    qa.score,
    qa.attempted_at,
    qa.time_taken,
    u.username,
    q.title as quiz_title
FROM quiz_attempts qa
JOIN user u ON qa.user_id = u.id
JOIN quiz q ON qa.quiz_id = q.id
ORDER BY qa.id DESC
LIMIT 5;

-- 6. Kiểm tra xem có attempt nào đang trong trạng thái IN_PROGRESS không
SELECT 
    qa.id as attempt_id,
    qa.user_id,
    qa.quiz_id,
    qa.score,
    qa.attempted_at,
    qa.time_taken,
    u.username,
    q.title as quiz_title,
    CASE 
        WHEN qa.score > 0 THEN 'COMPLETED'
        WHEN qa.attempted_at IS NOT NULL THEN 'IN_PROGRESS'
        ELSE 'CREATED'
    END as calculated_status
FROM quiz_attempts qa
JOIN user u ON qa.user_id = u.id
JOIN quiz q ON qa.quiz_id = q.id
ORDER BY qa.id DESC
LIMIT 10;
