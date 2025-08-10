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
          <button
            v-if="canLoadMore"
            class="btn btn-primary btn-sm px-3"
            @click="loadMore"
            :disabled="loadingMore"
          >
            <span v-if="loadingMore" class="spinner-border spinner-border-sm me-2"></span>
            Xem thêm ({{ remainingCount }} người)
          </button>

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

const props = defineProps({ limit: { type: Number, default: 10 } })
const userStore = useUserStore()

const loading = ref(false)
const loadingMore = ref(false)
const error = ref(null)
const leaderboardData = ref([])
const selectedPeriod = ref('all')
const offset = ref(0)
const total = ref(null)

const pageSize = computed(() => props.limit)
const currentUserId = computed(() => userStore.getUserId())

const periods = [
  { value: 'weekly', label: 'Tuần này' },
  { value: 'monthly', label: 'Tháng này' },
  { value: 'all', label: 'Tất cả thời gian' },
]

const podiumCount = 3
const podiumFiltered = computed(() => leaderboardData.value.slice(0, podiumCount).filter(Boolean))
const podiumThree = computed(() => {
  const p = podiumFiltered.value
  return p.length >= 3 ? [p[1], p[0], p[2]] : p
})
const rest = computed(() => leaderboardData.value.slice(podiumCount))

const currentViewRange = computed(() => {
  if (leaderboardData.value.length === 0) return '0-0'
  return `1-${leaderboardData.value.length}`
})
const visibleRange = computed(() => ({ start: 1, end: leaderboardData.value.length }))

