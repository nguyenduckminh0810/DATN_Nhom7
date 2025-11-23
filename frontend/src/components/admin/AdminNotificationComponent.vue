<template>
  <div class="admin-notification-container">
    <!-- (Giữ chuông nếu cần gọi trực tiếp sau này) -->
    <div class="admin-notification-bell" @click.stop="toggleNotificationPanel" style="display:none">
      <i class="bi bi-bell" />
      <span v-if="notificationStore.unreadCount > 0" class="admin-notification-badge">{{ notificationStore.unreadCount
        }}</span>
    </div>

    <!-- Toast -->
    <div v-if="showToast" class="admin-toast" :class="toastType">
      <div class="toast-content">
        <i :class="getToastIcon(toastType)" />
        <span>{{ toastMessage }}</span>
      </div>
      <button type="button" class="toast-close" @click="hideToast">×</button>
    </div>
  </div>

  <!-- Backdrop + Panel teleport ra body -->
  <teleport to="body">
    <div v-show="showPanel" class="notif-backdrop" @click="showPanel = false" aria-hidden="true"></div>

    <div v-show="showPanel" class="notif-panel" role="dialog" aria-modal="true" @click.stop>
      <header class="notif-header">
        <h3>Thông báo Admin</h3>
        <div class="actions">
          <button type="button" class="btn-close" @click.stop="showPanel = false">×</button>
        </div>
      </header>

      <div class="notif-list">
        <div v-if="notificationStore.isLoading" class="state">
          <div class="spinner" />
          <p>Đang tải thông báo...</p>
        </div>

        <div v-else-if="notificationStore.notifications.length === 0" class="state">
          <i class="bi bi-bell-slash" />
          <p>Không có thông báo nào</p>
        </div>

        <div v-else>
          <div v-for="n in notificationStore.sortedNotifications" :key="n.id"
            :class="['notif-item', { unread: !n.isRead }]" @click.stop="handleNotificationClick(n)">
            <div class="notif-icon"><i :class="getNotificationIcon(n.type || n.notificationType)" /></div>

            <div class="notif-content">
              <div class="title">{{ n.title || 'Thông báo' }}</div>
              <div class="message">{{ n.message }}</div>
              <div class="time">{{ formatTime(n.createdAt) }}</div>
            </div>

            <div class="item-actions">
              <button v-if="!n.isRead" type="button" class="btn-icon success" title="Đánh dấu đã đọc"
                @click.stop="markAsRead(n.id)">
                <i class="bi bi-check" />
              </button>
              <button type="button" class="btn-icon danger" title="Xóa thông báo"
                @click.stop="deleteNotification(n.id)">
                <i class="bi bi-trash" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import websocketService from '../../services/websocketService.js'
import { useNotificationStore } from '../../stores/notification.js'

const notificationStore = useNotificationStore()
const showPanel = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('info')

// Toggle panel (được Header gọi qua ref)
const toggleNotificationPanel = () => {
  showPanel.value = !showPanel.value
  if (showPanel.value) notificationStore.loadNotifications()
}

// Khóa scroll khi mở panel
watch(showPanel, (v) => {
  document.documentElement.classList.toggle('o-hidden', v)
})

// ESC để đóng
const onKeydown = (e) => { if (e.key === 'Escape') showPanel.value = false }

// ==== Actions ====
const markAsRead = async (id) => {
  await notificationStore.markAsRead?.(id)
}

// Sửa lỗi: nút "Đánh dấu tất cả đã đọc" không chạy
const onMarkAll = async () => {
  try {
    if (typeof notificationStore.markAllAsRead === 'function') {
      await notificationStore.markAllAsRead()
    } else {
      // fallback: cập nhật local nếu store chưa có hàm
      notificationStore.notifications = notificationStore.notifications.map(n => ({ ...n, isRead: true }))
    }
    // đồng bộ badge nếu store có trường unreadCount
    if (typeof notificationStore.updateUnreadCount === 'function') {
      notificationStore.updateUnreadCount(0)
    } else if ('unreadCount' in notificationStore) {
      notificationStore.unreadCount = 0
    }
    showToastMessage('Đã đánh dấu tất cả thông báo đã đọc', 'success')
  } catch (e) {
    console.error(e)
    showToastMessage('Không thể đánh dấu tất cả. Thử lại!', 'error')
  }
}

const deleteNotification = async (id) => {
  await notificationStore.deleteNotification?.(id)
}

const handleNotificationClick = (n) => {
  if (!n.isRead) markAsRead(n.id)
  if (n.actionUrl) console.log('Navigate to:', n.actionUrl)
}

// Toast
const showToastMessage = (message, type = 'info') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 3000)
}
const hideToast = () => { showToast.value = false }

// Icons
const getNotificationIcon = (type) => {
  const icons = {
    QUIZ_RESULT_READY: 'bi bi-trophy',
    QUIZ_APPROVED: 'bi bi-check-circle',
    QUIZ_REJECTED: 'bi bi-x-circle',
    ACCOUNT_STATUS_CHANGED: 'bi bi-shield-check',
    REVIEW_RECEIVED: 'bi bi-star',
    NEW_QUIZ_SUBMITTED: 'bi bi-plus-circle',
    QUIZ_COMPLETED: 'bi bi-play-circle',
    NEW_USER_REGISTERED: 'bi bi-person-plus',
    REPORT_SUBMITTED: 'bi bi-flag',
    SYSTEM_ACTIVITY: 'bi bi-gear'
  }
  return icons[type] || 'bi bi-bell'
}

