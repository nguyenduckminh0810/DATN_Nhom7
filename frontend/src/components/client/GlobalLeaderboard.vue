<template>
  <section class="lb">
    <!-- Header -->
    <header class="lb__header">
      <div class="lb__title">
        <i class="bi bi-trophy-fill"></i>
        <span>Bảng xếp hạng Toàn cầu</span>
      </div>

      <!-- Period selector -->
      <div class="lb__period" role="tablist" aria-label="Chọn mốc thời gian">
        <button
          v-for="period in periods"
          :key="period.value"
          class="pill"
          :class="{ 'is-active': selectedPeriod === period.value }"
          role="tab"
          :aria-selected="selectedPeriod === period.value"
          @click="selectPeriod(period.value)"
        >
          {{ period.label }}
        </button>
      </div>
    </header>

    <!-- Loading / Error -->
    <div v-if="loading && leaderboardData.length === 0" class="lb__state">Đang tải…</div>
    <div v-else-if="error" class="lb__state error">{{ error }}</div>

    <!-- Content -->
    <div v-else class="lb__body">
      <!-- Empty -->
      <div v-if="leaderboardData.length === 0" class="lb__empty">
        <p class="m-0 text-muted">Chưa có dữ liệu xếp hạng</p>
      </div>

      <!-- Stats Overview -->
      <div v-if="leaderboardData.length > 0" class="lb__stats">
        <div class="stats-info">
          <span class="stats-text"> Đang xem {{ currentViewRange }} trên {{ total }} người </span>
          <span v-if="myRank > 0" class="my-rank">
            Vị trí của tôi: <strong>#{{ myRank }}</strong>
          </span>
        </div>

        <!-- Jump to my rank button -->
        <button
          v-if="myRank > 0 && myRank > visibleRange.end"
          class="btn btn-outline-primary btn-sm jump-to-rank"
          @click="jumpToMyRank"
        >
          <i class="bi bi-arrow-down-circle"></i>
          Xem vị trí của tôi
        </button>
      </div>

      <!-- Podium Top 3 - Sticky -->
      <div v-if="leaderboardData.length > 0" class="lb__podium sticky-top" aria-label="Top 3">
        <div
          v-for="(p, idx) in podiumThree"
          :key="p.userId"
          :class="['podium', { 'podium--center': idx === 1 }]"
        >
          <div class="podium__rank">{{ idx === 1 ? 1 : idx === 0 ? 2 : 3 }}</div>
          <img
            class="podium__avatar"
            :src="p.avatarUrl || '/img/default-avatar.png'"
            :alt="p.fullName || p.username"
            loading="lazy"
          />
          <div class="podium__name" :title="p.fullName || p.username">
            {{ p.fullName || p.username }}
          </div>
          <div class="podium__score">
            <strong>{{ p.score }}</strong> điểm
          </div>
        </div>
      </div>

      <!-- List -->
      <div v-if="rest.length" class="lb__list-container">
        <ul class="lb__list">
          <li
            v-for="(entry, idx) in rest"
            :key="entry.userId"
            :class="['rowitem', { 'my-rank-highlight': entry.userId === currentUserId }]"
            :data-rank="idx + podiumCount + 1"
          >
            <div class="rowitem__rank">{{ idx + podiumCount + 1 }}</div>
            <img
              class="rowitem__avatar"
              :src="entry.avatarUrl || '/img/default-avatar.png'"
              :alt="entry.fullName || entry.username"
              loading="lazy"
            />
            <div class="rowitem__main">
              <div class="rowitem__name">{{ entry.fullName || entry.username }}</div>
            </div>
            <div class="rowitem__meta">
              <div class="score">
                <span class="score__val">{{ entry.score }}</span>
                <span class="score__label">điểm</span>
              </div>
              <div v-if="entry.timeTaken" class="time">
                <i class="bi bi-clock"></i> {{ formatTime(entry.timeTaken) }}
              </div>
            </div>
          </li>
        </ul>

        <!-- Load More Controls -->
        <div class="lb__controls">
          <!-- Load More Button -->
          <button
            v-if="canLoadMore"
            class="btn btn-primary btn-sm px-3"
            @click="loadMore"
            :disabled="loadingMore"
          >
            <span v-if="loadingMore" class="spinner-border spinner-border-sm me-2"></span>
            Xem thêm ({{ remainingCount }} người)
          </button>

          <!-- End of list indicator -->
          <div v-else-if="leaderboardData.length > 0" class="lb__end">
            <i class="bi bi-flag-checkered text-muted"></i>
            <span class="text-muted">Đã hiển thị tất cả {{ total }} người</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from '@/utils/axios'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'GlobalLeaderboard' })

/* Props */
const props = defineProps({
  limit: { type: Number, default: 10 },
})

/* Stores */
const userStore = useUserStore()

