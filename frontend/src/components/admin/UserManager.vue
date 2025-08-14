<template>
  <div class="admin-users" :class="{ 'is-light': !isDarkMode, 'is-dark': isDarkMode }">
    <div class="page-header">
      <div class="title-card">
        <div class="page-title">
          <div class="icon-badge"><i class="bi bi-people-fill"></i></div>
          <div class="title-text">
            <h1>Danh sách người dùng</h1>
            <p>Quản lý tài khoản và vai trò trong hệ thống</p>
          </div>
        </div>
      </div>
      <div class="page-actions">
        <div class="search-group">
          <i class="bi bi-search"></i>
          <input v-model="search" @keyup.enter="fetchUsers" placeholder="Tìm tên hoặc email..." />
        </div>
        <select v-model="role" @change="fetchUsers" class="filter-select">
          <option value="">Tất cả vai trò</option>
          <option value="ADMIN">Admin</option>
          <option value="USER">Người dùng</option>
        </select>
        <button class="btn btn-outline" @click="fetchUsers"><i class="bi bi-arrow-clockwise"></i> Làm mới</button>
      </div>
    </div>

    <div class="panel">
      <div class="panel-body no-padding">
        <div class="table-wrap">
          <table class="modern-table">
            <thead>
              <tr>
                <th>STT</th>
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
                <td class="muted">{{ index + 1 + currentPage * pageSize }}</td>
                <td>
                  <div class="cell-user">
                    <div class="avatar-placeholder"><i class="bi bi-person-circle"></i></div>
                    <div>
                      <div class="cell-title">{{ user.fullName || '—' }}</div>
                      <div class="cell-sub muted">@{{ user.username }}</div>
                    </div>
                  </div>
                </td>
                <td class="mono">{{ user.email }}</td>
                <td class="hide-desktop">{{ user.username }}</td>
                <td>
                  <span class="role-pill" :class="roleClass(user.role)">{{ user.role }}</span>
                </td>
                <td class="muted">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="chip primary" @click="editUser(user)"><i class="bi bi-pencil"></i> Sửa</button>
                    <button class="chip danger" @click="deleteUser(user.id)"><i class="bi bi-trash"></i> Xoá</button>
                    <button v-if="user.role !== 'BANNED'" class="chip warning" @click="banUser(user)"><i class="bi bi-ban"></i> Ban</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination-bar">
        <button class="pg-btn" :disabled="currentPage === 0" @click="prevPage"><i class="bi bi-chevron-left"></i></button>
        <span>Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button class="pg-btn" :disabled="currentPage >= totalPages - 1" @click="nextPage"><i class="bi bi-chevron-right"></i></button>
      </div>
    </div>
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
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'
import api from '@/utils/axios'

// State
const users = ref([])
const search = ref('')
const role = ref('')
const currentPage = ref(0)
const pageSize = 10
const totalPages = ref(1)
const selectedUser = ref(null)
const showEditModal = ref(false)

// theme reactive
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