const getToastIcon = (type) => ({
  success: 'bi bi-check-circle',
  error: 'bi bi-exclamation-circle',
  warning: 'bi bi-exclamation-triangle',
  info: 'bi bi-info-circle'
}[type] || 'bi bi-info-circle')

// Time
const formatTime = (ts) => {
  const d = new Date(ts)
  const diff = Date.now() - d.getTime()
  const m = Math.floor(diff / 60000)
  const h = Math.floor(diff / 3600000)
  const day = Math.floor(diff / 86400000)
  if (m < 1) return 'Vừa xong'
  if (m < 60) return `${m} phút trước`
  if (h < 24) return `${h} giờ trước`
  if (day < 7) return `${day} ngày trước`
  return d.toLocaleDateString('vi-VN')
}

// Websocket
const handleWebSocketNotification = (n) => {
  notificationStore.addNotification?.(n)
  showToastMessage(n.message, 'info')
}
const initializeWebSocket = () => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  if (token && username) {
    websocketService.connect(username, token)
      .then(() => websocketService.onNotification(handleWebSocketNotification))
      .catch((e) => console.error('❌ WebSocket connection failed:', e))
  }
}

onMounted(() => {
  notificationStore.initialize?.()
  initializeWebSocket()
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  websocketService.disconnect?.()
  window.removeEventListener('keydown', onKeydown)
  document.documentElement.classList.remove('o-hidden')
})

// cho Header gọi
defineExpose({ toggleNotificationPanel })
</script>

<style scoped>
/* ---- Backdrop + Panel ---- */
.notif-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, .28);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 1900
}

.notif-panel {
  position: fixed;
  top: 80px;
  right: 20px;
  width: min(440px, calc(100vw - 24px));
  max-height: min(70vh, 640px);
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, .12);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  overflow: hidden
}

/* Header */
.notif-header {
  position: sticky;
  top: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  color: #111827;
  border-bottom: 1px solid #e5e7eb
}

.notif-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 700
}

.actions {
  display: flex;
  gap: 8px
}

.btn-mark-all {
  background: transparent;
  color: #111827;
  border: 1px solid #e5e7eb;
  padding: 6px 10px;
  border-radius: 10px;
  font-size: .85rem;
  cursor: pointer
}

.btn-mark-all:hover {
  background: #f9fafb
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  line-height: 1;
  cursor: pointer;
  color: #6b7280;
  padding: 6px 8px;
  border-radius: 8px
}

.btn-close:hover {
  background: #f3f4f6;
  color: #111827
}

/* List */
.notif-list {
  overflow: auto;
  padding: 8px 6px 10px
}

.state {
  padding: 24px;
  text-align: center;
  color: #6b7280
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px
}

/* Item */
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #f3f4f6;
  transition: .2s ease;
  cursor: pointer;
  background: #fff
}

.notif-item:hover {
  background: #f9fafb;
  transform: translateX(2px)
}

.notif-item.unread {
  background: #f8fafc
}

.notif-icon {
  width: 40px;
  height: 40px;
  min-width: 40px;
  border-radius: 999px;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 1.1rem
}

.notif-content {
  flex: 1;
  min-width: 0
}

.title {
  font-weight: 600;
  color: #111827;
  font-size: .95rem;
  margin-bottom: 2px
}

.message {
  color: #4b5563;
  font-size: .88rem;
  line-height: 1.35;
  margin-bottom: 2px;
  word-break: break-word
}

.time {
  color: #9ca3af;
  font-size: .75rem
}

.item-actions {
  display: flex;
  gap: 6px;
  margin-left: 4px
}

.btn-icon {
  background: #fff;
  border: 1px solid #e5e7eb;
  padding: 6px;
  border-radius: 10px;
  cursor: pointer;
  font-size: .95rem
}

.btn-icon.success {
  color: #16a34a
}

.btn-icon.success:hover {
  background: #ecfdf5;
  border-color: #a7f3d0
}

.btn-icon.danger {
  color: #e11d48
}

.btn-icon.danger:hover {
  background: #fff1f2;
  border-color: #fecdd3
}

/* Toast */
.admin-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, .15);
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: .75rem;
  z-index: 10000;
  animation: slideIn .3s ease;
  min-width: 300px
}

.admin-toast.success {
  border-left: 4px solid #16a34a
}

.admin-toast.error {
  border-left: 4px solid #e11d48
}

.admin-toast.warning {
  border-left: 4px solid #d97706
}

.admin-toast.info {
  border-left: 4px solid #2563eb
}

.toast-content {
  display: flex;
  align-items: center;
  gap: .5rem;
  flex: 1
}

.toast-close {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  color: #6b7280;
  padding: 4px 6px;
  border-radius: 8px
}

.toast-close:hover {
  background: #f3f4f6;
  color: #111827
}

/* Helpers */
.o-hidden {
  overflow: hidden
}

@keyframes spin {
  to {
    transform: rotate(360deg)
  }
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0
  }

  to {
    transform: translateX(0);
    opacity: 1
  }
}
</style>
