<template>
  <div class="quiz-history-modern" :class="{ 'dark-theme': themeStore.isDarkMode }">
    <!-- Background Pattern -->
    <div class="background-pattern">
      <div class="pattern-grid"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <!-- Header Section -->
    <div class="header-section" role="banner">
      <div class="header-content">
        <div class="title-group">
          <div class="icon-wrapper" aria-hidden="true">
            <i class="bi bi-graph-up-arrow"></i>
          </div>
          <div class="title-text">
            <h1 class="header-title">{{ isAdmin ? 'Quản lý lịch sử làm bài' : 'Lịch sử làm bài của tôi' }}</h1>
            <p class="header-subtitle">{{ isAdmin ? 'Theo dõi và phân tích kết quả của tất cả người dùng' : 'Theo dõi tiến trình học tập và phân tích kết quả cá nhân' }}</p>
          </div>
        </div>

        <div class="header-controls">
          <button @click="refreshData" class="refresh-btn" :disabled="loading" aria-label="Làm mới dữ liệu">
            <i class="bi bi-arrow-clockwise" :class="{ 'spinning': loading }"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-section">
      <div class="loading-card">
        <div class="loading-animation">
          <div class="pulse-ring"></div>
          <div class="pulse-ring delay-1"></div>
          <div class="pulse-ring delay-2"></div>
        </div>
        <h3>Đang tải dữ liệu...</h3>
        <p>Vui lòng chờ trong giây lát</p>
      </div>
    </div>

    <!-- Main Content -->
    <div v-else class="main-content">
      <!-- Statistics Dashboard -->
      <div v-if="attempts.length > 0" class="stats-dashboard">
        <div class="stats-header">
          <h2>{{ isAdmin ? 'Thống kê tổng quan' : 'Thống kê cá nhân' }}</h2>
          <div class="stats-period">
            <span class="period-text">
              {{ isAdmin ? 'Tổng cộng' : `Của ${currentUserName}` }} {{ totalElements }} lần làm bài
            </span>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card primary">
            <div class="stat-icon">
              <i class="bi bi-trophy-fill"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ averageScore.toFixed(1) }}</div>
              <div class="stat-label">Điểm trung bình</div>
              <div class="stat-trend positive">
                <i class="bi bi-arrow-up"></i>
                <span>+2.5 so với tháng trước</span>
              </div>
            </div>
          </div>

          <div class="stat-card success">
            <div class="stat-icon">
              <i class="bi bi-star-fill"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ highestScore }}</div>
              <div class="stat-label">Điểm cao nhất</div>
              <div class="stat-trend positive">
                <i class="bi bi-arrow-up"></i>
                <span>Kỷ lục cá nhân</span>
              </div>
            </div>
          </div>

          <div class="stat-card info">
            <div class="stat-icon">
              <i class="bi bi-clock-history"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ averageTime }}</div>
              <div class="stat-label">Thời gian TB</div>
              <div class="stat-trend neutral">
                <i class="bi bi-dash"></i>
                <span>Ổn định</span>
              </div>
            </div>
          </div>

          <div class="stat-card warning">
            <div class="stat-icon">
              <i class="bi bi-lightning-fill"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ completionRate.toFixed(0) }}%</div>
              <div class="stat-label">Tỷ lệ hoàn thành</div>
              <div class="stat-trend positive">
                <i class="bi bi-arrow-up"></i>
                <span>Xuất sắc</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Performance Chart -->
        <div class="chart-section">
          <div class="chart-header">
            <h3>Phân tích hiệu suất</h3>
          <div class="chart-legend">
            <div class="legend-item excellent">
              <div class="legend-color" :style="{ background: 'var(--excellent-color)' }"></div>
              <span>Xuất sắc (90+)</span>
            </div>
            <div class="legend-item good">
              <div class="legend-color" :style="{ background: 'var(--good-color)' }"></div>
              <span>Tốt (70-89)</span>
            </div>
            <div class="legend-item average">
              <div class="legend-color" :style="{ background: 'var(--average-color)' }"></div>
              <span>Trung bình (50-69)</span>
            </div>
            <div class="legend-item poor">
              <div class="legend-color" :style="{ background: 'var(--poor-color)' }"></div>
              <span>Cần cải thiện (&lt;50)</span>
            </div>
          </div>
          </div>
          <div class="performance-bars">
            <div class="performance-bar excellent">
              <div class="bar-fill" :style="{ width: `${excellentPercentage}%`, background: 'var(--excellent-color)' }">
                <span class="bar-label">{{ excellentCount }}</span>
              </div>
            </div>
            <div class="performance-bar good">
              <div class="bar-fill" :style="{ width: `${goodPercentage}%`, background: 'var(--good-color)' }">
                <span class="bar-label">{{ goodCount }}</span>
              </div>
            </div>
            <div class="performance-bar average">
              <div class="bar-fill" :style="{ width: `${averagePercentage}%`, background: 'var(--average-color)' }">
                <span class="bar-label">{{ averageCount }}</span>
              </div>
            </div>
            <div class="performance-bar poor">
              <div class="bar-fill" :style="{ width: `${poorPercentage}%`, background: 'var(--poor-color)' }">
                <span class="bar-label">{{ poorCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="controls-section">
        <div class="search-container">
          <div class="search-box">
            <i class="bi bi-search"></i>
            <input v-model="searchQuery" type="text"
              :placeholder="isAdmin ? 'Tìm kiếm quiz hoặc người dùng...' : 'Tìm kiếm quiz...'" class="search-input" />
            <button v-if="searchQuery" @click="searchQuery = ''" class="clear-search">
              <i class="bi bi-x"></i>
            </button>
          </div>
        </div>

        <div class="filters-container">
          <div class="filter-group">
            <label>Sắp xếp theo:</label>
            <select v-model="sortBy" class="filter-select">
              <option value="recent">Mới nhất</option>
              <option value="score">Điểm cao nhất</option>
              <option value="title">Tên A-Z</option>
              <option value="time">Thời gian làm bài</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Lọc theo điểm:</label>
            <select v-model="scoreFilter" class="filter-select">
              <option value="all">Tất cả</option>
              <option value="excellent">Xuất sắc (90+)</option>
              <option value="good">Tốt (70-89)</option>
              <option value="average">Trung bình (50-69)</option>
              <option value="poor">Cần cải thiện (&lt;50)</option>
            </select>
          </div>

          <div v-if="isAdmin" class="filter-group">
            <label>Người dùng:</label>
            <select v-model="filters.userId" @change="loadData" class="filter-select">
              <option value="">Tất cả</option>
              <option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.username }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <!-- Quiz History Cards -->
      <div v-if="filteredAttempts.length > 0" class="history-section">
        <div class="section-header">
          <h2>{{ isAdmin ? 'Lịch sử làm bài' : 'Lịch sử của tôi' }}</h2>
          <div class="view-toggle">
            <button @click="viewMode = 'card'" :class="{ active: viewMode === 'card' }" class="view-btn">
              <i class="bi bi-grid-3x3-gap"></i>
            </button>
            <button @click="viewMode = 'list'" :class="{ active: viewMode === 'list' }" class="view-btn">
              <i class="bi bi-list"></i>
            </button>
          </div>
        </div>

        <div class="history-grid" :class="{ 'list-view': viewMode === 'list' }">
          <div v-for="(attempt, index) in paginatedAttempts" :key="attempt.id" class="history-card"
            :class="getScoreClass(attempt.score)" :style="{ 'animation-delay': `${index * 0.1}s` }">
            <div class="card-header">
              <div class="quiz-info">
                <h3 class="quiz-title">{{ attempt.quizTitle }}</h3>
                <div class="quiz-meta">
                  <span v-if="isAdmin" class="username">
                    <i class="bi bi-person-fill"></i>
                    {{ attempt.username }}
                  </span>
                  <span class="duration">
                    <i class="bi bi-stopwatch"></i>
                    {{ formatTime(attempt.timeTaken) }}
                  </span>
                </div>
              </div>
              <div class="score-badge" :class="getScoreClass(attempt.score)">
                <div class="score-value">{{ attempt.score }}</div>
                <div class="score-label">{{ getScoreLabel(attempt.score) }}</div>
              </div>
            </div>

            <div class="card-body">
              <div class="progress-section">
                <div class="progress-label">
                  <span>Điểm số</span>
                  <span>{{ attempt.score }}/100</span>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" :class="getScoreClass(attempt.score)"
                    :style="{ width: `${attempt.score}%` }"></div>
                </div>
              </div>

              <div class="stats-row">
                <div class="stat-item">
                  <i class="bi bi-clock"></i>
                  <span>{{ formatTime(attempt.timeTaken) }}</span>
                </div>
                <div class="stat-item">
                  <i class="bi bi-calendar-event"></i>
                  <span>{{ formatDateTime(attempt.attemptedAt) }}</span>
                </div>
              </div>
            </div>

            <div class="card-actions">
              <button class="action-btn primary" @click="openAttemptDetail(attempt)" title="Xem chi tiết lần làm">
                <i class="bi bi-eye"></i>
                Chi tiết
              </button>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination-section">
          <div class="pagination-info">
            Hiển thị {{ ((currentPage - 1) * pageSize) + 1 }} - {{ Math.min(currentPage * pageSize,
              filteredAttempts.length) }}
            trong tổng số {{ filteredAttempts.length }} kết quả
          </div>
          <div class="pagination-controls">
            <button @click="changePage(1)" :disabled="currentPage === 1" class="page-btn">
              <i class="bi bi-chevron-double-left"></i>
            </button>
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="page-numbers">
              <button v-for="page in visiblePages" :key="page" @click="changePage(page)"
                :class="{ active: page === currentPage }" class="page-number">
                {{ page }}
              </button>
            </div>

            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" class="page-btn">
              <i class="bi bi-chevron-right"></i>
            </button>
            <button @click="changePage(totalPages)" :disabled="currentPage === totalPages" class="page-btn">
              <i class="bi bi-chevron-double-right"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else-if="!loading" class="empty-section">
        <div class="empty-card">
          <div class="empty-illustration">
            <i class="bi bi-inbox"></i>
          </div>
          <h3>{{ getEmptyTitle() }}</h3>
          <p>{{ getEmptySubtitle() }}</p>
          <div class="empty-actions">
            <button v-if="hasFilters" @click="clearFilters" class="action-btn primary">
              <i class="bi bi-funnel"></i>
              Xóa bộ lọc
            </button>
            <button v-else class="action-btn secondary">
              <i class="bi bi-plus-circle"></i>
              Bắt đầu làm quiz
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Detail Modal (polished) -->
    <div v-if="showDetailModal" class="detail-overlay" @click.self="showDetailModal = false">
      <div class="detail-modal" role="dialog" aria-modal="true">
        <div class="modal-header">
          <div class="modal-title">
            <i class="bi bi-clipboard2-check"></i>
            Chi tiết lần làm
          </div>
          <button class="modal-close" @click="showDetailModal = false" aria-label="Đóng">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <div class="modal-body">
          <div class="modal-info">
            <h3 class="quiz-name">{{ modalData.title }}</h3>
            <div class="meta-grid">
              <div class="meta-item">
                <i class="bi bi-trophy-fill"></i>
                <span>Điểm:</span>
                <strong>{{ modalData.score }}/100</strong>
              </div>
              <div class="meta-item">
                <i class="bi bi-123"></i>
                <span>Lần làm:</span>
                <strong>#{{ modalData.nthAttempt }}</strong>
              </div>
              <div class="meta-item">
                <i class="bi bi-activity"></i>
                <span>So với lần trước:</span>
                <strong :class="{ up: modalData.prevDelta>0, down: modalData.prevDelta<0 }">
                  {{ modalData.prevDelta>0? '+' : '' }}{{ modalData.prevDelta }} điểm
                </strong>
              </div>
              <div class="meta-item">
                <i class="bi bi-bar-chart-line"></i>
                <span>Điểm trung bình:</span>
                <strong>{{ modalData.averageScore }}/100</strong>
              </div>
              <div class="meta-item">
                <i class="bi bi-stars"></i>
                <span>Điểm cao nhất:</span>
                <strong>{{ modalData.bestScore }}/100</strong>
              </div>
              <div class="meta-item">
                <i class="bi bi-clock"></i>
                <span>Thời gian TB:</span>
                <strong>{{ formatTime(modalData.averageTime) }}</strong>
              </div>
              <div class="meta-item">
                <i class="bi bi-calendar-event"></i>
                <span>Ngày làm:</span>
                <strong>{{ formatDateTime(modalData.attemptedAt) }}</strong>
              </div>
            </div>
          </div>
          <div class="modal-score">
            <div class="score-ring" :class="getScoreClass(modalData.score)">
              <div class="score-value">{{ modalData.score }}</div>
              <div class="score-text">{{ getScoreLabel(modalData.score) }}</div>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button class="action-btn primary" @click="showDetailModal = false">
            <i class="bi bi-check2"></i>
            Đóng
          </button>
          <button v-if="modalData.resultId" class="action-btn" @click="gotoResult(modalData.resultId)">
            <i class="bi bi-box-arrow-up-right"></i>
            Xem kết quả
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { quizAttemptService } from '@/services/quizAttemptService'
import { adminService } from '@/services/adminService'

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
const themeStore = useThemeStore()

// Reactive data
const attempts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalElements = ref(0)
const searchQuery = ref('')
const sortBy = ref('recent')
const scoreFilter = ref('all')
const viewMode = ref('card')
const showDetailModal = ref(false)
const modalData = ref({
  title: '',
  score: 0,
  attemptedAt: null,
  timeTaken: 0,
  resultId: null,
  attemptId: null,
  // Stats
  nthAttempt: 1,
  prevDelta: 0, // điểm chênh so với lần trước
  averageScore: 0,
  bestScore: 0,
  averageTime: 0
})

// Filters
const filters = ref({
  userId: '',
  quizId: ''
})

// Users list for admin filter (fetched from backend)
const users = ref([])

// Computed properties
const isAdmin = computed(() => userStore.isAdmin())

const filteredAttempts = computed(() => {
  let filtered = [...attempts.value]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(attempt =>
      attempt.quizTitle.toLowerCase().includes(query) ||
      (isAdmin.value && attempt.username && attempt.username.toLowerCase().includes(query))
    )
  }

  // Score filter
  if (scoreFilter.value !== 'all') {
    filtered = filtered.filter(attempt => {
      const score = attempt.score
      switch (scoreFilter.value) {
        case 'excellent': return score >= 90
        case 'good': return score >= 70 && score < 90
        case 'average': return score >= 50 && score < 70
        case 'poor': return score < 50
        default: return true
      }
    })
  }

  // Sort
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'recent':
        return new Date(b.attemptedAt) - new Date(a.attemptedAt)
      case 'score':
        return b.score - a.score
      case 'title':
        return a.quizTitle.localeCompare(b.quizTitle)
      case 'time':
        return a.timeTaken - b.timeTaken
      default:
        return 0
    }
  })

  return filtered
})

