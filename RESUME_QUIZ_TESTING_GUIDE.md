# 🧪 HƯỚNG DẪN TEST TÍNH NĂNG RESUME QUIZ

## 📋 **Tổng quan**
Hướng dẫn này giúp bạn test tính năng Resume Quiz đã được triển khai.

## 🚀 **Các bước test**

### **Bước 1: Cập nhật Database**
```sql
-- Chạy script SQL để tạo bảng và cột mới
-- File: add_resume_quiz_features.sql
```

### **Bước 2: Build và Deploy Backend**
```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### **Bước 3: Build và Deploy Frontend**
```bash
# Cài đặt dependencies
npm install

# Chạy development
npm run dev
```

## 🧪 **Test Backend API**

### **1. Test API Endpoints**
Sử dụng file `test_resume_quiz_api.js` để test các API:

```bash
# Chạy test từ browser console
node test_resume_quiz_api.js
```

### **2. Test với Postman/Insomnia**
- **GET** `/quiz-resume/check/{quizId}` - Kiểm tra attempt dở
- **POST** `/quiz-resume/new-attempt/{quizId}` - Tạo attempt mới
- **POST** `/quiz-resume/save-progress/{attemptId}` - Lưu tiến độ
- **GET** `/quiz-resume/resume/{attemptId}` - Resume attempt
- **GET** `/quiz-resume/answers/{attemptId}` - Lấy đáp án

### **3. Test Authentication**
Đảm bảo gửi JWT token trong header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

## 🎯 **Test Frontend Flow**

### **1. Test Modal Resume**
1. Vào quiz đã có attempt dở
2. Kiểm tra modal hiển thị với thông tin:
   - Tên quiz
   - Câu hiện tại
   - Thời gian còn lại
   - Nút "Tiếp tục" và "Làm lại"

### **2. Test Tiếp tục Quiz**
1. Chọn "Tiếp tục"
2. Kiểm tra:
   - Quiz load đúng câu hiện tại
   - Đáp án đã chọn được khôi phục
   - Thời gian còn lại chính xác
   - Auto-save hoạt động

### **3. Test Làm lại từ đầu**
1. Chọn "Làm lại từ đầu"
2. Kiểm tra:
   - Attempt cũ chuyển thành ABANDONED
   - Attempt mới được tạo
   - Quiz bắt đầu từ câu đầu tiên
   - Không có đáp án nào được chọn

### **4. Test Auto-save**
1. Chọn đáp án cho vài câu
2. Chuyển câu
3. Kiểm tra console logs:
   - `💾 AUTOSAVE scheduled`
   - `💾 AUTOSAVE sent`
   - `💾 AUTOSAVE ok`

## 🔍 **Kiểm tra Console Logs**

### **Expected Logs khi vào Quiz:**
```
🚀 CHECK start - Kiểm tra attempt dở cho quiz: {quizId}
🔍 CHECK ok - Response: {response}
📋 OPEN MODAL - Có attempt dở, hiển thị modal resume
```

### **Expected Logs khi Resume:**
```
🔄 CONTINUE - Resume attempt: {attemptId}
💾 AUTOSAVE scheduled - Bắt đầu auto-save mỗi 30 giây
```

### **Expected Logs khi Làm lại:**
```
🔄 RESTART - User chọn làm lại từ đầu
🆕 NEW-ATTEMPT - Tạo attempt mới cho quiz: {quizId}
✅ NEW-ATTEMPT ok - Attempt ID: {attemptId}
```

### **Expected Logs khi Auto-save:**
```
💾 AUTOSAVE scheduled - Chọn đáp án mới, lưu tiến độ sau 2 giây
💾 AUTOSAVE sent - Lưu tiến độ: {progressData}
💾 AUTOSAVE ok - Đã lưu tiến độ thành công
```

## 🐛 **Troubleshooting**

### **1. Lỗi "Cần implement cách lấy user ID"**
- Kiểm tra JWT token có hợp lệ không
- Kiểm tra user có tồn tại trong database không
- Kiểm tra UserRepo.findByUsername() hoạt động

### **2. Lỗi "Không tìm thấy attempt"**
- Kiểm tra database có bảng `quiz_attempt_progress` không
- Kiểm tra script SQL đã chạy thành công
- Kiểm tra foreign key constraints

### **3. Modal không hiển thị**
- Kiểm tra console có lỗi JavaScript không
- Kiểm tra component ResumeQuizModal đã import đúng
- Kiểm tra props và events đã truyền đúng

### **4. Auto-save không hoạt động**
- Kiểm tra console logs
- Kiểm tra API endpoint `/quiz-resume/save-progress` hoạt động
- Kiểm tra localStorage có dữ liệu không

## 📊 **Kiểm tra Database**

### **1. Kiểm tra bảng quiz_attempts**
```sql
SELECT id, user_id, quiz_id, status, score, time_taken 
FROM quiz_attempts 
WHERE user_id = {userId} AND quiz_id = {quizId}
ORDER BY attempted_at DESC;
```

### **2. Kiểm tra bảng quiz_attempt_progress**
```sql
SELECT * FROM quiz_attempt_progress 
WHERE attempt_id IN (
  SELECT id FROM quiz_attempts 
  WHERE user_id = {userId} AND quiz_id = {quizId}
);
```

### **3. Kiểm tra trạng thái attempt**
```sql
SELECT status, COUNT(*) as count
FROM quiz_attempts 
GROUP BY status;
```

## ✅ **Checklist Test**

- [ ] Database script đã chạy thành công
- [ ] Backend API endpoints hoạt động
- [ ] Frontend modal resume hiển thị
- [ ] Tiếp tục quiz khôi phục đúng tiến độ
- [ ] Làm lại từ đầu tạo attempt mới
- [ ] Auto-save hoạt động khi chọn đáp án
- [ ] Auto-save hoạt động khi chuyển câu
- [ ] Console logs hiển thị đúng
- [ ] Không có attempt mới được tạo khi click "Play"

## 🎉 **Kết quả mong đợi**

Sau khi test thành công:
1. **Click "Play"** → Chỉ chuyển trang, không tạo attempt
2. **Vào trang quiz** → Kiểm tra attempt dở trước  
3. **Có attempt dở** → Hiện modal resume
4. **Tiếp tục** → Khôi phục đúng tiến độ
5. **Làm lại** → Tạo attempt mới, attempt cũ thành ABANDONED
6. **Auto-save** → Lưu tiến độ mỗi 30 giây + khi có thay đổi

---

**🎯 Mục tiêu: Đảm bảo luồng resume quiz hoạt động mượt mà, không có attempt mới được tạo quá sớm, và auto-save thực sự hoạt động!**
