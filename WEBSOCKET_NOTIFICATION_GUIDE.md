# 🚀 **WEBSOCKET NOTIFICATION SYSTEM GUIDE**

## 📋 **TỔNG QUAN**

Hệ thống notification WebSocket đã được tích hợp vào dự án QuizApp với các tính năng:

- ✅ **Real-time notifications** qua WebSocket
- ✅ **Database storage** cho notifications
- ✅ **User-specific notifications** 
- ✅ **Admin notifications** cho quản lý
- ✅ **Frontend component** với UI đẹp
- ✅ **Toast notifications** cho trải nghiệm tốt hơn

## 🛠️ **CÀI ĐẶT**

### **Backend Setup:**

1. **Chạy SQL Script:**
```sql
-- Chạy file update_notification_table.sql trong SQL Server Management Studio
```

2. **Build và chạy Spring Boot:**
```bash
cd DATN_Nhom7
mvn clean install
mvn spring-boot:run
```

### **Frontend Setup:**

1. **Cài đặt dependencies:**
```bash
cd frontend
npm install
```

2. **Chạy development server:**
```bash
npm run dev
```

## 📡 **WEBSOCKET ENDPOINTS**

### **Connection:**
```
ws://localhost:8080/ws
```

### **User Notifications:**
```
/user/{username}/queue/notifications
```

### **Topics (cho broadcast):**
```
/topic/notifications
```

## 🔔 **NOTIFICATION TYPES**

### **Cho USER:**
- `QUIZ_RESULT_READY` - Kết quả quiz đã sẵn sàng
- `QUIZ_APPROVED` - Quiz được phê duyệt  
- `QUIZ_REJECTED` - Quiz bị từ chối
- `ACCOUNT_STATUS_CHANGED` - Tài khoản bị ban/unban
- `REVIEW_RECEIVED` - Nhận được đánh giá mới

### **Cho ADMIN:**
- `NEW_QUIZ_SUBMITTED` - Quiz mới cần phê duyệt
- `QUIZ_COMPLETED` - User hoàn thành quiz
- `NEW_USER_REGISTERED` - User mới đăng ký
- `REPORT_SUBMITTED` - Báo cáo mới
- `SYSTEM_ACTIVITY` - Hoạt động hệ thống

## 🎯 **PRIORITY LEVELS**

- `LOW` - Thông báo ít quan trọng
- `NORMAL` - Thông báo thường
- `HIGH` - Thông báo quan trọng
- `URGENT` - Thông báo khẩn cấp

## 📱 **FRONTEND INTEGRATION**

### **1. Import NotificationComponent:**
```javascript
import NotificationComponent from '@/components/NotificationComponent.vue'
```

### **2. Sử dụng trong layout:**
```vue
<template>
  <div class="layout">
    <header>
      <nav>
        <!-- Other nav items -->
        <NotificationComponent />
      </nav>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>
```

### **3. WebSocket Service:**
```javascript
import websocketService from '@/services/websocketService.js'

// Kết nối WebSocket
websocketService.connect(username, token)

// Lắng nghe notifications
websocketService.onNotification((notification) => {
  console.log('New notification:', notification)
})
```

## 🔧 **API ENDPOINTS**

### **GET /api/notifications**
- Lấy tất cả notifications của user hiện tại
- Cần authentication

### **GET /api/notifications/unread**
- Lấy notifications chưa đọc
- Cần authentication

### **GET /api/notifications/unread/count**
- Đếm số notifications chưa đọc
- Cần authentication

### **PUT /api/notifications/{id}/read**
- Đánh dấu notification đã đọc
- Cần authentication

### **PUT /api/notifications/read-all**
- Đánh dấu tất cả notifications đã đọc
- Cần authentication

### **DELETE /api/notifications/{id}**
- Xóa notification
- Cần authentication

## 🎨 **NOTIFICATION PAYLOAD**

```json
{
  "id": 123,
  "type": "QUIZ_RESULT_READY",
  "userId": 456,
  "title": "Kết quả Quiz Toán học",
  "message": "Bạn đã đạt 85 điểm trong quiz Toán học cơ bản",
  "priority": "NORMAL",
  "relatedEntityId": 789,
  "relatedEntityType": "QUIZ",
  "actionUrl": "/quiz/result/789",
  "isRead": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

## 🔄 **TRIGGER POINTS**

### **Quiz Completion:**
- User hoàn thành quiz → Notification cho user + admin
- Location: `ResultService.evaluateAndSave()`

### **User Ban/Unban:**
- Admin ban user → Notification cho user
- Location: `AdminController.banUser()`

### **Quiz Approval:**
- Admin approve quiz → Notification cho creator
- Location: `AdminController` (cần thêm)

### **New User Registration:**
- User đăng ký → Notification cho admin
- Location: `UserController.registerUser()` (cần thêm)

## 🎯 **TESTING**

### **1. Test WebSocket Connection:**
```javascript
// Trong browser console
const socket = new SockJS('http://localhost:8080/ws')
const stomp = Stomp.over(socket)
stomp.connect({}, (frame) => {
  console.log('Connected:', frame)
})
```

### **2. Test Notification API:**
```bash
# Lấy notifications
curl -H "Authorization: Bearer {token}" http://localhost:8080/api/notifications

# Đánh dấu đã đọc
curl -X PUT -H "Authorization: Bearer {token}" http://localhost:8080/api/notifications/1/read
```

### **3. Test Quiz Completion:**
1. Hoàn thành một quiz
2. Kiểm tra notification trong database
3. Kiểm tra WebSocket message

## 🚨 **TROUBLESHOOTING**

### **WebSocket không kết nối:**
- Kiểm tra CORS configuration
- Kiểm tra SecurityConfig cho `/ws/**`
- Kiểm tra JWT token

### **Notification không hiển thị:**
- Kiểm tra database connection
- Kiểm tra NotificationService logs
- Kiểm tra frontend console errors

### **Frontend không nhận notifications:**
- Kiểm tra WebSocket connection
- Kiểm tra username/token
- Kiểm tra subscription path

## 📈 **PERFORMANCE OPTIMIZATIONS**

### **Database:**
- Indexes trên `user_id`, `created_at`, `is_read`
- Pagination cho notifications
- Cleanup old notifications

### **WebSocket:**
- Connection pooling
- Heartbeat mechanism
- Reconnection logic

### **Frontend:**
- Debounced API calls
- Virtual scrolling cho large lists
- Lazy loading notifications

## 🔮 **FUTURE ENHANCEMENTS**

1. **Email Notifications** - Gửi email cho notifications quan trọng
2. **Push Notifications** - Browser push notifications
3. **Sound Effects** - Âm thanh cho notifications
4. **Notification Templates** - Templates cho different types
5. **Notification Preferences** - User có thể tùy chỉnh
6. **Bulk Actions** - Xóa/đánh dấu nhiều notifications
7. **Notification History** - Lịch sử notifications
8. **Real-time Chat** - Chat system qua WebSocket

## 📞 **SUPPORT**

Nếu có vấn đề với hệ thống notification:

1. Kiểm tra logs trong console
2. Kiểm tra database connections
3. Kiểm tra WebSocket connection
4. Kiểm tra JWT token validity
5. Kiểm tra CORS configuration

---

**🎉 Chúc mừng! Hệ thống WebSocket notification đã sẵn sàng sử dụng!** 