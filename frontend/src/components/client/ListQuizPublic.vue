<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import debounce from 'lodash.debounce'
import ReportModal from './ReportModal.vue'
import QuizDetailModal from './QuizDetailModal.vue'
import { Modal } from 'bootstrap'
import api from '@/utils/axios'
const publicQuizzes = ref([])
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 8
const isLoading = ref(true)
const error = ref('')
const router = useRouter()

const quizCache = new Map()
let hoveredQuizId = null
function onQuizReported(quiz) {
  console.log('Đã báo cáo quiz:', quiz)
  // Hoặc xử lý gì đó như: xóa quiz, reload list...
}

async function fetchPublicQuizzes(page = 0) {
  if (quizCache.has(page)) {
    const cached = quizCache.get(page)
    publicQuizzes.value = cached.content
    currentPage.value = cached.page
    totalPages.value = cached.totalPages
    return
  }

  isLoading.value = true
  try {
    const res = await api.get(`/quiz/public`, {
      params: { page, size: pageSize },
    })


    const transformed = res.data.content.map(q => {
      const mappedQuiz = {
        quiz_id: q.id,
        title: q.title,
        fullName: q.user?.fullName || 'Unknown',
        categoryName: q.category?.name || 'Uncategorized',
        categoryDescription: q.category?.description || '',
        publicQuiz: q.isPublic !== undefined ? q.isPublic : (q.public !== undefined ? q.public : true)
      }
      return mappedQuiz
    })


    quizCache.set(page, {
      content: transformed,
      page: res.data.number,
      totalPages: res.data.totalPages,
    })

    publicQuizzes.value = transformed
    currentPage.value = res.data.number
    totalPages.value = res.data.totalPages
  } catch (err) {
    error.value = 'Không thể tải quiz công khai.'
    console.error('❌ Error fetching public quizzes:', err)
  } finally {
    isLoading.value = false
  }
}

const fetchPublicQuizzesDebounced = debounce(fetchPublicQuizzes, 300)

// ✅ THÊM METHOD ĐỂ REFRESH DANH SÁCH
const refreshPublicQuizzes = () => {
  quizCache.clear() // Xóa cache
  fetchPublicQuizzes(currentPage.value) // Tải lại data
}

onMounted(() => {
  fetchPublicQuizzes()
  
  // ✅ THÊM EVENT LISTENER ĐỂ LẮNG NGHE KHI CÓ QUIZ BỊ XÓA
  window.addEventListener('quizDeleted', refreshPublicQuizzes)
})

function goToPage(page) {
  if (page >= 0 && page < totalPages.value) {
    fetchPublicQuizzesDebounced(page)
  }
}

function playQuiz(quizId) {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    console.error('❌ Missing userId - user not logged in')
    // Có thể chuyển hướng đến trang login
    router.push({ name: 'Login' })
    return
  }
  console.log('🎮 Playing quiz:', quizId, 'for user:', userId)
  router.push({ name: 'PlayQuiz', params: { quizId, userId } })
}

function goToQuizDetail(quizId) {
  if (router.currentRoute.value.params.id !== quizId.toString()) {
    router.push({ name: 'QuizDetail', params: { id: quizId } })
  }
}
const reportModalRef = ref(null)
const reportQuiz = (quiz) => {
  const quizData = {
    quiz_id: quiz.quiz_id,
    id: quiz.quiz_id,
    title: quiz.title,
    author: quiz.fullName,
  }
  reportModalRef.value?.openModal(quizData)
  setTimeout(() => new Modal(document.getElementById('reportModal')).show(), 100)
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

function handleImageError(event) {
  const canvas = document.createElement('canvas')
  canvas.width = 300
  canvas.height = 200
  const ctx = canvas.getContext('2d')
  const gradient = ctx.createLinearGradient(0, 0, 300, 200)
  gradient.addColorStop(0, '#667eea')
  gradient.addColorStop(1, '#764ba2')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, 300, 200)
  ctx.fillStyle = 'white'
  ctx.font = 'bold 18px Arial'
  ctx.textAlign = 'center'
  ctx.fillText('Quiz Image', 150, 100)
  ctx.font = '14px Arial'
  ctx.fillText('Không có ảnh', 150, 125)
  event.target.src = canvas.toDataURL()
}
</script>

