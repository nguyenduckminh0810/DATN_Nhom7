// useLogin.js
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'

const status = ref('loggedOut')
const username = ref('')
const password = ref('')
const userId = ref('')
const token = ref('')
const message = ref('')

export function useLogin() {
  const router = useRouter()   //  lấy router trong composable

  const initializeToken = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserId = localStorage.getItem('userId')
    const savedUsername = localStorage.getItem('username')

    if (savedToken) {
      token.value = savedToken
      status.value = 'loggedIn'
      if (savedUserId) userId.value = savedUserId
    }

    //  LUÔN LẤY USERNAME TỪ LOCALSTORAGE NẾU CÓ
    if (savedUsername) {
      username.value = savedUsername
    } else {
      //  THỬ LẤY TỪ USER OBJECT
      try {
        const userStr = localStorage.getItem('user')
        if (userStr) {
          const user = JSON.parse(userStr)
          if (user.username) {
            username.value = user.username
          }
        }
      } catch (e) {
        console.error('Error parsing user object:', e)
      }
    }
  }

  const isLoading = computed(() => status.value === 'loggingIn')
  const resetForm = () => { username.value = ''; password.value = '' }

  const getUserId = async () => {
    try {
      const res = await api.get('/user', { headers: { Authorization: `Bearer ${token.value}` } })
      userId.value = res.data.user_id
      return userId.value
    } catch { return null }
  }

  const login = async () => {
    status.value = 'loggingIn'; message.value = ''
    try {
      const res = await api.post('/login', { username: username.value, password: password.value })
      const data = res.data
      if (data.status === 'SUCCESS') {
        const jwt = data.token || data.accessToken
        const user = data.user || {}

        // Lưu thông tin đăng nhập
        const finalUsername = user.username || username.value
        localStorage.setItem('token', jwt)
        localStorage.setItem('user', JSON.stringify(user))
        localStorage.setItem('username', finalUsername)
        localStorage.removeItem('banned')    //  rất quan trọng khi unban

        // Cập nhật state
        username.value = finalUsername
        token.value = jwt
        status.value = 'loggedIn'

        await getUserId().then((id) => id && localStorage.setItem('userId', id))

        //  Không redirect ở đây nữa, để Login.vue xử lý
        console.log(' Login successful:', { userRole: (user.role || '').toUpperCase(), user })

        return { success: true, user }
      } else {
        message.value = data.message || ' Có lỗi xảy ra!'
        status.value = 'loggedOut'; resetForm()
        return { success: false }
      }
    } catch (err) {
      if (err.response?.status === 403 &&
        (err.response?.data?.status === 'BANNED' || err.response?.data?.error === 'ACCOUNT_BANNED')) {
        localStorage.setItem('banned', '1')
      }
      message.value =
        err.response?.data?.message ||
        (err.response?.status === 401 ? ' Tên đăng nhập hoặc mật khẩu không đúng!' :
          err.response?.status === 429 ? ' Quá nhiều lần thử đăng nhập!' :
            ' Đăng nhập thất bại!')
      status.value = 'loggedOut'; resetForm()
      return { success: false }
    }
  }

  const logout = () => {
    const remembered = localStorage.getItem('rememberMe') === '1'
    const rememberedUsername = remembered ? localStorage.getItem('username') : null
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('user')
    localStorage.removeItem('admin_user')
    if (!remembered) {
      localStorage.removeItem('username')
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('remembered_pw')
    } else if (rememberedUsername) {
      localStorage.setItem('username', rememberedUsername)
      localStorage.setItem('rememberMe', '1')
    }
    token.value = null; userId.value = null
    username.value = remembered ? rememberedUsername : null
    status.value = 'loggedOut'; message.value = ''
    resetForm()
  }

  // khởi tạo
  initializeToken()

  return { status, username, password, token, userId, message, getUserId, isLoading, login, logout }
}
