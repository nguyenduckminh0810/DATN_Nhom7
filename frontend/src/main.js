// ✅ IMPORT POLYFILLS TRƯỚC TIÊN
import './utils/polyfills'

import './assets/main.css'
import './assets/vietnamese-fonts.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Import axios để setup interceptor
import axios from 'axios'

// Import theme store
import { useThemeStore } from './stores/theme'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)

// Initialize theme store
const themeStore = useThemeStore()
themeStore.initTheme()

// ✅ RESET THEME VỀ LIGHT MODE MẶC ĐỊNH (nếu chưa có preference)
if (localStorage.getItem('app-dark-mode') === null) {
  themeStore.setTheme(false) // Force light mode
}

// ✅ SETUP AXIOS INTERCEPTOR ĐỂ TỰ ĐỘNG THÊM JWT TOKEN
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// ✅ SETUP RESPONSE INTERCEPTOR ĐỂ XỬ LÝ TOKEN HẾT HẠN
axios.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token hết hạn hoặc không hợp lệ - tự động logout
      localStorage.removeItem('token')
      localStorage.removeItem('user')

      // Chuyển về trang login nếu không phải đang ở public routes
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

app.mount('#app')
