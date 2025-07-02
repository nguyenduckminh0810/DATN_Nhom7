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
const router = useRouter()

onMounted(() => {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
        token.value = savedToken
        status.value = 'loggedIn'
    }
})

const isLoading = computed(() => status.value === 'loggingIn')

const resetForm = () => {
    username.value = ''
    password.value = ''
}
//Gửi lên server để lấy thông tin người dùng
const getUserId = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/user', {
            headers: {
                Authorization: `Bearer ${token.value}`
            }
        })
        userId.value = res.data.user_id
        return res.data.user_id
    } catch (err) {
        console.error('Lỗi khi lấy thông tin người dùng:', err)
        return null
    }
}

const login = async () => {
    status.value = 'loggingIn'
    message.value = ''

    try {
        const res = await axios.post('http://localhost:8080/api/login', {
            username: username.value,
            password: password.value
        })

        const data = res.data
        if (data.status === 'SUCCESS') {
            message.value = data.message || 'Đăng nhập thành công!'
            localStorage.setItem('token', data.token)
            token.value = data.token
            status.value = 'loggedIn'
        } else {
            message.value = data.message || 'Có lỗi xảy ra!'
            status.value = 'loggedOut'
            resetForm()
        }
    } catch (err) {
        message.value = err.response?.data?.message || '❌ Đăng nhập thất bại!'
        status.value = 'loggedOut'
        resetForm()
    }
}

const logout = () => {
    const router = useRouter()
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    username.value = null
    status.value = 'loggedOut'
    message.value = ''
    resetForm()
    router.push('/login')
}

export function useLogin() {
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
