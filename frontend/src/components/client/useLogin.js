import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
// import { API_BASE_URL } from '@/config' // nếu tách API URL ra

export function useLogin() {
    const status = ref('loggedOut')
    const username = ref('')
    const password = ref('')
    const message = ref('')
    const router = useRouter()

    onMounted(() => {
        if (localStorage.getItem('token')) {
            status.value = 'loggedIn'
        }
    })

    const isLoading = computed(() => status.value === 'loggingIn')

    const resetForm = () => {
        username.value = ''
        password.value = ''
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
            const token = res.headers['x-auth-token'] || res.data.token
            if (data.status === 'SUCCESS') {
                message.value = data.message || 'Đăng nhập thành công!'
                localStorage.setItem('token', data.token)

                console.log('Token:', data.token)


                status.value = 'loggedIn'
                // router.push('/')
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
        localStorage.removeItem('token')
        status.value = 'loggedOut'
        message.value = ''
        resetForm()
        router.push('/login')
    }

    return {
        status,
        username,
        password,
        message,
        isLoading,
        login,
        logout
    }
}