const paginatedAttempts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAttempts.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredAttempts.value.length / pageSize.value)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    }
  }

  return pages
})

// Statistics
const averageScore = computed(() => {
  if (attempts.value.length === 0) return 0
  return attempts.value.reduce((sum, attempt) => sum + attempt.score, 0) / attempts.value.length
})

const highestScore = computed(() => {
  if (attempts.value.length === 0) return 0
  return Math.max(...attempts.value.map(attempt => attempt.score))
})

const averageTime = computed(() => {
  if (attempts.value.length === 0) return '0:00'
  const avgSeconds = attempts.value.reduce((sum, attempt) => sum + attempt.timeTaken, 0) / attempts.value.length
  return formatTime(Math.round(avgSeconds))
})

const completionRate = computed(() => {
  if (attempts.value.length === 0) return 0
  return (attempts.value.filter(attempt => attempt.score >= 50).length / attempts.value.length) * 100
})

const excellentCount = computed(() => attempts.value.filter(a => a.score >= 90).length)
const goodCount = computed(() => attempts.value.filter(a => a.score >= 70 && a.score < 90).length)
const averageCount = computed(() => attempts.value.filter(a => a.score >= 50 && a.score < 70).length)
const poorCount = computed(() => attempts.value.filter(a => a.score < 50).length)

