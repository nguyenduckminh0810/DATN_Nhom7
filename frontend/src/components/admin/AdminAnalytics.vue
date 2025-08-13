<template>
  <div class="container-fluid">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="mb-1">
          <i class="bi bi-graph-up text-primary me-2"></i>
          Thống kê & Báo cáo
        </h2>
        <p class="text-muted mb-0">Phân tích chi tiết hoạt động hệ thống</p>
      </div>
      <div class="d-flex gap-2">
        <select v-model="selectedPeriod" class="form-select form-select-sm" style="width: auto;">
          <option value="7">7 ngày qua</option>
          <option value="30">30 ngày qua</option>
          <option value="90">90 ngày qua</option>
          <option value="365">1 năm qua</option>
        </select>
        <button @click="exportExcel" class="btn btn-success btn-sm">
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>
        
      </div>
    </div>

    <!-- (Loại bỏ KPI trùng với Dashboard) -->

    <!-- Charts Row (Trends) -->
    <div class="row mb-4">
      <!-- Attempts series -->
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0">
              <i class="bi bi-graph-up-arrow me-2"></i>
              Lượt làm theo ngày
            </h6>
          </div>
          <div class="card-body">
            <canvas ref="attemptsSeriesCanvas" height="200"></canvas>
          </div>
        </div>
      </div>

      <!-- Users series -->
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0">
              <i class="bi bi-people me-2"></i>
              Người dùng (DAU & mới)
            </h6>
          </div>
          <div class="card-body">
            <canvas ref="usersSeriesCanvas" height="200"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- Detailed Analytics -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-speedometer2 me-2"></i>Điểm trung bình theo ngày</h6>
          </div>
          <div class="card-body">
            <canvas ref="avgScoreCanvas" height="180"></canvas>
          </div>
        </div>
      </div>
    </div>

    <div class="row mb-4">
      <div class="col-md-7">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-bar-chart me-2"></i>Phân phối điểm</h6>
          </div>
          <div class="card-body">
            <canvas ref="scoreHistogramCanvas" height="200"></canvas>
          </div>
        </div>
      </div>
      <div class="col-md-5">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-pie-chart me-2"></i>Tỷ lệ hoàn thành</h6>
          </div>
          <div class="card-body">
            <canvas ref="completionPieCanvas" height="200"></canvas>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-7">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-tags me-2"></i>Phân bố theo danh mục (Top)</h6>
          </div>
          <div class="card-body">
            <canvas ref="categoryDistributionCanvas" height="220"></canvas>
          </div>
        </div>
      </div>
      <div class="col-md-5">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-grid-3x3-gap me-2"></i>HeatMap theo giờ và thứ</h6>
          </div>
          <div class="card-body">
            <div class="heatmap-grid" v-if="heatmapMatrix && heatmapMatrix.length">
              <div class="heatmap-corner"></div>
              <div v-for="h in 24" :key="'h'+h" class="heatmap-hour">{{ (h-1) + 'h' }}</div>
              <template v-for="(row, rIdx) in heatmapMatrix" :key="'r'+rIdx">
                <div class="heatmap-day">{{ weekDays[rIdx] }}</div>
                <div
                  v-for="(val, cIdx) in row"
                  :key="'c'+rIdx+'-'+cIdx"
                  class="heat-cell"
                  :style="{ backgroundColor: heatColor(val) }"
                  :title="`${weekDays[rIdx]}, ${cIdx}h: ${val}`"
                ></div>
              </template>
            </div>
            <div v-else class="text-muted">Chưa có dữ liệu.</div>
          </div>
        </div>
      </div>
    </div>

    <!-- (Loại bỏ hoạt động gần đây vì đã có ở Dashboard) -->
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import Chart from 'chart.js/auto'
import api from '@/utils/axios'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/vi'

dayjs.extend(relativeTime)
dayjs.locale('vi')

// ✅ REACTIVE DATA
const selectedPeriod = ref(30)
// Canvas refs
const attemptsSeriesCanvas = ref(null)
const usersSeriesCanvas = ref(null)
const avgScoreCanvas = ref(null)
const scoreHistogramCanvas = ref(null)
const completionPieCanvas = ref(null)
const categoryDistributionCanvas = ref(null)

// Chart instances
let attemptsSeriesChart = null
let usersSeriesChart = null
let avgScoreChart = null
let scoreHistogramChart = null
let completionPieChart = null
let categoryDistributionChart = null

// Dữ liệu động từ BE
const heatmapMatrix = ref(defaultHeatmap())
const weekDays = ['Th 2','Th 3','Th 4','Th 5','Th 6','Th 7','CN']

// ✅ METHODS
const loadAnalytics = async () => { await refreshAll() }

const updateStats = (data) => {}

const updateCharts = () => {}

