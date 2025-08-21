# 🚀 HƯỚNG DẪN TRIỂN KHAI TÍNH NĂNG RESUME QUIZ

## 📋 **Tổng quan**
Tính năng Resume Quiz cho phép người dùng tiếp tục làm quiz từ chỗ đã dừng, với auto-save tiến độ và khôi phục đáp án đã chọn.

## 🗂️ **Cấu trúc đã triển khai**

### **Backend (Java/Spring Boot)**
- ✅ `AttemptStatus.java` - Enum trạng thái attempt
- ✅ `QuizAttemptProgress.java` - Model lưu tiến độ
- ✅ `QuizAttempt.java` - Cập nhật với status và progress
- ✅ `QuizResumeDTO.java` - DTO cho resume data
- ✅ `QuizAttemptProgressRepo.java` - Repository cho progress
- ✅ `QuizAttemptRepo.java` - Cập nhật với methods mới
- ✅ `QuizResumeService.java` - Business logic
- ✅ `QuizResumeController.java` - REST API endpoints
- ✅ `SecurityConfig.java` - Cập nhật security rules

### **Frontend (Vue.js)**
- ✅ `ResumeQuizModal.vue` - Modal hiển thị khi có attempt dở
- ✅ `quizResumeService.js` - Service gọi API
- ✅ `progressStorageService` - Local storage backup
- ✅ `autoSaveService` - Auto-save định kỳ

### **Database**
- ✅ `add_resume_quiz_features.sql` - Script cập nhật database

## 🚀 **Các bước triển khai**

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

# Build production
npm run build

# Chạy development
npm run dev
```

## 🔧 **API Endpoints**

### **1. Kiểm tra attempt dở**
```
GET /api/quiz-resume/check/{quizId}
```
**Response:**
```json
{
  "hasInProgressAttempt": true,
  "attemptData": {
    "attemptId": 123,
    "quizId": 456,
    "quizTitle": "Quiz Title",
    "currentQuestionIndex": 2,
    "timeRemaining": 1800,
    "startedAt": "2024-01-01T10:00:00",
    "lastSavedAt": "2024-01-01T10:30:00"
  }
}
```

### **2. Resume attempt**
```
GET /api/quiz-resume/resume/{attemptId}
```

### **3. Lưu tiến độ**
```
POST /api/quiz-resume/save-progress/{attemptId}
```
**Body:**
```json
{
  "questionIndex": 3,
  "timeRemaining": 1500,
  "answers": {
    "1": "A",
    "2": "B",
    "3": "C"
  }
}
```

### **4. Tạo attempt mới**
```
POST /api/quiz-resume/new-attempt/{quizId}
```

### **5. Lấy đáp án**
```
GET /api/quiz-resume/answers/{attemptId}
```

## 🎯 **Luồng hoạt động**

### **1. User vào quiz**
```
User vào quiz → Kiểm tra attempt dở → Hiện modal (nếu có)
```

### **2. Modal Resume**
```
Modal hiển thị → User chọn:
- Tiếp tục → Khôi phục tiến độ
- Làm lại → Tạo attempt mới
```

### **3. Auto-save**
```
Mỗi 30 giây → Lưu tiến độ lên server + localStorage
```

### **4. Khôi phục**
```
Resume → Lấy đáp án đã chọn → Đặt câu hiện tại → Khôi phục thời gian
```

## 🔒 **Bảo mật**

- ✅ **User Isolation**: Mỗi user chỉ thấy attempt của mình
- ✅ **Idempotent Operations**: API calls an toàn khi gọi nhiều lần
- ✅ **Input Validation**: Kiểm tra dữ liệu đầu vào
- ✅ **SQL Injection Protection**: Sử dụng JPA Repository

## 📱 **Frontend Integration**

### **1. Import Modal vào component chính**
```vue
<template>
  <div>
    <!-- Quiz content -->
    
    <!-- Resume Modal -->
    <ResumeQuizModal
      v-if="showResumeModal"
      :quiz-id="quizId"
      :attempt-data="attemptData"
      @resume="handleResume"
      @new-attempt="handleNewAttempt"
    />
  </div>
</template>

<script>
import ResumeQuizModal from './ResumeQuizModal.vue'

export default {
  components: {
    ResumeQuizModal
  }
}
</script>
```

### **2. Kiểm tra attempt dở khi vào quiz**
```javascript
// Trong component chính
async checkInProgressAttempt() {
  try {
    const response = await quizResumeService.checkInProgressAttempt(this.quizId)
    
    if (response.hasInProgressAttempt) {
      this.attemptData = response.attemptData
      this.showResumeModal = true
    } else {
      // Vào quiz bình thường
      this.startNewQuiz()
    }
  } catch (error) {
    console.error('Lỗi kiểm tra attempt:', error)
  }
}
```

### **3. Xử lý resume**
```javascript
handleResume(resumeData) {
  // Khôi phục tiến độ
  this.currentQuestionIndex = resumeData.currentQuestionIndex
  this.timeRemaining = resumeData.timeRemaining
  this.answers = JSON.parse(resumeData.answersJson)
  
  // Bắt đầu auto-save
  this.startAutoSave()
  
  // Chuyển đến câu hiện tại
  this.goToQuestion(resumeData.currentQuestionIndex)
}
```

## 🧪 **Testing**

### **1. Test Backend**
```bash
# Chạy unit tests
mvn test

# Test API endpoints
curl -X GET "http://localhost:8080/api/quiz-resume/check/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **2. Test Frontend**
```bash
# Chạy unit tests
npm run test:unit

# Chạy linting
npm run lint
```

## 🐛 **Troubleshooting**

### **1. Lỗi Database**
- Kiểm tra script SQL đã chạy thành công
- Kiểm tra foreign key constraints
- Kiểm tra index đã được tạo

### **2. Lỗi API**
- Kiểm tra JWT token
- Kiểm tra user permissions
- Kiểm tra request/response format

### **3. Lỗi Frontend**
- Kiểm tra console errors
- Kiểm tra network requests
- Kiểm tra localStorage

## 📈 **Performance Optimization**

- ✅ **Database Indexes**: Index cho các cột thường query
- ✅ **Lazy Loading**: Progress chỉ load khi cần
- ✅ **Auto-save Interval**: 30 giây thay vì real-time
- ✅ **Local Storage**: Backup dữ liệu offline

## 🔮 **Tính năng mở rộng**

### **1. Time Limit Support**
- Thêm field `timeLimit` vào Quiz model
- Tính toán thời gian còn lại chính xác

### **2. Progress Analytics**
- Thống kê thời gian làm quiz
- Phân tích câu hỏi khó

### **3. Multi-device Sync**
- WebSocket real-time sync
- Conflict resolution

## 📞 **Hỗ trợ**

Nếu gặp vấn đề, hãy kiểm tra:
1. Database schema đã được cập nhật
2. Backend services đã được deploy
3. Frontend components đã được import
4. API endpoints đã được test

## ✅ **Checklist hoàn thành**

- [ ] Database script đã chạy
- [ ] Backend đã build và deploy
- [ ] Frontend đã build và deploy
- [ ] API endpoints đã test
- [ ] Modal resume đã hiển thị
- [ ] Auto-save đã hoạt động
- [ ] Resume quiz đã thành công
- [ ] Tạo attempt mới đã thành công

---

**🎉 Chúc mừng! Tính năng Resume Quiz đã được triển khai thành công!**