/* State */
const loading = ref(false)
const loadingMore = ref(false)
const error = ref(null)
const leaderboardData = ref([])
const selectedPeriod = ref('all')
const offset = ref(0)
const total = ref(null)
const pageSize = computed(() => props.limit)
const currentUserId = computed(() => userStore.getUserId())

/* UI: periods */
const periods = [
  { value: 'weekly', label: 'Tuần này' },
  { value: 'monthly', label: 'Tháng này' },
  { value: 'all', label: 'Tất cả thời gian' },
]

/* Derived */
const podiumCount = 3
const podiumFiltered = computed(() => leaderboardData.value.slice(0, podiumCount).filter(Boolean))
// Đặt #1 ở giữa: [2nd, 1st, 3rd]
const podiumThree = computed(() => {
  const p = podiumFiltered.value
  return p.length >= 3 ? [p[1], p[0], p[2]] : p
})
const rest = computed(() => leaderboardData.value.slice(podiumCount))

// Computed properties for stats and navigation
const currentViewRange = computed(() => {
  if (leaderboardData.value.length === 0) return '0-0'
  const start = 1
  const end = leaderboardData.value.length
  return `${start}-${end}`
})

const visibleRange = computed(() => ({
  start: 1,
  end: leaderboardData.value.length,
}))

const myRank = computed(() => {
  if (!currentUserId.value) return 0
  const userIndex = leaderboardData.value.findIndex((entry) => entry.userId === currentUserId.value)
  return userIndex >= 0 ? userIndex + 1 : 0
})

const remainingCount = computed(() => {
  if (total.value === null) return '?'
  return Math.max(0, total.value - leaderboardData.value.length)
})

const lastBatchLength = ref(0)
const canLoadMore = computed(() => {
  if (total.value != null) return leaderboardData.value.length < total.value
  return lastBatchLength.value === pageSize.value
})

/* Methods */
async function fetchLeaderboard() {
  if (loading.value) return

  loading.value = true
  error.value = null

  try {
    // dùng relative path để axios ghép với baseURL "/api"
    const base = 'leaderboard'
    const url = `${base}/global`
    const params = { limit: pageSize.value, offset: offset.value, period: selectedPeriod.value }

    console.log('🔍 Calling global leaderboard API:', url, params)
    const res = await axios.get(url, { params })
    console.log('✅ Global leaderboard response:', res.data)

    // Backend trả về trực tiếp List<LeaderboardEntry>, không có wrapper success
    const data = res.data || []

    if (offset.value === 0) {
      // Initial load
      leaderboardData.value = data
    } else {
      // Append more data
      leaderboardData.value.push(...data)
    }

    // Cập nhật total và lastBatchLength
    total.value = data.length + offset.value
    lastBatchLength.value = data.length
  } catch (err) {
    console.error('Error fetching leaderboard:', err)
    if (err.response?.status === 500) {
      error.value = 'Lỗi server. Vui lòng thử lại sau.'
    } else if (err.response?.status === 404) {
      error.value = 'Không tìm thấy dữ liệu xếp hạng.'
    } else {
      error.value = 'Lỗi khi tải bảng xếp hạng'
    }
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !canLoadMore.value) return

  loadingMore.value = true
  offset.value += pageSize.value

  try {
    await fetchLeaderboard()
  } finally {
    loadingMore.value = false
  }
}

function selectPeriod(period) {
  selectedPeriod.value = period
  offset.value = 0
  fetchLeaderboard()
}

function jumpToMyRank() {
  if (!myRank.value) return

  const myRankElement = document.querySelector(`[data-rank="${myRank.value}"]`)
  if (myRankElement) {
    myRankElement.scrollIntoView({ behavior: 'smooth', block: 'center' })

    // Add highlight effect
    myRankElement.classList.add('my-rank-highlight')
    setTimeout(() => {
      myRankElement.classList.remove('my-rank-highlight')
    }, 2000)
  }
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

/* Watchers */
watch(selectedPeriod, () => {
  offset.value = 0
  fetchLeaderboard()
})

watch(
  leaderboardData,
  () => {
    // Có thể thêm logic khác ở đây nếu cần
  },
  { deep: true },
)

/* Init */
onMounted(async () => {
  await fetchLeaderboard()
})
</script>

<style scoped>
/* ============ Theme tokens ============ */
:root {
  --bg: #f7f8fb; /* nền tổng thể dịu */
  --card: #ffffff;
  --border: rgba(0, 0, 0, 0.08);
  --text: #0f172a;
  --muted: #6b7280;
  --success: #16a34a;
  --accent1: #4f46e5;
  --accent2: #7c3aed;

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

/* ============ Container ============ */
.lb {
  max-width: 1100px;
  margin: 20px auto;
  background: transparent;
  border: none;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: none;
}

/* ============ Header ============ */
.lb__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
  background: linear-gradient(135deg, var(--accent1), var(--accent2));
  color: #fff;
}
.lb__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  font-size: 1.1rem;
}
.lb__title i {
  font-size: 1.25rem;
  color: #ffd700;
}

