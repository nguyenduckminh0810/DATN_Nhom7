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
        <button @click="exportReport" class="btn btn-outline-success">
          <i class="bi bi-download me-2"></i>
          Xuất báo cáo
        </button>
      </div>
    </div>

    <!-- Overview Stats -->
    <div class="row mb-4">
      <div class="col-md-3" v-for="stat in overviewStats" :key="stat.label">
        <div class="card shadow-sm border-start border-4" :class="stat.borderClass">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <i :class="stat.icon + ' fs-2'" :style="{ color: stat.color }"></i>
              </div>
              <div class="flex-grow-1 ms-3">
                <h4 class="mb-1 fw-bold">{{ stat.value }}</h4>
                <p class="mb-0 text-muted small">{{ stat.label }}</p>
                <small class="text-success" v-if="stat.change > 0">
                  <i class="bi bi-arrow-up"></i> +{{ stat.change }}%
                </small>
                <small class="text-danger" v-else-if="stat.change < 0">
                  <i class="bi bi-arrow-down"></i> {{ stat.change }}%
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="row mb-4">
      <!-- User Growth Chart -->
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0">
              <i class="bi bi-people me-2"></i>
              Tăng trưởng người dùng
            </h6>
          </div>
          <div class="card-body">
            <canvas ref="userGrowthChart" height="200"></canvas>
          </div>
        </div>
      </div>

      <!-- Quiz Activity Chart -->
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0">
              <i class="bi bi-journal-code me-2"></i>
              Hoạt động Quiz
            </h6>
          </div>
          <div class="card-body">
            <canvas ref="quizActivityChart" height="200"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- Detailed Analytics -->
    <div class="row">
      <!-- Top Categories -->
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0">
              <i class="bi bi-tags me-2"></i>
              Danh mục phổ biến
            </h6>
          </div>
          <div class="card-body">
            <div v-for="category in topCategories" :key="category.id" class="d-flex justify-content-between align-items-center mb-3">
              <div class="d-flex align-items-center">
                <div class="category-color me-2" :style="{ backgroundColor: category.color }"></div>
                <span class="fw-medium">{{ category.name }}</span>
              </div>
              <div class="d-flex align-items-center">
                <div class="progress me-2" style="width: 100px; height: 8px;">
                  <div 
                    class="progress-bar" 
                    :style="{ width: category.percentage + '%', backgroundColor: category.color }"
                  ></div>
                </div>
                <small class="text-muted">{{ category.count }} quizzes</small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- User Performance -->
      <div class="col-md-6">
        <div class="card shadow-sm">
          <div class="card-header bg-light">
            <h6 class="mb-0">
              <i class="bi bi-trophy me-2"></i>
              Top người dùng
            </h6>
          </div>
          <div class="card-body">
            <div v-for="(user, index) in topUsers" :key="user.id" class="d-flex justify-content-between align-items-center mb-3">
              <div class="d-flex align-items-center">
                <div class="rank-badge me-2" :class="getRankClass(index)">
                  {{ index + 1 }}
                </div>
                <div class="d-flex align-items-center">
                  <img 
                    v-if="user.avatarUrl" 
                    :src="user.avatarUrl" 
                    class="rounded-circle me-2"
                    width="32" 
                    height="32"
                  />
                  <i v-else class="bi bi-person-circle text-muted me-2"></i>
                  <div>
                    <div class="fw-medium">{{ user.fullName }}</div>
                    <small class="text-muted">{{ user.username }}</small>
                  </div>
                </div>
              </div>
              <div class="text-end">
                <div class="fw-bold">{{ user.quizCount }} quizzes</div>
                <small class="text-muted">{{ user.avgScore }}% avg</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="card shadow-sm mt-4">
      <div class="card-header bg-light">
        <h6 class="mb-0">
          <i class="bi bi-clock-history me-2"></i>
          Hoạt động gần đây
        </h6>
      </div>
      <div class="card-body">
        <div class="timeline">
          <div v-for="activity in recentActivities" :key="activity.id" class="timeline-item">
            <div class="timeline-marker" :class="getActivityClass(activity.type)"></div>
            <div class="timeline-content">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <strong>{{ activity.title }}</strong>
                  <p class="mb-1 text-muted">{{ activity.description }}</p>
                </div>
                <small class="text-muted">{{ formatTimeAgo(activity.timestamp) }}</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import Chart from 'chart.js/auto'
import api from '@/utils/axios'

// ✅ REACTIVE DATA
const selectedPeriod = ref(30)
const userGrowthChart = ref(null)
const quizActivityChart = ref(null)

const overviewStats = ref([
  {
    label: 'Tổng người dùng',
    value: 0,
    change: 12,
    icon: 'bi bi-people-fill',
    color: '#007bff',
    borderClass: 'border-primary'
  },
  {
    label: 'Quiz đã tạo',
    value: 0,
    change: 8,
    icon: 'bi bi-journal-code',
    color: '#28a745',
    borderClass: 'border-success'
  },
  {
    label: 'Lượt làm quiz',
    value: 0,
    change: -3,
    icon: 'bi bi-play-circle',
    color: '#ffc107',
    borderClass: 'border-warning'
  },
  {
    label: 'Điểm trung bình',
    value: '0%',
    change: 5,
    icon: 'bi bi-graph-up',
    color: '#dc3545',
    borderClass: 'border-danger'
  }
])

