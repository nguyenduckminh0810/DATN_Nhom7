<script setup>
// Imports
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLogin } from './useLogin'
import api from '@/utils/axios' // Use the axios instance from utils
import QuizDetailModal from './QuizDetailModal.vue'
// State & Hooks
const router = useRouter()
const { userId, getUserId } = useLogin()

// Data
const quizzes = ref([])
const allQuizzes = ref([])
const categories = ref([])
const currentPage = ref(0)
const totalPages = ref(1)
const isLoading = ref(true)
const error = ref('')
const search = ref('')
const filterPublic = ref('all')
const filterCategory = ref('all')
const hoveredQuiz = ref(null)
const toast = ref({ show: false, message: '', type: 'info' })

// ✅ THÊM STATE CHO QUIZ CODE MODAL
const showCodeModal = ref(false)
const quizCode = ref('')
const quizTitle = ref('')

// ✅ LƯU QUIZ INFO ĐỂ SHARE
const quizInfo = ref(null)
// ✅ METHOD XEM QUIZ CODE
const viewQuizCode = async (quizId) => {
  try {
    const response = await api.get(`/quiz/${quizId}/code`)

    if (response.data.success) {
      quizCode.value = response.data.quizCode
      quizTitle.value = response.data.quizTitle
      // ✅ LƯU QUIZ INFO ĐỂ SHARE
      quizInfo.value = {
        quizId: quizId,
        quizCode: response.data.quizCode,
        title: response.data.quizTitle,
      }
      showCodeModal.value = true
    } else {
      showToast(response.data.message || 'Lỗi khi lấy mã code', 'error')
    }
  } catch (error) {
    console.error('❌ Error getting quiz code:', error)
    showToast('Lỗi khi lấy mã code', 'error')
  }
}

// ✅ COPY CODE
const copyQuizCode = async () => {
  try {
    await navigator.clipboard.writeText(quizCode.value)
    showToast('Đã copy mã code!', 'success')
  } catch (error) {
    console.error('Lỗi khi copy:', error)
    showToast('Lỗi khi copy mã code', 'error')
  }
}

// ✅ SHARE CODE
const shareCode = async () => {
  try {
    // ✅ TẠO LINK TRỰC TIẾP ĐẾN QUIZ PLAY PAGE
    const userId = localStorage.getItem('userId') || '1'
    const quizId = quizInfo.value?.quizId
    const shareUrl = `${window.location.origin}/quiz/${quizId}/${userId}/play`
    const shareText = `Tham gia quiz "${quizTitle.value}" với mã code: ${quizCode.value}\nLink trực tiếp: ${shareUrl}`

    if (navigator.share) {
      await navigator.share({
        title: 'Tham gia Quiz',
        text: shareText,
        url: shareUrl,
      })
    } else {
      // Fallback: copy to clipboard
      await navigator.clipboard.writeText(shareText)
      showToast('Đã copy thông tin chia sẻ!', 'success')
    }
  } catch (error) {
    console.error('Error sharing:', error)
    showToast('Lỗi khi chia sẻ!', 'error')
  }
}

const pageSize = 6

// Methods
const fetchCategories = async () => {
  try {
    const { data } = await api.get('/categories')
    categories.value = data
  } catch (error) {
    console.error('Error fetching categories:', error)
  }
}

const fetchQuizzes = async (page = 0) => {
  // Kiểm tra token trước khi gọi API
  const token = localStorage.getItem('token')
  if (!token) {
    console.error('❌ No token found - redirecting to login')
    router.push('/login')
    return
  }

  isLoading.value = true
  try {
    const { data } = await api.get(`/quiz/user/${userId.value}/paginated`, {
      params: { page, size: pageSize },
    })

    allQuizzes.value = data.quizzes.map((q) => {
      const mappedQuiz = {
        quiz_id: q.id,
        title: q.title,
        user_id: q.user?.id,
        fullName: q.user?.fullName,
        categoryName: q.category?.name,
        categoryDescription: q.category?.description,
        publicQuiz:
          q.isPublic !== undefined ? q.isPublic : q.public !== undefined ? q.public : true,
        playCount: q.playCount || 0,
        createdAt: q.createdAt,
        imageUrl: q.imageUrl || null,
      }
      return mappedQuiz
    })
    currentPage.value = data.currentPage
    totalPages.value = data.totalPages
    applyFilters()
  } catch (err) {
    if (err.response?.status === 403) {
      console.error('❌ 403 Forbidden - Token may be invalid or expired')
      // Xóa token và chuyển hướng về login
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      router.push('/login')
    } else {
      error.value = 'Không thể tải quiz.'
      console.error('❌ Error fetching quizzes:', err)
    }
  } finally {
    isLoading.value = false
  }
}

