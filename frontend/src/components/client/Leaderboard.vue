<template>
  <section class="lb">
    <!-- Header -->
    <header class="lb__header">
      <div class="lb__title">
        <i class="bi bi-trophy-fill"></i>
        <span>{{ isQuizSpecific ? 'Bảng xếp hạng Quiz' : 'Bảng xếp hạng Toàn cầu' }}</span>
      </div>

      <!-- Period selector (global only) -->
      <div v-if="!isQuizSpecific" class="lb__period" role="tablist" aria-label="Chọn mốc thời gian">
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
    <div v-if="loading" class="lb__state">Đang tải…</div>
    <div v-else-if="error" class="lb__state error">{{ error }}</div>

    <!-- Content -->
    <div v-else class="lb__body">
      <!-- Empty -->
      <div v-if="leaderboardData.length === 0" class="lb__empty">
        <p class="m-0 text-muted">Chưa có dữ liệu xếp hạng</p>
      </div>

      <!-- Podium Top 3 -->
      <div v-else class="lb__podium" aria-label="Top 3">
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
      <ul class="lb__list" v-if="rest.length">
        <li v-for="(entry, idx) in rest" :key="entry.userId" class="rowitem">
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

      <!-- Load more / Collapse -->
      <div class="lb__more" v-if="canLoadMore || isExpanded">
        <button
          v-if="canLoadMore && !isExpanded"
          class="btn btn-primary btn-sm px-3"
          @click="loadMore"
        >
          Xem thêm
        </button>
        <button
          v-if="isExpanded"
          class="btn btn-outline-secondary btn-sm ms-2 px-3"
          @click="collapseList"
        >
          Thu gọn
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from '@/utils/axios'

defineOptions({ name: 'QuizLeaderboard' })

/* Props */
const props = defineProps({
  quizId: { type: Number, default: null },
  limit: { type: Number, default: 10 },
})

/* State */
const loading = ref(false)
const error = ref(null)
const leaderboardData = ref([])
const selectedPeriod = ref('all')
const offset = ref(0)
const total = ref(null)
const pageSize = computed(() => props.limit)

/* UI: periods */
const periods = [
  { value: 'weekly', label: 'Tuần này' },
  { value: 'monthly', label: 'Tháng này' },
  { value: 'all', label: 'Tất cả thời gian' },
]

const isQuizSpecific = computed(() => props.quizId !== null)

/* Derived */
const podiumCount = 3
const podiumFiltered = computed(() => leaderboardData.value.slice(0, podiumCount).filter(Boolean))
// Đặt #1 ở giữa: [2nd, 1st, 3rd]
const podiumThree = computed(() => {
  const p = podiumFiltered.value
  return p.length >= 3 ? [p[1], p[0], p[2]] : p
})
const rest = computed(() => leaderboardData.value.slice(podiumCount))

const lastBatchLength = ref(0)
const canLoadMore = computed(() => {
  if (total.value != null) return leaderboardData.value.length < total.value
  return lastBatchLength.value === pageSize.value
})

// Allow collapsing back to initial size when expanded
const initialSize = computed(() => props.limit)
const isExpanded = computed(() => leaderboardData.value.length > initialSize.value)

/* Fetch */
async function fetchLeaderboard(opts = { append: false }) {
  loading.value = true
  error.value = null
  try {
    // dùng relative path để axios ghép với baseURL "/api"
    const base = 'leaderboard'
    const url = isQuizSpecific.value ? `${base}/quiz/${props.quizId}` : `${base}/global`
    const params = { limit: pageSize.value, offset: offset.value }
    if (!isQuizSpecific.value) params.period = selectedPeriod.value

    const res = await axios.get(url, { params })
    const data = Array.isArray(res.data?.items)
      ? res.data.items
      : Array.isArray(res.data)
        ? res.data
        : []
    lastBatchLength.value = data.length
    if (typeof res.data?.total === 'number') total.value = res.data.total
    leaderboardData.value = opts.append ? [...leaderboardData.value, ...data] : data
  } catch (e) {
    console.error('fetchLeaderboard error:', e?.response?.status, e?.response?.data || e)
    error.value = 'Không thể tải bảng xếp hạng. Vui lòng thử lại sau.'
  } finally {
    loading.value = false
  }
}

function selectPeriod(val) {
  if (selectedPeriod.value === val) return
  selectedPeriod.value = val
}

function loadMore() {
  offset.value += pageSize.value
  fetchLeaderboard({ append: true })
}

function collapseList() {
  // Reset to initial state
  offset.value = 0
  total.value = null
  fetchLeaderboard({ append: false })
}

