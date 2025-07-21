<template>
  <div class="container my-4">
    <h4 class="mb-4">Danh sách người dùng</h4>
    
    <div class="row mb-3">
      <div class="col-md-4">
        <input v-model="search" @keyup.enter="fetchUsers" class="form-control" placeholder="Tìm kiếm tên hoặc email..." />
      </div>
      <div class="col-md-3">
        <select v-model="role" @change="fetchUsers" class="form-select">
          <option value="">Tất cả vai trò</option>
          <option value="ADMIN">Admin</option>
          <option value="USER">Người dùng</option>
        </select>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table align-middle table-hover table-bordered shadow-sm">
        <thead class="table-light">
          <tr>
            <th>#</th>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Username</th>
            <th>Vai trò</th>
            <th>Ngày tạo</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(user, index) in users" :key="user.id">
            <td>{{ index + 1 + currentPage * pageSize }}</td>
            <td>{{ user.fullName }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.username }}</td>
            <td>
              <span class="badge" :class="roleColor(user.role)">
                {{ user.role }}
              </span>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <button class="btn btn-sm btn-outline-primary" @click="editUser(user)">Sửa</button>
              <button class="btn btn-sm btn-outline-danger ms-1" @click="deleteUser(user.id)">Xoá</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <nav v-if="totalPages > 1" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <button class="page-link" @click="prevPage">«</button>
        </li>
        <li
          v-for="n in totalPages"
          :key="n"
          class="page-item"
          :class="{ active: currentPage === n - 1 }"
        >
          <button class="page-link" @click="goToPage(n - 1)">{{ n }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <button class="page-link" @click="nextPage">»</button>
        </li>
      </ul>
    </nav>
  </div>
  <div class="modal fade show" tabindex="-1" style="display: block;" v-if="showEditModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">Sửa người dùng</h5>
        <button type="button" class="btn-close" @click="showEditModal = false"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label">Họ tên</label>
          <input v-model="selectedUser.fullName" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">Email</label>
          <input v-model="selectedUser.email" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">Username</label>
          <input v-model="selectedUser.username" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">Vai trò</label>
          <select v-model="selectedUser.role" class="form-select">
            <option value="ADMIN">Admin</option>
            <option value="USER">Người dùng</option>
          </select>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="showEditModal = false">Đóng</button>
        <button class="btn btn-primary" @click="saveUser">Lưu</button>
      </div>
    </div>
  </div>
</div>
<div class="modal-backdrop fade show" v-if="showEditModal"></div>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// State
const users = ref([])
const search = ref('')
const role = ref('')
const currentPage = ref(0)
const pageSize = 5
const totalPages = ref(1)
const selectedUser = ref(null)
const showEditModal = ref(false)


// Fetch users
const fetchUsers = async () => {
  try {
    const res = await axios.get('/api/admin/all-users', {
      params: {
        page: currentPage.value,
        size: pageSize,
        search: search.value,
        role: role.value,
      },
    })
    users.value = res.data.content
    totalPages.value = res.data.totalPages
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu người dùng:', error)
  }
}

onMounted(fetchUsers)

// Pagination methods
const goToPage = (page) => {
  currentPage.value = page
  fetchUsers()
}
const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    fetchUsers()
  }
}
const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchUsers()
  }
}

const editUser = (user) => {
  selectedUser.value = { ...user } // Tạo bản sao để tránh sửa trực tiếp trong table
  showEditModal.value = true       // Hiển thị modal sửa
}

const saveUser = async () => {
  try {
    await axios.put(`/api/admin/users/${selectedUser.value.id}`, selectedUser.value)
    showEditModal.value = false
    await fetchUsers()
  } catch (err) {
    console.error('Lỗi khi lưu:', err)
    alert('Có lỗi xảy ra khi lưu thông tin!')
  }
}

const deleteUser = async (userId) => {
  if (confirm('Bạn có chắc chắn muốn xóa người dùng này không?')) {
    try {
      await axios.delete(`/api/admin/users/${userId}`)
      await fetchUsers()
    } catch (err) {
      console.error('Lỗi khi xóa:', err)
      alert('Không thể xóa người dùng này.')
    }
  }
}

// Helpers
const formatDate = (dateStr) => {
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN')
}

const roleColor = (role) => {
  switch (role) {
    case 'ADMIN':
      return 'bg-primary text-white'
    case 'USER':
      return 'bg-secondary text-white'
    default:
      return 'bg-light text-dark'
  }
}
</script>