<template>
  <div class="quiz-public-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <i class="bi bi-globe2"></i>
          Quiz Công Khai
        </h1>
        <p class="hero-subtitle">Khám phá hàng ngàn quiz thú vị được chia sẻ bởi cộng đồng</p>
      </div>
      <div class="hero-decoration">
        <div class="floating-icon">🧠</div>
        <div class="floating-icon">📚</div>
        <div class="floating-icon">🏆</div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container-fluid px-4">
      <!-- Loading State -->
      <div v-if="isLoading" class="loading-container">
        <!-- Loading State (Skeleton) -->
        <div v-if="isLoading" class="quiz-skeleton-grid">
          <div v-for="i in 4" :key="i" class="quiz-skeleton-card"></div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-container">
        <div class="error-card">
          <i class="bi bi-exclamation-triangle-fill"></i>
          <h3>Có lỗi xảy ra</h3>
          <p>{{ error }}</p>
          <button @click="fetchPublicQuizzes()" class="btn btn-primary">
            <i class="bi bi-arrow-clockwise"></i>
            Thử lại
          </button>
        </div>
      </div>

      <!-- Quiz Grid -->
      <div v-else>
        <!-- Empty State -->
        <div v-if="publicQuizzes.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-inbox"></i>
          </div>
          <h3>Chưa có quiz công khai</h3>
          <p>Hiện tại chưa có quiz nào được chia sẻ công khai.</p>
        </div>

        <!-- Quiz Cards -->
        <div v-else class="quiz-grid">
          <div
            v-for="quiz in publicQuizzes"
            :key="quiz.quiz_id"
            class="quiz-card"
            @mouseenter="hoveredQuizId = quiz.quiz_id"
            @mouseleave="hoveredQuizId = null"
          >
            <!-- Quiz Image -->
            <div class="quiz-image-container">
              <img :src="`/api/image/quiz/${quiz.quiz_id}`" :alt="quiz.title" class="quiz-image"
                @error="handleImageError" loading="lazy" />

              <div class="quiz-overlay">
                <button
                  @click="playQuiz(quiz.quiz_id)"
                  class="play-btn"
                  :class="{ hovered: hoveredQuizId === quiz.quiz_id }"
                >
                  <i class="bi bi-play-fill"></i>
                  Chơi ngay
                </button>
              </div>
            </div>

            <!-- Quiz Content -->
            <div class="quiz-content">
              <div class="quiz-header">
                <h3 class="quiz-title" @click="goToQuizDetail(quiz.quiz_id)">
                  {{ quiz.title }}
                </h3>
                <div class="quiz-actions">
                  <button
                    @click="reportQuiz(quiz)"
                    class="action-btn report-btn"
                    title="Báo cáo quiz"
                  >
                    <i class="bi bi-flag"></i>
                  </button>
                </div>
              </div>

              <div class="quiz-meta">
                <div class="meta-item">
                  <i class="bi bi-person-fill"></i>
                  <span>{{ quiz.fullName }}</span>
                </div>
                <div class="meta-item">
                  <i class="bi bi-tag-fill"></i>
                  <span>{{ quiz.categoryName }}</span>
                </div>
              </div>

              <p class="quiz-description">
                {{ quiz.categoryDescription }}
              </p>

              <div class="quiz-footer">
                <span class="quiz-status public">
                  <i class="bi bi-globe2"></i>
                  Công khai
                </span>
                <button @click="openDetailModal(quiz.quiz_id)" class="detail-btn">
                  <i class="bi bi-info-circle"></i>
                  Chi tiết
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination-container">
          <nav aria-label="Phân trang quiz">
            <ul class="pagination">
              <!-- Previous Button -->
              <li class="page-item" :class="{ disabled: currentPage === 0 }">
                <button
                  class="page-link"
                  @click="goToPage(currentPage - 1)"
                  :disabled="currentPage === 0"
                >
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>

              <!-- Page Numbers -->
              <li
                v-for="page in Math.min(5, totalPages)"
                :key="page"
                class="page-item"
                :class="{ active: currentPage === page - 1 }"
              >
                <button class="page-link" @click="goToPage(page - 1)">
                  {{ page }}
                </button>
              </li>

              <!-- Next Button -->
              <li class="page-item" :class="{ disabled: currentPage >= totalPages - 1 }">
                <button
                  class="page-link"
                  @click="goToPage(currentPage + 1)"
                  :disabled="currentPage >= totalPages - 1"
                >
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
        </div>
        <ReportModal ref="reportModalRef" @reported="onQuizReported" />

        <!-- Quiz Detail Modal -->
        <QuizDetailModal
          :show-modal="showDetailModal"
          :quiz-id="selectedQuizId"
          @close="closeDetailModal"
        />
      </div>
    </div>

    <!-- Report Modal -->
    <!-- <ReportModal 
      ref="reportModalRef" 
      @quiz-reported="onQuizReported"
    /> -->
  </div>
