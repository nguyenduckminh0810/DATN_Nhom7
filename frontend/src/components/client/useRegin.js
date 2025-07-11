import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

export function useRegin() {
    const status = ref('loggedOut')
    const username = ref('')
    const password = ref('')
    const confirmPassword = ref('')
    const fullName = ref('')
    const email = ref('')
    const bio = ref('')
    const avatarUrl = ref('')
    const message = ref('')
    const router = useRouter()
    const role = ref('USER')

    onMounted(() => {
        if (localStorage.getItem('token')) {
            status.value = 'loggedIn'
        }
    })

    const isLoading = computed(() => status.value === 'registering')

    const resetForm = () => {
        username.value = ''
        password.value = ''
        confirmPassword.value = ''
        email.value = ''
        fullName.value = ''
        bio.value = ''
        avatarUrl.value = ''
    }

    const register = async () => {
        status.value = 'registering'
        message.value = ''

        try {
            if (password.value !== confirmPassword.value) {
                throw new Error('Mật khẩu không khớp!')
            }

            const res = await axios.post('http://localhost:8080/api/register', {
                username: username.value,
                email: email.value,
                password: password.value,
                fullName: fullName.value,
                bio: bio.value,
                avatarUrl: avatarUrl.value,
                role: role.value
            })

            const data = res.data
            if (data.status === 'SUCCESS') {
                message.value = data.message || 'Đăng ký thành công!'
                localStorage.setItem('token', data.token)
                status.value = 'loggedIn'
                // router.push('/')
            } else {
                message.value = data.message || 'Có lỗi xảy ra!'
                status.value = 'registering'
            }
        } catch (err) {
            const resData = err.response?.data
            const resStatus = resData?.status || 'ERROR'
            const resMessage = resData?.message || err.message || 'Đăng ký thất bại!'

            message.value = resMessage
            status.value = resStatus
            console.error('[Đăng ký lỗi]', resStatus, resMessage)

            resetForm()
        }
    }

    return {
        status,
        username,
        email,
        password,
        confirmPassword,
        fullName,
        avatarUrl,
        bio,
        message,
        isLoading,
        register
    }
}