const getQuizImageUrl = (quizId) => `/api/image/quiz/${quizId}`

const applyFilters = () => {
  let filtered = allQuizzes.value
  if (search.value.trim()) {
    const term = search.value.trim().toLowerCase()
    filtered = filtered.filter((q) => q.title.toLowerCase().includes(term))
  }
  if (filterPublic.value !== 'all') {
    const isPublic = filterPublic.value === 'public'
    filtered = filtered.filter((q) => q.publicQuiz === isPublic)
  }
  if (filterCategory.value !== 'all') {
    filtered = filtered.filter((q) => q.categoryName === filterCategory.value)
  }
  quizzes.value = filtered
}

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) fetchQuizzes(page)
}

const playQuiz = async (quizId) => {
  try {
    const { quizAttemptService } = await import('@/services/quizAttemptService')
    const resp = await quizAttemptService.startAttempt(quizId)
    router.push({ name: 'PlayAttempt', params: { attemptId: resp.attemptId } })
  } catch (e) {
    console.error('Không thể bắt đầu attempt:', e)
  }
}
// Quiz Detail Modal
const showDetailModal = ref(false)
const selectedQuizId = ref(null)

const openDetailModal = (quizId) => {
  console.log('🔍 Opening detail modal for quiz ID:', quizId)
  selectedQuizId.value = quizId
  showDetailModal.value = true
  console.log(
    '✅ Modal state - showDetailModal:',
    showDetailModal.value,
    'selectedQuizId:',
    selectedQuizId.value,
  )
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedQuizId.value = null
}
const editQuiz = (quizId) => router.push(`/quiz-crud/edit/${userId.value}/${quizId}`)

