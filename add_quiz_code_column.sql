-- Thêm cột quiz_code cho tính năng join quiz bằng code
ALTER TABLE quizzes ADD COLUMN quiz_code VARCHAR(10) UNIQUE;

-- Tạo index để tìm kiếm nhanh theo code
CREATE INDEX idx_quizzes_code ON quizzes(quiz_code);

-- Thêm cột để theo dõi thời gian tạo code (optional)
ALTER TABLE quizzes ADD COLUMN code_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP; 