function formatTime(seconds) {
  if (!seconds && seconds !== 0) return ''
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}:${s.toString().padStart(2, '0')}` : `${s}s`
}

/* Watchers */
watch(selectedPeriod, () => {
  if (!isQuizSpecific.value) {
    offset.value = 0
    fetchLeaderboard()
  }
})

/* Init */
onMounted(() => {
  fetchLeaderboard()
})
</script>

<style scoped>
/* ===== Theme tokens ===== */
:root {
  --c-bg: #ffffff;
  --c-card: #ffffff;
  --c-border: rgba(0, 0, 0, 0.08);
  --c-text: #1f2937;
  --c-muted: #6b7280;
  --c-success: #16a34a;
  --c-accent-1: #667eea;
  --c-accent-2: #764ba2;
}
@media (prefers-color-scheme: dark) {
  :root {
    --c-bg: #0b0f19;
    --c-card: #0f1525;
    --c-border: rgba(255, 255, 255, 0.08);
    --c-text: #e5e7eb;
    --c-muted: #9ca3af;
  }
}

/* ===== Container ===== */
.lb {
  width: 100%;
  max-width: 1100px;
  margin: 16px auto;
  background: transparent; /* tránh dải trắng */
}

/* ===== Header ===== */
.lb__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 6px 8px;
  border-bottom: 1px solid var(--c-border);
  background: transparent; /* không phủ nền */
}
.lb__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 900;
  letter-spacing: 0.3px;
  color: var(--c-text);
  font-size: 1.15rem;
}
.lb__title .bi {
  color: #f59e0b;
}
.lb__period {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.pill {
  border: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-card) 85%, #fff);
  color: var(--c-text);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
}
.pill.is-active {
  background: linear-gradient(135deg, var(--c-accent-1), var(--c-accent-2));
  color: #fff;
  border-color: transparent;
  box-shadow: 0 6px 18px rgba(118, 75, 162, 0.35);
}

/* ===== States ===== */
.lb__state {
  padding: 24px 8px;
  text-align: center;
  color: var(--c-muted);
}
.lb__state.error {
  color: #dc2626;
}

/* ===== Body ===== */
.lb__body {
  padding: 12px 0 16px;
}
.lb__empty {
  padding: 20px 8px;
  text-align: center;
}

/* ===== Podium ===== */
.lb__podium {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 16px;
  padding: 8px 0 16px;
  position: relative;
}

.lb__podium::before {
  content: '';
  position: absolute;
  inset: -12px 0 -8px 0;
  background: radial-gradient(60% 80% at 50% 0%, rgba(255, 255, 255, 0.08), transparent 70%);
  pointer-events: none;
}

/* Subtle background pattern (SVG) + gradient ring for top-1 via ::before mask */
.podium {
  position: relative;
  display: grid;
  place-items: center;
  padding: 14px 12px;
  border-radius: 16px;
  border: 1px solid var(--c-border);
  background:
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--c-card) 90%, transparent),
      color-mix(in srgb, var(--c-card) 70%, transparent)
    ),
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160' viewBox='0 0 160 160'%3E%3Cg fill='none' stroke='%23838bab' stroke-opacity='0.10' stroke-width='1'%3E%3Cpath d='M80 0v160M0 80h160'/%3E%3Ccircle cx='80' cy='80' r='18'/%3E%3C/g%3E%3C/svg%3E");
  background-size:
    auto,
    180px 180px;
  background-position:
    0 0,
    center;
  background-repeat: no-repeat;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(8px);
}
.podium--center {
  transform: translateY(-8px) scale(1.05);
}
.podium--center::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 16px;
  padding: 2px; /* độ dày viền */
  background: linear-gradient(135deg, var(--c-accent-1), var(--c-accent-2));
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
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #fff;
  font-size: 0.9rem;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* Gold/Silver/Bronze medals according to visual order [2,1,3] */
.lb__podium .podium:nth-child(2) .podium__rank {
  /* #1 center */
  background: radial-gradient(circle at 30% 30%, #ffe98a, #f59e0b);
}
.lb__podium .podium:nth-child(1) .podium__rank {
  /* #2 left */
  background: radial-gradient(circle at 30% 30%, #e5e7eb, #9ca3af);
}
.lb__podium .podium:nth-child(3) .podium__rank {
  /* #3 right */
  background: radial-gradient(circle at 30% 30%, #ffd1a3, #c08457);
}
.podium__avatar {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid color-mix(in srgb, var(--c-card) 75%, #fff);
  margin: 8px 0;
}
.lb__podium .podium:nth-child(2) .podium__avatar {
  /* center bigger */
  width: 92px;
  height: 92px;
  border-width: 4px;
}
.podium__name {
  font-weight: 800;
  text-align: center;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--c-text);
}
.podium__score {
  color: var(--c-muted);
  font-size: 0.95rem;
}

/* ===== List rows ===== */
.lb__list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  /* Bỏ thanh cuộn để tránh hiện 2 scrollbar khi hover */
  max-height: none;
  overflow: visible;
}
.rowitem {
  display: grid;
  grid-template-columns: 40px 48px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-card) 88%, transparent);
  transition:
    transform 0.15s ease,
    box-shadow 0.2s ease;
  margin-bottom: 8px;
}
.rowitem:hover {
  transform: translateX(2px);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.12);
}
.rowitem__rank {
  font-weight: 800;
  color: var(--c-muted);
  text-align: center;
}
.rowitem__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--c-border);
}
.rowitem__name {
  font-weight: 700;
  color: var(--c-text);
}
.rowitem__meta {
  text-align: right;
  min-width: 120px;
}
.score__val {
  font-weight: 900;
  font-size: 1.1rem;
  color: var(--c-success);
}
.score__label {
  display: block;
  font-size: 0.72rem;
  color: var(--c-muted);
  letter-spacing: 0.4px;
  margin-top: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .lb__header {
    flex-direction: column;
    align-items: flex-start;
  }
  .lb__podium {
    grid-template-columns: 1fr;
  }
  .podium--center {
    transform: none;
  }
}
</style>