// ✅ THÊM METHOD XÓA QUIZ (SOFT DELETE)
const deleteQuiz = async (quizId) => {
  if (!confirm('Bạn có chắc chắn muốn xóa quiz này? Hành động này không thể hoàn tác.')) {
    return
  }

  try {
    const response = await api.delete(`/quiz/${quizId}`)

    // Kiểm tra status code và response data
    if (response.status === 200 && response.data && response.data.success) {
      // Hiển thị thông báo thành công
      showToast(response.data.message || 'Quiz đã được xóa thành công!', 'success')

      console.log('✅ Quiz deleted successfully, refreshing list...')
      // Refresh danh sách quiz
      await fetchQuizzes(currentPage.value)
      console.log('✅ Quiz list refreshed')

      // ✅ THÔNG BÁO CHO CÁC COMPONENT KHÁC BIẾT QUIZ ĐÃ BỊ XÓA
      window.dispatchEvent(
        new CustomEvent('quizDeleted', {
          detail: { quizId: quizId },
        }),
      )
    } else {
      showToast('Có lỗi xảy ra khi xóa quiz. Vui lòng thử lại.', 'error')
    }
  } catch (error) {
    console.error('Lỗi khi xóa quiz:', error)
    console.error('Error response:', error.response)
    console.error('Error status:', error.response?.status)
    console.error('Error data:', error.response?.data)

    // Kiểm tra nếu là lỗi 403 (Forbidden) - có thể do token hết hạn
    if (error.response && error.response.status === 403) {
      showToast('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'error')
      // Có thể redirect về trang login
      // router.push('/login')
    } else if (error.response && error.response.status === 404) {
      showToast('Quiz không tồn tại hoặc đã được xóa.', 'error')
    } else {
      showToast('Có lỗi xảy ra khi xóa quiz. Vui lòng thử lại.', 'error')
    }
  }
}

// ✅ THÊM METHOD HIỂN THỊ TOAST
const showToast = (message, type = 'info') => {
  toast.value = { show: true, type, message }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

const formatDate = (str) => (str ? new Date(str).toLocaleDateString('vi-VN') : '')

// ✅ THÊM METHODS XỬ LÝ LỖI ẢNH
const handleImageError = (event) => {
  // Thay thế bằng ảnh mặc định khi lỗi
  event.target.src = '/img/hero-img.png'
  event.target.style.opacity = '0.7'
  // Ẩn lỗi 404 trong console
  event.preventDefault()
}

const handleImageLoad = (event) => {
  // Khôi phục opacity khi ảnh load thành công
  event.target.style.opacity = '1'
}

onMounted(async () => {
  // Kiểm tra token trước khi load data
  const token = localStorage.getItem('token')
  if (!token) {
    console.error('❌ No token found - redirecting to login')
    router.push('/login')
    return
  }

  await getUserId()
  await fetchCategories()
  await fetchQuizzes()
})
</script>

<!-- Template and styles are already optimized visually and functionally -->

<template>
  <div class="user-quiz-container">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <i class="bi bi-list-check "></i>
          Quiz Của Bạn
        </h1>

      </div>
    </div>
    <!-- Filter & Search -->
    <div class="filter-bar">
      <input v-model="search" @input="applyFilters" class="search-input" placeholder="Tìm kiếm quiz theo tiêu đề..." />
      <select v-model="filterPublic" @change="applyFilters" class="filter-select">
        <option value="all">Tất cả</option>
        <option value="public">Công khai</option>
        <option value="private">Riêng tư</option>
      </select>
      <select v-model="filterCategory" @change="applyFilters" class="filter-select">
        <option value="all">Tất cả thể loại</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.name">{{ cat.name }}</option>
      </select>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-skeleton">
        <div class="skeleton-card" v-for="i in 6" :key="i">
          <div class="skeleton-header"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line short"></div>
          <div class="skeleton-footer"></div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <div class="error-card">
        <i class="bi bi-exclamation-triangle error-icon"></i>
        <h3 class="error-title">Không thể tải quiz</h3>
        <p class="error-message">{{ error }}</p>
        <button class="retry-btn" @click="fetchQuizzes()">
          <i class="bi bi-arrow-clockwise"></i>
          Thử lại
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="quizzes.length === 0" class="empty-container">
      <div class="empty-card">
        <i class="bi bi-folder-x empty-icon"></i>
        <h3 class="empty-title">Không tìm thấy quiz nào</h3>
        <p class="empty-message">Bạn chưa tạo quiz nào hoặc không có quiz phù hợp bộ lọc.</p>
        <button class="create-btn" @click="router.push('/quiz-crud')">
          <i class="bi bi-plus-circle"></i>
          Tạo Quiz Đầu Tiên
        </button>
      </div>
    </div>

    <!-- Quiz Grid -->
    <div v-else class="quiz-grid">
      <div class="quiz-card" v-for="quiz in quizzes" :key="quiz.quiz_id" @mouseenter="hoveredQuiz = quiz.quiz_id"
        @mouseleave="hoveredQuiz = null">
        <!-- Card Image -->
        <div class="quiz-image">
          <img :src="getQuizImageUrl(quiz.quiz_id)" alt="Quiz Image" @error="handleImageError"
            @load="handleImageLoad" />
        </div>
        <!-- Card Header -->
        <div class="card-header">
          <div class="category-badge">
            <i class="bi bi-tag"></i>
            <span>{{ quiz.categoryName }}</span>
          </div>
          <div class="visibility-badge" :class="{ public: quiz.publicQuiz, private: !quiz.publicQuiz }">
            <i :class="quiz.publicQuiz ? 'bi bi-globe' : 'bi bi-lock'"></i>
            <span>{{ quiz.publicQuiz ? 'Công khai' : 'Riêng tư' }}</span>
          </div>
        </div>

        <!-- Card Body -->
        <div class="card-body">
          <h3 class="quiz-title">{{ quiz.title }}</h3>
          <p class="quiz-description">{{ quiz.categoryDescription }}</p>

          <div class="quiz-meta">
            <div class="author-info">
              <i class="bi bi-person-circle"></i>
              <span>{{ quiz.fullName }}</span>
            </div>
            <div class="quiz-status">
              <span class="status-text">Của bạn</span>
            </div>
          </div>
          <div class="quiz-extra">
            <span class="badge play-count"><i class="bi bi-controller"></i> {{ quiz.playCount }} lượt chơi</span>
            <span class="badge created-at"><i class="bi bi-calendar"></i> {{ formatDate(quiz.createdAt) }}</span>
          </div>
        </div>

        <!-- Hover Overlay -->
        <transition name="slide-up">
          <div v-if="hoveredQuiz === quiz.quiz_id" class="card-overlay">
            <div class="overlay-content">
              <button class="action-btn primary" @click.stop="playQuiz(quiz.quiz_id)">
                <i class="bi bi-play-circle"></i>
                <span>Chơi thử</span>
              </button>
              <button class="action-btn secondary" @click.stop="editQuiz(quiz.quiz_id)">
                <i class="bi bi-pencil-square"></i>
                <span>Chỉnh sửa</span>
              </button>
              <button class="action-btn info" @click.stop="viewQuizCode(quiz.quiz_id)">
                <i class="bi bi-key"></i>
                <span>Xem Code</span>
              </button>
              <button class="action-btn info" @click.stop="openDetailModal(quiz.quiz_id)">
                <i class="bi bi-info-circle"></i>
                <span>Chi tiết</span>
              </button>
              <button class="action-btn danger" @click.stop="deleteQuiz(quiz.quiz_id)">
                <i class="bi bi-trash"></i>
                <span>Xóa</span>
              </button>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination-container">
      <div class="pagination-wrapper">
        <button class="page-btn prev" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="bi bi-chevron-left"></i>
          <span>Trước</span>
        </button>

        <div class="page-info">
          <span class="current-page">{{ currentPage + 1 }}</span>
          <span class="divider">/</span>
          <span class="total-pages">{{ totalPages }}</span>
        </div>

        <button class="page-btn next" :disabled="currentPage === totalPages - 1" @click="goToPage(currentPage + 1)">
          <span>Sau</span>
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- Toast Notification -->
    <transition name="slide-up">
      <div v-if="toast.show" :class="['toast', toast.type]">
        <i :class="{
          'bi bi-check-circle-fill': toast.type === 'success',
          'bi bi-x-circle-fill': toast.type === 'error',
          'bi bi-info-circle-fill': toast.type === 'info',
        }"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>

    <!-- Quiz Detail Modal -->
    <QuizDetailModal :show-modal="showDetailModal" :quiz-id="selectedQuizId" @close="closeDetailModal" />
  </div>

  <!-- ✅ QUIZ CODE MODAL -->
  <div v-if="showCodeModal" class="modal-overlay" @click="showCodeModal = false">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <div class="success-icon">
          <i class="bi bi-key-fill"></i>
        </div>
        <h3>Mã Code Quiz</h3>
        <button @click="showCodeModal = false" class="modal-close">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="code-section">
          <h4>{{ quizTitle }}</h4>
          <div class="code-display">
            <div class="code-container">
              <span class="quiz-code">{{ quizCode }}</span>
              <div class="code-actions">
                <button @click="copyQuizCode" class="copy-btn" title="Copy mã code">
                  <i class="bi bi-clipboard"></i>
                  Copy
                </button>
                <button @click="shareCode" class="share-btn" title="Chia sẻ">
                  <i class="bi bi-share"></i>
                  Chia sẻ
                </button>
              </div>
            </div>
          </div>

          <div class="code-info">
            <div class="info-item">
              <i class="bi bi-info-circle"></i>
              <span>Chia sẻ mã code này cho học sinh để họ có thể tham gia quiz</span>
            </div>
            <div class="info-item">
              <i class="bi bi-clock"></i>
              <span>Mã code có hiệu lực vĩnh viễn</span>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showCodeModal = false" class="btn btn-secondary">
            <i class="bi bi-check"></i>
            Đóng
          </button>
          <RouterLink to="/join-quiz" class="btn btn-primary">
            <i class="bi bi-key"></i>
            Thử nghiệm tham gia
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero-title i {
  margin-right: 15px;
  color: #ffd700;
  filter: drop-shadow(0 0 10px rgba(255, 215, 0, 0.5));
}

.hero-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  font-weight: 400;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: white;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 1s ease-out;
}

