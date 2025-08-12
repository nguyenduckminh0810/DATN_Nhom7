import api from '@/utils/axios'

export const quizService = {
  async getAllQuizzes(params = {}) {
    const { page = 0, size = 100 } = params
    const response = await api.get('/admin/all-quizzes/filter', {
      params: { page, size },
    })
    return response.data
  },
}

export default quizService


