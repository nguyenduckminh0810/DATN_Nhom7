-- ✅ KIỂM TRA VÀ SỬA DATABASE

-- 1. Kiểm tra cấu trúc bảng questions
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'questions';

-- 2. Thêm cột time_limit nếu chưa có
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'questions' AND COLUMN_NAME = 'time_limit')
BEGIN
    ALTER TABLE questions ADD time_limit INT DEFAULT 30;
    PRINT 'Đã thêm cột time_limit';
END
ELSE
BEGIN
    PRINT 'Cột time_limit đã tồn tại';
END

-- 3. Cập nhật giá trị mặc định cho các record hiện tại
UPDATE questions 
SET time_limit = 30 
WHERE time_limit IS NULL;

-- 4. Kiểm tra dữ liệu
SELECT TOP 5 id, content, point, time_limit 
FROM questions;

-- 5. Kiểm tra quiz có questions không
SELECT q.id as quiz_id, q.title, COUNT(qu.id) as question_count
FROM quizzes q
LEFT JOIN questions qu ON q.id = qu.quiz_id
GROUP BY q.id, q.title
ORDER BY q.id; 