.hero-section {
  position: relative;
  padding-top: 35px;
  text-align: center;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

/* --- BẮT ĐẦU STYLE ĐẦY ĐỦ CHO QUIZ CARD --- */
.user-quiz-container {
  margin: 60px 0;
  padding: 0 20px;
}

.section-header {
  max-width: 1200px;
  margin: 0 auto 50px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 25px;
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.8);
  position: relative;
  overflow: hidden;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 2;
}

.header-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  box-shadow: 0 8px 25px rgba(255, 107, 157, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.9);
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.section-subtitle {
  color: rgba(255, 255, 255, 0.9);
  margin: 5px 0 0;
  font-size: 1.1rem;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.create-btn {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.8);
  padding: 16px 32px;
  border-radius: 25px;
  font-weight: 700;
  font-size: 1.1rem;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(0, 212, 255, 0.5);
  background: linear-gradient(45deg, #00b8d4, #0288d1);
  border-color: white;
}

.filter-bar {
  max-width: 1200px;
  margin: 0 auto 30px;
  display: flex;
  gap: 18px;
  align-items: center;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  padding: 18px 24px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.08);
}

.search-input {
  flex: 2;
  padding: 12px 18px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  font-size: 1rem;
  outline: none;
  transition: border 0.2s;
}

.search-input:focus {
  border-color: #00d4ff;
}

