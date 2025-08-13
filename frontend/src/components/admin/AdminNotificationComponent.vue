<template>
  <div class="admin-notification-container">
    <!-- ✅ ADMIN NOTIFICATION BELL ICON (HIDDEN) -->
    <div class="admin-notification-bell" @click.stop="toggleNotificationPanel" style="display: none;">
      <i class="bi bi-bell"></i>
      <span v-if="notificationStore.unreadCount > 0" class="admin-notification-badge">{{ notificationStore.unreadCount }}</span>
    </div>

    <!-- ✅ ADMIN NOTIFICATION PANEL -->
    <div v-if="showPanel" class="admin-notification-panel">
      <div class="admin-notification-header">
        <h3>Thông báo Admin</h3>
        <div class="admin-notification-actions">
          <button @click.stop="markAllAsRead" class="btn-mark-all">Đánh dấu tất cả đã đọc</button>
          <button @click.stop="showPanel = false" class="btn-close">×</button>
        </div>
      </div>

      <!-- ✅ ADMIN NOTIFICATION LIST -->
      <div class="admin-notification-list">
        <div v-if="notificationStore.isLoading" class="loading-notifications">
          <div class="spinner"></div>
          <p>Đang tải thông báo...</p>
        </div>
        
        <div v-else-if="notificationStore.notifications.length === 0" class="no-notifications">
          <i class="bi bi-bell-slash"></i>
          <p>Không có thông báo nào</p>
        </div>
        
        <div v-else>
          <div v-for="notification in notificationStore.sortedNotifications" 
               :key="notification.id" 
               :class="['admin-notification-item', { 'unread': !notification.isRead }]"
               @click.stop="handleNotificationClick(notification)">
            
            <div class="admin-notification-icon">
              <i :class="getNotificationIcon(notification.type)"></i>
            </div>
            
            <div class="admin-notification-content">
              <div class="admin-notification-title">{{ notification.title }}</div>
              <div class="admin-notification-message">{{ notification.message }}</div>
              <div class="admin-notification-time">{{ formatTime(notification.createdAt) }}</div>
            </div>
            
            <div class="admin-notification-actions">
              <button @click.stop="markAsRead(notification.id)" 
                      v-if="!notification.isRead" 
                      class="btn-mark-read"
                      title="Đánh dấu đã đọc">
                <i class="bi bi-check"></i>
              </button>
              <button @click.stop="deleteNotification(notification.id)" 
                      class="btn-delete"
                      title="Xóa thông báo">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ ADMIN TOAST NOTIFICATION -->
    <div v-if="showToast" class="admin-toast-notification" :class="toastType">
      <div class="admin-toast-content">
        <i :class="getToastIcon(toastType)"></i>
        <span>{{ toastMessage }}</span>
      </div>
      <button @click="hideToast" class="admin-toast-close">×</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
// import websocketService from '../../services/websocketService.js';
import { useNotificationStore } from '../../stores/notification.js';

export default {
  name: 'AdminNotificationComponent',
  setup() {
    const notificationStore = useNotificationStore();
    const showPanel = ref(false);
    const showToast = ref(false);
    const toastMessage = ref('');
    const toastType = ref('info');

    // ✅ TOGGLE PANEL
    const toggleNotificationPanel = () => {
      showPanel.value = !showPanel.value;
      if (showPanel.value) {
        notificationStore.loadNotifications();
      }
    };

    // ✅ MARK AS READ
    const markAsRead = async (notificationId) => {
      await notificationStore.markAsRead(notificationId);
    };

    // ✅ MARK ALL AS READ
    const markAllAsRead = async () => {
      await notificationStore.markAllAsRead();
      showToastMessage('Đã đánh dấu tất cả thông báo đã đọc', 'success');
    };

    // ✅ DELETE NOTIFICATION
    const deleteNotification = async (notificationId) => {
      await notificationStore.deleteNotification(notificationId);
      showToastMessage('Đã xóa thông báo', 'success');
    };

    // ✅ HANDLE NOTIFICATION CLICK
    const handleNotificationClick = (notification) => {
      if (!notification.isRead) {
        markAsRead(notification.id);
      }
      
      // ✅ NAVIGATE TO ACTION URL IF AVAILABLE
      if (notification.actionUrl) {
        console.log('Navigate to:', notification.actionUrl);
      }
    };

    // ✅ SHOW TOAST MESSAGE
    const showToastMessage = (message, type = 'info') => {
      toastMessage.value = message;
      toastType.value = type;
      showToast.value = true;
      
      setTimeout(() => {
        hideToast();
      }, 3000);
    };

    // ✅ HIDE TOAST
    const hideToast = () => {
      showToast.value = false;
    };

    // ✅ GET NOTIFICATION ICON
    const getNotificationIcon = (type) => {
      const icons = {
        'QUIZ_RESULT_READY': 'bi bi-trophy',
        'QUIZ_APPROVED': 'bi bi-check-circle',
        'QUIZ_REJECTED': 'bi bi-x-circle',
        'ACCOUNT_STATUS_CHANGED': 'bi bi-shield-check',
        'REVIEW_RECEIVED': 'bi bi-star',
        'NEW_QUIZ_SUBMITTED': 'bi bi-plus-circle',
        'QUIZ_COMPLETED': 'bi bi-play-circle',
        'NEW_USER_REGISTERED': 'bi bi-person-plus',
        'REPORT_SUBMITTED': 'bi bi-flag',
        'SYSTEM_ACTIVITY': 'bi bi-gear'
      };
      return icons[type] || 'bi bi-bell';
    };

    // ✅ GET TOAST ICON
    const getToastIcon = (type) => {
      const icons = {
        'success': 'bi bi-check-circle',
        'error': 'bi bi-exclamation-circle',
        'warning': 'bi bi-exclamation-triangle',
        'info': 'bi bi-info-circle'
      };
      return icons[type] || 'bi bi-info-circle';
    };

    // ✅ FORMAT TIME
    const formatTime = (timestamp) => {
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now - date;
      
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 1) return 'Vừa xong';
      if (minutes < 60) return `${minutes} phút trước`;
      if (hours < 24) return `${hours} giờ trước`;
      if (days < 7) return `${days} ngày trước`;
      
      return date.toLocaleDateString('vi-VN');
    };

    // ✅ WEBSOCKET NOTIFICATION HANDLER (TẠM THỜI DISABLE)
    const handleWebSocketNotification = (notification) => {
      notificationStore.addNotification(notification);
      showToastMessage(notification.message, 'info');
    };

    // ✅ INITIALIZE WEBSOCKET (TẠM THỜI DISABLE)
    const initializeWebSocket = () => {
      // ✅ TẠM THỜI DISABLE WEBSOCKET ĐỂ TEST
      console.log('⚠️ WebSocket temporarily disabled for testing');
      /*
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      
      if (token && username) {
        websocketService.connect(username, token).then(() => {
          websocketService.onNotification(handleWebSocketNotification);
        }).catch(error => {
          console.error('❌ WebSocket connection failed:', error);
        });
      }
      */
    };

    // ✅ LIFECYCLE HOOKS
    onMounted(() => {
      notificationStore.initialize();
      initializeWebSocket();
    });

    onUnmounted(() => {
      // websocketService.disconnect();
    });

    return {
      notificationStore,
      showPanel,
      showToast,
      toastMessage,
      toastType,
      markAsRead,
      markAllAsRead,
      deleteNotification,
      handleNotificationClick,
      toggleNotificationPanel,
      showToastMessage,
      hideToast,
      getNotificationIcon,
      getToastIcon,
      formatTime
    };
  }
};
</script>

