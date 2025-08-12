<template>
  <div class="notification-container">
    <div class="notification-bell" @click.stop="toggleNotificationPanel">
      <i class="fas fa-bell"></i>
      <span v-if="notificationStore.unreadCount > 0" class="notification-badge">{{ notificationStore.unreadCount }}</span>
    </div>

    <Teleport to="body">
      <div v-if="showPanel">
        <div class="notif-backdrop" @click="showPanel = false"></div>
        <div class="notification-panel">
          <div class="notification-header">
            <h3>Thông báo</h3>
            <div class="notification-actions">
              <button @click.stop="markAllAsRead" class="btn-mark-all">Đánh dấu tất cả đã đọc</button>
              <button @click.stop="showPanel = false" class="btn-close">×</button>
            </div>
          </div>

          <div class="notification-list">
            <div v-if="notificationStore.isLoading" class="loading-notifications">
              <div class="spinner"></div>
              <p>Đang tải thông báo...</p>
            </div>

            <div v-else-if="notificationStore.notifications.length === 0" class="no-notifications">
              <div class="no-notifications-content">
                <i class="fas fa-bell-slash"></i>
                <p>Không có thông báo nào</p>
                <small>Bạn sẽ nhận được thông báo khi có hoạt động mới</small>
              </div>
            </div>

            <div v-else>
              <div
                v-for="notification in notificationStore.sortedNotifications"
                :key="notification.id"
                :class="['notification-item', { unread: !notification.isRead }]"
                @click.stop="handleNotificationClick(notification)"
              >
                <div class="notification-icon">
                  <i :class="getNotificationIcon(notification.type)"></i>
                </div>

                <div class="notification-content">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-message">{{ notification.message }}</div>
                  <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
                </div>

                <div class="notification-actions">
                  <button v-if="!notification.isRead" @click.stop="markAsRead(notification.id)" class="btn-mark-read">
                    <i class="fas fa-check"></i>
                  </button>
                  <button @click.stop="deleteNotification(notification.id)" class="btn-delete">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </Teleport>

    <div v-if="showToast" class="toast-notification" :class="toastType">
      <div class="toast-content">
        <i :class="getToastIcon(toastType)"></i>
        <span>{{ toastMessage }}</span>
      </div>
      <button @click="hideToast" class="toast-close">×</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import websocketService from '../services/websocketService.js';
import { useNotificationStore } from '../stores/notification.js';

export default {
  name: 'NotificationComponent',
  setup() {
    const notificationStore = useNotificationStore();
    const showPanel = ref(false);
    const showToast = ref(false);
    const toastMessage = ref('');
    const toastType = ref('info');

    // ✅ LOAD NOTIFICATIONS
    const loadNotifications = async () => {
      await notificationStore.loadNotifications();
    };

    // ✅ LOAD UNREAD COUNT
    const loadUnreadCount = async () => {
      await notificationStore.loadUnreadCount();
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
        // Use Vue Router to navigate
        // this.$router.push(notification.actionUrl);
        console.log('Navigate to:', notification.actionUrl);
      }
    };

    // ✅ TOGGLE PANEL
    const toggleNotificationPanel = () => {
      // Đảm bảo panel luôn hiển thị sau khi dropdown đóng hẳn
      requestAnimationFrame(() => {
        showPanel.value = !showPanel.value;
        if (showPanel.value) {
          loadNotifications();
        }
      });
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
        'QUIZ_RESULT_READY': 'fas fa-trophy',
        'QUIZ_APPROVED': 'fas fa-check-circle',
        'QUIZ_REJECTED': 'fas fa-times-circle',
        'ACCOUNT_STATUS_CHANGED': 'fas fa-user-shield',
        'REVIEW_RECEIVED': 'fas fa-star',
        'NEW_QUIZ_SUBMITTED': 'fas fa-plus-circle',
        'QUIZ_COMPLETED': 'fas fa-play-circle',
        'NEW_USER_REGISTERED': 'fas fa-user-plus',
        'REPORT_SUBMITTED': 'fas fa-flag',
        'SYSTEM_ACTIVITY': 'fas fa-cog'
      };
      return icons[type] || 'fas fa-bell';
    };

    // ✅ GET TOAST ICON
    const getToastIcon = (type) => {
      const icons = {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-circle',
        'warning': 'fas fa-exclamation-triangle',
        'info': 'fas fa-info-circle'
      };
      return icons[type] || 'fas fa-info-circle';
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

    // ✅ WEBSOCKET NOTIFICATION HANDLER
    const handleWebSocketNotification = (notification) => {
      // ✅ ADD NEW NOTIFICATION TO LIST
      notifications.value.unshift(notification);
      updateUnreadCount();
      
      // ✅ SHOW TOAST
      showToastMessage(notification.message, 'info');
      
      // ✅ PLAY SOUND (OPTIONAL)
      // playNotificationSound();
    };

    // ✅ INITIALIZE WEBSOCKET
    const initializeWebSocket = () => {
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      
      if (token && username) {
        websocketService.connect(username, token).then(() => {
          websocketService.onNotification(handleWebSocketNotification);
        }).catch(error => {
          console.error('❌ WebSocket connection failed:', error);
        });
      }
    };

    // ✅ LIFECYCLE HOOKS
    onMounted(() => {
      notificationStore.initialize();
      initializeWebSocket();
    });

    onUnmounted(() => {
      websocketService.disconnect();
    });

    return {
      notificationStore,
      showPanel,
      showToast,
      toastMessage,
      toastType,
      loadNotifications,
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
.notification-container {
  position: relative;
}

.notif-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.15);
  backdrop-filter: blur(2px);
  z-index: 1050;
}

