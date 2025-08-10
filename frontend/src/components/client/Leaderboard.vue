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
        <span class="badge bg-primary">{{ total }} người chơi</span>
      </div>

      <!-- Leaderboard List -->
      <div class="leaderboard-list">
        <div
          v-for="(entry, index) in visibleEntries"
          :key="entry.userId"
          :class="['leaderboard-item', { 'my-rank': entry.userId === currentUserId }]"
          :data-rank="index + 1"
        >
          <!-- Rank -->
          <div class="rank-badge">
            <span v-if="index < 3" class="rank-medal">
              <i :class="getMedalIcon(index)"></i>
            </span>
            <span v-else class="rank-number">{{ index + 1 }}</span>
          </div>

          <!-- User Info -->
          <div class="user-info">
            <img
              :src="entry.avatarUrl || '/img/default-avatar.png'"
              :alt="entry.fullName || entry.username"
              class="user-avatar"
              loading="lazy"
            />
            <div class="user-details">
              <div class="user-name">{{ entry.fullName || entry.username }}</div>
              <small class="text-muted">{{ formatTimeAgo(entry.attemptedAt) }}</small>
            </div>
          </div>

          <!-- Score & Time -->
          <div class="score-info">
            <div class="score-value">{{ entry.score }}%</div>
            <div v-if="entry.timeTaken" class="time-taken">
              <i class="bi bi-clock me-1"></i>
              {{ formatTime(entry.timeTaken) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Expand/Collapse Controls -->
      <div v-if="leaderboardData.length > 5" class="expand-controls text-center mt-3">
        <button
          v-if="!isExpanded"
          @click="expandLeaderboard"
          class="btn btn-outline-primary btn-sm"
        >
          <i class="bi bi-chevron-down me-1"></i>
          Xem thêm ({{ leaderboardData.length - 5 }} người)
        </button>
        <button v-else @click="collapseLeaderboard" class="btn btn-outline-secondary btn-sm">
          <i class="bi bi-chevron-up me-1"></i>
          Thu gọn
        </button>
      </div>

      <!-- Pagination Controls (only show if more than 10 people) -->
      <div v-if="leaderboardData.length > 10 && isExpanded" class="pagination-controls mt-3">
        <div class="d-flex justify-content-between align-items-center">
          <div class="pagination-info">
            <small class="text-muted">
              Hiển thị {{ currentPage * pageSize - pageSize + 1 }}-{{
                Math.min(currentPage * pageSize, total)
              }}
              trong tổng số {{ total }} người chơi
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

            <span class="page-info mx-2"> Trang {{ currentPage }} / {{ totalPages }} </span>

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
const pageSize = ref(5) // Hiển thị 5 người ban đầu
const currentPage = ref(1)
const isExpanded = ref(false)
const currentUserId = computed(() => userStore.getUserId())

/* Computed */
const total = computed(() => leaderboardData.value.length)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// Chỉ hiển thị 5 người đầu tiên khi chưa expand
const visibleEntries = computed(() => {
  if (isExpanded.value) {
    // Khi đã expand, hiển thị theo phân trang
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return leaderboardData.value.slice(start, end)
  } else {
    // Khi chưa expand, chỉ hiển thị 5 người đầu
    return leaderboardData.value.slice(0, 5)
  }
})

/* Methods */
async function fetchLeaderboard() {
  if (loading.value) return

  loading.value = true
  error.value = null

  try {
    // dùng relative path để axios ghép với baseURL "/api"
    const base = 'leaderboard'
    const url = `${base}/quiz/${props.quizId}`
    const params = { limit: 50, offset: 0 } // Lấy tối đa 50 người để có thể expand

    console.log('🔍 Calling leaderboard API:', url, params)
    const res = await axios.get(url, { params })
    console.log('✅ Leaderboard response:', res.data)

    // Backend trả về trực tiếp List<LeaderboardEntry>, không có wrapper success
    const data = res.data || []

    // Cập nhật dữ liệu
    leaderboardData.value = data
    currentPage.value = 1
    isExpanded.value = false
  } catch (err) {
    console.error('❌ Error fetching leaderboard:', err)
    error.value = 'Lỗi khi tải bảng xếp hạng'
  } finally {
    loading.value = false
  }
}

function expandLeaderboard() {
  isExpanded.value = true
  currentPage.value = 1
}

function collapseLeaderboard() {
  isExpanded.value = false
  currentPage.value = 1
}

function previousPage() {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
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
watch(
  leaderboardData,
  () => {
    // Reset pagination when data changes
    currentPage.value = 1
    isExpanded.value = false
  },
  { deep: true },
)

/* Init */
onMounted(async () => {
  await fetchLeaderboard()
})
</script>

<style scoped>
/* ===== Theme tokens ===== */
:root {
  --bg: #ffffff;
  --card: #ffffff;
  --border: rgba(0, 0, 0, 0.08);
  --text: #1f2937;
  --muted: #6b7280;
  --success: #16a34a;
  --accent1: #667eea;
  --accent2: #764ba2;

  --gold1: #ffe98a;
  --gold2: #f59e0b;
  --silver1: #e5e7eb;
  --silver2: #9ca3af;
  --bronze1: #ffd1a3;
  --bronze2: #c08457;
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #0b0f19;
    --card: #0f1525;
    --border: rgba(255, 255, 255, 0.08);
    --text: #e5e7eb;
    --muted: #9ca3af;
  }
}

/* Utility (để dùng cho icon đồng) */
.text-bronze {
  color: var(--bronze2);
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
  background: color-mix(in srgb, var(--card) 88%, transparent);
  transition:
    box-shadow 0.18s ease,
    transform 0.15s ease;
}
.leaderboard-item:hover {
  transform: translateX(2px);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.12);
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
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}
.rank-number {
  font-weight: 900;
  color: var(--muted);
}

/* User info */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0; /* enable ellipsis */
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
}
.user-details small {
  color: var(--muted);
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
  background: linear-gradient(
    90deg,
    rgba(59, 130, 246, 0.1),
    rgba(59, 130, 246, 0.05),
    rgba(59, 130, 246, 0.1)
  );
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
  grid-template-columns: 1fr 1.2fr 1fr; /* 2–1–3 */
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
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--card) 92%, transparent),
    color-mix(in srgb, var(--card) 72%, transparent)
  );
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