// Fetch users
const fetchUsers = async () => {
  try {
    const res = await api.get('/admin/all-users', {
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
    await api.put(`/admin/users/${selectedUser.value.id}`, selectedUser.value)
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
      await api.delete(`/admin/users/${userId}`)
       alert('Người dùng đã bị xóa.')
      await fetchUsers()
    } catch (err) {
      console.error('Lỗi khi xóa:', err)
      alert('Không thể xóa người dùng này.')
    }
  }
}

const banUser = async (user) => {
  if (confirm(`Bạn có chắc chắn muốn BAN người dùng: ${user.username}?`)) {
    try {
      await api.put(`/admin/users/${user.id}/ban`)
      alert('Người dùng đã bị ban.')
      await fetchUsers()
    } catch (err) {
      console.error('Lỗi khi ban người dùng:', err)
      alert('Không thể ban người dùng này.')
    }
  }
}


// Helpers
const formatDate = (dateStr) => {
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN')
}

const roleClass = (role) => (role === 'ADMIN' ? 'admin' : role === 'USER' ? 'user' : 'other')
</script>

<style scoped>
.admin-users { padding: 24px; color: var(--text-primary); }
.page-header { display:flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title-card { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 14px; padding: 10px 14px; box-shadow: 0 6px 18px var(--shadow-color); }
.page-title { display:flex; align-items:center; gap:12px; }
.icon-badge { width:44px; height:44px; display:flex; align-items:center; justify-content:center; border-radius:12px; }
.icon-badge i { font-size:22px; color: var(--info-color); }
.title-text h1 { font-size:22px; margin:0; }
.title-text h1::after { content:''; display:block; height:3px; width:80px; margin-top:6px; border-radius:999px; background:linear-gradient(90deg,#667eea 0%, #764ba2 100%); opacity:.6; }
.title-text p { margin:2px 0 0 0; font-size:13px; color: var(--text-secondary); }
.title-card .icon-badge i{ filter:none; }

/* Dark/Light fine-tune to ensure visible contrast like AdminDashboard */
.admin-users.is-light .title-card { background:#ffffff; border-color: rgba(2,6,23,0.12); }
.admin-users.is-dark .title-card { background: transparent; border-color: rgba(255,255,255,0.18); box-shadow: none; }
.admin-users.is-dark .page-title { background: transparent; }
.admin-users.is-light .title-text h1 { color:#0b1220; }
.admin-users.is-dark .title-text h1 { color:#f1f5f9; }
.admin-users.is-light .title-text h1::after { opacity:.95; background:linear-gradient(90deg,#4338ca 0%, #7c3aed 100%); }
.admin-users.is-dark .title-text h1::after { opacity:.7; }
.page-actions { display:flex; align-items:center; gap:10px; }
.search-group { display:flex; align-items:center; gap:8px; background: var(--bg-primary); border:1px solid var(--border-color); padding:8px 12px; border-radius:10px; }
.search-group input { border:0; outline:none; background:transparent; color: var(--text-primary); width: 220px; }
.filter-select { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }
.btn.btn-outline { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }

.panel { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 16px; box-shadow: 0 8px 22px var(--shadow-color); overflow:hidden; }
.panel-body.no-padding { padding: 0; }
.table-wrap { width: 100%; overflow-x: auto; }
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table thead th { text-align:left; padding: 14px 16px; background: var(--card-header-bg); color: var(--card-header-text); font-weight: 700; font-size: 13px; }
.modern-table tbody td { padding: 12px 16px; border-top:1px solid var(--border-color); font-size: 14px; }
.modern-table tbody tr:hover { background: rgba(102,126,234,0.06); }
.muted { color: var(--text-muted); }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace; }
.cell-user { display:flex; align-items:center; gap:10px; }
.avatar-placeholder { width:28px; height:28px; border-radius:50%; display:flex; align-items:center; justify-content:center; border:1px solid var(--border-color); color: var(--text-secondary); overflow:hidden; }
.avatar-placeholder img { width:100%; height:100%; object-fit: cover; display:block; }
.cell-title { font-weight: 600; }
.cell-sub { font-size: 12px; }
.role-pill { display:inline-flex; align-items:center; padding: 4px 10px; border-radius: 999px; font-weight:700; font-size: 12px; }
.role-pill.admin { background: #2563eb; color: #fff; }
.role-pill.user { background: #6b7280; color: #fff; }
.role-pill.other { background: #e5e7eb; color: #111827; }
.row-actions { display:flex; gap:8px; }
.chip { display:inline-flex; align-items:center; gap:6px; padding:6px 10px; border-radius:10px; border:1px solid var(--border-color); background: var(--bg-primary); color: var(--text-primary); cursor:pointer; }
.chip.primary { border-color:#93c5fd; }
.chip.danger { border-color:#fda4af; }
.chip.warning { border-color:#fbbf24; }

.pagination-bar { display:flex; align-items:center; justify-content:flex-end; gap:8px; padding: 10px 12px; border-top:1px solid var(--border-color); }
.pg-btn { border:1px solid var(--border-color); background: var(--bg-primary); color: var(--text-primary); border-radius:8px; width:32px; height:28px; display:flex; align-items:center; justify-content:center; }

@media (max-width: 900px) {
  .hide-desktop { display:none; }
  .search-group input { width: 140px; }
}
</style>
