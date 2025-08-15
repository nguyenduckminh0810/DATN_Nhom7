<template>
  <div class="user-report-notifications">
    <div class="notifications-header">
      <h3>📢 Thông báo Báo cáo</h3>
      <button @click="markAllAsRead" class="mark-all-read-btn">
        Đánh dấu tất cả đã đọc
      </button>
    </div>

    <div class="notifications-list">
      <!-- Loading state -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải thông báo...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-container">
        <p class="error-message">{{ error }}</p>
        <button @click="loadNotifications" class="retry-btn">Thử lại</button>
      </div>

      <!-- Notifications list -->
      <div v-else class="notifications-grid">
        <div v-for="notification in notifications" 
             :key="notification.id" 
             :class="['notification-card', { unread: !notification.isRead }]">
          
          <div class="notification-header">
            <div class="notification-icon">
              {{ getNotificationIcon(notification.type) }}
            </div>
            <div class="notification-info">
              <h4>{{ notification.title }}</h4>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
            <div class="notification-actions">
              <button @click="markAsRead(notification.id)" 
                      v-if="!notification.isRead"
                      class="mark-read-btn">
                ✓
              </button>
            </div>
          </div>
          
          <div class="notification-content">
            <p>{{ notification.message }}</p>
          </div>
          
          <div v-if="notification.actionUrl" class="notification-footer">
            <button @click="handleNotificationClick(notification)" 
                    class="action-btn">
              Xem chi tiết
            </button>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="notifications.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <p>Không có thông báo nào</p>
        </div>
      </div>
    </div>

    <!-- Real-time notification toast -->
    <div v-if="showToast" :class="['toast-notification', toastType]">
      <div class="toast-icon">{{ getToastIcon(toastType) }}</div>
      <div class="toast-content">
        <h5>{{ toastTitle }}</h5>
        <p>{{ toastMessage }}</p>
      </div>
      <button @click="hideToast" class="toast-close">×</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import websocketService from '@/services/websocketService';

