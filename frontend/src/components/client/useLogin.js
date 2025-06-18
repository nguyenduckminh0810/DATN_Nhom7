import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

export function useLogin() {
    const status = ref('loggedOut')
    const username = ref('')
    const password = ref('')
    const message = ref('')
    const router = useRouter()

    const redirectToServer = async () => {
        status.value = 'loggingIn'

        const user = {
            username: username.value,
            password: password.value
        }

        try {
            const res = await axios.post('http://localhost:8080/api/login', user)

            // Giả sử backend trả về { status: "SUCCESS", message: "...", token: "..." }
            const data = res.data

            if (data.status === 'SUCCESS') {
                message.value = data.message || 'Đăng nhập thành công!'
                localStorage.setItem('token', data.token || '')

                status.value = 'loggedIn'
                // Chuyển hướng sau khi login
                // router.push('/')
            } else {
                message.value = data.message || 'Có lỗi xảy ra!'
                status.value = 'loggedOut'
            }

        } catch (err) {
            message.value = err.response?.data?.message || '❌ Đăng nhập thất bại!'
            console.log(message.value)
            status.value = 'loggedOut'
        }
    }

    const logout = () => {
        localStorage.removeItem('token')
        status.value = 'loggedOut'
        // router.push('/login') // hoặc trang khác nếu cần
    }

    return {
        status,
        username,
        password,
        message,
        redirectToServer,
        logout
    }
}
