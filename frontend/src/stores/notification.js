import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/utils/axios'

export const useNotificationStore = defineStore('notification', () => {
  // ✅ STATE
  const notifications = ref([])
  const unreadCount = ref(0)
  const isLoading = ref(false)
  const error = ref(null)

  // ✅ COMPUTED
  const hasUnreadNotifications = computed(() => unreadCount.value > 0)
  const sortedNotifications = computed(() => {
    return [...notifications.value].sort((a, b) => {
      // Unread notifications first
      if (a.isRead !== b.isRead) {
        return a.isRead ? 1 : -1
      }
      // Then by creation date (newest first)
      return new Date(b.createdAt) - new Date(a.createdAt)
    })
  })

  // ✅ ACTIONS
  const loadNotifications = async () => {
    try {
      isLoading.value = true
      error.value = null
      
      // ✅ CLEAR CACHE TRƯỚC KHI LOAD
      notifications.value = []
      unreadCount.value = 0
      
      const response = await api.get('/notifications')
      notifications.value = response.data
      updateUnreadCount()
      
      console.log('✅ Notifications loaded:', notifications.value.length)
    } catch (err) {
      error.value = err.message
      console.error('❌ Error loading notifications:', err)
    } finally {
      isLoading.value = false
    }
  }

  const loadUnreadCount = async () => {
    try {
      // ✅ CLEAR CACHE TRƯỚC KHI LOAD
      unreadCount.value = 0
      
      const response = await api.get('/notifications/unread/count')
      unreadCount.value = response.data.count
      console.log('✅ Unread count loaded:', unreadCount.value)
    } catch (err) {
      console.error('❌ Error loading unread count:', err)
    }
  }

  const updateUnreadCount = () => {
    unreadCount.value = notifications.value.filter(n => !n.isRead).length
  }

  const markAsRead = async (notificationId) => {
    try {
      await api.put(`/notifications/${notificationId}/read`)
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.isRead = true
        updateUnreadCount()
      }
    } catch (err) {
      console.error('❌ Error marking notification as read:', err)
    }
  }

  const markAllAsRead = async () => {
    try {
      await api.put('/notifications/read-all')
      notifications.value.forEach(n => n.isRead = true)
      updateUnreadCount()
    } catch (err) {
      console.error('❌ Error marking all notifications as read:', err)
    }
  }

  const deleteNotification = async (notificationId) => {
    try {
      await api.delete(`/notifications/${notificationId}`)
      notifications.value = notifications.value.filter(n => n.id !== notificationId)
      updateUnreadCount()
    } catch (err) {
      console.error('❌ Error deleting notification:', err)
    }
  }

  const addNotification = (notification) => {
    notifications.value.unshift(notification)
    updateUnreadCount()
  }

  const clearNotifications = () => {
    notifications.value = []
    unreadCount.value = 0
  }

  // ✅ INITIALIZATION
  const initialize = async () => {
    await loadUnreadCount()
    await loadNotifications()
  }

  return {
    // State
    notifications,
    unreadCount,
    isLoading,
    error,
    
    // Computed
    hasUnreadNotifications,
    sortedNotifications,
    
    // Actions
    loadNotifications,
    loadUnreadCount,
    updateUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    addNotification,
    clearNotifications,
    initialize
  }
}) 