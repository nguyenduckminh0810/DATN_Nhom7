<template>
    <div class="container py-4">
      <h2>Danh mục Quiz</h2>
      <div v-if="isAdmin" class="mb-3">
        <input v-model="newCategory.name" placeholder="Tên danh mục" class="form-control mb-2" />
        <input v-model="newCategory.description" placeholder="Mô tả" class="form-control mb-2" />
        <button class="btn btn-primary" @click="addCategory">Thêm danh mục</button>
      </div>
      <table class="table table-bordered mt-3">
        <thead>
          <tr>
            <th>Tên</th>
            <th>Mô tả</th>
            <th>Ngày tạo</th>
            <th v-if="isAdmin">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cat in categories" :key="cat.id">
            <td>
              <span v-if="editId !== cat.id">{{ cat.name }}</span>
              <input v-else v-model="editCategory.name" class="form-control" />
            </td>
            <td>
              <span v-if="editId !== cat.id">{{ cat.description }}</span>
              <input v-else v-model="editCategory.description" class="form-control" />
            </td>
            <td>{{ formatDate(cat.createdAt) }}</td>
            <td v-if="isAdmin">
              <button v-if="editId !== cat.id" class="btn btn-sm btn-warning" @click="startEdit(cat)">Sửa</button>
              <button v-else class="btn btn-sm btn-success" @click="saveEdit(cat.id)">Lưu</button>
              <button class="btn btn-sm btn-danger ms-2" @click="deleteCategory(cat.id)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  import { useUserStore } from '@/stores/user'
  
  const categories = ref([])
  const newCategory = ref({ name: '', description: '' })
  const editId = ref(null)
  const editCategory = ref({ name: '', description: '' })
  const userStore = useUserStore()
  const isAdmin = userStore.isAdmin()
  
  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString()
  }
  
  async function fetchCategories() {
    const res = await axios.get('http://localhost:8080/api/categories')
    categories.value = res.data
  }
  
  async function addCategory() {
    if (!newCategory.value.name) return
    await axios.post('http://localhost:8080/api/categories', newCategory.value, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    newCategory.value = { name: '', description: '' }
    fetchCategories()
  }
  
  function startEdit(cat) {
    editId.value = cat.id
    editCategory.value = { name: cat.name, description: cat.description }
  }
  
  async function saveEdit(id) {
    await axios.put(`http://localhost:8080/api/categories/${id}`, editCategory.value, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    editId.value = null
    fetchCategories()
  }
  
  async function deleteCategory(id) {
    await axios.delete(`http://localhost:8080/api/categories/${id}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    fetchCategories()
  }
  
  onMounted(fetchCategories)
  </script>