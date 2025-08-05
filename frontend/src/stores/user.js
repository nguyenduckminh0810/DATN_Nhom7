import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const isAuthenticated = ref(!!localStorage.getItem('token'))

  // Lấy thông tin user từ localStorage
  const userInfo = localStorage.getItem('user')
  if (userInfo) {
    user.value = JSON.parse(userInfo)
  }

  function setUser(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function setToken(tokenValue) {
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
    isAuthenticated.value = true
  }

  function logout() {
    user.value = null
    token.value = null
    isAuthenticated.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // Computed properties
  const isAdmin = () => {
    return user.value && (user.value.role === 'ADMIN' || user.value.role === 'admin')
  }

  const getUserId = () => {
    return user.value ? user.value.id : null
  }

  return {
    user,
    token,
    isAuthenticated,
    setUser,
    setToken,
    logout,
    isAdmin,
    getUserId
  }
}) 