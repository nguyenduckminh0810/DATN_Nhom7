<template>
  <div class="leaderboard">
    <!-- Loading State -->
    <div v-if="loading && leaderboardData.length === 0" class="text-center py-3">
      <div class="spinner-border spinner-border-sm text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
      <small class="text-muted d-block mt-2">Đang tải bảng xếp hạng...</small>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle me-2"></i>
      {{ error }}
    </div>

    <!-- Content -->
    <div v-else-if="leaderboardData.length > 0" class="leaderboard-content">
      <!-- Header -->
      <div class="leaderboard-header d-flex justify-content-between align-items-center mb-3">
        <h6 class="mb-0">
          <i class="bi bi-trophy text-warning me-2"></i>
          Bảng xếp hạng Quiz
        </h6>
        <span class="badge bg-primary">{{ totalElements }} người chơi</span>
      </div>

      <!-- Leaderboard List -->
      <div class="leaderboard-list">
        <div v-for="(entry, index) in visibleEntries" :key="`${entry.userId}-${entry.attemptedAt}-${index}`"
          :class="['leaderboard-item', { 'my-rank': entry.userId === currentUserId }]" :data-rank="index + 1">
          <!-- Rank -->
          <div class="rank-badge">
            <span v-if="(currentPage * pageSize + index) < 3" class="rank-medal">
              <i :class="getMedalIcon(currentPage * pageSize + index)"></i>
            </span>
            <span v-else class="rank-number">{{ currentPage * pageSize + index + 1 }}</span>
          </div>

          <!-- User Info -->
          <div class="user-info">
            <img :src="entry.avatarUrl || '/img/default-avatar.png'" :alt="entry.fullName || entry.username"
              class="user-avatar" loading="lazy" />
            <div class="user-details">
              <div class="user-name">{{ entry.fullName || entry.username }}</div>
              <small class="text-muted">{{ formatTimeAgo(entry.attemptedAt) }}</small>
            </div>
          </div>

          <!-- Score & Time -->
          <div class="score-info">
            <div class="score-value">{{ entry.score }} điểm</div>
            <div v-if="entry.timeTaken" class="time-taken">
              <i class="bi bi-clock me-1"></i>
              {{ formatTime(entry.timeTaken) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination Controls (hiển thị nếu có nhiều hơn 1 trang) -->
      <div v-if="totalPages > 1" class="pagination-controls mt-3">
        <div class="d-flex justify-content-between align-items-center">
          <div class="pagination-info">
            <small class="text-muted">
              Hiển thị {{ (currentPage * pageSize) + 1 }}-{{
                Math.min((currentPage + 1) * pageSize, totalElements)
              }}
              trong tổng số {{ totalElements }} người chơi
            </small>
          </div>

          <div class="pagination-buttons">
            <button class="btn btn-outline-primary btn-sm me-2"
              @click="() => { console.log(' Previous button clicked, currentPage:', currentPage.value); previousPage(); }"
              :disabled="currentPage === 0">
              <i class="bi bi-chevron-left"></i> Trước
            </button>

            <span class="page-info mx-2"> Trang {{ currentPage + 1 }} / {{ totalPages }} </span>

            <button class="btn btn-outline-primary btn-sm ms-2"
              @click="() => { console.log(' Next button clicked, currentPage:', currentPage.value); nextPage(); }"
              :disabled="currentPage >= totalPages - 1">
              Sau <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-4">
      <i class="bi bi-info-circle text-muted" style="font-size: 2rem"></i>
      <p class="text-muted mt-2 mb-1">Chưa có dữ liệu xếp hạng</p>
      <small class="text-muted">Hãy là người đầu tiên thử sức với quiz này!</small>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from '@/utils/axios'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'QuizLeaderboard' })

/* Props */
const props = defineProps({
  quizId: {
    type: [Number, String],
    required: true,
  },
  limit: {
    type: Number,
    default: 10,
  },
})

/* Stores */
const userStore = useUserStore()