export default {
  name: 'UserReportNotification',
  setup() {
    const router = useRouter();
    const notifications = ref([]);
    const loading = ref(false);
    const error = ref('');
    
    // Toast notification state
    const showToast = ref(false);
    const toastTitle = ref('');
    const toastMessage = ref('');
    const toastType = ref('info');

    // Load notifications
    const loadNotifications = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        // ✅ SỬA ENDPOINT ĐÚNG
        const response = await axios.get('/api/notifications');
        notifications.value = response.data;
        console.log('✅ Loaded notifications:', notifications.value);
      } catch (err) {
        console.error('❌ Error loading notifications:', err);
        error.value = 'Không thể tải thông báo';
      } finally {
        loading.value = false;
      }
    };

    // Mark notification as read
    const markAsRead = async (notificationId) => {
      try {
        await axios.put(`/api/notifications/${notificationId}/read`);
        
        // Update local state
        const notification = notifications.value.find(n => n.id === notificationId);
        if (notification) {
          notification.isRead = true;
        }
        
        console.log('✅ Marked notification as read:', notificationId);
      } catch (err) {
        console.error('❌ Error marking notification as read:', err);
      }
    };

    // Mark all notifications as read
    const markAllAsRead = async () => {
      try {
        await axios.put('/api/notifications/mark-all-read');
        
        // Update local state
        notifications.value.forEach(n => n.isRead = true);
        
        console.log('✅ Marked all notifications as read');
      } catch (err) {
        console.error('❌ Error marking all notifications as read:', err);
      }
    };

    // Handle notification click
    const handleNotificationClick = (notification) => {
      // Mark as read first
      if (!notification.isRead) {
        markAsRead(notification.id);
      }
      
      // Navigate to action URL if available
      if (notification.actionUrl) {
        router.push(notification.actionUrl);
      }
    };

    // Show toast notification
    const showToastNotification = (notification) => {
      toastTitle.value = notification.title;
      toastMessage.value = notification.message;
      toastType.value = notification.type === 'REPORT_ACTION' ? 'success' : 'info';
      showToast.value = true;
      
      // Auto-hide after 5 seconds
      setTimeout(() => {
        hideToast();
      }, 5000);
    };

    // Hide toast
    const hideToast = () => {
      showToast.value = false;
    };

    // WebSocket notification handler
    const handleWebSocketNotification = (notification) => {
      console.log('📨 Received WebSocket notification:', notification);
      
      // Add to notifications list
      notifications.value.unshift(notification);
      
      // Show toast notification
      showToastNotification(notification);
    };

    // Helper functions
    const getNotificationIcon = (type) => {
      const icons = {
        'REPORT_ACTION': '📋',
        'QUIZ_RESULT_READY': '📊',
        'QUIZ_APPROVED': '✅',
        'QUIZ_REJECTED': '❌',
        'ACCOUNT_STATUS_CHANGED': '🔒'
      };
      return icons[type] || '📢';
    };

    const getToastIcon = (type) => {
      const icons = {
        'success': '✅',
        'error': '❌',
        'warning': '⚠️',
        'info': 'ℹ️'
      };
      return icons[type] || '📢';
    };

    const formatTime = (dateString) => {
      const date = new Date(dateString);
      const now = new Date();
      const diffInMinutes = Math.floor((now - date) / (1000 * 60));
      
      if (diffInMinutes < 1) return 'Vừa xong';
      if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
      if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)} giờ trước`;
      return date.toLocaleDateString('vi-VN');
    };

    onMounted(() => {
      loadNotifications();
      
      // ✅ CONNECT TO WEBSOCKET VỚI ERROR HANDLING
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      
      if (token && username) {
        console.log('🔌 Connecting to WebSocket for user:', username);
        websocketService.connect(username, token)
          .then(() => {
            console.log('✅ WebSocket connected successfully');
            websocketService.onNotification(handleWebSocketNotification);
          })
          .catch((error) => {
            console.error('❌ WebSocket connection failed:', error);
          });
      } else {
        console.error('❌ Missing token or username for WebSocket connection');
      }
    });

    onUnmounted(() => {
      // Cleanup WebSocket
      websocketService.removeNotificationCallback(handleWebSocketNotification);
    });

    return {
      notifications,
      loading,
      error,
      showToast,
      toastTitle,
      toastMessage,
      toastType,
      loadNotifications,
      markAsRead,
      markAllAsRead,
      handleNotificationClick,
      showToastNotification,
      hideToast,
      getNotificationIcon,
      getToastIcon,
      formatTime
    };
  }
};
</script>

<style scoped>
.user-report-notifications {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notifications-header h3 {
  margin: 0;
  color: #333;
}

.mark-all-read-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.mark-all-read-btn:hover {
  background: #0056b3;
}

.notifications-grid {
  display: grid;
  gap: 15px;
}

.notification-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.notification-card.unread {
  border-left: 4px solid #007bff;
  background: #f8f9fa;
}

.notification-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
}

.notification-icon {
  font-size: 24px;
  margin-right: 12px;
  flex-shrink: 0;
}

.notification-info {
  flex: 1;
}

.notification-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #666;
}

.notification-actions {
  margin-left: 10px;
}

.mark-read-btn {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mark-read-btn:hover {
  background: #218838;
}

.notification-content {
  margin-bottom: 10px;
}

.notification-content p {
  margin: 0;
  color: #555;
  line-height: 1.4;
}

.notification-footer {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  padding: 6px 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.action-btn:hover {
  background: #0056b3;
}

.loading-container, .error-container, .empty-state {
  text-align: center;
  padding: 40px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #dc3545;
  margin-bottom: 15px;
}

.retry-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

/* Toast notification */
.toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1000;
  max-width: 400px;
  animation: slideInRight 0.3s ease;
}

.toast-notification.success {
  background: #d4edda;
  border-left: 4px solid #28a745;
  color: #155724;
}

.toast-notification.error {
  background: #f8d7da;
  border-left: 4px solid #dc3545;
  color: #721c24;
}

.toast-notification.warning {
  background: #fff3cd;
  border-left: 4px solid #ffc107;
  color: #856404;
}

.toast-notification.info {
  background: #d1ecf1;
  border-left: 4px solid #17a2b8;
  color: #0c5460;
}

.toast-icon {
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.toast-content {
  flex: 1;
}

.toast-content h5 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: bold;
}

.toast-content p {
  margin: 0;
  font-size: 12px;
  line-height: 1.3;
}

.toast-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: inherit;
  margin-left: 10px;
  opacity: 0.7;
}

.toast-close:hover {
  opacity: 1;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
