# 🏆 Hướng dẫn Debug Leaderboard

## 🔍 Vấn đề đã xác định

Leaderboard đang gặp lỗi 500 (Internal Server Error) khi frontend gọi API. Các vấn đề chính:

1. **Endpoint thiếu**: Đã thêm endpoint chính `/api/leaderboard`
2. **Xử lý lỗi**: Đã cải thiện xử lý lỗi trong service
3. **Database connection**: Cần kiểm tra kết nối SQL Server

## 🚀 Các bước test

### 1. Test endpoint cơ bản

```bash
# Test endpoint chính
GET http://localhost:8080/api/leaderboard

# Test endpoint test
GET http://localhost:8080/api/leaderboard/test

# Test kết nối database
GET http://localhost:8080/api/leaderboard/test-db
```

### 2. Test với tham số

```bash
# Test global leaderboard
GET http://localhost:8080/api/leaderboard?type=global&period=weekly&limit=10

# Test quiz leaderboard
GET http://localhost:8080/api/leaderboard?type=quiz&quizId=1&limit=10
```

### 3. Test endpoint cụ thể

```bash
# Test global leaderboard
GET http://localhost:8080/api/leaderboard/global?period=weekly&limit=10

# Test quiz leaderboard
GET http://localhost:8080/api/leaderboard/quiz/1?limit=10
```

## 🗄️ Kiểm tra Database

### 1. Chạy SQL test

```sql
-- Chạy file test_leaderboard.sql để tạo dữ liệu mẫu
-- Kiểm tra các bảng: users, quizzes, results
```

### 2. Kiểm tra dữ liệu

```sql
-- Kiểm tra bảng results
SELECT * FROM results;

-- Kiểm tra bảng users
SELECT * FROM users;

-- Kiểm tra bảng quizzes
SELECT * FROM quizzes;
```

## 🔧 Sửa lỗi đã thực hiện

### 1. LeaderboardController.java

- ✅ Thêm endpoint chính `/api/leaderboard`
- ✅ Thêm endpoint test database `/api/leaderboard/test-db`
- ✅ Cải thiện xử lý lỗi

### 2. LeaderboardService.java

- ✅ Sửa lỗi xử lý Badge
- ✅ Thêm try-catch để xử lý lỗi từng row
- ✅ Trả về list rỗng thay vì null khi có lỗi

### 3. Xử lý Badge

- ✅ Thay thế `Badge.BadgeType` bằng string trực tiếp
- ✅ Thêm xử lý lỗi cho việc tính toán badge

## 🧪 Test Cases

### Test Case 1: Endpoint cơ bản

- **Input**: `GET /api/leaderboard`
- **Expected**: Trả về danh sách leaderboard hoặc list rỗng
- **Status**: 200 OK

### Test Case 2: Global Leaderboard

- **Input**: `GET /api/leaderboard/global?period=weekly&limit=10`
- **Expected**: Trả về top 10 user theo điểm tuần này
- **Status**: 200 OK

### Test Case 3: Quiz Leaderboard

- **Input**: `GET /api/leaderboard/quiz/1?limit=10`
- **Expected**: Trả về top 10 user cho quiz ID 1
- **Status**: 200 OK

## 🚨 Troubleshooting

### Lỗi 500 Internal Server Error

1. **Kiểm tra log Spring Boot** để xem lỗi chi tiết
2. **Kiểm tra kết nối database** SQL Server
3. **Kiểm tra dữ liệu** trong bảng results, users, quizzes

### Lỗi Database Connection

1. **Kiểm tra SQL Server** có đang chạy không
2. **Kiểm tra thông tin kết nối** trong application.properties
3. **Kiểm tra firewall** và port 1433

### Lỗi Data Mapping

1. **Kiểm tra cấu trúc bảng** có khớp với model không
2. **Kiểm tra dữ liệu** có null/empty không
3. **Kiểm tra query** trong ResultRepo

## 📝 Log Debug

### Spring Boot Log

```properties
# Thêm vào application.properties
logging.level.com.nhom7.quiz.quizapp=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Console Log

```java
// Trong LeaderboardService
System.err.println("❌ Error in getGlobalLeaderboard: " + e.getMessage());
e.printStackTrace();
```

## 🔄 Restart và Test

1. **Restart Spring Boot application**
2. **Test endpoint cơ bản** trước
3. **Test endpoint cụ thể** sau
4. **Kiểm tra log** để xem lỗi chi tiết

## 📞 Hỗ trợ

Nếu vẫn gặp lỗi, hãy:

1. **Chia sẻ log lỗi** từ console
2. **Chia sẻ response** từ API endpoint
3. **Kiểm tra database** có dữ liệu không