/* State */
const loading = ref(false)
const error = ref(null)
const leaderboardData = ref([])
const pageSize = ref(5) // Hiển thị 5 người mỗi trang
const currentPage = ref(0) // Bắt đầu từ page 0 (backend pagination)
const currentUserId = computed(() => userStore.getUserId())

//  Backend pagination state
const totalElements = ref(0)
const totalPages = ref(0)
const isFirst = ref(true)
const isLast = ref(true)

/* Computed */
const total = computed(() => totalElements.value)

//  Chỉ hiển thị dữ liệu từ backend (đã được phân trang)
const visibleEntries = computed(() => {
  // Lọc dữ liệu để đảm bảo không có duplicate keys
  const uniqueData = leaderboardData.value.filter((entry, index, array) => {
    // Sử dụng userId + attemptedAt + index để đảm bảo tính duy nhất
    const key = `${entry.userId}-${entry.attemptedAt}-${index}`
    const firstIndex = array.findIndex(item =>
      `${item.userId}-${item.attemptedAt}-${array.indexOf(item)}` === key
    )
    return firstIndex === index
  })

  return uniqueData
})

/* Methods */
async function fetchLeaderboard(page = 0) {
  if (loading.value) return

  loading.value = true
  error.value = null

  try {
    // dùng relative path để axios ghép với baseURL "/api"
    const base = 'leaderboard'
    const url = `${base}/quiz/${props.quizId}`
    const params = { page: page, size: pageSize.value }

    console.log(' Calling leaderboard API:', url, params)
    const res = await axios.get(url, { params })
    console.log(' Leaderboard response:', res.data)

    // Backend trả về Map với pagination info
    const data = res.data
    console.log(' Raw API response data:', data)
    console.log(' Data type:', typeof data)
    console.log(' Data keys:', Object.keys(data))

    // Kiểm tra dữ liệu trước khi parse
    if (!data || typeof data !== 'object') {
      throw new Error('Invalid response data format')
    }

    const content = data.content || []
    const totalElementsFromAPI = parseInt(data.totalElements) || 0
    const totalPagesFromAPI = parseInt(data.totalPages) || 0
    const currentPageFromAPI = parseInt(data.currentPage) || 0
    const isFirstFromAPI = Boolean(data.first)
    const isLastFromAPI = Boolean(data.last)

    console.log(' Parsed values:', {
      content: content,
      totalElementsFromAPI: totalElementsFromAPI,
      totalPagesFromAPI: totalPagesFromAPI,
      currentPageFromAPI: currentPageFromAPI,
      isFirstFromAPI: isFirstFromAPI,
      isLastFromAPI: isLastFromAPI
    })

    // Cập nhật dữ liệu
    // Chuẩn hóa timeTaken về giây nếu backend trả ms hoặc string
    const normalized = (content || []).map((e) => {
      let t = typeof e.timeTaken === 'string' ? parseInt(e.timeTaken) : e.timeTaken
      if (Number.isNaN(t)) t = 0
      // Heuristic: nếu lớn hơn 300 và nhỏ hơn 100000 (ms), chia 1000
      if (t > 300 && t < 100000) t = Math.round(t / 1000)
      return { ...e, timeTaken: t }
    })
    leaderboardData.value = normalized
    totalElements.value = totalElementsFromAPI
    totalPages.value = totalPagesFromAPI
    currentPage.value = currentPageFromAPI
    isFirst.value = isFirstFromAPI
    isLast.value = isLastFromAPI

    console.log(' UI Data updated:', {
      leaderboardDataLength: leaderboardData.value.length,
      visibleEntriesLength: visibleEntries.value.length,
      firstEntry: leaderboardData.value[0],
      lastEntry: leaderboardData.value[leaderboardData.value.length - 1]
    })

    // Debug: Kiểm tra pagination state
    console.log(' Pagination state updated:', {
      currentPage: currentPage.value,
      totalPages: totalPages.value,
      isFirst: isFirst.value,
      isLast: isLast.value,
      canGoPrevious: currentPage.value > 0,
      canGoNext: currentPage.value < totalPages.value - 1
    })

    // Debug: Kiểm tra pagination
    console.log(' Paginated leaderboard loaded:', {
      content: content.length,
      totalElements: totalElementsFromAPI,
      totalPages: totalPagesFromAPI,
      currentPage: currentPageFromAPI,
      isFirst: isFirstFromAPI,
      isLast: isLastFromAPI
    })
  } catch (err) {
    console.error(' Error fetching leaderboard:', err)
    error.value = 'Lỗi khi tải bảng xếp hạng'
  } finally {
    loading.value = false
  }
}