const excellentPercentage = computed(() => attempts.value.length > 0 ? (excellentCount.value / attempts.value.length) * 100 : 0)
const goodPercentage = computed(() => attempts.value.length > 0 ? (goodCount.value / attempts.value.length) * 100 : 0)
const averagePercentage = computed(() => attempts.value.length > 0 ? (averageCount.value / attempts.value.length) * 100 : 0)
const poorPercentage = computed(() => attempts.value.length > 0 ? (poorCount.value / attempts.value.length) * 100 : 0)

const hasFilters = computed(() => {
  return searchQuery.value || scoreFilter.value !== 'all' ||
    (isAdmin.value && filters.value.userId) || filters.value.quizId
})

const currentUserName = computed(() => {
  return userStore.user?.username || 'Người dùng'
})

// Methods
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: 0, // Load all for client-side pagination
      size: 1000,
      ...(filters.value.quizId && { quizId: filters.value.quizId })
    }

    // Nếu là admin và có chọn userId, thì lọc theo userId
    if (isAdmin.value && filters.value.userId) {
      params.userId = filters.value.userId
    } else if (!isAdmin.value) {
      // Nếu không phải admin, chỉ lấy dữ liệu của người dùng hiện tại
      params.userId = userStore.user?.id
    }

    const response = await quizAttemptService.getQuizAttempts(params)
    attempts.value = response.content || []
    totalElements.value = response.totalElements || 0

    // Load users list for admin filter once
    if (isAdmin.value && users.value.length === 0) {
      try {
        const usersPage = await adminService.getAllUsers({ page: 0, size: 200 })
        users.value = usersPage.content?.map(u => ({ id: u.id, username: u.username })) || []
      } catch (e) {
        console.warn('Unable to load users for admin filter:', e?.message || e)
      }
    }
  } catch (error) {
    console.error('Error loading quiz attempts:', error)
    attempts.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value && page !== '...') {
    currentPage.value = page
  }
}



