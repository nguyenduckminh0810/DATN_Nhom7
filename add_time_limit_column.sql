-- ✅ THÊM CỘT TIME_LIMIT VÀO BẢNG QUESTIONS
-- Chạy script này trong SQL Server Management Studio hoặc database tool

-- Thêm cột time_limit với giá trị mặc định 30 giây
ALTER TABLE questions 
ADD time_limit INT DEFAULT 30;

-- Cập nhật tất cả records hiện tại với giá trị mặc định
UPDATE questions 
SET time_limit = 30 
WHERE time_limit IS NULL;

-- Kiểm tra kết quả
SELECT id, content, point, time_limit 
FROM questions 
LIMIT 5; 