function previousPage() {
  if (currentPage.value > 0) {
    const newPage = currentPage.value - 1
    console.log(' Going to previous page:', newPage)
    currentPage.value = newPage
    fetchLeaderboard(newPage)
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    const newPage = currentPage.value + 1
    console.log(' Going to next page:', newPage)
    currentPage.value = newPage
    fetchLeaderboard(newPage)
  }
}

function getMedalIcon(index) {
  const icons = [
    'bi bi-trophy-fill text-warning', // 1st place - gold
    'bi bi-award-fill text-secondary', // 2nd place - silver
    'bi bi-award-fill text-bronze', // 3rd place - bronze
  ]
  return icons[index] || 'bi bi-award'
}

function formatTime(seconds) {
  if (!seconds) return ''

  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60

  if (minutes > 0) {
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
  }
  return `${remainingSeconds}s`
}

function formatTimeAgo(dateString) {
  if (!dateString) return ''

  const date = new Date(dateString)
  const now = new Date()
  const diffInSeconds = Math.floor((now - date) / 1000)

  if (diffInSeconds < 60) return 'Vừa xong'
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} phút trước`
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} giờ trước`
  return `${Math.floor(diffInSeconds / 86400)} ngày trước`
}

/* Watchers */
// Reset pagination khi đổi quiz, không reset khi dữ liệu trang thay đổi
watch(
  () => props.quizId,
  () => {
    currentPage.value = 0
    fetchLeaderboard(0)
  },
)

/* Init */
onMounted(async () => {
  await fetchLeaderboard(0)
})
</script>

<style scoped>
/* ===== Theme tokens ===== */
:root {
  /* Sử dụng global CSS variables để đồng bộ với hệ thống theme */
  --bg: var(--app-background);
  --card: var(--card-bg);
  --border: var(--border-color);
  --text: var(--text-primary);
  --muted: var(--text-secondary);
  --success: var(--success-color);
  --accent1: var(--primary-color);
  --accent2: var(--primary-dark);

  --gold1: #ffe98a;
  --gold2: #f59e0b;
  --silver1: #e5e7eb;
  --silver2: #9ca3af;
  --bronze1: #ffd1a3;
  --bronze2: #c08457;
}

/* Utility (để dùng cho icon đồng) */
.text-bronze {
  color: var(--bronze2);
}

/* Đảm bảo tất cả text đều có màu sắc phù hợp cho dark mode */
.leaderboard-header h6 {
  color: var(--text-primary) !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2) !important;
}

.leaderboard-header .badge {
  background: var(--primary-color) !important;
  color: white !important;
  border: none !important;
}

.score-info .score-value {
  color: var(--success-color) !important;
  font-weight: 700 !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2) !important;
}

.score-info .time-taken {
  color: var(--text-secondary) !important;
  font-size: 0.875rem !important;
}

.pagination-info small {
  color: var(--text-secondary) !important;
}

.page-info {
  color: var(--text-primary) !important;
  font-weight: 600 !important;
}

.btn-outline-primary {
  color: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
}

.btn-outline-primary:hover {
  background: var(--primary-color) !important;
  color: white !important;
}

.btn-outline-primary:disabled {
  color: var(--text-muted) !important;
  border-color: var(--border-color) !important;
  opacity: 0.6 !important;
}

/* ===== Container ===== */
.leaderboard {
  width: 100%;
  max-width: 1100px;
  margin: 16px auto;
  background: transparent;
  color: var(--text);
}

/* ===== Header ===== */
.leaderboard-header {
  padding: 10px 6px 8px;
  border-bottom: 1px solid var(--border);
}

