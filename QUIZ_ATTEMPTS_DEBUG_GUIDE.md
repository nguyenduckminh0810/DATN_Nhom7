# Hướng Dẫn Debug Lỗi Điều Hướng Quiz Attempts

## Vấn Đề Đã Được Xác Định

Khi click vào "Chơi Quiz", ứng dụng không thể vào trang chơi quiz mà lại điều hướng về `/home`. Vấn đề này xảy ra do:

1. **Navigation Guard Issue**: Route `PlayAttempt` bị chặn bởi navigation guard
2. **Status Check Problem**: `QuizAttemptService.getAttemptStatus()` trả về status không chính xác
3. **Redirect Logic**: Navigation guard redirect về `Home` thay vì cho phép vào trang chơi quiz

## Các Thay Đổi Đã Thực Hiện

### 1. Sửa QuizAttemptService.java
- **File**: `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/service/QuizAttemptService.java`
- **Thay đổi**: Sửa method `getAttemptStatus()` để trả về status chính xác
- **Trước**: Luôn trả về `"COMPLETED"`
- **Sau**: Kiểm tra logic để trả về status phù hợp:
  - `"COMPLETED"` nếu `score > 0`
  - `"IN_PROGRESS"` nếu có `attemptedAt`
  - `"CREATED"` nếu mới tạo

### 2. Cải Thiện Navigation Guard
- **File**: `frontend/src/router/index.js`
- **Thay đổi**: Cải thiện logic kiểm tra `PlayAttempt` route
- **Thêm**: Timeout handling, error handling tốt hơn, logging chi tiết

### 3. Thêm Logging vào Backend
- **File**: `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/controller/QuizAttemptController.java`
- **Thay đổi**: Thêm logging chi tiết vào các method `startAttempt` và `getAttemptStatus`

## Cách Kiểm Tra và Debug

### Bước 1: Kiểm Tra Backend Logs
1. Khởi động backend Spring Boot
2. Mở console/logs để xem các log messages
3. Thử click vào "Chơi Quiz" từ frontend
4. Quan sát logs để xem:
   - Có tạo attempt thành công không?
   - Status trả về là gì?
   - Có lỗi gì xảy ra không?

### Bước 2: Kiểm Tra Frontend Console
1. Mở Developer Tools trong browser
2. Chuyển sang tab Console
3. Thử click vào "Chơi Quiz"
4. Quan sát:
   - Navigation guard logs
   - API call logs
   - Error messages

### Bước 3: Sử Dụng Test Script
1. Copy nội dung file `test_quiz_attempts.js`
2. Mở browser console
3. Paste và chạy `runAllTests()`
4. Quan sát kết quả test

### Bước 4: Kiểm Tra Database
1. Chạy các câu SQL trong file `test_quiz_attempts.sql`
2. Kiểm tra:
   - Cấu trúc bảng `quiz_attempts`
   - Dữ liệu attempts gần nhất
   - Status của các attempts

## Các Trường Hợp Có Thể Xảy Ra

### Trường Hợp 1: Attempt được tạo nhưng status không đúng
- **Nguyên nhân**: Logic trong `getAttemptStatus()` chưa chính xác
- **Giải pháp**: Kiểm tra và sửa logic trong service

### Trường Hợp 2: Navigation guard bị lỗi
- **Nguyên nhân**: Lỗi khi gọi API hoặc xử lý response
- **Giải pháp**: Kiểm tra error handling và fallback logic

### Trường Hợp 3: Authentication issue
- **Nguyên nhân**: Token không hợp lệ hoặc hết hạn
- **Giải pháp**: Kiểm tra token và refresh nếu cần

### Trường Hợp 4: Network issue
- **Nguyên nhân**: Backend không phản hồi hoặc timeout
- **Giải pháp**: Kiểm tra backend connection và timeout settings

## Cách Khắc Phục

### Nếu vẫn còn lỗi:
1. **Kiểm tra logs**: Xem backend và frontend logs để xác định nguyên nhân cụ thể
2. **Test API trực tiếp**: Sử dụng Postman hoặc curl để test API endpoints
3. **Kiểm tra database**: Xem dữ liệu attempts có được tạo đúng không
4. **Debug step by step**: Đặt breakpoints và debug từng bước

### Nếu đã khắc phục:
1. **Test lại flow**: Thử chơi quiz từ đầu đến cuối
2. **Kiểm tra các trường hợp**: Test với quiz khác nhau, user khác nhau
3. **Monitor logs**: Quan sát logs để đảm bảo không còn lỗi

## Files Đã Sửa

1. `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/service/QuizAttemptService.java`
2. `DATN_Nhom7/src/main/java/com/nhom7/quiz/quizapp/controller/QuizAttemptController.java`
3. `frontend/src/router/index.js`

## Files Test và Debug

1. `test_quiz_attempts.sql` - SQL queries để kiểm tra database
2. `test_quiz_attempts.js` - JavaScript test script cho frontend
3. `QUIZ_ATTEMPTS_DEBUG_GUIDE.md` - Hướng dẫn này

## Lưu Ý Quan Trọng

- **Restart backend** sau khi sửa code Java
- **Clear browser cache** nếu cần thiết
- **Kiểm tra CORS** nếu có vấn đề với API calls
- **Monitor logs** để phát hiện vấn đề sớm

## Liên Hệ Hỗ Trợ

Nếu vẫn gặp vấn đề sau khi thực hiện các bước trên, hãy:
1. Cung cấp logs từ backend và frontend
2. Mô tả chi tiết các bước thực hiện
3. Cung cấp thông tin về environment (OS, browser, etc.)
