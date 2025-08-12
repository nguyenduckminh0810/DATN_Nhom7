<template>
  <div
    v-if="showModal"
    class="modal fade show d-block"
    tabindex="-1"
    style="background-color: rgba(0, 0, 0, 0.5); z-index: 1050"
  >
    <!-- ✅ LOADING SPINNER -->
    <LoadingSpinner :loading="loading" message="Đang tải thông tin chi tiết..." />

    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header bg-primary text-white">
          <h5 class="modal-title">
            <i class="bi bi-info-circle me-2"></i>
            Chi tiết Quiz
          </h5>
          <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
        </div>

        <div class="modal-body" v-if="quizDetail" :class="{ 'loading-fade': loading }">
          <!-- Content -->
          <div>
            <!-- Header Section -->
            <div class="row mb-4">
              <div class="col-md-4">
                <div class="quiz-image-container">
                  <img
                    :src="quizImageUrl"
                    :alt="quizDetail.title"
                    class="quiz-image"
                    @error="handleImageError"
                  />
                  <!-- Ẩn nhãn công khai/riêng tư theo yêu cầu -->
                </div>
              </div>

              <div class="col-md-8">
                <h3 class="quiz-title mb-3">{{ quizDetail.title }}</h3>

                <div class="quiz-meta mb-3">
                  <div class="meta-item">
                    <i class="bi bi-person-circle text-primary"></i>
                    <span
                      ><strong>Tác giả:</strong> {{ quizDetail.creatorName || 'Không rõ' }}</span
                    >
                  </div>

                  <div class="meta-item">
                    <i class="bi bi-calendar3 text-success"></i>
                    <span><strong>Ngày tạo:</strong> {{ formatDate(quizDetail.createdAt) }}</span>
                  </div>

                  <div class="meta-item">
                    <i class="bi bi-tag text-warning"></i>
                    <span
                      ><strong>Danh mục:</strong>
                      {{ quizDetail.categoryName || 'Không phân loại' }}</span
                    >
                  </div>

                  <div class="meta-item" v-if="quizDetail.tags && quizDetail.tags.length">
                    <i class="bi bi-bookmark text-info"></i>
                    <span
                      ><strong>Tags:</strong>
                      <span
                        class="badge bg-secondary me-1"
                        v-for="tag in quizDetail.tags"
                        :key="tag"
                      >
                        {{ tag }}
                      </span>
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Stats Section -->
            <div class="row mb-4">
              <div class="col-12">
                <h5 class="section-title">
                  <i class="bi bi-graph-up text-primary"></i>
                  Thống kê tổng quan
                </h5>

                <div class="stats-grid">
                  <div class="stat-card">
                    <div class="stat-icon questions">
                      <i class="bi bi-list-ul"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ quizStats.totalQuestions }}</div>
                      <div class="stat-label">Câu hỏi</div>
                    </div>
                  </div>

                  <div class="stat-card">
                    <div class="stat-icon points">
                      <i class="bi bi-star-fill"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">0</div>
                      <div class="stat-label">Điểm tối đa</div>
                    </div>
                  </div>

                  <div class="stat-card">
                    <div class="stat-icon time">
                      <i class="bi bi-clock"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ formatTime(quizStats.totalTime) }}</div>
                      <div class="stat-label">Thời gian</div>
                    </div>
                  </div>

                  <div class="stat-card">
                    <div class="stat-icon plays">
                      <i class="bi bi-play-circle"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ quizStats.totalPlays }}</div>
                      <div class="stat-label">Lượt chơi</div>
                    </div>
                  </div>

                  <div class="stat-card">
                    <div class="stat-icon avg-score">
                      <i class="bi bi-bar-chart"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ quizStats.averageScore }}%</div>
                      <div class="stat-label">Điểm TB</div>
                    </div>
                  </div>

                  <div class="stat-card">
                    <div class="stat-icon participants">
                      <i class="bi bi-people"></i>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ quizStats.uniqueParticipants }}</div>
                      <div class="stat-label">Người tham gia</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Questions Preview Section - CHỈ HIỂN THỊ CHO NGƯỜI TẠO QUIZ -->
            <div class="row mb-4" v-if="questions.length > 0 && isQuizCreator">
              <div class="col-12">
                <h5 class="section-title">
                  <i class="bi bi-question-circle text-success"></i>
                  Xem trước câu hỏi ({{ questions.length }} câu)
                </h5>

                <div class="questions-preview">
                  <!-- Preview 3 câu đầu khi chưa mở xem tất cả -->
                  <template v-if="!showAllQuestions">
                    <div
                      class="question-item"
                      v-for="(question, index) in questions.slice(0, 3)"
                      :key="question.id"
                    >
                      <div class="question-header">
                        <span class="question-number">Câu {{ index + 1 }}</span>
                        <span class="question-time">{{
                          question.timeLimit === 0 ? '∞' : question.timeLimit + 's'
                        }}</span>
                      </div>
                      <div class="question-content">
                        {{ question.content }}
                      </div>
                      <div class="question-image" v-if="question.image">
                        <img
                          :src="question.image"
                          :alt="'Hình ảnh câu hỏi ' + (index + 1)"
                          class="img-fluid"
                        />
                      </div>
                    </div>
                  </template>

                  <!-- Xem tất cả: hiển thị phân trang -->
                  <template v-else>
                    <div
                      class="question-item"
                      v-for="(question, idx) in paginatedQuestions"
                      :key="question.id"
                    >
                      <div class="question-header">
                        <span class="question-number">Câu {{ questionsPageStart + idx + 1 }}</span>
                        <span class="question-time">{{
                          question.timeLimit === 0 ? '∞' : question.timeLimit + 's'
                        }}</span>
                      </div>
                      <div class="question-content">
                        {{ question.content }}
                      </div>
                    </div>

                    <div v-if="totalQuestionPages > 1" class="questions-pagination mt-2">
                      <div class="d-flex justify-content-between align-items-center">
                        <button
                          class="btn btn-outline-primary btn-sm"
                          @click="previousQuestionsPage"
                          :disabled="currentQuestionsPage === 1"
                        >
                          <i class="bi bi-chevron-left"></i> Trước
                        </button>
                        <span class="page-info"
                          >Trang {{ currentQuestionsPage }} / {{ totalQuestionPages }}</span
                        >
                        <button
                          class="btn btn-outline-primary btn-sm"
                          @click="nextQuestionsPage"
                          :disabled="currentQuestionsPage >= totalQuestionPages"
                        >
                          Sau <i class="bi bi-chevron-right"></i>
                        </button>
                      </div>
                    </div>
                  </template>

                  <!-- Toggle button luôn ở cuối danh sách -->
                  <div v-if="questions.length > 3" class="text-center mt-3">
                    <button class="btn btn-outline-primary" @click="toggleShowAllQuestions">
                      {{
                        showAllQuestions ? 'Ẩn bớt' : 'Xem tất cả ' + questions.length + ' câu hỏi'
                      }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Thông báo cho tất cả người không phải tác giả -->
            <div class="row mb-4" v-if="questions.length > 0 && !isQuizCreator">
              <div class="col-12">
                <div class="alert alert-info">
                  <i class="bi bi-info-circle me-2"></i>
                  <strong>Thông báo:</strong> Chỉ người tạo quiz mới có thể xem trước câu hỏi để đảm
                  bảo tính công bằng.
                </div>
              </div>
            </div>

            <!-- Recent Activity Section -->
            <div class="row mb-4">
              <div class="col-12">
                <h5 class="section-title">
                  <i class="bi bi-activity text-warning"></i>
                  Hoạt động gần đây
                </h5>

                <!-- Recent Attempts - HIỂN THỊ CHO TẤT CẢ NGƯỜI DÙNG -->
                <div v-if="recentAttempts.length > 0" class="recent-activity mb-3">
                  <h6 class="activity-subtitle">
                    <i class="bi bi-play-circle text-success"></i>
                    Lượt chơi gần đây
                  </h6>
                  <div
                    class="activity-item"
                    v-for="attempt in recentAttempts.slice(0, 3)"
                    :key="attempt.id"
                  >
                    <div class="activity-avatar">
                      <i class="bi bi-person-circle"></i>
                    </div>
                    <div class="activity-content">
                      <div class="activity-text">
                        <strong>{{ attempt.userName }}</strong> đạt {{ attempt.score }} điểm
                      </div>
                      <div class="activity-time">{{ formatTimeAgo(attempt.attemptedAt) }}</div>
                    </div>
                    <div class="activity-score" :class="getScoreClass(attempt.score)">
                      {{ attempt.score }} điểm
                    </div>
                  </div>
                </div>

                <!-- Empty state - HIỂN THỊ CHO TẤT CẢ NGƯỜI DÙNG -->
                <div v-if="recentAttempts.length === 0" class="text-center py-4">
                  <i class="bi bi-info-circle text-muted" style="font-size: 2rem"></i>
                  <p class="text-muted mt-2">Chưa có lượt chơi nào</p>
                  <small class="text-muted">Hãy là người đầu tiên thử sức với quiz này!</small>
                </div>

                <!-- Comments -->
                <div class="recent-activity">
                  <h6 class="activity-subtitle">
                    <i class="bi bi-chat-dots text-primary"></i>
                    Bình luận gần đây
                  </h6>
                  <div
                    class="activity-item comment-item"
                    v-for="comment in displayedComments"
                    :key="comment.id"
                  >
                    <div class="activity-avatar">
                      <img
                        :src="comment.userAvatarUrl || '/img/default-avatar.png'"
                        :alt="comment.userFullName || comment.username"
                        class="avatar-img"
                        @error="handleAvatarError"
                      />
                    </div>
                    <div class="activity-content">
                      <div class="activity-text">
                        <strong>{{ comment.userFullName || comment.username }}</strong>
                        <span class="comment-text">{{ comment.reviewText }}</span>
                      </div>
                      <div class="activity-time">{{ formatTimeAgo(comment.createdAt) }}</div>
                      <div class="comment-rating" v-if="comment.rating">
                        <div class="stars">
                          <i
                            v-for="star in 5"
                            :key="star"
                            class="bi"
                            :class="
                              star <= comment.rating
                                ? 'bi-star-fill text-warning'
                                : 'bi-star text-muted'
                            "
                          ></i>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Show more comments button -->
                  <div v-if="comments.length > 3" class="text-center mt-3">
                    <button
                      class="btn btn-outline-primary btn-sm"
                      @click="showAllComments = !showAllComments"
                    >
                      <i
                        class="bi"
                        :class="showAllComments ? 'bi-chevron-up' : 'bi-chevron-down'"
                      ></i>
                      {{
                        showAllComments
                          ? 'Ẩn bớt'
                          : 'Xem thêm ' + (comments.length - 3) + ' bình luận'
                      }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Leaderboard Section -->
            <div class="row mb-4">
              <div class="col-12">
                <h5 class="section-title">
                  <i class="bi bi-trophy text-warning"></i>
                  Bảng xếp hạng
                </h5>
                <!-- Cải thiện Leaderboard với phân trang rõ ràng -->
                <div class="leaderboard-container">
                  <Leaderboard :quizId="props.quizId" :limit="10" ref="leaderboardRef" />

                  <!-- Pagination Controls -->
                  <div
                    class="pagination-controls mt-3"
                    v-if="leaderboardData && leaderboardData.length > 0"
                  >
                    <div class="d-flex justify-content-between align-items-center">
                      <div class="pagination-info">
                        <small class="text-muted">
                          Hiển thị {{ currentPage * 10 - 9 }}-{{
                            Math.min(currentPage * 10, totalEntries)
                          }}
                          trong tổng số {{ totalEntries }} người chơi
                        </small>
                      </div>

                      <div class="pagination-buttons">
                        <button
                          class="btn btn-outline-primary btn-sm me-2"
                          @click="previousPage"
                          :disabled="currentPage === 1"
                        >
                          <i class="bi bi-chevron-left"></i> Trước
                        </button>

                        <span class="page-info mx-2">
                          Trang {{ currentPage }} / {{ totalPages }}
                        </span>

                        <button
                          class="btn btn-outline-primary btn-sm ms-2"
                          @click="nextPage"
                          :disabled="currentPage >= totalPages"
                        >
                          Sau <i class="bi bi-chevron-right"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">
            <i class="bi bi-x-circle me-2"></i>
            Đóng
          </button>
          <button class="btn btn-primary" @click="playQuiz" v-if="quizDetail">
            <i class="bi bi-play-circle me-2"></i>
            Chơi ngay
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/axios'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import Leaderboard from './Leaderboard.vue'

const router = useRouter()

// Props
const props = defineProps({
  showModal: {
    type: Boolean,
    default: false,
  },
  quizId: {
    type: [Number, String],
    default: null,
  },
})

// Emits
const emit = defineEmits(['close'])

// Reactive data
const loading = ref(false)
const quizDetail = ref(null)
// Normalize quiz image to absolute URL
const quizImageUrl = computed(() => {
  const img = quizDetail.value?.image
  const id = quizDetail.value?.id
  if (img && img.startsWith('http')) return img
  if (img && img.startsWith('/uploads/')) return `http://localhost:8080${img}`
  // Nếu có id thì ưu tiên endpoint image-by-quiz
  if (id) return `http://localhost:8080/api/image/quiz/${id}`
  return '/img/default-quiz.jpg'
})
const questions = ref([])
const recentAttempts = ref([])
const showAllQuestions = ref(false)
const comments = ref([])

// Leaderboard pagination
const leaderboardRef = ref(null)
const currentPage = ref(1)
const totalEntries = ref(0)
const totalPages = ref(1)

// Computed properties for pagination
const leaderboardData = computed(() => {
  if (leaderboardRef.value) {
    return leaderboardRef.value.leaderboardData || []
  }
  return []
})

// Computed stats
const quizStats = computed(() => {
  if (!quizDetail.value) return {}

  const totalQuestions = questions.value.length
  const totalPoints = 0
  const totalTime = questions.value.reduce(
    (sum, q) => sum + (q.timeLimit === 0 ? 0 : q.timeLimit || 30),
    0,
  )

  // ✅ SỬ DỤNG DỮ LIỆU TỪ ENDPOINT THỐNG KÊ CÔNG KHAI
  return {
    totalQuestions,
    totalPoints,
    totalTime,
    totalPlays: quizDetail.value.totalPlays || 0,
    averageScore: quizDetail.value.averageScore || 0,
    uniqueParticipants: quizDetail.value.uniqueParticipants || 0,
    completionRate: quizDetail.value.completionRate || 0,
    averageTime: quizDetail.value.averageTime || 0,
  }
})

// Methods
const closeModal = () => {
  // Reset data when closing modal
  quizDetail.value = null
  questions.value = []
  recentAttempts.value = []
  loading.value = false
  showAllQuestions.value = false
  loadedQuizId.value = null

  emit('close')
}

// Track if we've already loaded for this quiz
const loadedQuizId = ref(null)
const loadingTimeout = ref(null)

const loadQuizDetail = async () => {
  // ✅ DEBOUNCE ĐỂ TRÁNH MULTIPLE CALLS
  if (loadingTimeout.value) {
    clearTimeout(loadingTimeout.value)
  }

  loadingTimeout.value = setTimeout(async () => {
    if (loading.value) {
      console.log('⏳ Already loading, skipping...')
      return
    }

    if (!props.quizId) {
      console.log('❌ No quiz ID provided')
      return
    }

    console.log('🔄 Loading quiz detail for ID:', props.quizId)
    loading.value = true

    try {
      // ✅ LOAD TẤT CẢ CÙNG LÚC THAY VÌ SEQUENTIAL
      const [quizRes, questionsRes, attemptsRes, reviewsRes, statsRes] = await Promise.allSettled([
        api.get(`/quiz/detail/${props.quizId}`),
        api.get(`/question/play/${props.quizId}`), // ✅ SỬA: Dùng endpoint play cho tất cả quiz
        api.get(`/quiz-attempts/public/recent/${props.quizId}`),
        api.get(`/quizzes/${props.quizId}/reviews`),
        api.get(`/quiz/public/stats/${props.quizId}`), // ✅ THÊM: Lấy thống kê công khai
      ])

      // ✅ XỬ LÝ KẾT QUẢ
      if (quizRes.status === 'fulfilled') {
        console.log('✅ Quiz detail response:', quizRes.value.data)
        quizDetail.value = quizRes.value.data
      } else {
        console.error('❌ Quiz detail error:', quizRes.reason)
        quizDetail.value = null
      }

      // Xử lý reviews riêng biệt
      if (reviewsRes.status === 'fulfilled') {
        console.log('✅ Reviews response:', reviewsRes.value.data)
        comments.value = reviewsRes.value.data || []
      } else {
        console.warn('⚠️ Reviews error:', reviewsRes.reason)
        comments.value = []
      }

      if (questionsRes.status === 'fulfilled') {
        console.log('✅ Questions response:', questionsRes.value.data)
        questions.value = questionsRes.value.data
      } else {
        console.error('❌ Questions error:', questionsRes.reason)
        // Với endpoint play, không cần kiểm tra 403 nữa
        questions.value = []
      }

      if (attemptsRes.status === 'fulfilled') {
        console.log('✅ Recent attempts response:', attemptsRes.value.data)
        recentAttempts.value = attemptsRes.value.data || []
      } else {
        console.warn('⚠️ Recent attempts error:', attemptsRes.reason)
        recentAttempts.value = []
      }

      if (statsRes.status === 'fulfilled') {
        console.log('✅ Public stats response:', statsRes.value.data)
        // Update quizDetail with public stats if available
        quizDetail.value = { ...quizDetail.value, ...statsRes.value.data }
      } else {
        console.warn('⚠️ Public stats error:', statsRes.reason)
      }
    } catch (error) {
      console.error('❌ Lỗi khi tải chi tiết quiz:', error)
      quizDetail.value = null
      questions.value = []
      recentAttempts.value = []
    } finally {
      loading.value = false
    }
  }, 100) // ✅ DEBOUNCE 100MS
}

const formatDate = (dateString) => {
  if (!dateString) return 'Không rõ'
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatTime = (seconds) => {
  if (!seconds) return '0s'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return minutes > 0 ? `${minutes}m ${remainingSeconds}s` : `${remainingSeconds}s`
}

const formatTimeAgo = (dateString) => {
  if (!dateString) return 'Không rõ'
  const now = new Date()
  const date = new Date(dateString)
  const diffInMinutes = Math.floor((now - date) / (1000 * 60))

  if (diffInMinutes < 1) return 'Vừa xong'
  if (diffInMinutes < 60) return `${diffInMinutes} phút trước`
  if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)} giờ trước`
  return `${Math.floor(diffInMinutes / 1440)} ngày trước`
}

const getScoreClass = (score) => {
  if (score >= 80) return 'score-excellent'
  if (score >= 60) return 'score-good'
  if (score >= 40) return 'score-average'
  return 'score-poor'
}

// Difficulty helpers removed with analysis section

const handleImageError = (event) => {
  event.target.src = '/img/default-quiz.jpg'
}

const handleAvatarError = (event) => {
  event.target.src = '/img/default-avatar.png'
}

const playQuiz = async () => {
  if (quizDetail.value) {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      console.error('❌ Missing userId - user not logged in')
      // Có thể chuyển hướng đến trang login
      router.push({ name: 'Login' })
      return
    }
    console.log('🎮 Playing quiz:', quizDetail.value.id, 'for user:', userId)
    try {
      const { quizAttemptService } = await import('@/services/quizAttemptService')
      const resp = await quizAttemptService.startAttempt(quizDetail.value.id)
      router.push({
        name: 'PlayAttempt',
        params: { attemptId: resp.attemptId || resp.attempt_id || resp?.attemptId },
      })
    } catch (e) {
      console.error('Không thể bắt đầu attempt:', e)
    }
    closeModal()
  }
}

// State for showing all comments
const showAllComments = ref(false)

// Computed property for displayed comments
const displayedComments = computed(() => {
  if (showAllComments.value) {
    return comments.value
  }
  return comments.value.slice(0, 3)
})

// Pagination for questions when showing all
const currentQuestionsPage = ref(1)
const questionsPerPage = 5
const totalQuestionPages = computed(() =>
  Math.ceil(Math.max(questions.value.length, 0) / questionsPerPage),
)
const questionsPageStart = computed(() => (currentQuestionsPage.value - 1) * questionsPerPage)
const paginatedQuestions = computed(() => {
  const start = questionsPageStart.value
  return questions.value.slice(start, start + questionsPerPage)
})

const nextQuestionsPage = () => {
  if (currentQuestionsPage.value < totalQuestionPages.value) currentQuestionsPage.value++
}
const previousQuestionsPage = () => {
  if (currentQuestionsPage.value > 1) currentQuestionsPage.value--
}

const toggleShowAllQuestions = () => {
  showAllQuestions.value = !showAllQuestions.value
  // reset to first page when toggling on
  if (showAllQuestions.value) currentQuestionsPage.value = 1
}

// Computed property to check if the current user is the quiz creator
const isQuizCreator = computed(() => {
  const currentUserId = localStorage.getItem('userId')
  const creatorId = quizDetail.value?.creatorId

  console.log('🔍 Checking quiz creator:')
  console.log('  - Current user ID:', currentUserId, '(type:', typeof currentUserId, ')')
  console.log('  - Creator ID:', creatorId, '(type:', typeof creatorId, ')')
  console.log('  - Quiz detail:', quizDetail.value)

  // Kiểm tra null/undefined
  if (!currentUserId) {
    console.log('❌ Current user ID is null/undefined')
    return false
  }
  if (!creatorId) {
    console.log('❌ Creator ID is null/undefined')
    return false
  }

  // Chuyển đổi sang number và so sánh
  const currentUserIdNum = Number(currentUserId)
  const creatorIdNum = Number(creatorId)

  console.log('  - Current user ID (number):', currentUserIdNum)
  console.log('  - Creator ID (number):', creatorIdNum)
  console.log('  - IDs equal:', currentUserIdNum === creatorIdNum)

  const isCreator = currentUserIdNum === creatorIdNum
  console.log('  - Is creator:', isCreator)

  return isCreator
})

// Computed property to check if the quiz is public (removed - no longer needed)
// const isPublicQuiz = computed(() => {
//   if (!quizDetail.value) return false
//   const isPublic = quizDetail.value.isPublic || quizDetail.value.public || quizDetail.value.is_public || quizDetail.value.status === 'PUBLIC' || quizDetail.value.status === 'public'
//   console.log('🔍 Quiz isPublic:', isPublic, 'Quiz detail:', quizDetail.value)
//   return isPublic
// })

// Watch for quizId changes - SỬA ĐỂ TRÁNH MULTIPLE CALLS
watch(
  () => props.quizId,
  (newQuizId) => {
    console.log('👀 Quiz ID changed to:', newQuizId)
    if (newQuizId && props.showModal && newQuizId !== loadedQuizId.value) {
      loadedQuizId.value = newQuizId
      loadQuizDetail()
    }
  },
)

// Watch for modal show - TÁCH RIÊNG
watch(
  () => props.showModal,
  (show) => {
    console.log('👀 Modal show changed to:', show)
    if (show && props.quizId && props.quizId !== loadedQuizId.value) {
      loadedQuizId.value = props.quizId
      loadQuizDetail()
    }
  },
)

// Pagination methods
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    if (leaderboardRef.value) {
      leaderboardRef.value.loadMore()
    }
  }
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    if (leaderboardRef.value) {
      leaderboardRef.value.collapseList()
      // Load specific page
      setTimeout(() => {
        if (leaderboardRef.value) {
          for (let i = 1; i < currentPage.value; i++) {
            leaderboardRef.value.loadMore()
          }
        }
      }, 100)
    }
  }
}

// Watch for leaderboard data changes to update pagination
watch(
  leaderboardData,
  (newData) => {
    if (newData && newData.length > 0) {
      totalEntries.value = newData.length
      totalPages.value = Math.ceil(totalEntries.value / 10)
    }
  },
  { deep: true },
)
</script>

<style scoped>
.modal-xl {
  max-width: 90%;
}

.modal-content {
  min-height: 400px;
  /* ✅ TRÁNH LAYOUT SHIFT */
  transition: all 0.3s ease;
}

.modal-body {
  min-height: 300px;
  /* ✅ TRÁNH LAYOUT SHIFT */
}

/* ✅ SMOOTH TRANSITIONS */
.quiz-image-container {
  transition: opacity 0.3s ease;
}

.quiz-image {
  transition: opacity 0.3s ease;
}

.stats-grid {
  transition: opacity 0.3s ease;
}

.questions-preview {
  transition: opacity 0.3s ease;
}

/* ✅ LOADING STATES */
.loading-fade {
  opacity: 0.6;
  pointer-events: none;
}

.loading-fade * {
  transition: opacity 0.2s ease;
}

.quiz-image-container {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quiz-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.quiz-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.quiz-status-badge.public {
  background: rgba(40, 167, 69, 0.9);
  color: white;
}

.quiz-status-badge.private {
  background: rgba(108, 117, 125, 0.9);
  color: white;
}

.quiz-title {
  color: #2c3e50;
  font-weight: 700;
}

.quiz-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.section-title {
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e9ecef;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 1rem;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: white;
}

.stat-icon.questions {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.points {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.stat-icon.time {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.stat-icon.plays {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stat-icon.avg-score {
  background: linear-gradient(135deg, #fa709a, #fee140);
}

.stat-icon.participants {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 0.8rem;
  color: #6c757d;
  font-weight: 500;
}

.questions-preview {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1rem;
}

/* Pagination for questions */
.questions-pagination .page-info {
  font-weight: 600;
  color: #495057;
}

.question-item {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.question-number {
  font-weight: 600;
  color: #495057;
}

.question-points {
  background: #28a745;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
}

.question-time {
  background: #17a2b8;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
}

.question-content {
  color: #2c3e50;
  line-height: 1.5;
}

.question-image {
  margin-top: 0.5rem;
}

.question-image img {
  max-width: 100%;
  border-radius: 8px;
}

.recent-activity {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1rem;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 1rem;
  background: white;
  border-radius: 12px;
  margin-bottom: 0.75rem;
  transition: all 0.2s ease;
  border: 1px solid #f1f3f4;
}

.activity-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.activity-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-weight: 500;
  color: #2c3e50;
  line-height: 1.4;
  margin-bottom: 0.25rem;
}

.activity-time {
  font-size: 0.75rem;
  color: #6c757d;
  font-weight: 400;
}

.activity-score {
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.85rem;
  flex-shrink: 0;
  min-width: 60px;
  text-align: center;
}

.score-excellent {
  background: #d4edda;
  color: #155724;
}

.score-good {
  background: #d1ecf1;
  color: #0c5460;
}

.score-average {
  background: #fff3cd;
  color: #856404;
}

.score-poor {
  background: #f8d7da;
  color: #721c24;
}

.difficulty-analysis {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1rem;
}

.difficulty-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: white;
  border-radius: 8px;
  margin-bottom: 0.5rem;
}

.difficulty-label {
  font-weight: 500;
  color: #2c3e50;
}

.difficulty-value {
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
}

.difficulty-easy {
  background: #d4edda;
  color: #155724;
}

.difficulty-medium {
  background: #fff3cd;
  color: #856404;
}

.difficulty-hard {
  background: #f8d7da;
  color: #721c24;
}

.difficulty-very-hard {
  background: #f8d7da;
  color: #721c24;
}

.activity-subtitle {
  color: #495057;
  font-weight: 600;
  margin-bottom: 0.75rem;
  padding-bottom: 0.3rem;
  border-bottom: 1px solid #e9ecef;
  font-size: 0.9rem;
}

.comment-item {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border: 1px solid #dee2e6;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 0.75rem;
  transition: all 0.2s ease;
}

.comment-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.comment-text {
  font-style: italic;
  color: #495057;
  margin-left: 8px;
  font-size: 0.9rem;
  line-height: 1.4;
}

.comment-rating {
  margin-top: 0.5rem;
}

.stars {
  display: flex;
  gap: 2px;
  align-items: center;
}

.stars i {
  font-size: 0.8rem;
}

.avatar-img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* ✅ SHOW MORE BUTTON STYLING */
.btn-outline-primary.btn-sm {
  font-size: 0.8rem;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  transition: all 0.2s ease;
}

.btn-outline-primary.btn-sm:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 123, 255, 0.3);
}

.btn-outline-primary.btn-sm i {
  margin-right: 4px;
  font-size: 0.75rem;
}

@media (max-width: 768px) {
  .modal-xl {
    max-width: 95%;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

/* Leaderboard Pagination Styles */
.leaderboard-container {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e9ecef;
}

.pagination-controls {
  background: white;
  border-radius: 6px;
  padding: 15px;
  border: 1px solid #dee2e6;
}

.pagination-info {
  font-size: 0.875rem;
}

.pagination-buttons {
  display: flex;
  align-items: center;
}

.page-info {
  font-weight: 600;
  color: #495057;
  min-width: 80px;
  text-align: center;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive pagination */
@media (max-width: 768px) {
  .pagination-controls .d-flex {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .pagination-buttons {
    justify-content: center;
  }
}
</style>
