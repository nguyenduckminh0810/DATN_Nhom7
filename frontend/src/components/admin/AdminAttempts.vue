<template>
  <div class="container-fluid">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="mb-1">
          <i class="bi bi-play-circle text-primary me-2"></i>
          Quản lý Quiz Attempts
        </h2>
        <p class="text-muted mb-0">Xem và quản lý tất cả lượt làm quiz trong hệ thống</p>
      </div>
      <div class="d-flex gap-2">
        <button @click="exportData" class="btn btn-outline-success">
          <i class="bi bi-download me-2"></i>
          Xuất Excel
        </button>
        <button @click="refreshData" class="btn btn-outline-primary">
          <i class="bi bi-arrow-clockwise me-2"></i>
          Làm mới
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label">Người dùng</label>
            <input 
              v-model="filters.user" 
              type="text" 
              class="form-control" 
              placeholder="Tìm theo tên user..."
            />
          </div>
          <div class="col-md-3">
            <label class="form-label">Quiz</label>
            <input 
              v-model="filters.quiz" 
              type="text" 
              class="form-control" 
              placeholder="Tìm theo tên quiz..."
            />
          </div>
          <div class="col-md-2">
            <label class="form-label">Điểm từ</label>
            <input 
              v-model="filters.minScore" 
              type="number" 
              class="form-control" 
              min="0" 
              max="100"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label">Điểm đến</label>
            <input 
              v-model="filters.maxScore" 
              type="number" 
              class="form-control" 
              min="0" 
              max="100"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label">Ngày từ</label>
            <input 
              v-model="filters.startDate" 
              type="date" 
              class="form-control"
            />
          </div>
        </div>
        <div class="row mt-3">
          <div class="col-12">
            <button @click="applyFilters" class="btn btn-primary me-2">
              <i class="bi bi-search me-2"></i>
              Lọc
            </button>
            <button @click="clearFilters" class="btn btn-outline-secondary">
              <i class="bi bi-x-circle me-2"></i>
              Xóa bộ lọc
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="row mb-4">
      <div class="col-md-3" v-for="stat in stats" :key="stat.label">
        <div class="card shadow-sm border-start border-4" :class="stat.borderClass">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <i :class="stat.icon + ' fs-2'" :style="{ color: stat.color }"></i>
              </div>
              <div class="flex-grow-1 ms-3">
                <h4 class="mb-1 fw-bold">{{ stat.value }}</h4>
                <p class="mb-0 text-muted small">{{ stat.label }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Attempts Table -->
    <div class="card shadow-sm">
      <div class="card-header bg-light">
        <div class="d-flex justify-content-between align-items-center">
          <h5 class="mb-0">
            <i class="bi bi-table me-2"></i>
            Danh sách Attempts ({{ totalItems }})
          </h5>
          <div class="d-flex align-items-center gap-2">
            <select v-model="pageSize" class="form-select form-select-sm" style="width: auto;">
              <option value="10">10</option>
              <option value="25">25</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
            <span class="text-muted small">items/page</span>
          </div>
        </div>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>User</th>
                <th>Quiz</th>
                <th>Điểm</th>
                <th>Thời gian</th>
                <th>Ngày làm</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="attempt in attempts" :key="attempt.id">
                <td>
                  <div class="d-flex align-items-center">
                    <div class="avatar-sm me-2">
                      <img 
                        v-if="attempt.user?.avatarUrl" 
                        :src="attempt.user.avatarUrl" 
                        class="rounded-circle"
                        width="32" 
                        height="32"
                      />
                      <i v-else class="bi bi-person-circle text-muted"></i>
                    </div>
                    <div>
                      <div class="fw-bold">{{ attempt.user?.fullName || 'Unknown' }}</div>
                      <small class="text-muted">{{ attempt.user?.username }}</small>
                    </div>
                  </div>
                </td>
                <td>
                  <div>
                    <div class="fw-bold">{{ attempt.quiz?.title || 'Unknown Quiz' }}</div>
                    <small class="text-muted">{{ attempt.quiz?.category?.name }}</small>
                  </div>
                </td>
                <td>
                  <span class="badge" :class="getScoreBadgeClass(attempt.score)">
                    {{ attempt.score || 0 }}%
                  </span>
                </td>
                <td>
                  <span class="text-muted">{{ formatTime(attempt.timeTaken) }}</span>
                </td>
                <td>
                  <span class="text-muted">{{ formatDate(attempt.attemptedAt) }}</span>
                </td>
                <td>
                  <span class="badge" :class="getStatusBadgeClass(attempt.score)">
                    {{ getStatusText(attempt.score) }}
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button @click="viewDetails(attempt)" class="btn btn-outline-primary">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button @click="deleteAttempt(attempt.id)" class="btn btn-outline-danger">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- Pagination -->
      <div class="card-footer">
        <nav>
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button @click="changePage(currentPage - 1)" class="page-link">
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>
            <li 
              v-for="page in visiblePages" 
              :key="page" 
              class="page-item"
              :class="{ active: page === currentPage }"
            >
              <button @click="changePage(page)" class="page-link">{{ page }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button @click="changePage(currentPage + 1)" class="page-link">
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- Details Modal -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết Attempt</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body" v-if="selectedAttempt">
            <div class="row">
              <div class="col-md-6">
                <h6>Thông tin User</h6>
                <p><strong>Tên:</strong> {{ selectedAttempt.user?.fullName }}</p>
                <p><strong>Username:</strong> {{ selectedAttempt.user?.username }}</p>
                <p><strong>Email:</strong> {{ selectedAttempt.user?.email }}</p>
              </div>
              <div class="col-md-6">
                <h6>Thông tin Quiz</h6>
                <p><strong>Tiêu đề:</strong> {{ selectedAttempt.quiz?.title }}</p>
                <p><strong>Danh mục:</strong> {{ selectedAttempt.quiz?.category?.name }}</p>
                <p><strong>Người tạo:</strong> {{ selectedAttempt.quiz?.user?.fullName }}</p>
              </div>
            </div>
            <hr>
            <div class="row">
              <div class="col-md-6">
                <h6>Kết quả</h6>
                <p><strong>Điểm:</strong> {{ selectedAttempt.score }}%</p>
                <p><strong>Thời gian:</strong> {{ formatTime(selectedAttempt.timeTaken) }}</p>
                <p><strong>Ngày làm:</strong> {{ formatDate(selectedAttempt.attemptedAt) }}</p>
              </div>
              <div class="col-md-6">
                <h6>Trạng thái</h6>
                <p><strong>Kết quả:</strong> {{ getStatusText(selectedAttempt.score) }}</p>
                <p><strong>Thời gian hoàn thành:</strong> {{ formatTime(selectedAttempt.timeTaken) }}</p>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Đóng</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/utils/axios'

// ✅ REACTIVE DATA
const attempts = ref([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(25)
const showModal = ref(false)
const selectedAttempt = ref(null)

const filters = ref({
  user: '',
  quiz: '',
  minScore: '',
  maxScore: '',
  startDate: ''
})

const stats = ref([
  {
    label: 'Tổng Attempts',
    value: 0,
    icon: 'bi bi-play-circle',
    color: 'var(--info-color)',
    borderClass: 'border-primary'
  },
  {
    label: 'Điểm trung bình',
    value: '0%',
    icon: 'bi bi-graph-up',
    color: 'var(--success-color)',
    borderClass: 'border-success'
  },
  {
    label: 'Thời gian TB',
    value: '0 phút',
    icon: 'bi bi-clock',
    color: 'var(--warning-color)',
    borderClass: 'border-warning'
  },
  {
    label: 'Tỷ lệ đỗ',
    value: '0%',
    icon: 'bi bi-check-circle',
    color: 'var(--danger-color)',
    borderClass: 'border-danger'
  }
])

// ✅ COMPUTED PROPERTIES
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// ✅ METHODS
const loadAttempts = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      ...filters.value
    }
    
    const response = await api.get('/admin/all-attempts', { params })
    attempts.value = response.data.content || response.data
    totalItems.value = response.data.totalElements || response.data.length
    
    updateStats()
  } catch (error) {
    console.error('Error loading attempts:', error)
  }
}

const updateStats = () => {
  if (attempts.value.length === 0) return
  
  const totalAttempts = attempts.value.length
  const avgScore = attempts.value.reduce((sum, a) => sum + (a.score || 0), 0) / totalAttempts
  const avgTime = attempts.value.reduce((sum, a) => sum + (a.timeTaken || 0), 0) / totalAttempts
  const passRate = (attempts.value.filter(a => (a.score || 0) >= 50).length / totalAttempts) * 100
  
  stats.value[0].value = totalAttempts
  stats.value[1].value = `${avgScore.toFixed(1)}%`
  stats.value[2].value = `${Math.round(avgTime / 60)} phút`
  stats.value[3].value = `${passRate.toFixed(1)}%`
}

const applyFilters = () => {
  currentPage.value = 1
  loadAttempts()
}

const clearFilters = () => {
  filters.value = {
    user: '',
    quiz: '',
    minScore: '',
    maxScore: '',
    startDate: ''
  }
  applyFilters()
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadAttempts()
  }
}

