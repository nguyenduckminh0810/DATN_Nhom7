<template>
  <div class="admin-analytics" :class="{ 'is-light': !isDarkMode, 'is-dark': isDarkMode }">
    <div class="page-header">
      <div class="title-card">
        <div class="page-title">
          <div class="icon-badge"><i class="bi bi-graph-up"></i></div>
          <div class="title-text">
            <h1>Thống kê & Báo cáo</h1>
            <p>Phân tích chi tiết hoạt động hệ thống</p>
          </div>
        </div>
      </div>
      <div class="page-actions">
        <select v-model="selectedPeriod" class="filter-select">
          <option value="7">7 ngày qua</option>
          <option value="30">30 ngày qua</option>
          <option value="90">90 ngày qua</option>
          <option value="365">1 năm qua</option>
        </select>
        <button @click="exportExcel" class="btn btn-outline"><i class="bi bi-file-earmark-excel"></i> Xuất Excel</button>
      </div>
    </div>

    <!-- (Loại bỏ KPI trùng với Dashboard) -->

    <!-- Charts Row (Trends) -->
    <div class="row mb-4 row-balanced">
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
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-bar-chart me-2"></i>Phân phối điểm</h6>
          </div>
          <div class="card-body chart-body">
            <canvas ref="scoreHistogramCanvas" height="200"></canvas>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-pie-chart me-2"></i>Tỷ lệ hoàn thành</h6>
          </div>
          <div class="card-body chart-body donut-body">
            <canvas ref="completionPieCanvas" height="200"></canvas>
          </div>
        </div>
      </div>
    </div>

    <div class="row mb-4 row-balanced">
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-tags me-2"></i>Phân bố theo danh mục (Top)</h6>
          </div>
          <div class="card-body chart-body">
            <canvas ref="categoryDistributionCanvas" height="220"></canvas>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0"><i class="bi bi-grid-3x3-gap me-2"></i>HeatMap theo giờ và thứ</h6>
          </div>
          <div class="card-body heatmap-body chart-body">
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
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'
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
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

// Chart theme helpers
function chartTheme() {
  if (isDarkMode.value) {
    return {
      textColor: '#e5e7eb',
      gridColor: 'rgba(255,255,255,0.25)',
      axisColor: 'rgba(255,255,255,0.55)'
    }
  }
  return {
    textColor: '#1f2937',
    gridColor: 'rgba(0,0,0,0.08)',
    axisColor: 'rgba(0,0,0,0.35)'
  }
}

function applyThemeToScales(scales) {
  if (!scales) return
  const { textColor, gridColor, axisColor } = chartTheme()
  Object.keys(scales).forEach((k) => {
    const s = scales[k]
    if (!s) return
    s.ticks = Object.assign({}, s.ticks, { color: textColor, font: { size: 12 } })
    s.grid = Object.assign({}, s.grid, { color: gridColor, lineWidth: 1 })
    s.border = Object.assign({}, s.border, { color: axisColor })
  })
}

function applyThemeToChart(chart) {
  if (!chart) return
  const opts = chart.options || {}
  if (opts.scales) applyThemeToScales(opts.scales)
  if (opts.plugins && opts.plugins.legend && opts.plugins.legend.labels) {
    opts.plugins.legend.labels.color = chartTheme().textColor
  }
  chart.update('none')
}

// Date label formatter (dd/MM/yyyy)
function formatDateLabel(input) {
  const d = dayjs(input)
  return d.isValid() ? d.format('DD/MM/YYYY') : input
}
function formatLabels(labels) {
  return (labels || []).map(formatDateLabel)
}

// ✅ METHODS
const loadAnalytics = async () => { await refreshAll() }

const updateStats = (data) => {}

const updateCharts = () => {}

