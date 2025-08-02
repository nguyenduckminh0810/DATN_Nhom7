-- Script sửa table reports để thêm column reported_user_id
-- Chạy script này trong SQL Server Management Studio

-- Bước 1: Thêm column reported_user_id với giá trị mặc định
ALTER TABLE reports 
ADD reported_user_id BIGINT NULL;

-- Bước 2: Cập nhật giá trị cho các record hiện tại (nếu có)
-- Giả sử reported_user_id = user_id cho các record hiện tại
UPDATE reports 
SET reported_user_id = user_id 
WHERE reported_user_id IS NULL;

-- Bước 3: Thay đổi column thành NOT NULL
ALTER TABLE reports 
ALTER COLUMN reported_user_id BIGINT NOT NULL;

-- Bước 4: Thêm foreign key constraint
ALTER TABLE reports 
ADD CONSTRAINT FK_reports_reported_user 
FOREIGN KEY (reported_user_id) REFERENCES users(id);

-- Bước 5: Kiểm tra kết quả
SELECT '=== SAU KHI SỬA TABLE REPORTS ===' as info;
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'reports' 
ORDER BY ORDINAL_POSITION;

PRINT '✅ Đã sửa table reports thành công!';
PRINT '📝 Column reported_user_id đã được thêm với foreign key constraint'; 