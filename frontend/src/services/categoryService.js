import axios from 'axios'
const API = 'http://localhost:8080/api/categories'

export const categoryService = {
  getAll({ search = '', page = 0, size = 10 } = {}) {
    return axios.get(API, { params: { search, page, size } })
  },
  create(data) {
    return axios.post(API, data)
  },
  update(id, data) {
    return axios.put(`${API}/${id}`, data)
  },
  delete(id) {
    return axios.delete(`${API}/${id}`)
  }
} 