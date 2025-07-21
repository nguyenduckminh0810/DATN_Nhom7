-- Tạo dữ liệu mẫu cho hệ thống QuizApp
-- Chạy từng phần để tránh lỗi khóa ngoại

-- 1. USERS
INSERT INTO users (username, email, password, full_name, avatar_url, bio, role, created_at) VALUES
('nguyenvana', 'vana@gmail.com', '123456', N'Nguyễn Văn A', NULL, N'Sinh viên CNTT', 'USER', GETDATE()),
('tranthib', 'thib@gmail.com', '123456', N'Trần Thị B', NULL, N'Giáo viên Toán', 'USER', GETDATE()),
('admin', 'admin@gmail.com', 'admin123', N'Quản trị viên', NULL, N'Quản trị hệ thống', 'ADMIN', GETDATE());

-- 2. CATEGORIES
INSERT INTO categories (name, description, created_at) VALUES
(N'Toán học', N'Các bài quiz về toán', GETDATE()),
(N'Lịch sử', N'Các bài quiz về lịch sử', GETDATE());

-- 3. QUIZZES
-- Giả sử user_id=1, category_id=1
INSERT INTO quizzes (title, user_id, category_id, is_public, created_at) VALUES
(N'Quiz Toán cơ bản', 1, 1, 1, GETDATE()),
(N'Quiz Lịch sử Việt Nam', 2, 2, 1, GETDATE());

-- 4. QUESTIONS
-- Giả sử quiz_id=1,2
INSERT INTO questions (quiz_id, content, image_id, point) VALUES
(1, N'2 + 2 bằng bao nhiêu?', NULL, 1),
(1, N'5 x 6 bằng bao nhiêu?', NULL, 1),
(2, N'Năm nào Bác Hồ đọc Tuyên ngôn Độc lập?', NULL, 1);

-- 5. ANSWERS
-- Giả sử question_id=1,2,3
INSERT INTO answers (question_id, content, is_correct) VALUES
(1, N'4', 1), (1, N'5', 0), (1, N'3', 0),
(2, N'30', 1), (2, N'25', 0), (2, N'35', 0),
(3, N'1945', 1), (3, N'1975', 0), (3, N'1930', 0);

-- 6. TAGS
INSERT INTO tags (name, description) VALUES
(N'Cơ bản', N'Quiz dễ'),
(N'Lịch sử', N'Quiz về lịch sử');

-- 7. QUIZ_TAGS
-- quiz_id=1,2; tag_id=1,2
INSERT INTO quiz_tags (quiz_id, tag_id) VALUES
(1, 1), (2, 2);

-- 8. QUIZ_ATTEMPTS
-- user_id=1, quiz_id=1
INSERT INTO quiz_attempts (user_id, quiz_id, score, attempted_at, time_taken) VALUES
(1, 1, 2, GETDATE(), 60);

-- 9. RESULTS
-- user_id=1, quiz_id=1
INSERT INTO results (user_id, quiz_id, score, completed_at) VALUES
(1, 1, 2, GETDATE());

-- 10. COMMENTS
-- user_id=2, quiz_id=1
INSERT INTO comments (user_id, quiz_id, content, created_at) VALUES
(2, 1, N'Quiz rất hay!', GETDATE());

-- 11. NOTIFICATIONS
-- user_id=1
INSERT INTO notifications (user_id, content, is_read, created_at) VALUES
(1, N'Bạn vừa hoàn thành quiz Toán cơ bản!', 0, GETDATE());

-- 12. QUIZ_REVIEWS
-- user_id=2, quiz_id=1
INSERT INTO quiz_reviews (user_id, quiz_id, rating, review_text, created_at) VALUES
(2, 1, 5, N'Quiz tuyệt vời!', GETDATE());

-- 13. REPORTS
-- user_id=1, quiz_id=1, comment_id=1
INSERT INTO reports (user_id, quiz_id, comment_id, reason, status, created_at) VALUES
(1, 1, 1, N'Ngôn từ không phù hợp', 'PENDING', GETDATE());

-- 14. IMAGES
-- quiz_id=1
INSERT INTO images (url, question_id, quiz_id, uploaded_at) VALUES
(N'/uploads/toan.jpg', NULL, 1, GETDATE());

-- Kết thúc dữ liệu mẫu 