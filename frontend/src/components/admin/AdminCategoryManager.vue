<template>
  <div class="container py-4">
    <h2 class="mb-4">Quản lý danh mục</h2>
    <form class="row g-2 align-items-end mb-4" @submit.prevent="onSubmit">
      <div class="col-md-4">
        <label class="form-label">Tên danh mục</label>
        <input v-model="form.name" type="text" class="form-control" placeholder="Tên danh mục" required />
      </div>
      <div class="col-md-5">
        <label class="form-label">Mô tả</label>
        <input v-model="form.description" type="text" class="form-control" placeholder="Mô tả" />
      </div>
      <div class="col-md-3 d-flex gap-2">
        <button type="submit" class="btn btn-primary">
          {{ form.id ? 'Cập nhật' : 'Thêm mới' }}
        </button>
        <button v-if="form.id" @click="resetForm" type="button" class="btn btn-secondary">Hủy</button>
      </div>
    </form>

    <div class="table-responsive">
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-light">
          <tr>
            <th>Tên</th>
            <th>Mô tả</th>
            <th>Ngày tạo</th>
            <th class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cat in categories" :key="cat.id">
            <td>{{ cat.name }}</td>
            <td>{{ cat.description }}</td>
            <td>{{ formatDate(cat.createdAt) }}</td>
            <td class="text-center">
              <button @click="edit(cat)" class="btn btn-sm btn-outline-primary me-2">Sửa</button>
              <button @click="remove(cat.id)" class="btn btn-sm btn-outline-danger">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <nav class="d-flex justify-content-center align-items-center mt-3">
      <ul class="pagination mb-0">
        <li class="page-item" :class="{ disabled: page === 0 }">
          <button class="page-link" @click="prevPage">Trước</button>
        </li>
        <li class="page-item disabled">
          <span class="page-link">Trang {{ page + 1 }}</span>
        </li>
        <li class="page-item" :class="{ disabled: !hasNext }">
          <button class="page-link" @click="nextPage">Sau</button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { categoryService } from '@/services/categoryService'

const categories = ref([])
const page = ref(0)
const size = ref(10)
const hasNext = ref(false)
const form = ref({ id: null, name: '', description: '' })

const load = async () => {
  const res = await categoryService.getAll({ page: page.value, size: size.value })
  categories.value = res.data.content
  hasNext.value = !res.data.last
}
onMounted(load)

const onSubmit = async () => {
  if (form.value.id) {
    await categoryService.update(form.value.id, form.value)
  } else {
    await categoryService.create(form.value)
  }
  resetForm()
  load()
}
const edit = (cat) => {
  form.value = { ...cat }
}
const remove = async (id) => {
  if (confirm('Bạn chắc chắn muốn xóa?')) {
    await categoryService.delete(id)
    load()
  }
}
const resetForm = () => {
  form.value = { id: null, name: '', description: '' }
}
const prevPage = () => { if (page.value > 0) { page.value--; load() } }
const nextPage = () => { page.value++; load() }
const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('vi-VN')
}
</script> 