<style scoped>
.admin-notification-container {
  position: relative;
  margin-right: 1rem;
}

.admin-notification-bell {
  position: relative;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  color: var(--info-color);
  font-size: 1.2rem;
}

.admin-notification-bell:hover {
  background-color: var(--card-header-bg);
  transform: scale(1.05);
}

.admin-notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: linear-gradient(135deg, var(--danger-color), #ff3742);
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  animation: pulse 2s infinite;
}

.admin-notification-panel {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 450px;
  max-height: 600px;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 10px 25px var(--shadow-color);
  z-index: 1000;
  overflow: hidden;
}

.admin-notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, var(--info-color), #764ba2);
  color: white;
}

.admin-notification-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.admin-notification-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-mark-all {
  background: var(--card-header-bg);
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-mark-all:hover {
  background: var(--card-header-bg);
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: white;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.btn-close:hover {
  background: var(--card-header-bg);
}

.admin-notification-list {
  max-height: 500px;
  overflow-y: auto;
}

.loading-notifications {
  padding: 2rem;
  text-align: center;
  color: var(--text-secondary);
}

.spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid var(--border-color);
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.no-notifications {
  padding: 2rem;
  text-align: center;
  color: var(--text-secondary);
}

.no-notifications i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.admin-notification-item {
  display: flex;
  align-items: flex-start;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #f7fafc;
  cursor: pointer;
  transition: all 0.3s ease;
}

.admin-notification-item:hover {
  background-color: var(--bg-primary);
  transform: translateX(4px);
}

.admin-notification-item.unread {
  background: linear-gradient(135deg, #e3f2fd, #f3e5f5);
  border-left: 4px solid #667eea;
}

.admin-notification-icon {
  margin-right: 1rem;
  color: #667eea;
  font-size: 1.2rem;
  width: 24px;
  text-align: center;
  margin-top: 0.25rem;
}

.admin-notification-content {
  flex: 1;
  min-width: 0;
}

.admin-notification-title {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  color: #2d3748;
}

.admin-notification-message {
  font-size: 0.85rem;
  color: #718096;
  margin-bottom: 0.25rem;
  line-height: 1.4;
}

.admin-notification-time {
  font-size: 0.75rem;
  color: #a0aec0;
}

.admin-notification-actions {
  display: flex;
  gap: 0.25rem;
  margin-left: 0.5rem;
}

.btn-mark-read,
.btn-delete {
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-mark-read {
  color: #38a169;
}

.btn-mark-read:hover {
  background-color: rgba(56, 161, 105, 0.1);
}

.btn-delete {
  color: #e53e3e;
}

.btn-delete:hover {
  background-color: rgba(229, 62, 62, 0.1);
}

.admin-toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  z-index: 10000;
  animation: slideIn 0.3s ease;
  min-width: 300px;
}

.admin-toast-notification.success {
  border-left: 4px solid #38a169;
}

.admin-toast-notification.error {
  border-left: 4px solid #e53e3e;
}

.admin-toast-notification.warning {
  border-left: 4px solid #d69e2e;
}

.admin-toast-notification.info {
  border-left: 4px solid #3182ce;
}

.admin-toast-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
}

.admin-toast-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #718096;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.admin-toast-close:hover {
  background-color: #f7fafc;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0% {
    box-shadow: 0 2px 8px rgba(255, 71, 87, 0.4);
  }
  50% {
    box-shadow: 0 4px 16px rgba(255, 71, 87, 0.6);
    transform: scale(1.05);
  }
  100% {
    box-shadow: 0 2px 8px rgba(255, 71, 87, 0.4);
  }
}
</style> 