<template>
  <div class="admin-dashboard">
    <!-- Header bar -->
    <div class="page-header">
      <div class="page-title">
        <i class="bi bi-shield-check"></i>
        <div>
          <h1>Admin Dashboard</h1>
          <p>Giám sát tổng quan hệ thống QuizMaster</p>
        </div>
      </div>
      <div class="page-actions">
        <button class="btn btn-outline" @click="refresh()">
          <i class="bi bi-arrow-clockwise"></i>
          Làm mới
        </button>
      </div>
    </div>

    <!-- KPIs -->
    <div class="kpi-grid">
      <div v-for="card in statsCards" :key="card.label" class="kpi-card">
        <div class="kpi-icon">
          <i :class="card.icon"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-label">{{ card.label }}</div>
          <div class="kpi-value">{{ card.value }}</div>
          <div class="kpi-sub">{{ card.subLabel }}</div>
        </div>
      </div>
    </div>

    <!-- Chart tổng quan -->
    <div class="panel">
      <div class="panel-header">
        <h3>
          <i class="bi bi-graph-up"></i>
          Biểu đồ hoạt động
        </h3>
      </div>
      <div class="panel-body">
        <DashboardChart v-if="dashboardStats" :key="themeKey" :stats="dashboardStats" />
      </div>
    </div>

    <!-- Lưới 2 cột: Attempts theo giờ + Hoạt động gần đây / Hành động nhanh -->
    <div class="grid-2">
      <div class="panel">
        <div class="panel-header">
          <h3>
            <i class="bi bi-clock-history"></i>
            Lượt làm hôm nay theo giờ
          </h3>
        </div>
        <div class="panel-body">
          <div class="chart-fixed">
            <canvas ref="attemptsTodayCanvas"></canvas>
          </div>
          <div class="top-quizzes">
            <h4>Top 5 quiz hot hôm nay</h4>
            <div v-if="topQuizzesToday.length === 0" class="empty-top">Chưa có dữ liệu hôm nay</div>
            <ul v-else>
              <li v-for="q in topQuizzesToday" :key="q.title">
                <span class="q-title">{{ q.title }}</span>
                <span class="q-count">{{ q.count }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>
            <i class="bi bi-activity"></i>
            Hoạt động gần đây
          </h3>
        </div>
        <div class="panel-body recent-list">
          <div v-if="recentActivities.length === 0" class="empty-recent">Chưa có hoạt động gần đây</div>
          <div v-for="item in recentActivities" :key="item.id" class="recent-item">
            <div class="recent-icon">
              <i :class="getActivityIcon(item.type)"></i>
            </div>
            <div class="recent-content">
              <div class="recent-title">{{ item.title }}</div>
              <div class="recent-desc">{{ item.description }}</div>
            </div>
            <div class="recent-time">{{ formatTimeAgo(item.timestamp) }}</div>
          </div>

          <!-- Pagination -->
          <div class="pagination-bar" v-if="activityTotalPages > 1">
            <button class="pg-btn" :disabled="activityPage === 0" @click="prevActivityPage">
              <i class="bi bi-chevron-left"></i>
            </button>
            <span>Trang {{ activityPage + 1 }} / {{ activityTotalPages }}</span>
            <button class="pg-btn" :disabled="activityPage >= activityTotalPages - 1" @click="nextActivityPage">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <div class="quick-actions">
            <button class="qa-btn" @click="goCreateQuiz"><i class="bi bi-plus-lg"></i> Tạo quiz</button>
            <button class="qa-btn" @click="goReports"><i class="bi bi-flag"></i> Báo cáo</button>
            <button class="qa-btn" @click="goCategories"><i class="bi bi-tags"></i> Danh mục</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import api from '@/utils/axios';
import { adminService } from '@/services/adminService'
import DashboardChart from './DashboardChart.vue';
import { useThemeStore } from '@/stores/theme'
import { useRouter } from 'vue-router'

// Chart.js cho biểu đồ attempts hôm nay
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale
} from 'chart.js';
ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const dashboardStats = ref(null);
const statsCards = ref([]);
const themeStore = useThemeStore()
const themeKey = computed(() => (themeStore.isDarkMode ? 'dark' : 'light'))
const router = useRouter()

