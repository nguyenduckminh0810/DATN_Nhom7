import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // sửa lại nếu baseURL khác
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
    console.log('🔍 Adding Authorization header with token')
  } else {
    console.log('❌ No token found for request:', config.url)
  }
  return config
})

// Thêm response interceptor để xử lý lỗi 401
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      console.log('❌ 401 Unauthorized - Token may be expired')
      // Có thể thêm logic redirect về login page ở đây
    }
    return Promise.reject(error)
  },
)

export default api
