import axios from 'axios'
import { useUserStore } from '@/stores/user'

const API_BASE_URL = 'http://localhost:8080/api'

export const quizAttemptService = {

  async getQuizAttempts(params = {}) {
    const userStore = useUserStore()
    const token = userStore.token
    
    const config = {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params: {
        page: params.page || 0,
        size: params.size || 10,
        ...(params.userId && { userId: params.userId }),
        ...(params.quizId && { quizId: params.quizId })
      }
    }
    
    try {
      const response = await axios.get(`${API_BASE_URL}/quiz-attempts`, config)
      return response.data
    } catch (error) {
      console.error('Error fetching quiz attempts:', error)
      throw error
    }
  },

  /**
   * Lấy lịch sử làm quiz của user hiện tại
   */
  async getMyQuizHistory(params = {}) {
    const userStore = useUserStore()
    const token = userStore.token
    
    const config = {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params: {
        page: params.page || 0,
        size: params.size || 10,
        ...(params.quizId && { quizId: params.quizId })
      }
    }
    
    try {
      const response = await axios.get(`${API_BASE_URL}/quiz-attempts/my-history`, config)
      return response.data
    } catch (error) {
      console.error('Error fetching my quiz history:', error)
      throw error
    }
  }
} 