const clearFilters = () => {
  searchQuery.value = ''
  scoreFilter.value = 'all'
  if (isAdmin.value) {
    filters.value.userId = ''
  }
  filters.value.quizId = ''
  currentPage.value = 1
}

  // Open attempt detail - navigate if có resultId/attemptId; fallback: expand card modal (todo)
  const openAttemptDetail = (attempt) => {
    try {
      // Nếu backend trả id kết quả, ưu tiên điều hướng về trang kết quả
      if (attempt.resultId) {
        // name 'QuizResult' đã có trong router
        window.location.href = `/result/${attempt.resultId}`
        return
      }
      // Nếu chưa có resultId, thử gọi API lấy kết quả theo attemptId (nếu sau này có API)
      // TODO: hiện tại fallback: hiển thị modal đơn giản với thông tin cơ bản
      // Tính thống kê theo cùng quiz (dựa theo title)
      const sameQuizAttempts = attempts.value
        .filter(a => a.quizTitle === attempt.quizTitle)
        .slice()
        .sort((a, b) => new Date(a.attemptedAt) - new Date(b.attemptedAt))

      const index = sameQuizAttempts.findIndex(a => a.id === attempt.id)
      const prev = index > 0 ? sameQuizAttempts[index - 1] : null
      const avgScore = sameQuizAttempts.reduce((s, a) => s + a.score, 0) / Math.max(1, sameQuizAttempts.length)
      const bestScore = Math.max(...sameQuizAttempts.map(a => a.score))
      const avgTime = sameQuizAttempts.reduce((s, a) => s + (a.timeTaken || 0), 0) / Math.max(1, sameQuizAttempts.length)

      modalData.value = {
        title: attempt.quizTitle,
        score: attempt.score,
        attemptedAt: attempt.attemptedAt,
        timeTaken: attempt.timeTaken,
        resultId: attempt.resultId,
        attemptId: attempt.id,
        nthAttempt: index + 1,
        prevDelta: prev ? (attempt.score - prev.score) : 0,
        averageScore: Math.round(avgScore * 10) / 10,
        bestScore: bestScore,
        averageTime: Math.round(avgTime)
      }
      showDetailModal.value = true
      // Nếu có attemptId riêng
      if (attempt.id) {
        // fallback: mở lại play attempt ở chế độ chỉ xem nếu có (hoặc có thể mở modal sau)
        // Ở đây điều hướng tới trang Home để tránh lỗi nếu route không tồn tại
        console.log('Attempt detail not implemented; attempt id:', attempt.id)
      }
    } catch (e) {
      console.warn('Cannot open attempt detail:', e)
    }
  }

  const gotoResult = (resultId) => {
    window.location.href = `/result/${resultId}`
  }

const getScoreClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 70) return 'good'
  if (score >= 50) return 'average'
  return 'poor'
}

const getScoreLabel = (score) => {
  if (score >= 90) return 'Xuất sắc'
  if (score >= 70) return 'Tốt'
  if (score >= 50) return 'Trung bình'
  return 'Cần cải thiện'
}

const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 1) return 'Hôm qua'
  if (diffDays <= 7) return `${diffDays} ngày trước`
  if (diffDays <= 30) return `${Math.ceil(diffDays / 7)} tuần trước`
  return date.toLocaleDateString('vi-VN')
}