</template>

<style scoped>
/* Skeleton Loading */
.quiz-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  padding: 20px 0;
}

.quiz-skeleton-card {
  height: 320px;
  border-radius: 20px;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }

  100% {
    background-position: 200% 0;
  }
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 60px 0;
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

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: white;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 1s ease-out;
}

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

.floating-icon {
  position: absolute;
  font-size: 2rem;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.floating-icon:nth-child(1) {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.floating-icon:nth-child(2) {
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.floating-icon:nth-child(3) {
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

/* Container */
.container-fluid {
  position: relative;
  z-index: 2;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 20px;
}

.loading-text {
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0;
}

.spinner-border {
  width: 3rem;
  height: 3rem;
}

/* Error State */
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.error-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  max-width: 400px;
}

.error-card i {
  font-size: 3rem;
  color: #dc3545;
  margin-bottom: 20px;
}

.error-card h3 {
  color: #333;
  margin-bottom: 15px;
  font-weight: 600;
}

.error-card p {
  color: #666;
  margin-bottom: 25px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: white;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 30px;
  opacity: 0.7;
}

.empty-state h3 {
  font-size: 2rem;
  margin-bottom: 15px;
  font-weight: 600;
}

.empty-state p {
  font-size: 1.1rem;
  opacity: 0.8;
  max-width: 500px;
  margin: 0 auto;
}

/* Quiz Grid */
.quiz-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  padding: 20px 0;
}

/* Quiz Card */
.quiz-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.quiz-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}

/* Quiz Image */
.quiz-image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.quiz-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.quiz-card:hover .quiz-image {
  transform: scale(1.05);
}

.quiz-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.quiz-card:hover .quiz-overlay {
  opacity: 1;
}

.play-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  transform: translateY(20px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
}

.play-btn.hovered {
  transform: translateY(0);
}

.play-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
}

.play-btn i {
  margin-right: 8px;
  font-size: 1.1rem;
}

/* Quiz Content */
.quiz-content {
  padding: 25px;
}

.quiz-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.quiz-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin: 0;
  cursor: pointer;
  transition: color 0.3s ease;
  flex: 1;
  margin-right: 15px;
  line-height: 1.4;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quiz-title:hover {
  color: #667eea;
}

.quiz-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.report-btn {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.report-btn:hover {
  background: #dc3545;
  color: white;
  transform: scale(1.1);
}

.quiz-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 0.9rem;
}

.meta-item i {
  color: #667eea;
  font-size: 0.8rem;
}

.quiz-description {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.quiz-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quiz-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.quiz-status.public {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.detail-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: center;
  margin: 50px 0 30px;
}

.pagination {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 15px;
  padding: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.page-item {
  margin: 0 2px;
}

.page-link {
  background: transparent;
  border: none;
  color: #667eea;
  padding: 10px 15px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 45px;
}

.page-link:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateY(-2px);
}

.page-item.active .page-link {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.page-item.disabled .page-link {
  color: #ccc;
  cursor: not-allowed;
}

.page-item.disabled .page-link:hover {
  background: transparent;
  transform: none;
}

/* Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-20px);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.1rem;
  }

  .quiz-grid {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px 10px;
  }

  .quiz-card {
    margin: 0 10px;
  }

  .quiz-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .quiz-actions {
    align-self: flex-end;
  }
}

@media (max-width: 576px) {
  .hero-section {
    padding: 40px 0;
  }

  .hero-title {
    font-size: 2rem;
  }

  .container-fluid {
    padding-left: 15px;
    padding-right: 15px;
  }
}
</style>