const topCategories = ref([
  { id: 1, name: 'Toán học', count: 45, percentage: 85, color: '#007bff' },
  { id: 2, name: 'Tiếng Anh', count: 32, percentage: 60, color: '#28a745' },
  { id: 3, name: 'Lịch sử', count: 28, percentage: 52, color: '#ffc107' },
  { id: 4, name: 'Khoa học', count: 25, percentage: 47, color: '#dc3545' },
  { id: 5, name: 'Văn học', count: 20, percentage: 38, color: '#6f42c1' }
])

const topUsers = ref([
  { id: 1, fullName: 'Nguyễn Văn A', username: 'nguyenvana', avatarUrl: null, quizCount: 15, avgScore: 85 },
  { id: 2, fullName: 'Trần Thị B', username: 'tranthib', avatarUrl: null, quizCount: 12, avgScore: 78 },
  { id: 3, fullName: 'Lê Văn C', username: 'levanc', avatarUrl: null, quizCount: 10, avgScore: 92 },
  { id: 4, fullName: 'Phạm Thị D', username: 'phamthid', avatarUrl: null, quizCount: 8, avgScore: 76 },
  { id: 5, fullName: 'Hoàng Văn E', username: 'hoangvane', avatarUrl: null, quizCount: 6, avgScore: 88 }
])

const recentActivities = ref([
  {
    id: 1,
    type: 'user',
    title: 'Người dùng mới đăng ký',
    description: 'user123 đã đăng ký tài khoản',
    timestamp: new Date(Date.now() - 5 * 60 * 1000)
  },
  {
    id: 2,
    type: 'quiz',
    title: 'Quiz mới được tạo',
    description: 'Quiz "Toán lớp 10" đã được tạo bởi teacher001',
    timestamp: new Date(Date.now() - 15 * 60 * 1000)
  },
  {
    id: 3,
    type: 'attempt',
    title: 'Hoàn thành quiz',
    description: 'student456 đã hoàn thành quiz "Tiếng Anh cơ bản" với điểm 85%',
    timestamp: new Date(Date.now() - 30 * 60 * 1000)
  },
  {
    id: 4,
    type: 'report',
    title: 'Báo cáo vi phạm',
    description: 'Có báo cáo mới về quiz "Lịch sử Việt Nam"',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000)
  }
])

// ✅ METHODS
const loadAnalytics = async () => {
  try {
    const response = await api.get(`/admin/analytics?period=${selectedPeriod.value}`)
    updateStats(response.data)
  } catch (error) {
    console.error('Error loading analytics:', error)
  }
}

const updateStats = (data) => {
  // Update overview stats
  overviewStats.value[0].value = data.totalUsers || 0
  overviewStats.value[1].value = data.totalQuizzes || 0
  overviewStats.value[2].value = data.totalAttempts || 0
  overviewStats.value[3].value = `${data.avgScore || 0}%`
  
  // Update charts
  updateCharts(data)
}

const updateCharts = (data) => {
  // User Growth Chart
  if (userGrowthChart.value) {
    userGrowthChart.value.data.labels = data.userGrowth?.labels || []
    userGrowthChart.value.data.datasets[0].data = data.userGrowth?.data || []
    userGrowthChart.value.update()
  }
  
  // Quiz Activity Chart
  if (quizActivityChart.value) {
    quizActivityChart.value.data.labels = data.quizActivity?.labels || []
    quizActivityChart.value.data.datasets[0].data = data.quizActivity?.data || []
    quizActivityChart.value.update()
  }
}

const initCharts = () => {
  // User Growth Chart
  const userCtx = document.getElementById('userGrowthChart')
  if (userCtx) {
    userGrowthChart.value = new Chart(userCtx, {
      type: 'line',
      data: {
        labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
        datasets: [{
          label: 'Người dùng mới',
          data: [12, 19, 15, 25, 22, 30, 28],
          borderColor: '#007bff',
          backgroundColor: 'rgba(0, 123, 255, 0.1)',
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    })
  }
  
  // Quiz Activity Chart
  const quizCtx = document.getElementById('quizActivityChart')
  if (quizCtx) {
    quizActivityChart.value = new Chart(quizCtx, {
      type: 'bar',
      data: {
        labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
        datasets: [{
          label: 'Quiz được tạo',
          data: [5, 8, 12, 15, 10, 18, 22],
          backgroundColor: '#28a745'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    })
  }
}

const exportReport = () => {
  // TODO: Implement export functionality
  alert('Tính năng xuất báo cáo sẽ được implement sau')
}

// ✅ UTILITY FUNCTIONS
const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

const getActivityClass = (type) => {
  const classes = {
    user: 'bg-primary',
    quiz: 'bg-success',
    attempt: 'bg-warning',
    report: 'bg-danger'
  }
  return classes[type] || 'bg-secondary'
}

const formatTimeAgo = (timestamp) => {
  const now = new Date()
  const diff = now - timestamp
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) return `${minutes} phút trước`
  if (hours < 24) return `${hours} giờ trước`
  return `${days} ngày trước`
}

// ✅ WATCHERS
watch(selectedPeriod, () => {
  loadAnalytics()
})

// ✅ MOUNTED
onMounted(() => {
  loadAnalytics()
  setTimeout(() => {
    initCharts()
  }, 100)
})
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
  background: linear-gradient(45deg, #ffd700, #ffed4e);
}

.rank-silver {
  background: linear-gradient(45deg, #c0c0c0, #e5e5e5);
}

.rank-bronze {
  background: linear-gradient(45deg, #cd7f32, #daa520);
}

.rank-normal {
  background: #6c757d;
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
  background: #e9ecef;
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
  box-shadow: 0 0 0 2px #e9ecef;
}

.timeline-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-left: 10px;
}

.progress {
  background-color: #e9ecef;
}

.progress-bar {
  transition: width 0.6s ease;
}
</style> 