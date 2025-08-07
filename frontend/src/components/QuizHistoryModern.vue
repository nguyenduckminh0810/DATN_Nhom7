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
    <div class="header-section">
      <div class="header-content">
        <div class="title-group">
          <div class="icon-wrapper">
            <i class="bi bi-graph-up-arrow"></i>
          </div>
          <div class="title-text">
            <h1>{{ isAdmin ? 'Quản lý lịch sử làm bài' : 'Lịch sử làm bài của tôi' }}</h1>
            <p>{{ isAdmin ? 'Theo dõi và phân tích kết quả của tất cả người dùng' : 'Theo dõi tiến trình học tập và phân tích kết quả cá nhân' }}</p>
          </div>
        </div>
        
        <div class="header-controls">
          <button @click="refreshData" class="refresh-btn" :disabled="loading">
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
                <div class="legend-color"></div>
                <span>Xuất sắc (90+)</span>
              </div>
              <div class="legend-item good">
                <div class="legend-color"></div>
                <span>Tốt (70-89)</span>
              </div>
              <div class="legend-item average">
                <div class="legend-color"></div>
                <span>Trung bình (50-69)</span>
              </div>
              <div class="legend-item poor">
                <div class="legend-color"></div>
                <span>Cần cải thiện (&lt;50)</span>
              </div>
            </div>
          </div>
          <div class="performance-bars">
            <div class="performance-bar excellent">
              <div class="bar-fill" :style="{ width: `${excellentPercentage}%` }">
                <span class="bar-label">{{ excellentCount }}</span>
              </div>
            </div>
            <div class="performance-bar good">
              <div class="bar-fill" :style="{ width: `${goodPercentage}%` }">
                <span class="bar-label">{{ goodCount }}</span>
              </div>
            </div>
            <div class="performance-bar average">
              <div class="bar-fill" :style="{ width: `${averagePercentage}%` }">
                <span class="bar-label">{{ averageCount }}</span>
              </div>
            </div>
            <div class="performance-bar poor">
              <div class="bar-fill" :style="{ width: `${poorPercentage}%` }">
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
            <input 
              v-model="searchQuery" 
              type="text" 
              :placeholder="isAdmin ? 'Tìm kiếm quiz hoặc người dùng...' : 'Tìm kiếm quiz...'"
              class="search-input"
            />
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
            <button 
              @click="viewMode = 'card'" 
              :class="{ active: viewMode === 'card' }"
              class="view-btn"
            >
              <i class="bi bi-grid-3x3-gap"></i>
            </button>
            <button 
              @click="viewMode = 'list'" 
              :class="{ active: viewMode === 'list' }"
              class="view-btn"
            >
              <i class="bi bi-list"></i>
            </button>
          </div>
        </div>

        <div class="history-grid" :class="{ 'list-view': viewMode === 'list' }">
          <div 
            v-for="(attempt, index) in paginatedAttempts" 
            :key="attempt.id"
            class="history-card"
            :class="getScoreClass(attempt.score)"
            :style="{ 'animation-delay': `${index * 0.1}s` }"
          >
            <div class="card-header">
              <div class="quiz-info">
                <h3 class="quiz-title">{{ attempt.quizTitle }}</h3>
                <div class="quiz-meta">
                  <span v-if="isAdmin" class="username">
                    <i class="bi bi-person-fill"></i>
                    {{ attempt.username }}
                  </span>
                  <span class="date">
                    <i class="bi bi-calendar3"></i>
                    {{ formatDate(attempt.attemptedAt) }}
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
                  <div 
                    class="progress-fill" 
                    :class="getScoreClass(attempt.score)"
                    :style="{ width: `${attempt.score}%` }"
                  ></div>
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
              <button class="action-btn secondary">
                <i class="bi bi-eye"></i>
                Chi tiết
              </button>
              <button class="action-btn primary">
                <i class="bi bi-arrow-repeat"></i>
                Làm lại
              </button>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination-section">
          <div class="pagination-info">
            Hiển thị {{ ((currentPage - 1) * pageSize) + 1 }} - {{ Math.min(currentPage * pageSize, filteredAttempts.length) }} 
            trong tổng số {{ filteredAttempts.length }} kết quả
          </div>
          <div class="pagination-controls">
            <button 
              @click="changePage(1)" 
              :disabled="currentPage === 1"
              class="page-btn"
            >
              <i class="bi bi-chevron-double-left"></i>
            </button>
            <button 
              @click="changePage(currentPage - 1)" 
              :disabled="currentPage === 1"
              class="page-btn"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
            
            <div class="page-numbers">
              <button 
                v-for="page in visiblePages" 
                :key="page"
                @click="changePage(page)"
                :class="{ active: page === currentPage }"
                class="page-number"
              >
                {{ page }}
              </button>
            </div>
            
            <button 
              @click="changePage(currentPage + 1)" 
              :disabled="currentPage === totalPages"
              class="page-btn"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
            <button 
              @click="changePage(totalPages)" 
              :disabled="currentPage === totalPages"
              class="page-btn"
            >
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
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

// Filters
const filters = ref({
  userId: '',
  quizId: ''
})

// Mock data
const users = ref([
  { id: 1, username: 'nguyenvana' },
  { id: 2, username: 'tranthib' }
])

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

<style scoped>
@import './QuizHistoryModern.css';
@import './QuizHistoryModern2.css';

/* Component-specific overrides if needed */
.quiz-history-modern {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow-x: hidden;
  transition: all 0.3s ease;
}

.quiz-history-modern.dark-theme {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: #ecf0f1;
}

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
    radial-gradient(circle at 25% 25%, rgba(255,255,255,0.1) 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: drift 20s linear infinite;
}

.dark-theme .pattern-grid {
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255,255,255,0.05) 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, rgba(255,255,255,0.05) 1px, transparent 1px);
}

@keyframes drift {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
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
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(180deg); }
}
</style>