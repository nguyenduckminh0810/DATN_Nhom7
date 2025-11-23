<template>
  <div class="quiz-trash-container">
    <!-- Header Section -->
    <div class="section-header">
      <div class="header-content">
        <div class="header-icon">
          <i class="bi bi-trash3"></i>
        </div>
        <div class="header-text">
          <h2 class="section-title">Thùng Rác</h2>
          <p class="section-subtitle">Các quiz đã bị xóa và có thể khôi phục</p>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Đang tải...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="quizzes.length === 0" class="empty-state">
      <div class="empty-icon">
        <i class="bi bi-trash3"></i>
      </div>
      <h3>Thùng rác trống</h3>
      <p>Không có quiz nào đã bị xóa</p>
    </div>

    <!-- Quiz List -->
    <div v-else class="quiz-grid">
      <div v-for="quiz in quizzes" :key="quiz.id" class="quiz-card deleted">
        <!-- Quiz Image -->
        <div class="quiz-image">
          <img :src="quiz.image ? `/api/image/quiz/${quiz.id}` : '/img/hero-img.png'" :alt="quiz.title"
            @error="handleImageError" @load="handleImageLoad" />
          <div class="deleted-overlay">
            <i class="bi bi-trash3"></i>
            <span>Đã xóa</span>
          </div>
        </div>

        <!-- Quiz Info -->
        <div class="quiz-info">
          <h3 class="quiz-title">{{ quiz.title }}</h3>
          <p class="quiz-category">{{ quiz.category?.name || 'Không phân loại' }}</p>
          <p class="quiz-date">Xóa ngày: {{ formatDate(quiz.deletedAt) }}</p>
        </div>

        <!-- Action Buttons -->
        <div class="quiz-actions">
          <button class="restore-btn" @click="restoreQuiz(quiz.id)" title="Khôi phục quiz">
            <i class="bi bi-arrow-clockwise"></i>
            Khôi phục
          </button>
          <button class="delete-btn" @click="hardDeleteQuiz(quiz.id)" title="Xóa hoàn toàn">
            <i class="bi bi-trash3-fill"></i>
            Xóa vĩnh viễn
          </button>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="currentPage === 0" @click="changePage(currentPage - 1)" class="page-btn">
        <i class="bi bi-chevron-left"></i>
      </button>

      <span class="page-info">
        Trang {{ currentPage + 1 }} / {{ totalPages }}
      </span>

      <button :disabled="currentPage === totalPages - 1" @click="changePage(currentPage + 1)" class="page-btn">
        <i class="bi bi-chevron-right"></i>
      </button>
    </div>

    <!-- Toast Notification -->
    <div v-if="toast.show" :class="['toast', `toast-${toast.type}`]">
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'

const router = useRouter()

