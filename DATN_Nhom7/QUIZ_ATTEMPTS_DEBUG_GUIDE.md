# Hướng Dẫn Debug Quiz Attempts API

## Vấn đề hiện tại

Bảng xếp hạng (recent attempts) không hiển thị trong `QuizDetailModal.vue` mặc dù API `/api/quiz-attempts/public/recent/{quizId}` đã hoạt động thành công.

## Nguyên nhân đã xác định ✅

**Vấn đề chính:** Bảng xếp hạng chỉ hiển thị cho người tạo quiz (`isQuizCreator = true`), không phải cho tất cả người dùng.

**Vấn đề mới phát hiện:** Component `Leaderboard` nhận `quizId` là `undefined` do lỗi trong template.

**Vấn đề mới nhất:** Component `Leaderboard` xử lý response sai - backend trả về `List<LeaderboardEntry>` trực tiếp nhưng frontend vẫn kiểm tra `res.data.success`.

**Chi tiết:**

- API `/api/quiz-attempts/public/recent/6` trả về dữ liệu thành công: `[{…}]`
- `recentAttempts.value` được cập nhật đúng từ API
- Nhưng template chỉ hiển thị khi `isQuizCreator = true`
- Người dùng hiện tại (ID: 6) không phải là người tạo quiz (ID: 1)
- Component `Leaderboard` gọi API với `quizId = undefined` → lỗi 500
- Component `Leaderboard` xử lý response sai → không hiển thị dữ liệu

## Các thay đổi đã thực hiện ✅

### 1. Backend - QuizAttempt Model

- ✅ Đã xóa field `status` và `AttemptStatus` enum khỏi `QuizAttempt.java`
- ✅ Đã cập nhật `QuizAttemptService.java` để xử lý không có status field
- ✅ Đã sửa `QuizAttemptController.java` để không sử dụng status field

### 2. Frontend - QuizDetailModal.vue

- ✅ Đã thay đổi logic hiển thị recent attempts từ `v-if="recentAttempts.length > 0 && isQuizCreator"` thành `v-if="recentAttempts.length > 0"`
- ✅ Đã thay đổi empty state message để phù hợp với tất cả người dùng
- ✅ Đã xóa thông báo "Thông tin riêng tư" không còn cần thiết
- ✅ Đã sửa template để sử dụng `:quizId="props.quizId"` thay vì `:quizId="quizId"`

### 3. Frontend - Leaderboard.vue

- ✅ Đã sửa xử lý response để làm việc với `List<LeaderboardEntry>` trực tiếp từ backend
- ✅ Đã thêm logging để debug API calls
- ✅ Đã loại bỏ kiểm tra `res.data.success` không còn cần thiết

## Kết quả mong đợi

Bây giờ bảng xếp hạng (recent attempts) sẽ hiển thị cho **tất cả người dùng**, không chỉ người tạo quiz, component `Leaderboard` sẽ nhận được `quizId` hợp lệ, và xử lý response đúng cách để hiển thị dữ liệu.

## Cách kiểm tra

1. **Refresh trang** và mở lại quiz detail modal
2. **Kiểm tra console** - không còn lỗi 500 từ cả hai API và có log leaderboard API calls
3. **Kiểm tra giao diện** - cả recent attempts và leaderboard đều hiển thị với dữ liệu từ API
4. **Kiểm tra component Leaderboard** - không còn lỗi "quizId undefined" và xử lý response đúng

## Lưu ý

- API đã hoạt động bình thường và trả về dữ liệu
- Vấn đề chính là logic hiển thị trong frontend, truyền props sai, và xử lý response sai
- Bảng xếp hạng giờ đây công khai cho tất cả người dùng xem
- Component Leaderboard giờ đây nhận được quizId hợp lệ và xử lý dữ liệu đúng cách