const formatDateTime = (dateString) => {
  return new Date(dateString).toLocaleString('vi-VN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getEmptyTitle = () => {
  if (hasFilters.value) return 'Không tìm thấy kết quả'
  return isAdmin.value ? 'Chưa có lịch sử làm bài' : 'Bạn chưa có lịch sử làm bài'
}

const getEmptySubtitle = () => {
  if (hasFilters.value) return 'Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm'
  return isAdmin.value ? 'Hãy bắt đầu làm quiz để xem lịch sử tại đây' : 'Hãy bắt đầu làm quiz để xem lịch sử của bạn tại đây'
}

// Watchers
watch([searchQuery, scoreFilter], () => {
  currentPage.value = 1
})

// Lifecycle
onMounted(() => {
  loadData()
})
</script>

<style>
/* Global variables to avoid scoped isolation */
:root {
  --font-family: 'Inter', 'Noto Sans', 'Roboto', sans-serif;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --primary-color: #667eea;
  --secondary-color: #764ba2;
  --success-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --success-color: #667eea;
  --info-gradient: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  --info-color: #8b5cf6;
  --warning-gradient: linear-gradient(135deg, #a855f7 0%, #9333ea 100%);
  --warning-color: #a855f7;
  --danger-gradient: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  --danger-color: #7c3aed;
  /* Distinct category colors for performance analysis (requested palette) */
  --excellent-color: #4CAF50; /* green success */
  --good-color: #2196F3;      /* blue */
  --average-color: #FFC107;   /* amber */
  --poor-color: #F44336;      /* red */
  --bg-primary: rgba(255, 255, 255, 0.95);
  --bg-secondary: rgba(255, 255, 255, 0.1);
  --bg-dark: rgba(52, 73, 94, 0.95);
  --bg-dark-secondary: rgba(0, 0, 0, 0.2);
  --text-primary: #333;
  --text-secondary: #666;
  --text-light: #ecf0f1;
  --text-muted: #bdc3c7;
  --border-light: rgba(255, 255, 255, 0.2);
  --border-dark: rgba(255, 255, 255, 0.1);
  --shadow-primary: rgba(102, 126, 234, 0.3);
  --shadow-secondary: rgba(0, 0, 0, 0.1);
  --shadow-dark: rgba(0, 0, 0, 0.15);
}
</style>

<style scoped>
/* Merged styles from QuizHistoryModern.css and QuizHistoryModern2.css */
/* Variables */
:root {
  --font-family: 'Inter', 'Noto Sans', 'Roboto', sans-serif;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --primary-color: #667eea;
  --secondary-color: #764ba2;
  --success-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --success-color: #667eea;
  --info-gradient: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  --info-color: #8b5cf6;
  --warning-gradient: linear-gradient(135deg, #a855f7 0%, #9333ea 100%);
  --warning-color: #a855f7;
  --danger-gradient: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  --danger-color: #7c3aed;
  --excellent-color: #667eea;
  --good-color: #8b5cf6;
  --average-color: #a855f7;
  --poor-color: #7c3aed;
  --bg-primary: rgba(255, 255, 255, 0.95);
  --bg-secondary: rgba(255, 255, 255, 0.1);
  --bg-dark: rgba(52, 73, 94, 0.95);
  --bg-dark-secondary: rgba(0, 0, 0, 0.2);
  --text-primary: #333;
  --text-secondary: #666;
  --text-light: #ecf0f1;
  --text-muted: #bdc3c7;
  --border-light: rgba(255, 255, 255, 0.2);
  --border-dark: rgba(255, 255, 255, 0.1);
  --shadow-primary: rgba(102, 126, 234, 0.3);
  --shadow-secondary: rgba(0, 0, 0, 0.1);
  --shadow-dark: rgba(0, 0, 0, 0.15);
}

.quiz-history-modern { position: relative; min-height: 100vh; overflow-x: hidden; background: var(--primary-gradient); }
.quiz-history-modern.dark-theme { color: var(--text-primary); }

/* Background Pattern */
.background-pattern {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.pattern-grid {
  width: 100%;
  height: 100%;
  background-image:
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: drift 20s linear infinite;
}

.dark-theme .pattern-grid {
  background-image:
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
}

@keyframes drift {
  0% {
    transform: translate(0, 0);
  }

  100% {
    transform: translate(50px, 50px);
  }
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 100px;
  height: 100px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 80px;
  height: 80px;
  bottom: 20%;
  left: 60%;
  animation-delay: 4s;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }

  50% {
    transform: translateY(-30px) rotate(180deg);
  }
}

/* Header Section */
.header-section { position: relative; z-index: 1; padding: 2rem 1rem; background: rgba(255,255,255,0.18); backdrop-filter: blur(14px); border-bottom: 1px solid rgba(255,255,255,0.25); font-family: var(--font-family); }
.dark-theme .header-section { background: rgba(0,0,0,0.25); border-bottom: 1px solid rgba(255,255,255,0.15); }
.header-content { max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; gap: 2rem; }
.title-group { display: flex; align-items: center; gap: 1.5rem; }
.icon-wrapper { width: 70px; height: 70px; background: var(--primary-gradient); border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 2rem; color: #fff; box-shadow: 0 12px 40px var(--shadow-primary); position: relative; overflow: hidden; transition: .3s; }
.icon-wrapper::before { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: linear-gradient(45deg, transparent, rgba(255,255,255,.3), transparent); transform: rotate(45deg); transition: .6s; opacity: 0; }
.icon-wrapper:hover::before { opacity: 1; transform: rotate(45deg) translateX(100%); }
.icon-wrapper:hover { transform: translateY(-5px) scale(1.05); box-shadow: 0 20px 60px var(--shadow-primary); }
.title-text h1 { font-size: 2.4rem; font-weight: 800; margin: 0; color: var(--text-primary); text-shadow: 0 2px 6px rgba(0,0,0,.08); }
.title-text p { margin: .5rem 0 0; color: var(--text-secondary); font-size: 1.05rem; }

/* Header controls (refresh) */
.header-section .header-controls { display:flex; align-items:center; gap: 0.75rem; }
.header-section .refresh-btn { width:48px; height:48px; border-radius:14px; border:1px solid rgba(0,0,0,.06); background:#ffffff; color:#34495e; cursor:pointer; transition:.2s; display:flex; align-items:center; justify-content:center; font-size:20px; box-shadow:0 8px 20px rgba(0,0,0,.12); }
.dark-theme .header-section .refresh-btn { background:#2d3748; color:#e2e8f0; border-color:#4a5568; }
.header-section .refresh-btn:hover { transform: translateY(-2px); box-shadow:0 12px 28px rgba(0,0,0,.18); }
.header-section .refresh-btn i { display:inline-block; width:20px; height:20px; font-size:20px; line-height:20px; }
.header-section .refresh-btn svg, .header-section .refresh-btn img { width:20px; height:20px; object-fit:contain; }

/* Loading Section */
.loading-section { display: flex; justify-content: center; align-items: center; min-height: 60vh; position: relative; z-index: 1; }
.loading-card { background: var(--bg-primary); backdrop-filter: blur(25px); border-radius: 28px; padding: 3.5rem; text-align: center; box-shadow: 0 25px 80px var(--shadow-secondary); border: 1px solid var(--border-light); max-width: 450px; position: relative; overflow: hidden; }
.loading-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: var(--primary-gradient); }
.loading-animation { display: flex; justify-content: center; align-items: center; margin-bottom: 2.5rem; position: relative; width: 100px; height: 100px; margin-left: auto; margin-right: auto; }
.pulse-ring { position: absolute; width: 100px; height: 100px; border: 4px solid var(--primary-color); border-radius: 50%; animation: pulse 2.5s ease-out infinite; }
@keyframes pulse { 0% { transform: scale(.1); opacity: 1; } 100% { transform: scale(1.4); opacity: 0; } }
.loading-card h3 { font-size: 1.8rem; font-weight: 700; margin-bottom: 1rem; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.loading-card p { color: var(--text-secondary); margin: 0; font-size: 1.1rem; }

/* Main Content */
.main-content { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; padding: 2rem 1rem; }
.stats-dashboard { margin-bottom: 3rem; }
.stats-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2.5rem; }
.stats-header h2 { font-size: 2.0rem; font-weight: 800; margin: 0; color: var(--text-primary); text-shadow: 0 2px 5px rgba(0,0,0,.06); }
.stats-period { background: var(--bg-primary); backdrop-filter: blur(15px); padding: .75rem 1.5rem; border-radius: 25px; border: 1px solid var(--border-light); display: flex; align-items: center; gap: .5rem; }
.period-text { font-size: .95rem; font-weight: 600; color: var(--text-secondary); }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 2rem; margin-bottom: 2.5rem; }
.stat-card { background: var(--bg-primary); backdrop-filter: blur(25px); border-radius: 24px; padding: 2.5rem; display: flex; align-items: center; gap: 2rem; box-shadow: 0 15px 50px var(--shadow-secondary); border: 1px solid var(--border-light); transition: .4s; position: relative; overflow: hidden; cursor: pointer; }
.stat-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 5px; background: var(--primary-gradient); transition: .4s; }
.stat-card:hover { transform: translateY(-12px) scale(1.02); box-shadow: 0 25px 80px var(--shadow-dark); }
.stat-icon { width: 75px; height: 75px; border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 2rem; color: #fff; background: var(--primary-gradient); box-shadow: 0 12px 40px var(--shadow-primary); }
.stat-value { font-size: 3rem; font-weight: 900; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; line-height: 1; margin-bottom: .5rem; }
.stat-label { font-size: 1.1rem; color: var(--text-secondary); font-weight: 600; margin-bottom: .75rem; text-transform: uppercase; letter-spacing: .5px; }
.stat-trend { display: flex; align-items: center; gap: .5rem; font-size: .9rem; font-weight: 600; padding: .5rem 1rem; border-radius: 20px; width: fit-content; }
.stat-trend.positive { color: var(--success-color); background: rgba(102,126,234,.1); border: 1px solid rgba(102,126,234,.2); }
.stat-trend.neutral { color: var(--text-secondary); background: rgba(102,102,102,.1); border: 1px solid rgba(102,102,102,.2); }

/* Chart Section */
.chart-section { background: var(--bg-primary); backdrop-filter: blur(25px); border-radius: 24px; padding: 2.5rem; box-shadow: 0 15px 50px var(--shadow-secondary); border: 1px solid var(--border-light); position: relative; overflow: hidden; }
.chart-section::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: var(--primary-gradient); }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2.5rem; flex-wrap: wrap; gap: 1.5rem; }
.chart-header h3 { font-size: 1.8rem; font-weight: 800; margin: 0; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.chart-legend { display: flex; gap: 2rem; flex-wrap: wrap; }
.legend-item { display: flex; align-items: center; gap: .75rem; font-size: .95rem; font-weight: 600; padding: .5rem 1rem; border-radius: 20px; background: var(--bg-secondary); backdrop-filter: blur(10px); border: 1px solid var(--border-light); transition: .3s; }
.legend-color { width: 16px; height: 16px; border-radius: 4px; position: relative; border: 2px solid rgba(0,0,0,0.06); box-shadow: 0 0 0 2px rgba(255,255,255,0.8) inset; }
.legend-item.excellent .legend-color { background: var(--excellent-color) !important; }
.legend-item.good .legend-color { background: var(--good-color) !important; }
.legend-item.average .legend-color { background: var(--average-color) !important; }
.legend-item.poor .legend-color { background: var(--poor-color) !important; }
.performance-bars { display: flex; flex-direction: column; gap: 1.5rem; }
.performance-bar { position: relative; height: 50px; background: var(--bg-primary); border-radius: 25px; overflow: hidden; box-shadow: inset 0 2px 10px rgba(0,0,0,.1); }
.bar-fill { height: 100%; border-radius: 25px; display: flex; align-items: center; justify-content: flex-end; padding-right: 1.5rem; color: #fff; font-weight: 700; font-size: 1rem; transition: width 2s ease; min-width: 80px; position: relative; overflow: hidden; }
.bar-fill::before { content: ''; position: absolute; inset: 0; background: linear-gradient(90deg, transparent, rgba(255,255,255,.2), transparent); animation: shimmer 2s infinite; }
@keyframes shimmer { 0% { transform: translateX(-100%);} 100% { transform: translateX(100%);} }
.performance-bar.excellent .bar-fill { background: var(--excellent-color); }
.performance-bar.good .bar-fill { background: var(--good-color); }
.performance-bar.average .bar-fill { background: var(--average-color); }
.performance-bar.poor .bar-fill { background: var(--poor-color); }

/* Controls */
.controls-section { background: var(--bg-primary); backdrop-filter: blur(25px); border-radius: 24px; padding: 2rem; margin-bottom: 2.5rem; box-shadow: 0 15px 50px var(--shadow-secondary); border: 1px solid var(--border-light); position: relative; overflow: hidden; }
.search-container { margin-bottom: 2rem; }
.search-box { position: relative; max-width: 450px; }
.search-box i { position: absolute; left: 1.25rem; top: 50%; transform: translateY(-50%); color: var(--text-secondary); font-size: 1.2rem; z-index: 2; }
.search-input { width: 100%; padding: 1rem 1rem 1rem 3.5rem; border: 2px solid #e9ecef; border-radius: 16px; font-size: 1rem; background: #fff; transition: .3s; font-weight: 500; }
.clear-search { position: absolute; right: .75rem; top: 50%; transform: translateY(-50%); width: 35px; height: 35px; border: none; background: var(--bg-primary); border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: .3s; color: var(--text-secondary); }
.filters-container { display: flex; gap: 2rem; flex-wrap: wrap; }
.filter-group { display: flex; flex-direction: column; gap: .75rem; min-width: 180px; }
.filter-group label { font-size: .95rem; font-weight: 700; color: var(--text-primary); display: flex; align-items: center; gap: .5rem; }
.filter-select { padding: 1rem 1.25rem; border: 2px solid #e9ecef; border-radius: 16px; font-size: .95rem; background: #fff; cursor: pointer; transition: .3s; font-weight: 500; position: relative; }

/* History List */
.history-section { margin-top: 3rem; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2.5rem; }
.view-toggle { display: flex; gap: .5rem; background: var(--bg-secondary); backdrop-filter: blur(15px); padding: .5rem; border-radius: 16px; border: 1px solid var(--border-light); }
.view-btn { width: 45px; height: 45px; border: none; background: transparent; border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; color: var(--text-secondary); transition: .3s; }
.view-btn.active { background: var(--primary-gradient); color: #fff; box-shadow: 0 8px 25px var(--shadow-primary); }
.history-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 2rem; }
.history-grid.list-view { grid-template-columns: 1fr; }
.history-card { background: var(--bg-primary); backdrop-filter: blur(25px); border-radius: 20px; padding: 2rem; box-shadow: 0 15px 50px var(--shadow-secondary); border: 1px solid var(--border-light); transition: .4s; position: relative; overflow: hidden; cursor: pointer; animation: slideInUp .6s ease forwards; opacity: 0; transform: translateY(30px); }
@keyframes slideInUp { to { opacity: 1; transform: translateY(0); } }
.history-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: var(--primary-gradient); transition: .4s; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
.quiz-info { flex: 1; }
.quiz-title { font-size: 1.4rem; font-weight: 700; color: var(--text-primary); margin: 0 0 .75rem 0; line-height: 1.3; }
.username, .date { display: flex; align-items: center; gap: .5rem; font-size: .9rem; color: var(--text-secondary); font-weight: 500; }
.score-badge { display: flex; flex-direction: column; align-items: center; padding: 1rem 1.5rem; border-radius: 16px; color: #fff; font-weight: 700; min-width: 80px; position: relative; overflow: hidden; }
.score-badge.excellent { background: var(--excellent-color); }
.score-badge.good { background: var(--good-color); }
.score-badge.average { background: var(--average-color); }
.score-badge.poor { background: var(--poor-color); }
.score-value { font-size: 1.8rem; font-weight: 900; line-height: 1; }
.score-label { font-size: .8rem; font-weight: 600; text-transform: uppercase; letter-spacing: .5px; margin-top: .25rem; }
.progress-section { margin-bottom: 1.5rem; }
.progress-label { display: flex; justify-content: space-between; align-items: center; margin-bottom: .75rem; font-size: .9rem; font-weight: 600; color: var(--text-secondary); }
.progress-bar { height: 8px; background: #f0f0f0; border-radius: 4px; overflow: hidden; position: relative; }
.progress-fill { height: 100%; border-radius: 4px; transition: width 1.2s ease; position: relative; overflow: hidden; }
.progress-fill.excellent { background: var(--excellent-color); }
.progress-fill.good { background: var(--good-color); }
.progress-fill.average { background: var(--average-color); }
.progress-fill.poor { background: var(--poor-color); }
.stats-row { display: flex; gap: 2rem; flex-wrap: wrap; }
.card-actions { display: flex; gap: 1rem; flex-wrap: wrap; }
.action-btn { padding: 0.75rem 1.25rem; border: none; border-radius: 10px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: .5rem; background: var(--bg-secondary); color: var(--text-primary); transition: transform .2s ease, box-shadow .2s ease; }
.action-btn i { font-size: 1rem; }
.action-btn.primary { background: #ffffff; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 6px 18px rgba(0,0,0,.08); }
.dark-theme .action-btn.primary { background: var(--bg-dark); border: 1px solid var(--border-dark); color: var(--text-light); }
.action-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 24px rgba(0,0,0,.12); }

/* Pagination */
.pagination-section { margin-top: 3rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; background: var(--bg-primary); backdrop-filter: blur(20px); border-radius: 20px; padding: 1.5rem; box-shadow: 0 10px 40px var(--shadow-secondary); border: 1px solid var(--border-light); }
.pagination-info { font-size: .9rem; color: var(--text-secondary); font-weight: 500; }
.pagination-controls { display: flex; align-items: center; gap: .5rem; }
.page-btn { width: 40px; height: 40px; border: none; border-radius: 10px; background: rgba(0,0,0,.05); color: #333; cursor: pointer; transition: .3s; display: flex; align-items: center; justify-content: center; }
.page-btn:hover:not(:disabled) { background: var(--primary-gradient); color: #fff; transform: translateY(-2px); }
.page-btn:disabled { opacity: .5; cursor: not-allowed; }
.page-numbers { display: flex; gap: .25rem; }
.page-number { min-width: 40px; height: 40px; border: none; border-radius: 10px; background: rgba(0,0,0,.05); color: #333; cursor: pointer; transition: .3s; font-weight: 500; }
.page-number.active { background: var(--primary-gradient); color: #fff; box-shadow: 0 4px 15px rgba(102,126,234,.3); }

/* Detail modal polished */
.detail-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.5); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 1rem; }
.detail-modal { width: 100%; max-width: 760px; background: var(--bg-primary); border: 1px solid var(--border-light); border-radius: 20px; box-shadow: 0 30px 80px rgba(0,0,0,.25); overflow: hidden; }
.dark-theme .detail-modal { background: var(--bg-dark); border: 1px solid var(--border-dark); }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 1rem 1.25rem; border-bottom: 1px solid var(--border-light); background: rgba(255,255,255,0.6); }
.dark-theme .modal-header { background: rgba(0,0,0,0.25); border-bottom-color: var(--border-dark); }
.modal-title { display: flex; align-items: center; gap: .5rem; font-weight: 800; font-size: 1.1rem; color: var(--text-primary); }
.modal-close { background: transparent; border: none; cursor: pointer; color: var(--text-secondary); font-size: 1rem; }
.modal-body { display: grid; grid-template-columns: 1.4fr .8fr; gap: 1.25rem; padding: 1.25rem; }
.quiz-name { margin: 0 0 .5rem 0; font-size: 1.25rem; font-weight: 800; color: var(--text-primary); }
.meta-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: .75rem; }
.meta-item { display: grid; grid-template-columns: 22px auto auto; align-items: center; gap: .5rem; padding: .75rem; background: rgba(0,0,0,.03); border: 1px solid var(--border-light); border-radius: 12px; }
.dark-theme .meta-item { background: rgba(255,255,255,.06); border-color: var(--border-dark); }
.meta-item i { color: var(--primary-color); }
.meta-item strong.up { color: #16a34a; }
.meta-item strong.down { color: #ef4444; }
.modal-score { display: flex; align-items: center; justify-content: center; }
.score-ring { width: 160px; height: 160px; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; box-shadow: 0 12px 40px var(--shadow-secondary); }
.score-ring.excellent { background: var(--excellent-color); }
.score-ring.good { background: var(--good-color); }
.score-ring.average { background: var(--average-color); }
.score-ring.poor { background: var(--poor-color); }
.score-ring .score-value { font-size: 3rem; font-weight: 900; line-height: 1; }
.score-ring .score-text { font-size: .9rem; font-weight: 700; opacity: .95; text-transform: uppercase; letter-spacing: .5px; }
.modal-actions { display: flex; justify-content: flex-end; gap: .75rem; padding: 1rem 1.25rem; border-top: 1px solid var(--border-light); background: rgba(255,255,255,0.6); }
.dark-theme .modal-actions { background: rgba(0,0,0,0.25); border-top-color: var(--border-dark); }
</style>