.filter-select {
  flex: 1;
  padding: 12px 18px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  font-size: 1rem;
  outline: none;
  background: white;
  margin-left: 8px;
}

.quiz-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
}

.quiz-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border: 3px solid rgba(255, 255, 255, 0.7);
  border-radius: 25px;
  padding: 0;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

.quiz-card:hover {
  transform: translateY(-10px);
  border: 3px solid #ff6b9d;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.2);
}

.card-header {
  padding: 20px 25px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-badge,
.visibility-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 15px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.category-badge {
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  color: white;
}

.visibility-badge.public {
  background: linear-gradient(45deg, #4ecdc4, #26d0ce);
  color: white;
}

.visibility-badge.private {
  background: linear-gradient(45deg, #a0aec0, #718096);
  color: white;
}

.quiz-image {
  width: 100%;
  height: 160px;
  background: rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.quiz-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.quiz-image.default {
  color: #a0aec0;
  font-size: 3rem;
}

.card-body {
  padding: 0 25px 25px;
}

.quiz-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
  line-height: 1.3;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);

  /* Giới hạn chữ */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quiz-description {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.quiz-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  font-weight: 600;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.author-info i {
  font-size: 1.2rem;
  color: #ff6b9d;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.quiz-status {
  background: rgba(255, 107, 157, 0.3);
  backdrop-filter: blur(10px);
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 107, 157, 0.5);
}

.status-text {
  color: #ff6b9d;
  font-size: 0.8rem;
  font-weight: 600;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.quiz-extra {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.18);
  color: #1a202c;
  font-size: 0.92rem;
  font-weight: 600;
  border-radius: 12px;
  padding: 4px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.07);
}

.badge.play-count i {
  color: #00b8d4;
}

.badge.created-at i {
  color: #ff6b9d;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
      rgba(0, 0, 0, 0.85) 0%,
      rgba(255, 107, 157, 0.9) 50%,
      rgba(255, 61, 113, 0.9) 100%);
  backdrop-filter: blur(15px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border: 2px solid rgba(255, 255, 255, 0.8);

  /* Animation */
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s ease-in-out;
  pointer-events: none;
}

.quiz-card:hover .card-overlay {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  padding: 15px;
  max-height: 80%;
  overflow-y: auto;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.85rem;
  transition: all 0.3s ease;
  cursor: pointer;
  min-width: 120px;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.action-btn.primary {
  background: linear-gradient(45deg, #00d4ff, #00b8d4);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.action-btn.primary:hover {
  background: linear-gradient(45deg, #00b8d4, #0288d1);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
  border-color: white;
}

.action-btn.secondary {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
  color: #1a202c;
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.action-btn.secondary:hover {
  background: linear-gradient(45deg, #ffed4e, #ffd700);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
  border-color: white;
}

.action-btn.info {
  background: rgba(255, 255, 255, 0.9);
  color: #1a202c;
  border: 2px solid rgba(255, 255, 255, 1);
}

.action-btn.info:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 255, 255, 0.4);
}

.action-btn.danger {
  background: linear-gradient(45deg, #ff4757, #ff3742);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.action-btn.danger:hover {
  background: linear-gradient(45deg, #ff3742, #ff2d55);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 71, 87, 0.4);
  border-color: white;
}

/* ✅ RESPONSIVE CHO MOBILE */
@media (max-width: 768px) {
  .overlay-content {
    gap: 8px;
    padding: 10px;
  }

  .action-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
    min-width: 100px;
  }
}

.pagination-container {
  max-width: 1200px;
  margin: 50px auto 0;
  display: flex;
  justify-content: center;
}

.pagination-wrapper {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 25px;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.8);
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 20px;
  background: linear-gradient(45deg, #ff6b9d, #ff3d71);
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 107, 157, 0.3);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(45deg, #ff3d71, #ff6b9d);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 157, 0.4);
  border-color: white;
}

.page-btn:disabled {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.5);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.page-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.current-page {
  color: #ff6b9d;
  font-size: 1.2rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.divider {
  color: rgba(255, 255, 255, 0.7);
}

.total-pages {
  color: rgba(255, 255, 255, 0.9);
}

@keyframes pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.7;
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.toast {
  position: fixed;
  top: 2rem;
  right: 2rem;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 15px;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.3);
  z-index: 1000;
  min-width: 300px;
  font-weight: 600;
  font-size: 1rem;
}

.toast.success {
  border-left: 4px solid #2ed573;
  color: #2ed573;
}

.toast.error {
  border-left: 4px solid #ff4757;
  color: #ff4757;
}

.toast.info {
  border-left: 4px solid #00d4ff;
  color: #00d4ff;
}

.toast i {
  font-size: 1.5rem;
}

/* --- KẾT THÚC STYLE ĐẦY ĐỦ --- */

/* === QUIZ CODE MODAL === */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px;
  border-radius: 20px 20px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.success-icon {
  font-size: 2rem;
  color: #28a745;
  margin-right: 15px;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  flex: 1;
}

.modal-close {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 30px;
}

.code-section {
  text-align: center;
  margin-bottom: 30px;
}

.code-section h4 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.2rem;
}

.code-display {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 20px;
}

.code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.quiz-code {
  font-size: 2.5rem;
  font-weight: 700;
  color: #667eea;
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
  background: white;
  padding: 15px 25px;
  border-radius: 10px;
  border: 2px solid #e9ecef;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.code-actions {
  display: flex;
  gap: 10px;
}

.copy-btn,
.share-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.copy-btn {
  background: #667eea;
  color: white;
}

.copy-btn:hover {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.share-btn {
  background: #28a745;
  color: white;
}

.share-btn:hover {
  background: #218838;
  transform: translateY(-1px);
}

.code-info {
  margin-top: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 8px;
}

.info-item i {
  color: #667eea;
  font-size: 1rem;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.modal-actions .btn {
  padding: 12px 25px;
  border-radius: 10px;
  font-weight: 600;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}
</style>