const initCharts = () => {
  if (attemptsSeriesCanvas.value) {
    attemptsSeriesChart = new Chart(attemptsSeriesCanvas.value.getContext('2d'), {
      type: 'line',
      data: { labels: [], datasets: [{ label: 'Attempts', data: [], borderColor: '#2563eb', backgroundColor: 'rgba(37, 99, 235, 0.15)', fill: true, tension: 0.35 }] },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' } } } }
    })
    applyThemeToChart(attemptsSeriesChart)
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
    applyThemeToChart(usersSeriesChart)
  }
  if (avgScoreCanvas.value) {
    avgScoreChart = new Chart(avgScoreCanvas.value.getContext('2d'), {
      type: 'line',
      data: { labels: [], datasets: [{ label: 'Điểm trung bình (%)', data: [], borderColor: '#8b5cf6', backgroundColor: 'rgba(139,92,246,0.15)', fill: true, tension: 0.35 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, max: 100, ticks: { stepSize: 10 } } } }
    })
    applyThemeToChart(avgScoreChart)
  }
  if (scoreHistogramCanvas.value) {
    scoreHistogramChart = new Chart(scoreHistogramCanvas.value.getContext('2d'), {
      type: 'bar',
      data: { labels: [], datasets: [{ label: 'Số lượt', data: [], backgroundColor: '#60a5fa' }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' } } } }
    })
    applyThemeToChart(scoreHistogramChart)
  }
  if (completionPieCanvas.value) {
    completionPieChart = new Chart(completionPieCanvas.value.getContext('2d'), {
      type: 'doughnut',
      data: { labels: ['Hoàn thành', 'Chưa hoàn thành'], datasets: [{ data: [0, 0], backgroundColor: ['#22c55e', '#ef4444'] }] },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: chartTheme().textColor } } } }
    })
    applyThemeToChart(completionPieChart)
  }
  if (categoryDistributionCanvas.value) {
    categoryDistributionChart = new Chart(categoryDistributionCanvas.value.getContext('2d'), {
      type: 'bar',
      data: { labels: [], datasets: [{ label: 'Lượt làm', data: [], backgroundColor: '#0ea5e9' }] },
      options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, scales: { x: { beginAtZero: true, ticks: { stepSize: 1, precision: 0, callback: (v) => Number.isInteger(v) ? v : '' } } } }
    })
    applyThemeToChart(categoryDistributionChart)
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
  // Re-apply theme when switching modes
  const obs = new MutationObserver(() => {
    [attemptsSeriesChart, usersSeriesChart, avgScoreChart, scoreHistogramChart, completionPieChart, categoryDistributionChart].forEach(applyThemeToChart)
  })
  obs.observe(document.documentElement, { attributes: true, attributeFilter: ['class', 'data-theme'] })
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
      attemptsSeriesChart.data.labels = formatLabels(attempts.data?.labels)
      attemptsSeriesChart.data.datasets[0].data = attempts.data?.data || []
      attemptsSeriesChart.update()
    }
    if (usersSeriesChart) {
      usersSeriesChart.data.labels = formatLabels(users.data?.labels)
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
      avgScoreChart.data.labels = formatLabels(quality.data?.labels)
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
.admin-analytics { padding: 24px; color: var(--text-primary); }
.page-header { display:flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title-card { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 14px; padding: 10px 14px; box-shadow: 0 6px 18px var(--shadow-color); }
.page-title { display:flex; align-items:center; gap:12px; }
.icon-badge { width:44px; height:44px; display:flex; align-items:center; justify-content:center; border-radius:12px; }
.icon-badge i { font-size:22px; color: var(--info-color); }
.title-text h1 { font-size:22px; margin:0; }
.title-text h1::after { content:''; display:block; height:3px; width:80px; margin-top:6px; border-radius:999px; background:linear-gradient(90deg,#667eea 0%, #764ba2 100%); opacity:.6; }
.title-text p { margin:2px 0 0 0; font-size:13px; color: var(--text-secondary); }
.page-actions { display:flex; align-items:center; gap:10px; }
.filter-select { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }
.btn.btn-outline { background: var(--bg-primary); color: var(--text-primary); border:1px solid var(--border-color); border-radius:10px; padding:8px 12px; }

.panel { background: var(--bg-primary); border:1px solid var(--border-color); border-radius: 16px; box-shadow: 0 8px 22px var(--shadow-color); overflow:hidden; }
.panel-body.no-padding { padding: 0; }
.table-wrap { width: 100%; overflow-x: auto; }

/* Dark/Light tweaks */
.admin-analytics .title-text h1 { text-shadow: none; letter-spacing: 0.2px; }
.admin-analytics.is-light .title-card { background:#ffffff; border-color: rgba(2,6,23,0.12); }
.admin-analytics.is-dark .title-card { background: transparent; border-color: rgba(255,255,255,0.18); box-shadow: none; backdrop-filter: none; }
.admin-analytics.is-dark .page-title { background: transparent; box-shadow: none; }
.admin-analytics.is-light .title-text h1 { color:#0b1220; }
.admin-analytics.is-dark .title-text h1 { color:#f8fafc; }
.admin-analytics.is-light .title-text h1::after { opacity:.95; background:linear-gradient(90deg,#4338ca 0%, #7c3aed 100%); }
.admin-analytics.is-dark .title-text h1::after { opacity:.6; }
.admin-analytics.is-dark .title-text p { color:#cbd5e1; }
.admin-analytics.is-dark .icon-badge i { color: var(--info-color); }

/* Balanced rows: equal card heights & spacing */
.row-balanced > [class^='col-'] .card { height: 100%; }
.chart-body { min-height: 260px; display:flex; align-items:center; }
.donut-body { justify-content:center; }

/* Dark mode fix for legacy Bootstrap cards/tables used by charts */
.admin-analytics.is-dark .card { background: transparent; border:1px solid var(--border-color); box-shadow: none; }
/* Increase contrast for headers in dark mode */
.admin-analytics.is-dark .card-header,
.admin-analytics.is-dark .panel-header { 
  background: rgba(255,255,255,0.10);
  border-bottom: 1px solid rgba(255,255,255,0.22);
  color: #f1f5f9;
}
.admin-analytics.is-dark .card-header h6,
.admin-analytics.is-dark .panel-header h6,
.admin-analytics.is-dark .panel-header h3 { 
  color: #f8fafc;
  letter-spacing: .2px;
  font-weight: 700;
}
.admin-analytics.is-dark .card-body { background: transparent; }
.admin-analytics.is-dark .table { color: var(--text-primary); }
.admin-analytics.is-dark .table thead { background: var(--card-header-bg); color: var(--card-header-text); }
.admin-analytics.is-dark .table tbody tr { background: transparent; }
.admin-analytics.is-dark .table tbody tr:hover { background: rgba(102,126,234,0.06); }
.admin-analytics.is-dark .table td, .admin-analytics.is-dark .table th { border-color: var(--border-color); }
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
.heat-cell { height: 100%; border-radius: 3px; background: rgba(59,130,246,0.1); }

/* Grid with headers */
.heatmap-grid {
  display: grid;
  grid-template-columns: 48px repeat(24, minmax(12px, 1fr));
  grid-auto-rows: clamp(18px, 2.1vw, 28px);
  gap: clamp(3px, 0.45vw, 6px);
  align-items: center;
  width: 100%;
}
.heatmap-body { padding-top: 10px; padding-bottom: 10px; }
.admin-analytics .card .card-body.heatmap-body { min-height: 300px; display:flex; align-items:flex-start; }

/* Cleanup equal-width override (reverted to default grid) */
.heatmap-corner { height: 0; }
.heatmap-hour { font-size: clamp(10px, 0.8vw, 12px); color: var(--text-secondary); text-align: center; }
.heatmap-day { font-size: clamp(11px, 0.9vw, 13px); color: var(--text-secondary); padding-right: 6px; text-align: right; }
</style> 