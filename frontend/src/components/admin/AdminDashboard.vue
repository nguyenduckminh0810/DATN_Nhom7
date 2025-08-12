<template>
  <div class="admin-dashboard">
    <!-- Hero Section -->
    <div class="hero-section mb-5">
      <div class="hero-content">
        <h1 class="hero-title">
          <i class="bi bi-shield-check me-3"></i>
          Admin Dashboard
        </h1>
        <p class="hero-subtitle">
          Quản lý và giám sát toàn bộ hệ thống QuizMaster
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-item">
          <div class="stat-number">{{ dashboardStats?.totalUsers || 0 }}</div>
          <div class="stat-label">Người dùng</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ dashboardStats?.totalQuizzes || 0 }}</div>
          <div class="stat-label">Quiz</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ dashboardStats?.totalAttempts || 0 }}</div>
          <div class="stat-label">Lượt làm</div>
        </div>
      </div>
    </div>

    <!-- Stats Cards Grid -->
    <div class="stats-grid mb-5">
      <div
        class="stat-card"
        v-for="card in statsCards"
        :key="card.label"
        :class="card.cardClass"
      >
        <div class="stat-card-icon">
          <i :class="card.icon"></i>
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ card.value }}</div>
          <div class="stat-card-label">{{ card.label }}</div>
          <div class="stat-card-subtitle">{{ card.subLabel }}</div>
        </div>
        <div class="stat-card-decoration"></div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="content-section">
      <div class="section-header">
        <h3 class="section-title">
          <i class="bi bi-graph-up me-2"></i>
          Biểu đồ hoạt động
        </h3>
      </div>
      <div class="chart-container">
        <DashboardChart v-if="dashboardStats" :stats="dashboardStats" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/axios';
import DashboardChart from './DashboardChart.vue';

const dashboardStats = ref(null);
const statsCards = ref([]);

onMounted(async () => {
  try {
    const [dashboardRes] = await Promise.all([
      api.get('/admin/dashboard'),
    ]);

    dashboardStats.value = dashboardRes.data;

    updateStatsCards();
  } catch (err) {
    console.error("Lỗi khi lấy dữ liệu dashboard:", err);
  }
});

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
</script>

<style scoped>
.admin-dashboard {
  padding: 2rem;
  background: #f8f9fa;
  min-height: 100vh;
}

/* Hero Section */
.hero-section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 3rem;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.hero-content {
  flex: 1;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.hero-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.stat-card-icon {
  position: absolute;
  top: 1rem;
  right: 1rem;
  font-size: 2rem;
  opacity: 0.1;
}

.stat-card-content {
  position: relative;
  z-index: 1;
}

.stat-card-value {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.stat-card-label {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.stat-card-subtitle {
  font-size: 0.9rem;
  color: #666;
}

.stat-card-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  border-radius: 0 0 16px 16px;
}

/* Card Color Variants */
.stat-card-primary .stat-card-value { color: #007bff; }
.stat-card-primary .stat-card-decoration { background: linear-gradient(90deg, #007bff, #0056b3); }

.stat-card-success .stat-card-value { color: #28a745; }
.stat-card-success .stat-card-decoration { background: linear-gradient(90deg, #28a745, #1e7e34); }

.stat-card-warning .stat-card-value { color: #ffc107; }
.stat-card-warning .stat-card-decoration { background: linear-gradient(90deg, #ffc107, #e0a800); }

.stat-card-danger .stat-card-value { color: #dc3545; }
.stat-card-danger .stat-card-decoration { background: linear-gradient(90deg, #dc3545, #c82333); }

.stat-card-info .stat-card-value { color: #17a2b8; }
.stat-card-info .stat-card-decoration { background: linear-gradient(90deg, #17a2b8, #138496); }

.stat-card-secondary .stat-card-value { color: #6c757d; }
.stat-card-secondary .stat-card-decoration { background: linear-gradient(90deg, #6c757d, #545b62); }



/* Chart Container */
.chart-container {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}
/* Responsive Design */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 1rem;
  }
  
  .hero-section {
    flex-direction: column;
    text-align: center;
    gap: 2rem;
  }
  
  .hero-stats {
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .modern-table {
    font-size: 0.9rem;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
}
</style>
