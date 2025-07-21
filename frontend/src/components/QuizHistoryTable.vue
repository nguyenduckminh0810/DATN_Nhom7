<template>
  <div class="quiz-history-container">
    <div class="header">
      <h2>{{ title }}</h2>
      
      <!-- Filter cho admin -->
      <div v-if="showUserFilter && isAdmin" class="filters">
        <div class="filter-group">
          <label>Lọc theo người dùng:</label>
          <select v-model="filters.userId" @change="loadData">
            <option value="">Tất cả người dùng</option>
            <option v-for="user in users" :key="user.id" :value="user.id">
              {{ user.username }}
            </option>
          </select>
        </div>
        
        <div class="filter-group">
          <label>Lọc theo quiz:</label>
          <select v-model="filters.quizId" @change="loadData">
            <option value="">Tất cả quiz</option>
            <option v-for="quiz in quizzes" :key="quiz.id" :value="quiz.id">
              {{ quiz.title }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Bảng dữ liệu -->
    <div class="table-container">
      <table class="quiz-history-table">
        <thead>
          <tr>
            <th>STT</th>
            <th v-if="isAdmin">Người dùng</th>
            <th>Quiz</th>
            <th>Điểm</th>
            <th>Thời gian làm</th>
            <th>Ngày làm</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(attempt, index) in attempts" :key="attempt.id">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td v-if="isAdmin">{{ attempt.username }}</td>
            <td>{{ attempt.quizTitle }}</td>
            <td>{{ attempt.score }}</td>
            <td>{{ formatTime(attempt.timeTaken) }}</td>
            <td>{{ formatDate(attempt.attemptedAt) }}</td>
          </tr>
        </tbody>
      </table>
      
      <!-- Loading -->
      <div v-if="loading" class="loading">
        Đang tải dữ liệu...
      </div>
      
      <!-- Empty state -->
      <div v-if="!loading && attempts.length === 0" class="empty-state">
        Không có lịch sử làm quiz nào.
      </div>
    </div>

    <!-- Phân trang -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        Trước
      </button>
      
      <span class="page-info">
        Trang {{ currentPage }} / {{ totalPages }}
      </span>
      
      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        Sau
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { quizAttemptService } from '@/services/quizAttemptService'

// Props
const props = defineProps({
  title: {
    type: String,
    default: 'Lịch sử làm quiz'
  },
  showUserFilter: {
    type: Boolean,
    default: true
  }
})

// Store
const userStore = useUserStore()

// Reactive data
const attempts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)

// Filters
const filters = ref({
  userId: '',
  quizId: ''
})

// Mock data cho demo (trong thực tế sẽ lấy từ API)
const users = ref([
  { id: 1, username: 'nguyenvana' },
  { id: 2, username: 'tranthib' }
])

const quizzes = ref([
  { id: 1, title: 'Quiz Toán cơ bản' },
  { id: 2, title: 'Quiz Lịch sử Việt Nam' }
])

// Computed
const isAdmin = computed(() => userStore.isAdmin())

// Methods
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // API dùng 0-based index
      size: pageSize.value,
      ...(filters.value.userId && { userId: filters.value.userId }),
      ...(filters.value.quizId && { quizId: filters.value.quizId })
    }
    
    const response = await quizAttemptService.getQuizAttempts(params)
    
    attempts.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (error) {
    console.error('Error loading quiz attempts:', error)
    // Trong thực tế sẽ hiển thị thông báo lỗi
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadData()
  }
}

const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('vi-VN')
}

// Lifecycle
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.quiz-history-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 15px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-group label {
  font-weight: bold;
  font-size: 14px;
}

.filter-group select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 150px;
}

.table-container {
  margin-bottom: 20px;
}

.quiz-history-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.quiz-history-table th,
.quiz-history-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.quiz-history-table th {
  background-color: #f8f9fa;
  font-weight: bold;
}

.quiz-history-table tr:hover {
  background-color: #f5f5f5;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

.page-btn:hover:not(:disabled) {
  background: #f0f0f0;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: bold;
}
</style> 