.notification-bell {
  position: relative;
  cursor: pointer;
  padding: 10px;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-bell:hover {
  background-color: rgba(102, 126, 234, 0.1);
  transform: scale(1.05);
}

.notification-bell i {
  font-size: 18px;
  color: #667eea;
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: linear-gradient(135deg, #ff4757 0%, #ff3742 100%);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 4px 16px rgba(255, 71, 87, 0.5);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
  }
}

.notification-panel {
  position: fixed;
  top: 88px; /* below navbar */
  right: 24px;
  width: 420px;
  max-height: 520px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  z-index: 1101;
  overflow: hidden;
  backdrop-filter: blur(16px);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--card-header-bg);
  color: var(--card-header-text);
}

.notification-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--card-header-text);
}

.notification-actions {
  display: flex;
  gap: 8px;
}

.btn-mark-all {
  background: transparent;
  color: var(--card-header-text);
  border: 1px solid var(--border-color);
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-mark-all:hover {
  background: rgba(0,0,0,0.04);
}

.btn-close {
  background: transparent;
  border: 1px solid var(--border-color);
  font-size: 16px;
  cursor: pointer;
  color: var(--card-header-text);
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.btn-close:hover {
  background: rgba(0,0,0,0.06);
}

.notification-list {
  max-height: 440px;
  overflow-y: auto;
}

.notification-list::-webkit-scrollbar {
  width: 8px;
}
.notification-list::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.15);
  border-radius: 6px;
}

.no-notifications {
  padding: 32px;
  text-align: center;
  color: #666;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
  position: relative;
  background: transparent;
}

.notification-item:hover {
  background: rgba(0,0,0,0.03);
}

.notification-item.unread {
  background: rgba(102, 126, 234, 0.08);
  border-left: 4px solid #667eea;
}

.notification-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.notification-icon {
  color: #667eea;
  font-size: 18px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.12);
  border-radius: 12px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 700;
  font-size: 14px;
  margin-bottom: 4px;
  color: var(--text-primary);
  line-height: 1.35;
}

.notification-message {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

.loading-notifications {
  padding: 20px;
  text-align: center;
  color: #666;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e0e0e0;
  border-top: 2px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

.no-notifications {
  padding: 40px 20px;
  text-align: center;
  color: #666;
  background: #fafafa;
}

.no-notifications-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.no-notifications i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.3;
  color: #ccc;
}

.no-notifications p {
  margin: 0;
  font-weight: 500;
  font-size: 16px;
  color: #666;
}

.no-notifications small {
  color: #999;
  font-size: 13px;
  line-height: 1.4;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.notification-actions {
  display: flex;
  gap: 4px;
  margin-left: 8px;
}

.btn-mark-read,
.btn-delete {
  background: transparent;
  border: 1px solid var(--border-color);
  padding: 6px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-mark-read {
  color: #28a745;
}

.btn-mark-read:hover {
  background: rgba(40, 167, 69, 0.12);
  color: #28a745;
}

.btn-delete {
  color: #dc3545;
}

.btn-delete:hover {
  background: rgba(220, 53, 69, 0.12);
  color: #dc3545;
}

.toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 10000;
  animation: slideIn 0.3s ease;
}

.toast-notification.success {
  border-left: 4px solid #28a745;
}

.toast-notification.error {
  border-left: 4px solid #dc3545;
}

.toast-notification.warning {
  border-left: 4px solid #ffc107;
}

.toast-notification.info {
  border-left: 4px solid #17a2b8;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toast-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
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
</style> 