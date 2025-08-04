-- Thêm cột deleted cho soft delete
ALTER TABLE quizzes ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE quizzes ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE quizzes ADD COLUMN deleted_by BIGINT NULL;

-- Thêm index để tối ưu query
CREATE INDEX idx_quizzes_deleted ON quizzes(deleted);
CREATE INDEX idx_quizzes_deleted_at ON quizzes(deleted_at);

-- Thêm foreign key cho deleted_by (nếu cần)
-- ALTER TABLE quizzes ADD CONSTRAINT fk_quizzes_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id); 