const viewDetails = (attempt) => {
  selectedAttempt.value = attempt
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedAttempt.value = null
}

const deleteAttempt = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa attempt này?')) return
  
  try {
    await api.delete(`/admin/attempts/${id}`)
    loadAttempts()
  } catch (error) {
    console.error('Error deleting attempt:', error)
  }
}

const refreshData = () => {
  loadAttempts()
}

const exportData = () => {
  // TODO: Implement export to Excel
  alert('Tính năng xuất Excel sẽ được implement sau')
}

// ✅ UTILITY FUNCTIONS
const formatDate = (date) => {
  return new Date(date).toLocaleString('vi-VN')
}

const formatTime = (seconds) => {
  if (!seconds) return '0 phút'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const getScoreBadgeClass = (score) => {
  if (score >= 80) return 'bg-success'
  if (score >= 60) return 'bg-warning'
  return 'bg-danger'
}

const getStatusBadgeClass = (score) => {
  if (score >= 50) return 'bg-success'
  return 'bg-danger'
}

const getStatusText = (score) => {
  return score >= 50 ? 'Đỗ' : 'Trượt'
}

// ✅ WATCHERS
const pageSizeComputed = computed({
  get: () => pageSize.value,
  set: (newValue) => {
    pageSize.value = newValue
    currentPage.value = 1
    loadAttempts()
  }
})

// ✅ MOUNTED
onMounted(() => {
  loadAttempts()
})
</script>

<style scoped>
.avatar-sm {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  z-index: 1055;
}

.table th {
  font-weight: 600;
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
}

.btn-group-sm .btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}
</style> 