const myRank = computed(() => {
  if (!currentUserId.value) return 0
  const userIndex = leaderboardData.value.findIndex((e) => e.userId === currentUserId.value)
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

async function fetchLeaderboard() {
  if (loading.value) return
  loading.value = true
  error.value = null
  try {
    const res = await axios.get('leaderboard/global', {
      params: { limit: pageSize.value, offset: offset.value, period: selectedPeriod.value },
    })
    const data = res.data || []
    if (offset.value === 0) leaderboardData.value = data
    else leaderboardData.value.push(...data)
    total.value = data.length + offset.value
    lastBatchLength.value = data.length
  } catch (err) {
    if (err.response?.status === 500) error.value = 'Lỗi server. Vui lòng thử lại sau.'
    else if (err.response?.status === 404) error.value = 'Không tìm thấy dữ liệu xếp hạng.'
    else error.value = 'Lỗi khi tải bảng xếp hạng'
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
  const el = document.querySelector(`[data-rank="${myRank.value}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('my-rank-highlight')
    setTimeout(() => el.classList.remove('my-rank-highlight'), 2000)
  }
}

function formatTime(seconds) {
  if (!seconds) return ''
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}:${s.toString().padStart(2, '0')}` : `${s}s`
}

watch(selectedPeriod, () => {
  offset.value = 0
  fetchLeaderboard()
})
watch(leaderboardData, () => {}, { deep: true })

onMounted(async () => {
  await fetchLeaderboard()
})
</script>

<style scoped>
/* ===== Tokens ===== */
:root {
  --bg: #0b0f19; /* sẽ bị override bởi theme của site nếu có */
  --card: #ffffff;
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

  --nav-h: 72px; /* CHỈNH đúng chiều cao navbar của bạn */
}

/* ===== Container: đẩy hẳn xuống dưới navbar, bỏ phần trắng thừa ===== */
.lb {
  max-width: 1100px;
  margin: 20px auto;
  margin-top: calc(var(--nav-h) + 20px);
  background: transparent;
}

/* ===== Header ton-sur-ton nền tím, dễ đọc ===== */
.lb__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 16px;
  backdrop-filter: blur(12px);
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
  color: #ffd54f;
}

.lb__period {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.pill {
  padding: 6px 14px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.85rem;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  backdrop-filter: blur(4px);
  transition: all 0.2s ease;
}
.pill:hover {
  background: rgba(124, 58, 237, 0.28);
  border-color: rgba(124, 58, 237, 0.55);
}
.pill.is-active {
  background: linear-gradient(135deg, var(--accent1), var(--accent2));
  color: #fff;
  border-color: transparent;
  box-shadow: 0 8px 22px rgba(79, 70, 229, 0.35);
}

/* ===== States & Stats ===== */
.lb__state,
.lb__empty {
  padding: 32px 22px;
  margin: 0 18px;
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}
.lb__state.error {
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
}

.lb__stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 12px 18px;
  margin: 0 18px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  backdrop-filter: blur(10px);
}
.stats-text {
  color: rgba(255, 255, 255, 0.9);
}
.my-rank {
  color: #ffe082;
  font-weight: 600;
}

/* ===== Podium ===== */
.lb__podium {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 20px;
  padding: 20px 22px;
  margin: 0 18px 20px 18px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.podium {
  position: relative;
  display: grid;
  place-items: center;
  gap: 10px;
  padding: 18px 16px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}
.podium:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.2);
}
.podium--center {
  transform: translateY(-16px) scale(1.08);
}
.podium--center:hover {
  transform: translateY(-18px) scale(1.08);
}
.podium--center::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 18px;
  padding: 2px;
  background: linear-gradient(135deg, var(--accent1), var(--accent2));
  -webkit-mask:
    linear-gradient(#000 0 0) content-box,
    linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
}

/* Huy hiệu số thứ hạng – MÀU RÕ RÀNG, dễ nhìn */
.podium__rank {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 1.1rem;
  border: 3px solid rgba(255, 255, 255, 0.8);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.25),
    inset 0 0 8px rgba(255, 255, 255, 0.4);
}
/* thứ tự hiển thị 2-1-3 -> gán màu #1,#2,#3 đúng và DỄ NHÌN */
.lb__podium .podium:nth-child(2) .podium__rank {
  /* #1 - VÀNG */
  background: linear-gradient(135deg, #ffd700, #ffb347);
  color: #8b4513;
  border-color: #ffd700;
  box-shadow:
    0 12px 28px rgba(255, 215, 0, 0.5),
    0 0 0 3px rgba(255, 215, 0, 0.6) inset,
    inset 0 0 12px rgba(255, 255, 255, 0.6);
}
.lb__podium .podium:nth-child(1) .podium__rank {
  /* #2 - BẠC */
  background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
  color: #2f2f2f;
  border-color: #c0c0c0;
  box-shadow:
    0 10px 24px rgba(192, 192, 192, 0.4),
    0 0 0 3px rgba(192, 192, 192, 0.5) inset,
    inset 0 0 10px rgba(255, 255, 255, 0.5);
}
.lb__podium .podium:nth-child(3) .podium__rank {
  /* #3 - ĐỒNG */
  background: linear-gradient(135deg, #cd7f32, #b8860b);
  color: #2f1b14;
  border-color: #cd7f32;
  box-shadow:
    0 10px 24px rgba(205, 127, 50, 0.4),
    0 0 0 3px rgba(205, 127, 50, 0.5) inset,
    inset 0 0 10px rgba(255, 255, 255, 0.5);
}
/* khối podium glow theo huy chương */
.lb__podium .podium:nth-child(2) {
  box-shadow:
    0 20px 40px rgba(255, 215, 0, 0.4),
    0 0 0 2px rgba(255, 215, 0, 0.4) inset;
}
.lb__podium .podium:nth-child(1) {
  box-shadow:
    0 16px 32px rgba(192, 192, 192, 0.4),
    0 0 0 2px rgba(192, 192, 192, 0.4) inset;
}
.lb__podium .podium:nth-child(3) {
  box-shadow:
    0 16px 32px rgba(205, 127, 50, 0.4),
    0 0 0 2px rgba(205, 127, 50, 0.4) inset;
}

.podium__avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin: 8px 0;
  border: 3px solid color-mix(in srgb, var(--card) 75%, #fff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.podium--center .podium__avatar {
  width: 104px;
  height: 104px;
  border-width: 4px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}
.podium__name {
  font-weight: 800;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text);
  font-size: 1.05rem;
}
.podium__score {
  color: var(--muted);
  font-size: 0.95rem;
  font-weight: 600;
}

/* ===== List ===== */
.lb__list {
  list-style: none;
  margin: 0;
  padding: 0;
  margin-top: 20px;
}
.rowitem {
  display: grid;
  grid-template-columns: 44px 52px 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 12px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  transition:
    background 0.2s ease,
    transform 0.15s ease,
    box-shadow 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.rowitem:hover {
  background: rgba(255, 255, 255, 0.98);
  transform: translateX(2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}
.rowitem__rank {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #e5e7eb, #9ca3af);
  color: #1f2937;
  font-weight: 800;
  font-size: 0.9rem;
  border: 2px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
.rowitem__avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  font-size: 1.02rem;
}
.rowitem__meta {
  text-align: right;
  min-width: 130px;
}
.score__val {
  font-weight: 900;
  font-size: 1.08rem;
  color: var(--success);
}
.score__label {
  display: block;
  font-size: 0.74rem;
  color: var(--muted);
  letter-spacing: 0.4px;
  margin-top: 3px;
}
.time {
  font-size: 0.76rem;
  color: var(--muted);
  margin-top: 4px;
}

/* highlight "vị trí của tôi" */
.my-rank-highlight {
  animation: hl 0.6s ease-in-out;
  background: linear-gradient(
    90deg,
    rgba(79, 70, 229, 0.15),
    rgba(79, 70, 229, 0.08),
    rgba(79, 70, 229, 0.15)
  ) !important;
  border-left: 4px solid var(--accent1) !important;
  box-shadow: 0 6px 18px rgba(79, 70, 229, 0.2) !important;
}
@keyframes hl {
  50% {
    transform: scale(1.02);
    box-shadow: 0 8px 24px rgba(79, 70, 229, 0.25);
  }
}

/* ===== Controls ===== */
.lb__controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  margin: 18px 18px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 14px;
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.lb__end {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.92rem;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .lb {
    margin: 12px;
    margin-top: calc(var(--nav-h) + 12px);
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
    gap: 16px;
  }
  .podium--center {
    transform: none;
  }
  .podium--center:hover {
    transform: none;
  }
  .rowitem {
    grid-template-columns: 40px 48px 1fr;
    gap: 12px;
  }
  .rowitem__meta {
    grid-column: 1/-1;
    text-align: center;
    margin-top: 8px;
  }
}
</style>