.lb__period {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.pill {
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.85rem;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  backdrop-filter: blur(4px);
  transition: all 0.2s ease;
}
.pill:hover {
  background: rgba(255, 255, 255, 0.22);
}
.pill.is-active {
  background: #fff;
  color: var(--accent1);
  border-color: #fff;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* ============ States & stats ============ */
.lb__state {
  padding: 32px 22px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  margin: 0 18px;
}
.lb__state.error {
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
}
.lb__empty {
  padding: 36px 22px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  margin: 0 18px;
}

.lb__stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 12px 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  margin: 0 18px;
}
.stats-text {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}
.my-rank {
  font-size: 0.9rem;
  color: #ffd700;
  font-weight: 600;
}
.jump-to-rank {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 0.85rem;
  transition:
    transform 0.15s ease,
    box-shadow 0.2s ease;
}
.jump-to-rank:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.15);
}

/* ============ Podium ============ */
.lb__podium {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 14px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  margin: 0 18px 16px 18px;
}
.podium {
  position: relative;
  display: grid;
  place-items: center;
  gap: 8px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.15);
}
.podium--center {
  transform: translateY(-8px) scale(1.04);
}
.podium--center::before {
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
  mask-composite: exclude; /* chỉ giữ phần viền */
}
.podium__rank {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 800;
  font-size: 0.9rem;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}
/* màu huy hiệu 2-1-3 theo thứ tự hiển thị */
.lb__podium .podium:nth-child(2) .podium__rank {
  background: radial-gradient(circle at 30% 30%, var(--gold1), var(--gold2));
}
.lb__podium .podium:nth-child(1) .podium__rank {
  background: radial-gradient(circle at 30% 30%, var(--silver1), var(--silver2));
}
.lb__podium .podium:nth-child(3) .podium__rank {
  background: radial-gradient(circle at 30% 30%, var(--bronze1), var(--bronze2));
}

.podium__avatar {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  object-fit: cover;
  margin: 6px 0;
  border: 3px solid color-mix(in srgb, var(--card) 75%, #fff);
}
.podium--center .podium__avatar {
  width: 92px;
  height: 92px;
  border-width: 4px;
}
.podium__name {
  font-weight: 800;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text);
}
.podium__score {
  color: var(--muted);
  font-size: 0.95rem;
}

/* ============ List ============ */
.lb__list {
  list-style: none;
  margin: 0;
  padding: 0;
  margin-top: 16px;
}
.rowitem {
  display: grid;
  grid-template-columns: 40px 48px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  transition:
    background 0.15s ease,
    transform 0.12s ease,
    box-shadow 0.18s ease;
  margin-bottom: 8px;
}
.rowitem:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateX(1px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.15);
}
.rowitem:last-child {
  border-bottom: none;
}

.rowitem__rank {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--silver1), var(--silver2));
  color: #111827;
  font-weight: 800;
  font-size: 0.85rem;
}
.rowitem__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
}
.rowitem__main {
  min-width: 0;
}
.rowitem__name {
  font-weight: 700;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rowitem__meta {
  text-align: right;
  min-width: 120px;
}
.score__val {
  font-weight: 900;
  font-size: 1.06rem;
  color: var(--success);
}
.score__label {
  display: block;
  font-size: 0.72rem;
  color: var(--muted);
  letter-spacing: 0.4px;
  margin-top: 2px;
}
.time {
  font-size: 0.75rem;
  color: var(--muted);
}

/* highlight “vị trí của tôi” khi jump */
.my-rank-highlight {
  animation: hl 0.6s ease-in-out;
  background: linear-gradient(
    90deg,
    rgba(79, 70, 229, 0.12),
    rgba(79, 70, 229, 0.06),
    rgba(79, 70, 229, 0.12)
  ) !important;
  border-left: 4px solid var(--accent1) !important;
}
@keyframes hl {
  50% {
    transform: scale(1.01);
    box-shadow: 0 6px 18px rgba(79, 70, 229, 0.2);
  }
}

/* ============ Controls ============ */
.lb__controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  margin: 14px 18px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.lb__end {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

/* ============ Responsive ============ */
@media (max-width: 768px) {
  .lb {
    margin: 12px;
  }
  .lb__header {
    flex-direction: column;
    text-align: center;
  }
  .lb__period {
    justify-content: center;
  }
  .lb__stats {
    flex-direction: column;
    text-align: center;
  }
  .lb__podium {
    grid-template-columns: 1fr;
    gap: 14px;
  }
  .podium--center {
    transform: none;
  }
  .rowitem {
    grid-template-columns: 36px 44px 1fr;
    gap: 10px;
  }
  .rowitem__meta {
    grid-column: 1/-1;
    text-align: center;
    margin-top: 6px;
  }
}
</style>