const initCharts = () => {
  if (attemptsSeriesCanvas.value) {
    attemptsSeriesChart = new Chart(attemptsSeriesCanvas.value.getContext('2d'), {
      type: 'line',
      data: { labels: [], datasets: [{ label: 'Attempts', data: [], borderColor: '#2563eb', backgroundColor: 'rgba(37, 99, 235, 0.15)', fill: true, tension: 0.35 }] },
      options: { 
        responsive: true, 
        maintainAspectRatio: false, 
        plugins: { legend: { display: false } }, 
        scales: { 
          y: { 
            beginAtZero: true,
            ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' }
          } 
        } 
      }
    })
  }
  if (usersSeriesCanvas.value) {
    usersSeriesChart = new Chart(usersSeriesCanvas.value.getContext('2d'), {
      type: 'line',
      data: { labels: [], datasets: [
        { label: 'Active Users', data: [], borderColor: '#10b981', backgroundColor: 'rgba(16,185,129,0.15)', fill: true, tension: 0.35 },
        { label: 'New Users', data: [], borderColor: '#f59e0b', backgroundColor: 'rgba(245,158,11,0.15)', fill: true, tension: 0.35 }
      ] },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' } } } }
    })
  }
  if (avgScoreCanvas.value) {
    avgScoreChart = new Chart(avgScoreCanvas.value.getContext('2d'), {
      type: 'line',
      data: { labels: [], datasets: [{ label: 'Điểm trung bình (%)', data: [], borderColor: '#8b5cf6', backgroundColor: 'rgba(139,92,246,0.15)', fill: true, tension: 0.35 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, max: 100, ticks: { stepSize: 10 } } } }
    })
  }
  if (scoreHistogramCanvas.value) {
    scoreHistogramChart = new Chart(scoreHistogramCanvas.value.getContext('2d'), {
      type: 'bar',
      data: { labels: [], datasets: [{ label: 'Số lượt', data: [], backgroundColor: '#60a5fa' }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' } } } }
    })
  }
  if (completionPieCanvas.value) {
    completionPieChart = new Chart(completionPieCanvas.value.getContext('2d'), {
      type: 'doughnut',
      data: { labels: ['Hoàn thành', 'Chưa hoàn thành'], datasets: [{ data: [0, 0], backgroundColor: ['#22c55e', '#ef4444'] }] },
      options: { responsive: true, maintainAspectRatio: false }
    })
  }
  if (categoryDistributionCanvas.value) {
    categoryDistributionChart = new Chart(categoryDistributionCanvas.value.getContext('2d'), {
      type: 'bar',
      data: { labels: [], datasets: [{ label: 'Lượt làm', data: [], backgroundColor: '#0ea5e9' }] },
      options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, scales: { x: { beginAtZero: true, ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' } } } }
    })
  }
}

const exportReport = () => {
  // Chưa có API export: để trống, nút vẫn hiện nhằm mục đích UX (sẽ kết nối BE sau)
}

async function exportExcel() {
  try {
    const { from, to } = periodRange(selectedPeriod.value)
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone
    const url = `/admin/analytics/export/xlsx?from=${from}&to=${to}&tz=${encodeURIComponent(tz)}&bins=20&topLimit=10&minAttempts=5`
    const res = await api.get(url, { responseType: 'blob' })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `analytics-${from}_${to}.xlsx`
    a.click()
    URL.revokeObjectURL(a.href)
  } catch (e) {
    console.error('Xuất Excel thất bại', e)
  }
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

const getActivityClass = (type) => {
  const classes = { user: 'bg-primary', quiz: 'bg-success', attempt: 'bg-warning', report: 'bg-danger' }
  return classes[type] || 'bg-secondary'
}

const formatTimeAgo = (timestamp) => {
  return dayjs(timestamp).fromNow()
}

// ✅ WATCHERS
watch(selectedPeriod, () => { refreshAll() })

// ✅ MOUNTED
onMounted(() => {
  initCharts()
  loadAnalytics()
})

// Helpers & data loaders for new BE endpoints
function periodRange(days) {
  const to = new Date()
  const from = new Date()
  from.setDate(to.getDate() - Number(days) + 1)
  const pad = (n) => String(n).padStart(2, '0')
  const toStr = `${to.getFullYear()}-${pad(to.getMonth()+1)}-${pad(to.getDate())}`
  const fromStr = `${from.getFullYear()}-${pad(from.getMonth()+1)}-${pad(from.getDate())}`
  return { from: fromStr, to: toStr }
}

async function refreshAll() {
  const { from, to } = periodRange(selectedPeriod.value)
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone
  await Promise.all([
    loadTrends(from, to, tz),
    loadQuality(from, to, tz),
    loadDistribution(from, to, tz)
  ])
}

watch(selectedPeriod, () => { refreshAll() })

async function loadTrends(from, to, tz) {
  try {
    const [attempts, users] = await Promise.all([
      api.get('/admin/analytics/stats/attempts-series', { params: { from, to, tz } }),
      api.get('/admin/analytics/stats/users-series', { params: { from, to, tz } })
    ])
    if (attemptsSeriesChart) {
      attemptsSeriesChart.data.labels = attempts.data?.labels || []
      attemptsSeriesChart.data.datasets[0].data = attempts.data?.data || []
      attemptsSeriesChart.update()
    }
    if (usersSeriesChart) {
      usersSeriesChart.data.labels = users.data?.labels || []
      usersSeriesChart.data.datasets[0].data = users.data?.activeUsers || []
      usersSeriesChart.data.datasets[1].data = users.data?.newUsers || []
      usersSeriesChart.update()
    }
  } catch (e) { console.warn('Trends API chưa sẵn sàng', e) }
}

async function loadQuality(from, to, tz) {
  try {
    const [quality, histogram, completion] = await Promise.all([
      api.get('/admin/analytics/stats/quality-series', { params: { from, to, tz } }),
      api.get('/admin/analytics/stats/score-histogram', { params: { from, to, bins: 20 } }),
      api.get('/admin/analytics/stats/completion', { params: { from, to } })
    ])
    if (avgScoreChart) {
      avgScoreChart.data.labels = quality.data?.labels || []
      avgScoreChart.data.datasets[0].data = quality.data?.avgScore || []
      avgScoreChart.update()
    }
    if (scoreHistogramChart) {
      const labels = (histogram.data?.bins || []).map(b => `${b.min}-${b.max}`)
      const values = (histogram.data?.bins || []).map(b => b.count)
      scoreHistogramChart.data.labels = labels
      scoreHistogramChart.data.datasets[0].data = values
      scoreHistogramChart.update()
    }
    if (completionPieChart) {
      const attempts = completion.data?.attempts ?? 0
      const completed = completion.data?.completed ?? 0
      completionPieChart.data.datasets[0].data = [completed, Math.max(0, attempts - completed)]
      completionPieChart.update()
    }
  } catch (e) { console.warn('Quality API chưa sẵn sàng', e) }
}

async function loadDistribution(from, to, tz) {
  try {
    const [category, heatmap] = await Promise.all([
      api.get('/admin/analytics/stats/category-distribution', { params: { from, to, limit: 10 } }),
      api.get('/admin/analytics/stats/heatmap', { params: { from, to, tz } })
    ])
    if (categoryDistributionChart) {
      categoryDistributionChart.data.labels = (category.data?.items || []).map(i => i.name)
      categoryDistributionChart.data.datasets[0].data = (category.data?.items || []).map(i => i.count)
      categoryDistributionChart.update()
    }
    const m = heatmap.data?.matrix
    heatmapMatrix.value = (Array.isArray(m) && m.length) ? m : defaultHeatmap()
    if (!Array.isArray(m) || !m.length) {
      console.warn('Heatmap trả về rỗng hoặc không đúng cấu trúc', heatmap.data)
    } else {
      console.debug('Heatmap matrix', heatmapMatrix.value)
    }
  } catch (e) { console.warn('Distribution API chưa sẵn sàng', e) }
}

// Color scale for heatmap cells
function heatColor(value) {
  const flat = (heatmapMatrix.value || []).flat()
  const max = Math.max(1, ...flat)
  const ratio = Math.min(1, (value || 0) / max)
  const alpha = 0.15 + ratio * 0.85
  return `rgba(59,130,246,${alpha.toFixed(2)})`
}

function defaultHeatmap() {
  const rows = []
  for (let r = 0; r < 7; r++) rows.push(new Array(24).fill(0))
  return rows
}
</script>

<style scoped>
.category-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
  color: white;
}

.rank-gold {
  background: linear-gradient(45deg, var(--warning-color), #ffed4e);
}

.rank-silver {
  background: linear-gradient(45deg, #c0c0c0, #e5e5e5);
}

.rank-bronze {
  background: linear-gradient(45deg, #cd7f32, #daa520);
}

.rank-normal {
  background: var(--bg-tertiary);
}

.timeline {
  position: relative;
  padding-left: 30px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--bg-tertiary);
}

.timeline-item {
  position: relative;
  margin-bottom: 20px;
}

.timeline-marker {
  position: absolute;
  left: -22px;
  top: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 2px var(--border-color);
}

.timeline-content {
  background: var(--bg-primary);
  padding: 15px;
  border-radius: 8px;
  margin-left: 10px;
}

.progress {
  background-color: var(--bg-tertiary);
}

.progress-bar {
  transition: width 0.6s ease;
}

/* Heatmap */
.heatmap { display: grid; gap: 6px; }
.heatmap-row { display: grid; grid-template-columns: repeat(24, 1fr); gap: 3px; }
.heat-cell { height: 16px; border-radius: 2px; background: rgba(59,130,246,0.1); }

/* Grid with headers */
.heatmap-grid {
  display: grid;
  grid-template-columns: 40px repeat(24, 1fr);
  grid-auto-rows: 20px;
  gap: 4px;
  align-items: center;
}
.heatmap-corner { height: 0; }
.heatmap-hour { font-size: 10px; color: var(--text-secondary); text-align: center; }
.heatmap-day { font-size: 12px; color: var(--text-secondary); padding-right: 4px; text-align: right; }
</style> 