// Recent activities + attempts theo giờ trong ngày
const recentActivities = ref([])
const attemptsByHour = ref(Array.from({ length: 24 }, () => 0))
const attemptsTodayCanvas = ref(null)
let attemptsChartInstance = null
let refreshTimer = null
const activityPage = ref(0)
const activitySize = ref(10)
const activityTotalPages = ref(1)
// Today stats
const todayAttemptsCount = ref(0)
const todayReportsCount = ref(0)
const topQuizzesToday = ref([])

onMounted(async () => {
  try {
    const [dashboardRes] = await Promise.all([
      api.get('/admin/dashboard'),
    ]);

    dashboardStats.value = dashboardRes.data;

    updateStatsCards();
    await loadRecentActivities();
    await loadTodayStats();
    await loadAttemptsTodayFromBE();
    buildAttemptsToday();
    // Auto refresh hoạt động mỗi 60s
    refreshTimer = setInterval(async () => {
      await loadRecentActivities();
      await loadTodayStats();
      await loadAttemptsTodayFromBE();
      updateAttemptsTodayChart();
    }, 60000)
  } catch (err) {
    console.error("Lỗi khi lấy dữ liệu dashboard:", err);
  }
});

onUnmounted(() => {
  if (attemptsChartInstance) attemptsChartInstance.destroy()
  if (refreshTimer) clearInterval(refreshTimer)
})

function refresh() {
  window.location.reload()
}

function updateStatsCards() {
  if (!dashboardStats.value) return;

  statsCards.value = [
    {
      label: 'Người dùng',
      value: dashboardStats.value.totalUsers,
      subLabel: 'Tổng cộng',
      cardClass: 'stat-card-primary',
      icon: 'bi bi-people-fill'
    },
    {
      label: 'Quiz đã tạo',
      value: dashboardStats.value.totalQuizzes,
      subLabel: 'Bao gồm cả public/private',
      cardClass: 'stat-card-success',
      icon: 'bi bi-journal-code'
    },
    {
      label: 'Lượt làm Quiz',
      value: dashboardStats.value.totalAttempts,
      subLabel: 'Tất cả attempts',
      cardClass: 'stat-card-warning',
      icon: 'bi bi-play-circle'
    },
    {
      label: 'Báo cáo vi phạm',
      value: dashboardStats.value.totalReports,
      subLabel: 'Chưa xử lý',
      cardClass: 'stat-card-danger',
      icon: 'bi bi-flag-fill'
    },
    {
      label: 'Danh mục',
      value: dashboardStats.value.totalCategories,
      subLabel: 'Chủ đề khác nhau',
      cardClass: 'stat-card-secondary',
      icon: 'bi bi-tags-fill'
    }
  ];
}

async function loadRecentActivities() {
  try {
    const res = await api.get('/activities/recent', { params: { page: activityPage.value, size: activitySize.value } })
    const list = (res.data?.content || res.data || []).map(item => ({
      id: item.id,
      type: mapActivityType(item.activityType),
      title: item.fullName || item.activityType,
      description: item.activityType,
      timestamp: item.activityTime
    }))
    recentActivities.value = list
    if (res.data?.totalPages != null) activityTotalPages.value = res.data.totalPages
    if (res.data?.number != null) activityPage.value = res.data.number
    // recompute attempts by hour (today)
    const today = new Date()
    const arr = Array.from({ length: 24 }, () => 0)
    for (const a of list) {
      if (a.type === 'attempt' && a.timestamp) {
        const d = new Date(a.timestamp)
        if (
          d.getFullYear() === today.getFullYear() &&
          d.getMonth() === today.getMonth() &&
          d.getDate() === today.getDate()
        ) {
          arr[d.getHours()] += 1
        }
      }
    }
    attemptsByHour.value = arr
  } catch (e) {
    // Không chặn dashboard nếu lỗi
    console.error('Lỗi tải hoạt động gần đây:', e)
  }
}