.leaderboard-header h6 {
  font-weight: 800;
  letter-spacing: 0.2px;
}

/* ===== List wrapper ===== */
.leaderboard-list {
  margin-top: 12px;
}

/* ===== Row item ===== */
.leaderboard-item {
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 10px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--card) !important;
  transition:
    box-shadow 0.18s ease,
    transform 0.15s ease;
  box-shadow: 0 2px 8px var(--shadow-color) !important;
}

.leaderboard-item:hover {
  transform: translateX(2px);
  box-shadow: 0 10px 22px var(--shadow-color) !important;
  background: var(--bg-secondary) !important;
  border-color: var(--primary-color) !important;
}

/* Rank badge (1–3 = medal, còn lại là số) */
.rank-badge {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-weight: 800;
}

.rank-medal {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #fff;
  font-size: 0.9rem;
  box-shadow: 0 6px 16px var(--shadow-color) !important;
}

.rank-number {
  font-weight: 900;
  color: var(--text-primary) !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3) !important;
}

/* User info */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  /* enable ellipsis */
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border);
}

.user-details {
  min-width: 0;
}

.user-name {
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-primary) !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2) !important;
}

.user-details small {
  color: var(--text-secondary) !important;
  font-weight: 500 !important;
}

/* Score block */
.score-info {
  text-align: right;
  min-width: 120px;
}

.score-value {
  font-weight: 900;
  font-size: 1.05rem;
  color: var(--success);
}

.time-taken {
  font-size: 0.75rem;
  color: var(--muted);
}

/* ===== My rank highlight ===== */
.leaderboard-item.my-rank {
  border-left: 4px solid var(--accent1);
  background: linear-gradient(90deg,
      rgba(59, 130, 246, 0.1),
      rgba(59, 130, 246, 0.05),
      rgba(59, 130, 246, 0.1));
  animation: pulse 0.6s ease-in-out;
}

@keyframes pulse {
  50% {
    transform: scale(1.01);
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
  }
}

/* ===== Expand / Pagination ===== */
.expand-controls,
.pagination-controls {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 10px 12px;
}

/* ===== Podium (nếu bạn render Top 3 riêng ở trên list) ===== */
.leaderboard .podium-wrap {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  /* 2–1–3 */
  gap: 16px;
  margin: 8px 0 14px;
}

.podium-card {
  position: relative;
  display: grid;
  place-items: center;
  padding: 14px 12px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg,
      color-mix(in srgb, var(--card) 92%, transparent),
      color-mix(in srgb, var(--card) 72%, transparent));
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
}

.podium-card.center {
  transform: translateY(-8px) scale(1.04);
}

.podium-card.center::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 16px;
  padding: 2px;
  background: linear-gradient(135deg, var(--accent1), var(--accent2));
  -webkit-mask:
    linear-gradient(#000 0 0) content-box,
    linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
}

.podium-rank {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 800;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* màu huy hiệu podium 2-1-3 theo thứ tự cột */
.podium-wrap .podium-card:nth-child(2) .podium-rank {
  background: radial-gradient(circle at 30% 30%, var(--gold1), var(--gold2));
}

.podium-wrap .podium-card:nth-child(1) .podium-rank {
  background: radial-gradient(circle at 30% 30%, var(--silver1), var(--silver2));
}

.podium-wrap .podium-card:nth-child(3) .podium-rank {
  background: radial-gradient(circle at 30% 30%, var(--bronze1), var(--bronze2));
}

.podium-avatar {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid color-mix(in srgb, var(--card) 75%, #fff);
  margin: 8px 0;
}

.podium-card.center .podium-avatar {
  width: 92px;
  height: 92px;
  border-width: 4px;
}

.podium-name {
  font-weight: 800;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.podium-score {
  color: var(--muted);
  font-size: 0.95rem;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .leaderboard-item {
    grid-template-columns: 38px 1fr auto;
  }

  .score-info {
    min-width: 96px;
  }

  .leaderboard .podium-wrap {
    grid-template-columns: 1fr;
  }

  .podium-card.center {
    transform: none;
  }
}
</style>
