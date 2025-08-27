import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'

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

      const res = await api.post('/register', {
        username: username.value,
        email: email.value,
        password: password.value,
        fullName: fullName.value,
        bio: bio.value,
        avatarUrl: avatarUrl.value,
        role: role.value,
      })

      const data = res.data
      if (data.status === 'SUCCESS') {
        message.value = data.message || 'Đăng ký thành công!'

        // Lưu token và thông tin user vào localStorage
        localStorage.setItem('token', data.token)
        if (data.user) {
          localStorage.setItem('user', JSON.stringify(data.user))
          localStorage.setItem('userId', data.user.id)
        }

        // Cập nhật status để hiển thị thông báo thành công
        status.value = 'SUCCESS'

        // Đảm bảo rằng user được đăng nhập
        console.log(' Registration successful, user logged in automatically')

        // Tự động chuyển hướng đến dashboard sau 2 giây
        setTimeout(() => {
          // Đảm bảo token đã được lưu trước khi chuyển hướng
          if (localStorage.getItem('token')) {
            console.log(' Token found, redirecting to dashboard')
            router.push('/dashboard')
          } else {
            console.error(' Token not found after registration')
            router.push('/login')
          }
        }, 2000)
      } else {
        message.value = data.message || 'Có lỗi xảy ra!'
        status.value = 'loggedOut'
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
    register,
  }
}