function buildAttemptsToday() {
  if (!attemptsTodayCanvas.value) return
  // Destroy instance cũ nếu có để tránh leak và canvas giãn vô hạn
  if (attemptsChartInstance) {
    try { attemptsChartInstance.destroy() } catch {}
    attemptsChartInstance = null
  }
  const ctx = attemptsTodayCanvas.value.getContext('2d')
  // Chọn màu theo theme thực tế để đảm bảo tương phản
  const theme = document.documentElement.getAttribute('data-theme') || (themeStore.isDarkMode ? 'dark' : 'light')
  const isDark = theme === 'dark'
  const textColor = isDark ? '#e5e7eb' : '#1f2937'
  const gridColor = isDark ? 'rgba(255,255,255,0.18)' : 'rgba(0,0,0,0.08)'
  attemptsChartInstance = new ChartJS(ctx, {
    type: 'line',
    data: {
      labels: Array.from({ length: 24 }, (_, i) => `${i}h`),
      datasets: [{
        label: 'Attempts',
        data: attemptsByHour.value,
        borderColor: '#60a5fa',
        backgroundColor: 'rgba(96,165,250,0.15)',
        tension: 0.35,
        fill: true,
        pointRadius: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { ticks: { color: textColor, font: { size: 12 } }, grid: { color: gridColor } },
        y: {
          beginAtZero: true,
          suggestedMax: Math.max(1, Math.ceil(Math.max(...attemptsByHour.value, 1))),
          ticks: {
            color: textColor,
            font: { size: 12 },
            stepSize: 1,
            precision: 0,
            callback: (value) => Number.isInteger(value) ? value : ''
          },
          grid: { color: gridColor }
        }
      }
    }
  })
}

function updateAttemptsTodayChart() {
  if (!attemptsChartInstance) return
  attemptsChartInstance.data.datasets[0].data = attemptsByHour.value
  attemptsChartInstance.options.scales.y.suggestedMax = Math.max(1, Math.ceil(Math.max(...attemptsByHour.value, 1)))
  attemptsChartInstance.options.scales.y.ticks.stepSize = 1
  attemptsChartInstance.options.scales.y.ticks.precision = 0
  attemptsChartInstance.update()
}

// Gọi API BE mới để lấy attempts theo giờ
async function loadAttemptsTodayFromBE() {
  try {
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone
    const res = await adminService.getAttemptsTodayByHour(tz)
    if (Array.isArray(res?.countsByHour) && res.countsByHour.length === 24) {
      attemptsByHour.value = res.countsByHour.slice()
    }
  } catch (e) {
    // fallback giữ nguyên logic cũ
    console.warn('Không lấy được attempts-today từ BE, dùng fallback từ activities.', e)
  }
}

function nextActivityPage() {
  if (activityPage.value < activityTotalPages.value - 1) {
    activityPage.value += 1
    loadRecentActivities()
  }
}

function prevActivityPage() {
  if (activityPage.value > 0) {
    activityPage.value -= 1
    loadRecentActivities()
  }
}

// Thống kê hôm nay + top quiz hôm nay
async function loadTodayStats() {
  try {
    const res = await api.get('/activities/recent', { params: { page: 0, size: 1000 } })
    const data = (res.data?.content || res.data || [])
    const today = new Date()
    let attempts = 0
    let reports = 0
    const mapTitleToCount = new Map()
    data.forEach(item => {
      const d = new Date(item.activityTime)
      if (
        d.getFullYear() === today.getFullYear() &&
        d.getMonth() === today.getMonth() &&
        d.getDate() === today.getDate()
      ) {
        if (String(item.activityType).includes('Played Quiz')) {
          attempts += 1
          const prefix = 'Played Quiz: '
          const title = String(item.activityType).startsWith(prefix)
            ? String(item.activityType).substring(prefix.length)
            : String(item.activityType)
          mapTitleToCount.set(title, (mapTitleToCount.get(title) || 0) + 1)
        } else if (String(item.activityType).includes('Reported')) {
          reports += 1
        }
      }
    })
    todayAttemptsCount.value = attempts
    todayReportsCount.value = reports
    const top = Array.from(mapTitleToCount.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([title, count]) => ({ title, count }))
    topQuizzesToday.value = top
    // cập nhật 2 KPI phụ nếu đã render
    augmentTodayKpi()
  } catch (e) {
    console.error('Lỗi tải thống kê hôm nay:', e)
  }
}

function augmentTodayKpi() {
  // chèn/điều chỉnh 2 KPI cuối danh sách nếu chưa có
  const idxAttempt = statsCards.value.findIndex(c => c.key === 'todayAttempts')
  const todayAttemptCard = {
    key: 'todayAttempts',
    label: 'Lượt làm hôm nay',
    value: todayAttemptsCount.value,
    subLabel: 'Theo giờ ở biểu đồ',
    cardClass: 'stat-card-info',
    icon: 'bi bi-speedometer'
  }
  if (idxAttempt >= 0) statsCards.value[idxAttempt] = todayAttemptCard
  else statsCards.value.push(todayAttemptCard)

  const idxReport = statsCards.value.findIndex(c => c.key === 'todayReports')
  const todayReportCard = {
    key: 'todayReports',
    label: 'Báo cáo hôm nay',
    value: todayReportsCount.value,
    subLabel: 'Đã gửi trong ngày',
    cardClass: 'stat-card-danger',
    icon: 'bi bi-flag'
  }
  if (idxReport >= 0) statsCards.value[idxReport] = todayReportCard
  else statsCards.value.push(todayReportCard)
}

function mapActivityType(type) {
  if (type?.includes('Quiz')) return 'quiz'
  if (type?.includes('Report')) return 'report'
  if (type?.includes('Attempt')) return 'attempt'
  return 'user'
}

function getActivityIcon(type) {
  const map = { quiz: 'bi bi-journal-code', report: 'bi bi-flag', attempt: 'bi bi-play-circle', user: 'bi bi-person' }
  return map[type] || 'bi bi-activity'
}

function formatTimeAgo(ts) {
  try {
    const d = new Date(ts)
    const diffMs = Date.now() - d.getTime()
    const m = Math.floor(diffMs / 60000)
    if (m < 1) return 'vừa xong'
    if (m < 60) return `${m} phút trước`
    const h = Math.floor(m / 60)
    if (h < 24) return `${h} giờ trước`
    const dd = Math.floor(h / 24)
    return `${dd} ngày trước`
  } catch { return '' }
}

// Quick actions
function goCreateQuiz() { router.push('/quiz-crud') }
function goReports() { router.push('/admin/reports') }
function goCategories() { router.push('/admin/categories') }
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: 100vh;
  color: var(--text-primary);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title i {
  font-size: 28px;
  color: var(--info-color);
}

.page-title h1 {
  font-size: 22px;
  margin: 0;
}

.page-title p {
  margin: 0;
  font-size: 13px;
  color: #8ea1b2;
}

.page-actions .btn {
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 10px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 6px 18px var(--shadow-color);
}

.kpi-icon i {
  font-size: 22px;
  color: var(--info-color);
}

.kpi-label {
  font-size: 12px;
  color: var(--text-muted);
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
}

.kpi-sub {
  font-size: 12px;
  color: var(--text-secondary);
}

.panel {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 22px var(--shadow-color);
}

.panel-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--card-header-text) !important;
}

