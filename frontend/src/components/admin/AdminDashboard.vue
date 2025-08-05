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

    <!-- Pending Quizzes Section -->
    <div class="content-section mb-5">
      <div class="section-header">
        <h3 class="section-title">
          <i class="bi bi-hourglass-split me-2"></i>
          Quiz đang chờ duyệt
        </h3>
        <div class="section-badge">{{ pendingQuizzes.length }} quiz</div>
      </div>
      
      <div class="table-container">
        <div class="table-responsive">
          <table class="modern-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Tiêu đề</th>
                <th>Người tạo</th>
                <th>Ngày tạo</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(quiz, index) in pendingQuizzes" :key="quiz.id" class="table-row">
                <td class="text-center">{{ index + 1 }}</td>
                <td>
                  <div class="quiz-title">{{ quiz.title }}</div>
                </td>
                <td>
                  <div class="creator-info">
                    <div class="creator-name">{{ quiz.creatorName }}</div>
                  </div>
                </td>
                <td>
                  <div class="date-info">
                    {{ formatDate(quiz.createdAt) }}
                  </div>
                </td>
                <td>
                  <span class="status-badge status-pending">
                    <i class="bi bi-clock me-1"></i>
                    Chờ duyệt
                  </span>
                </td>
                <td>
                  <button class="action-btn action-view" @click="openModal(quiz)">
                    <i class="bi bi-eye"></i>
                    Xem chi tiết
                  </button>
                </td>
              </tr>
              <tr v-if="pendingQuizzes.length === 0">
                <td colspan="6" class="text-center empty-state">
                  <div class="empty-icon">
                    <i class="bi bi-check-circle"></i>
                  </div>
                  <div class="empty-text">Không có quiz nào chờ duyệt</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
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

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-file-text me-2"></i>
            Chi tiết Quiz
          </h5>
          <button type="button" class="modal-close" @click="closeModal">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body" v-if="selectedQuiz">
          <div class="quiz-details">
            <div class="detail-item">
              <label>Tiêu đề:</label>
              <span>{{ selectedQuiz.title }}</span>
            </div>
            <div class="detail-item">
              <label>Người tạo:</label>
              <span>{{ selectedQuiz.creatorName }}</span>
            </div>
            <div class="detail-item">
              <label>Ngày tạo:</label>
              <span>{{ formatDate(selectedQuiz.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <label>Danh mục:</label>
              <span>{{ selectedQuiz.categoryName || 'Không rõ' }}</span>
            </div>
            <div class="detail-item">
              <label>Tags:</label>
              <span>{{ selectedQuiz.tags?.length ? selectedQuiz.tags.join(', ') : 'Không có' }}</span>
            </div>
            <div class="detail-item">
              <label>Trạng thái:</label>
              <span class="status-badge" :class="selectedQuiz.isPublic ? 'status-public' : 'status-private'">
                {{ selectedQuiz.isPublic ? 'Công khai' : 'Riêng tư' }}
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">
            <i class="bi bi-x me-1"></i>
            Đóng
          </button>
          <button class="btn btn-success" @click="approveQuiz(selectedQuiz.id)">
            <i class="bi bi-check-lg me-1"></i>
            Duyệt
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/axios';
import DashboardChart from './DashboardChart.vue';

const pendingQuizzes = ref([]);
const dashboardStats = ref(null);
const statsCards = ref([]);
const selectedQuiz = ref(null);
const showModal = ref(false);

function openModal(quiz) {
  selectedQuiz.value = quiz;
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

onMounted(async () => {
  try {
    const [pendingRes, dashboardRes] = await Promise.all([
      api.get('/admin/dashboard/pending-quizzes'),
      api.get('/admin/dashboard'),
    ]);

    pendingQuizzes.value = pendingRes.data;
    dashboardStats.value = dashboardRes.data;

    updateStatsCards();
  } catch (err) {
    console.error("Lỗi khi lấy dữ liệu dashboard:", err);
  }
});

async function approveQuiz(quizId) {
  if (!quizId) return;

  try {
    await api.put(`/admin/quizzes/${quizId}/approve`);

    // Xóa quiz khỏi danh sách pending
    pendingQuizzes.value = pendingQuizzes.value.filter(q => q.id !== quizId);

    // Ẩn modal
    closeModal();

    // Cập nhật lại thống kê
    dashboardStats.value.pendingApproval--;

    alert('Quiz đã được duyệt thành công!');
  } catch (error) {
    console.error('Lỗi khi duyệt quiz:', error);
    alert('Có lỗi xảy ra khi duyệt quiz!');
  }
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
      label: 'Quiz chờ duyệt',
      value: dashboardStats.value.pendingApproval,
      subLabel: 'Chưa xét duyệt',
      cardClass: 'stat-card-info',
      icon: 'bi bi-hourglass-split'
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

/* Content Sections */
.content-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.section-badge {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

/* Modern Table */
.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.modern-table {
  width: 100%;
  border-collapse: collapse;
}

.modern-table th {
  background: linear-gradient(45deg, #f8f9fa, #e9ecef);
  padding: 1rem;
  font-weight: 600;
  color: #495057;
  border: none;
}

.modern-table td {
  padding: 1rem;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: middle;
}

.table-row:hover {
  background: #f8f9fa;
}

.quiz-title {
  font-weight: 500;
  color: #333;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.creator-name {
  font-weight: 500;
  color: #666;
}

.date-info {
  color: #666;
  font-size: 0.9rem;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-public {
  background: #d1ecf1;
  color: #0c5460;
}

.status-private {
  background: #f8d7da;
  color: #721c24;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-view {
  background: #007bff;
  color: white;
}

.action-view:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.empty-state {
  padding: 3rem 1rem;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  color: #28a745;
  margin-bottom: 1rem;
}

.empty-text {
  color: #666;
  font-size: 1.1rem;
}

/* Chart Container */
.chart-container {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1055;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: #f8f9fa;
  color: #333;
}

.modal-body {
  padding: 1.5rem;
  max-height: 60vh;
  overflow-y: auto;
}

.quiz-details {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-item label {
  font-weight: 600;
  color: #495057;
  min-width: 120px;
}

.detail-item span {
  color: #333;
  text-align: right;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover {
  background: #1e7e34;
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
