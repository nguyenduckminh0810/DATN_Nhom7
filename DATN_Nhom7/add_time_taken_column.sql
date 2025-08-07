-- Thêm cột time_taken vào bảng results
ALTER TABLE results ADD COLUMN time_taken INT;

-- Cập nhật dữ liệu mẫu (nếu có)
UPDATE results SET time_taken = 120 WHERE time_taken IS NULL;

-- Thêm comment cho cột
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Thời gian làm quiz (giây)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'results', 
    @level2type = N'COLUMN', @level2name = N'time_taken';

-- Tạo index cho performance
CREATE INDEX IX_results_time_taken ON results(time_taken);
CREATE INDEX IX_results_quiz_time ON results(quiz_id, time_taken);
