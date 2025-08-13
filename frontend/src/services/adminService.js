import api from '@/utils/axios'

export const adminService = {
  async getAllUsers(params = {}) {
    const {
      page = 0,
      size = 100,
      search,
      role,
    } = params

    const response = await api.get('/admin/all-users', {
      params: {
        page,
        size,
        ...(search ? { search } : {}),
        ...(role ? { role } : {}),
      },
    })
    return response.data
  },

  async getAttemptsTodayByHour(tz) {
    const response = await api.get('/admin/stats/attempts-today', {
      params: { tz }
    })
    return response.data
  },
}

export default adminService


