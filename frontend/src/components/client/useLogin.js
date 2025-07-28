// useLogin.js
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const status = ref('loggedOut')
const username = ref('')
const password = ref('')
const userId = ref('')
const token = ref('')
const message = ref('')

// ✅ INITIALIZE TOKEN FROM LOCALSTORAGE
const initializeToken = () => {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
        token.value = savedToken
        status.value = 'loggedIn'
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
        const res = await axios.get('http://localhost:8080/api/user', {
            headers: {
                Authorization: `Bearer ${token.value}`
            }
        })
        console.log('✅ User ID response:', res.data)
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
        const res = await axios.post('http://localhost:8080/api/login', {
            username: username.value,
            password: password.value
        })

        console.log('📥 Login response:', res.data)
        const data = res.data
        if (data.status === 'SUCCESS') {
            message.value = data.message || '✅ Đăng nhập thành công!'
            localStorage.setItem('token', data.token)
            token.value = data.token
            status.value = 'loggedIn'
            await getUserId()
        } else {
            message.value = data.message || '❌ Có lỗi xảy ra!'
            status.value = 'loggedOut'
            resetForm()
        }
    } catch (err) {
        console.error('Login error:', err)
        
        // ✅ IMPROVED ERROR HANDLING
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
    }
}

const logout = () => {
    // ✅ CLEAR AUTH DATA WITHOUT ROUTER
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('username')
    username.value = null
    status.value = 'loggedOut'
    message.value = ''
    resetForm()
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
        logout
    }
}