// Reactive data
const quizzes = ref([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const totalItems = ref(0)
const pageSize = ref(6)

// Toast notification
const toast = ref({ show: false, type: 'info', message: '' })

// Get user ID from localStorage
const getUserId = () => {
  const storedUserId = localStorage.getItem('userId')
  return storedUserId ? parseInt(storedUserId) : null
}

// Fetch deleted quizzes
const fetchDeletedQuizzes = async (page = 0) => {
  const userId = getUserId()
  if (!userId) {
    showToast('Vui lòng đăng nhập để xem thùng rác', 'error')
    return
  }

  loading.value = true
  try {
    console.log(' Fetching deleted quizzes for user:', userId, 'page:', page)
    const response = await api.get(`/quiz/user/${userId}/deleted/paginated`, {
      params: {
        page: page,
        size: pageSize.value
      }
    })

    const data = response.data
    console.log(' Received deleted quiz data:', data)

    quizzes.value = data.quizzes || []
    currentPage.value = data.currentPage || 0
    totalPages.value = data.totalPages || 0
    totalItems.value = data.totalItems || 0

    console.log(' Deleted quizzes loaded successfully')
  } catch (error) {
    console.error(' Error fetching deleted quizzes:', error)
    showToast('Có lỗi xảy ra khi tải danh sách quiz đã xóa', 'error')
  } finally {
    loading.value = false
  }
}

// Restore quiz
const restoreQuiz = async (quizId) => {
  if (!confirm('Bạn có chắc chắn muốn khôi phục quiz này?')) {
    return
  }

  try {
    const response = await api.post(`/quiz/${quizId}/restore`)

    if (response.status === 200 && response.data && response.data.success) {
      showToast(response.data.message || 'Quiz đã được khôi phục thành công!', 'success')

      console.log(' Quiz restored successfully, refreshing list...')
      await fetchDeletedQuizzes(currentPage.value)
      console.log(' Deleted quiz list refreshed')

      // Thông báo cho các component khác
      window.dispatchEvent(new CustomEvent('quizRestored', {
        detail: { quizId: quizId }
      }))
    } else {
      showToast('Có lỗi xảy ra khi khôi phục quiz. Vui lòng thử lại.', 'error')
    }
  } catch (error) {
    console.error('Lỗi khi khôi phục quiz:', error)
    showToast('Có lỗi xảy ra khi khôi phục quiz. Vui lòng thử lại.', 'error')
  }
}

// Hard delete quiz
const hardDeleteQuiz = async (quizId) => {
  if (!confirm('Bạn có chắc chắn muốn xóa hoàn toàn quiz này? Hành động này không thể hoàn tác và sẽ xóa vĩnh viễn tất cả dữ liệu liên quan.')) {
    return
  }

  try {
    const response = await api.delete(`/quiz/${quizId}/hard`)

    if (response.status === 200 && response.data && response.data.success) {
      showToast(response.data.message || 'Quiz đã được xóa hoàn toàn!', 'success')

      console.log(' Quiz hard deleted successfully, refreshing list...')
      await fetchDeletedQuizzes(currentPage.value)
      console.log(' Deleted quiz list refreshed')

      // Thông báo cho các component khác
      window.dispatchEvent(new CustomEvent('quizHardDeleted', {
        detail: { quizId: quizId }
      }))
    } else {
      showToast('Có lỗi xảy ra khi xóa quiz. Vui lòng thử lại.', 'error')
    }
  } catch (error) {
    console.error('Lỗi khi xóa hoàn toàn quiz:', error)
    showToast('Có lỗi xảy ra khi xóa quiz. Vui lòng thử lại.', 'error')
  }
}

// Change page
const changePage = async (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    await fetchDeletedQuizzes(page)
  }
}

// Show toast notification
const showToast = (message, type = 'info') => {
  toast.value = { show: true, type, message }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// Format date
const formatDate = (str) => (str ? new Date(str).toLocaleDateString('vi-VN') : '')

// Handle image error
const handleImageError = (event) => {
  event.target.src = '/img/hero-img.png'
  event.target.style.opacity = '0.7'
  event.preventDefault()
}

// Handle image load
const handleImageLoad = (event) => {
  event.target.style.opacity = '1'
}

// Listen for quiz events
onMounted(() => {
  fetchDeletedQuizzes()

  // Listen for quiz deletion events
  window.addEventListener('quizDeleted', () => {
    fetchDeletedQuizzes(currentPage.value)
  })
})
</script>

<style scoped>
.quiz-trash-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: var(--app-background);
  border-radius: 15px;
  color: white;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-icon {
  font-size: 2rem;
}

.section-title {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
}

.section-subtitle {
  margin: 5px 0 0 0;
  opacity: 0.9;
}

.loading-container {
  text-align: center;
  padding: 50px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.empty-state p {
  margin: 0;
  color: #666;
}

.quiz-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.quiz-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}

.quiz-card.deleted {
  opacity: 0.8;
  border: 2px solid #ff6b6b;
}

.quiz-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.quiz-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.quiz-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.deleted-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 107, 107, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
}

.deleted-overlay i {
  font-size: 2rem;
  margin-bottom: 10px;
}

.quiz-info {
  padding: 20px;
}

.quiz-title {
  margin: 0 0 10px 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.quiz-category {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 0.9rem;
}

.quiz-date {
  margin: 0;
  color: #ff6b6b;
  font-size: 0.8rem;
  font-weight: 600;
}

.quiz-actions {
  padding: 0 20px 20px;
  display: flex;
  gap: 10px;
}

.restore-btn,
.delete-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.restore-btn {
  background: #4CAF50;
  color: white;
}

.restore-btn:hover {
  background: #45a049;
  transform: translateY(-2px);
}

.delete-btn {
  background: #f44336;
  color: white;
}

.delete-btn:hover {
  background: #da190b;
  transform: translateY(-2px);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page-btn {
  padding: 10px 15px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
}

.page-btn:hover:not(:disabled) {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 600;
  color: #333;
}

.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

.toast-success {
  background: #4CAF50;
}

.toast-error {
  background: #f44336;
}

.toast-info {
  background: #2196F3;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .quiz-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .quiz-actions {
    flex-direction: column;
  }
}
</style>