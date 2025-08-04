# Hướng dẫn Debug Quiz Preview

## 🔍 Vấn đề hiện tại

Bạn không thể xem trước câu hỏi dù là người tạo quiz.

## 🛠️ Các bước debug

### 1. Kiểm tra Backend

```bash
# Chạy test quiz detail
node test_quiz_detail.js

# Chạy test toàn bộ flow
node test_full_flow.js
```

### 2. Kiểm tra Frontend Console

1. Mở DevTools (F12)
2. Vào tab Console
3. Mở quiz detail modal
4. Tìm các log messages:
   - `🔍 Checking quiz creator:`
   - `Current user ID:`
   - `Creator ID:`
   - `Is creator:`

### 3. Kiểm tra localStorage

1. Mở DevTools (F12)
2. Vào tab Application → Local Storage
3. Kiểm tra:
   - `userId` có tồn tại không
   - `accessToken` có tồn tại không
   - `username` có tồn tại không

### 4. Kiểm tra Network Tab

1. Mở DevTools (F12)
2. Vào tab Network
3. Mở quiz detail modal
4. Tìm request `/api/quiz/detail/{id}`
5. Kiểm tra response có `creatorId` không

## 🔧 Các thay đổi đã thực hiện

### Backend:

1. ✅ Thêm `creatorId` vào `QuizDetailDTO`
2. ✅ Cập nhật constructor và getter/setter
3. ✅ Cập nhật `QuizService.getQuizDetail()` để set `creatorId`

### Frontend:

1. ✅ Cập nhật logic `isQuizCreator` với debug logs
2. ✅ Thêm kiểm tra null/undefined
3. ✅ Chuyển đổi sang number để so sánh

## 🚨 Các vấn đề có thể gặp

### 1. creatorId không được trả về

- **Nguyên nhân**: Backend chưa set `creatorId` trong DTO
- **Giải pháp**: Kiểm tra `QuizService.getQuizDetail()`

### 2. userId không tồn tại trong localStorage

- **Nguyên nhân**: Login không lưu `userId`
- **Giải pháp**: Kiểm tra `useLogin.js`

### 3. So sánh kiểu dữ liệu sai

- **Nguyên nhân**: String vs Number
- **Giải pháp**: Đã fix bằng `Number()` conversion

### 4. API endpoint không hoạt động

- **Nguyên nhân**: Security config hoặc JWT filter
- **Giải pháp**: Kiểm tra logs backend

## 📝 Test Cases

### Test Case 1: Quiz Creator

1. Login với tài khoản tạo quiz
2. Mở quiz detail modal
3. **Expected**: Thấy preview câu hỏi

### Test Case 2: Non-Creator

1. Login với tài khoản khác
2. Mở quiz detail modal của quiz không phải mình tạo
3. **Expected**: Thấy thông báo "Chỉ người tạo quiz mới có thể xem trước câu hỏi"

### Test Case 3: Play Quiz

1. Vào trang chơi quiz
2. **Expected**: Có thể chơi bình thường

## 🔍 Debug Commands

```bash
# Test backend
cd DATN_Nhom7
mvn spring-boot:run

# Test frontend
cd frontend
npm run dev

# Test API
node test_full_flow.js
```

## 📞 Nếu vẫn không hoạt động

1. **Kiểm tra console logs** trong browser
2. **Kiểm tra backend logs** trong terminal
3. **Chạy test scripts** để xác định vấn đề
4. **Kiểm tra database** xem quiz có user_id đúng không

## 🎯 Expected Results

Sau khi fix, bạn sẽ thấy:

- ✅ Quiz creator có thể xem preview câu hỏi
- ✅ Non-creator thấy thông báo thay vì preview
- ✅ Console logs hiển thị đúng thông tin
- ✅ API trả về đúng status codes
