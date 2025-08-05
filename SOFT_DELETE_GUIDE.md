# Hướng dẫn Triển khai Soft Delete cho Quiz App

## Tổng quan
Tài liệu này hướng dẫn cách triển khai **Soft Delete** cho ứng dụng Quiz.

## Các thay đổi đã thực hiện

### 1. Database Schema
- **File**: `add_soft_delete_columns.sql`
- **Thay đổi**: Thêm các cột cho soft delete
  - `deleted` (BOOLEAN): Đánh dấu quiz đã bị xóa
  - `deleted_at` (TIMESTAMP): Thời gian xóa
  - `deleted_by` (BIGINT): User ID người xóa

### 2. Model Quiz
- **File**: `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/model/Quiz.java`
- **Thay đổi**: Thêm các field và getter/setter cho soft delete

### 3. Repository
- **File**: `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/repository/QuizRepo.java`
- **Thay đổi**: 
  - Cập nhật các method hiện tại để chỉ lấy quiz chưa bị xóa
  - Thêm các method mới cho soft delete

### 4. Service
- **File**: `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/service/QuizService.java`
- **Thay đổi**:
  - Thêm `softDeleteQuiz()`: Xóa mềm quiz
  - Thêm `restoreQuiz()`: Khôi phục quiz
  - Thêm `hardDeleteQuiz()`: Xóa hoàn toàn
  - Cập nhật `deleteQuiz()`: Mặc định là soft delete

### 5. Controller
- **File**: `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/controller/QuizController.java`
- **Thay đổi**:
  - Cập nhật endpoint DELETE `/{id}` để sử dụng soft delete
  - Thêm endpoint `/{id}/hard` cho hard delete
  - Thêm endpoint `/{id}/restore` cho restore
  - Thêm endpoint `/user/{userId}/deleted/paginated` cho trash

### 6. Frontend Components
- **File**: `frontend/src/components/client/QuizTrash.vue` (Mới)
  - Component hiển thị quiz đã bị xóa mềm
  - Chức năng restore và hard delete

### 7. Router
- **File**: `frontend/src/router/index.js`
- **Thay đổi**: Thêm route `/trash` cho QuizTrash component

### 8. Navigation
- **File**: `frontend/src/components/client/Navbar.vue`
- **Thay đổi**: Thêm link "Thùng rác" trong user dropdown

## Cách triển khai

### Bước 1: Cập nhật Database
```sql
-- Chạy file SQL để thêm các cột soft delete
source add_soft_delete_columns.sql;
```

### Bước 2: Restart Backend
```bash
# Dừng backend hiện tại
# Khởi động lại để áp dụng các thay đổi model
```

### Bước 3: Test chức năng
1. **Soft Delete**: Xóa quiz từ "My Quizzes" → Quiz sẽ biến mất khỏi danh sách
2. **Trash**: Vào "Thùng rác" → Xem quiz đã bị xóa
3. **Restore**: Khôi phục quiz từ thùng rác
4. **Hard Delete**: Xóa hoàn toàn quiz từ thùng rác

## API Endpoints mới

### Soft Delete
```
DELETE /api/quiz/{id}
```
- Xóa mềm quiz (chuyển vào thùng rác)

### Hard Delete
```
DELETE /api/quiz/{id}/hard
```
- Xóa hoàn toàn quiz (không thể khôi phục)

### Restore
```
POST /api/quiz/{id}/restore
```
- Khôi phục quiz từ thùng rác

### Get Deleted Quizzes
```
GET /api/quiz/user/{userId}/deleted/paginated
```
- Lấy danh sách quiz đã bị xóa mềm

## Lợi ích của Soft Delete

### 1. Bảo vệ dữ liệu
- Tránh mất dữ liệu do xóa nhầm
- Có thể khôi phục khi cần thiết

### 2. Trải nghiệm người dùng
- Giao diện thân thiện với thùng rác
- Cảnh báo rõ ràng về hành động xóa

### 3. Tuân thủ quy định
- Phù hợp với các nền tảng quiz nổi tiếng (Kahoot, Quizizz)
- Đáp ứng yêu cầu về lưu trữ dữ liệu

## Lưu ý quan trọng

### 1. Performance
- Các query cần thêm điều kiện `deleted = false`
- Index được tạo cho cột `deleted` để tối ưu

### 2. Data Integrity
- Quiz đã xóa mềm không xuất hiện trong tìm kiếm
- Quiz đã xóa mềm không thể chơi được

### 3. Security
- Chỉ owner mới có thể xóa/restore quiz
- Admin có thể xem tất cả quiz (kể cả đã xóa)

## Troubleshooting

### Lỗi thường gặp
1. **Quiz không hiển thị sau khi xóa**: Kiểm tra query có điều kiện `deleted = false`
2. **Không thể restore**: Kiểm tra quiz có tồn tại trong database không
3. **Performance chậm**: Thêm index cho cột `deleted`

### Debug
- Kiểm tra log backend để xem quá trình soft delete
- Sử dụng database client để kiểm tra trạng thái `deleted`

## Kết luận
Soft delete đã được triển khai thành công với đầy đủ chức năng:
- ✅ Xóa mềm quiz
- ✅ Khôi phục quiz
- ✅ Xóa hoàn toàn
- ✅ Giao diện thùng rác
- ✅ Tương thích ngược với code cũ

Hệ thống hiện tại đã sẵn sàng cho production với tính năng soft delete hoàn chỉnh. 