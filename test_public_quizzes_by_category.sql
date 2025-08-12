-- Test script để kiểm tra API lấy quiz public theo category
-- Chạy các câu lệnh này để tạo dữ liệu test

-- 1. Kiểm tra bảng categories hiện có
SELECT '=== CATEGORIES ===' as info;
SELECT id, name, description FROM categories ORDER BY id;

-- 2. Kiểm tra bảng quizzes hiện có
SELECT '=== ALL QUIZZES ===' as info;
SELECT 
    q.id,
    q.title,
    q.is_public,
    q.deleted,
    c.name as category_name,
    u.full_name as creator_name,
    q.created_at
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN users u ON q.user_id = u.id
ORDER BY q.created_at DESC;

-- 3. Kiểm tra quiz public theo category
SELECT '=== PUBLIC QUIZZES BY CATEGORY ===' as info;

-- Category 1
SELECT 'Category 1:' as category_info;
SELECT 
    q.id,
    q.title,
    q.is_public,
    q.deleted,
    c.name as category_name,
    u.full_name as creator_name,
    q.created_at
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN users u ON q.user_id = u.id
WHERE q.is_public = true 
    AND (q.deleted IS NULL OR q.deleted = false)
    AND q.category_id = 1
ORDER BY q.created_at DESC;

-- Category 2
SELECT 'Category 2:' as category_info;
SELECT 
    q.id,
    q.title,
    q.is_public,
    q.deleted,
    c.name as category_name,
    u.full_name as creator_name,
    q.created_at
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN users u ON q.user_id = u.id
WHERE q.is_public = true 
    AND (q.deleted IS NULL OR q.deleted = false)
    AND q.category_id = 2
ORDER BY q.created_at DESC;

-- Category 3
SELECT 'Category 3:' as category_info;
SELECT 
    q.id,
    q.title,
    q.is_public,
    q.deleted,
    c.name as category_name,
    u.full_name as creator_name,
    q.created_at
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN users u ON q.user_id = u.id
WHERE q.is_public = true 
    AND (q.deleted IS NULL OR q.deleted = false)
    AND q.category_id = 3
ORDER BY q.created_at DESC;

-- 4. Thống kê tổng quan
SELECT '=== STATISTICS ===' as info;

-- Tổng số quiz theo category và trạng thái
SELECT 
    c.name as category_name,
    COUNT(CASE WHEN q.is_public = true AND (q.deleted IS NULL OR q.deleted = false) THEN 1 END) as public_active,
    COUNT(CASE WHEN q.is_public = false AND (q.deleted IS NULL OR q.deleted = false) THEN 1 END) as private_active,
    COUNT(CASE WHEN q.deleted = true THEN 1 END) as deleted,
    COUNT(*) as total
FROM categories c
LEFT JOIN quizzes q ON c.id = q.category_id
GROUP BY c.id, c.name
ORDER BY c.id;

-- 5. Test query tương tự như trong repository
SELECT '=== REPOSITORY QUERY TEST ===' as info;

-- Test với category ID = 1, page = 0, size = 5
SELECT 
    q.id,
    q.title,
    q.is_public,
    q.deleted,
    c.name as category_name,
    u.full_name as creator_name,
    q.created_at
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN users u ON q.user_id = u.id
WHERE q.is_public = true 
    AND (q.deleted IS NULL OR q.deleted = false)
    AND q.category_id = 1
ORDER BY q.created_at DESC
LIMIT 5 OFFSET 0;

-- Test với category ID = 1, page = 1, size = 5
SELECT 
    q.id,
    q.title,
    q.is_public,
    q.deleted,
    c.name as category_name,
    u.full_name as creator_name,
    q.created_at
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN users u ON q.user_id = u.id
WHERE q.is_public = true 
    AND (q.deleted IS NULL OR q.deleted = false)
    AND q.category_id = 1
ORDER BY q.created_at DESC
LIMIT 5 OFFSET 5;

-- 6. Kiểm tra quiz có image
SELECT '=== QUIZZES WITH IMAGES ===' as info;
SELECT 
    q.id,
    q.title,
    q.image,
    c.name as category_name,
    q.is_public,
    q.deleted
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
WHERE q.image IS NOT NULL
    AND q.is_public = true
    AND (q.deleted IS NULL OR q.deleted = false)
ORDER BY q.created_at DESC;

-- 7. Tạo dữ liệu test nếu cần (uncomment để sử dụng)
/*
-- Thêm category test nếu chưa có
INSERT INTO categories (name, description) VALUES 
('Test Category 1', 'Category để test API'),
('Test Category 2', 'Category thứ 2 để test API')
ON DUPLICATE KEY UPDATE name = name;

-- Thêm quiz test nếu cần
INSERT INTO quizzes (title, description, is_public, category_id, user_id, created_at) VALUES 
('Test Quiz Public 1', 'Quiz test công khai 1', true, 1, 1, NOW()),
('Test Quiz Public 2', 'Quiz test công khai 2', true, 1, 1, NOW()),
('Test Quiz Private 1', 'Quiz test riêng tư 1', false, 1, 1, NOW()),
('Test Quiz Public 3', 'Quiz test công khai 3', true, 2, 1, NOW())
ON DUPLICATE KEY UPDATE title = title;
*/

-- 8. Kiểm tra kết quả cuối cùng
SELECT '=== FINAL CHECK ===' as info;
SELECT 
    'Total Categories' as metric,
    COUNT(*) as value
FROM categories
UNION ALL
SELECT 
    'Total Public Active Quizzes' as metric,
    COUNT(*) as value
FROM quizzes 
WHERE is_public = true AND (deleted IS NULL OR deleted = false)
UNION ALL
SELECT 
    'Total Private Active Quizzes' as metric,
    COUNT(*) as value
FROM quizzes 
WHERE is_public = false AND (deleted IS NULL OR deleted = false)
UNION ALL
SELECT 
    'Total Deleted Quizzes' as metric,
    COUNT(*) as value
FROM quizzes 
WHERE deleted = true; 