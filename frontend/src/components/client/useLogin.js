// useLogin.js
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'
const status = ref('loggedOut')
const username = ref('')
const password = ref('')
const userId = ref('')
const token = ref('')
const message = ref('')

const initializeToken = () => {
  const savedToken = localStorage.getItem('token')
  const savedUserId = localStorage.getItem('userId')
  // chỉ khôi phục username nếu user đã chọn ghi nhớ
  const savedUsername = localStorage.getItem('rememberMe') === '1' ? localStorage.getItem('username') : null

  if (savedToken) {
    token.value = savedToken
    status.value = 'loggedIn'

    if (savedUserId) {
      userId.value = savedUserId
      console.log('userId restored from localStorage:', savedUserId)
    } else {
      getUserId()
    }

    if (savedUsername) {
      username.value = savedUsername
      console.log('username restored from localStorage:', savedUsername)
    }
  }
}

const isLoading = computed(() => status.value === 'loggingIn')

const resetForm = () => {
  username.value = ''
  password.value = ''
}

// Gửi lên server để lấy thông tin người dùng
const getUserId = async () => {
  try {
    console.log('📡 Getting user ID with token:', token.value ? 'Token exists' : 'No token')
    const res = await api.get('/user', {
      headers: {
        Authorization: `Bearer ${token.value}`,
      },
    })
    console.log('✅ User ID response:', res.data)
    console.log('✅ User ID from response:', res.data.user_id)
    userId.value = res.data.user_id
    return res.data.user_id
  } catch (err) {
    console.error('❌ Lỗi khi lấy thông tin người dùng:', err)
    console.error('❌ Error status:', err.response?.status)
    console.error('❌ Error data:', err.response?.data)
    return null
  }
}

const login = async () => {
  status.value = 'loggingIn'
  message.value = ''
  console.log('🚀 Starting login process...')

  try {
    console.log('📡 Sending login request...')

    const res = await api.post('/login', {
      username: username.value,
      password: password.value,
    })

    console.log('📥 Login response:', res.data)
    const data = res.data

    if (data.status === 'SUCCESS') {
      const jwt = data.token || data.accessToken
      const user = data.user || {}

      message.value = data.message || '✅ Đăng nhập thành công!'
      localStorage.setItem('username', user.username || username.value)

      localStorage.setItem('token', jwt)
      localStorage.setItem('user', JSON.stringify(user))
      token.value = jwt
      status.value = 'loggedIn'

      await getUserId().then((id) => {
        if (id) {
          localStorage.setItem('userId', id) // ✅ lưu userId
          console.log('✅ Saved userId to localStorage:', id)
        } else {
          console.log('❌ Failed to get userId')
        }
      })
      
      // ✅ RETURN USER DATA FOR REDIRECT LOGIC
      return { success: true, user }
    } else {
      message.value = data.message || '❌ Có lỗi xảy ra!'
      status.value = 'loggedOut'
      resetForm()
      return { success: false }
    }
  } catch (err) {
    console.error('Login error:', err)

    if (err.response?.status === 429) {
      message.value = '🚫 Quá nhiều lần thử đăng nhập! Vui lòng thử lại sau 1 phút.'
    } else if (err.response?.status === 401) {
      message.value = '❌ Tên đăng nhập hoặc mật khẩu không đúng!'
    } else if (err.response?.status === 403) {
      message.value = '🚫 Tài khoản bị khóa hoặc không có quyền truy cập!'
    } else if (err.code === 'NETWORK_ERROR') {
      message.value = '🌐 Không thể kết nối đến server! Vui lòng kiểm tra internet.'
    } else {
      message.value = err.response?.data?.message || '❌ Đăng nhập thất bại! Vui lòng thử lại.'
    }

    status.value = 'loggedOut'
    resetForm()
    return { success: false }
  }
}

const logout = () => {
  // ✅ CLEAR AUTH DATA WITHOUT ROUTER
  const remembered = localStorage.getItem('rememberMe') === '1'
  const rememberedUsername = remembered ? localStorage.getItem('username') : null

  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('user')
  localStorage.removeItem('admin_user') // ✅ Xóa cả admin_user nếu có

  // Nếu KHÔNG nhớ đăng nhập → xóa username/rememberMe
  if (!remembered) {
    localStorage.removeItem('username')
    localStorage.removeItem('rememberMe')
    localStorage.removeItem('remembered_pw')
  } else if (rememberedUsername) {
    // Đảm bảo username vẫn còn (phòng trường hợp nơi khác xóa)
    localStorage.setItem('username', rememberedUsername)
    localStorage.setItem('rememberMe', '1')
  }

  // ✅ Reset reactive values
  token.value = null
  userId.value = null
  username.value = remembered ? rememberedUsername : null
  status.value = 'loggedOut'
  message.value = ''
  resetForm()
  
  console.log('✅ Logout completed - all auth data cleared')
}

export function useLogin() {
  // ✅ INITIALIZE TOKEN WHEN COMPOSABLE IS USED
  initializeToken()

  return {
    status,
    username,
    password,
    token,
    userId,
    message,
    getUserId,
    isLoading,
    login,
    logout,
  }
}