.panel-body {
  padding: 16px;
}

.chart-fixed {
  height: 260px;
  position: relative;
  overflow: hidden;
}
.chart-fixed canvas {
  height: 100% !important;
  width: 100% !important;
}

.grid-2 {
  display: grid;
  grid-template-columns: 2fr 1.4fr;
  gap: 16px;
  margin-top: 16px;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.recent-item {
  display: grid;
  grid-template-columns: 32px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}
.recent-icon i { color: var(--info-color); font-size: 18px; }
.recent-title { font-weight: 600; }
.recent-desc { font-size: 12px; color: var(--text-secondary); }
.recent-time { font-size: 12px; color: var(--text-muted); }

.empty-recent { color: var(--text-secondary); font-size: 14px; }

.quick-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}
.qa-btn {
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 8px;
  padding: 8px 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.top-quizzes { margin-top: 12px; }
.top-quizzes h4 { font-size: 14px; margin: 8px 0; color: var(--card-header-text); }
.top-quizzes ul { list-style: none; padding: 0; margin: 0; display: grid; gap: 6px; }
.top-quizzes li { display: flex; justify-content: space-between; border-bottom: 1px dashed var(--border-color); padding-bottom: 4px; }
.top-quizzes .q-title { color: var(--text-secondary); }
.top-quizzes .q-count { font-weight: 700; }

/* Pagination */
.pagination-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.pg-btn {
  appearance: none;
  -webkit-appearance: none;
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 8px;
  width: 32px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  outline: none;
}
.pg-btn:hover:not(:disabled) {
  background: var(--card-header-bg);
}
.pg-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 16px;
  }

  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
