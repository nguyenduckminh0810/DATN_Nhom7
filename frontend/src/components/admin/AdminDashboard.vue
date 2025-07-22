<template>
  <div class="container-fluid">
    <!-- Welcome Message -->
    <div class="alert alert-primary shadow-sm mb-4">
      👋 Chào mừng bạn đến với trang quản trị dành cho Admin. Kiểm soát mọi thứ tại đây!
    </div>

    <!-- Section 1: Stats cards -->
    <div class="row">
      <div
        class="col-lg-4 col-md-6 mb-4"
        v-for="card in statsCards"
        :key="card.label"
      >
        <div
          class="card shadow-sm h-100 border-start border-4"
          :class="[card.borderClass, 'rounded-3', 'hover-card']"
        >
          <div class="card-body d-flex flex-column justify-content-center align-items-start">
            <div class="mb-2">
              <i :class="card.icon" class="fs-4 me-2 text-muted"></i>
              <span class="text-muted small">{{ card.label }}</span>
            </div>
            <h4 class="fw-bold mb-1">{{ card.value }}</h4>
            <div class="text-secondary small">{{ card.subLabel }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Section 2: Pending Quizzes -->
    <div class="card shadow-sm mb-4">
      <div class="card-header bg-light">
        <strong>Quiz đang chờ duyệt</strong>
      </div>
      <div class="card-body p-0">
        <table class="table table-striped table-hover mb-0">
          <thead class="table-light">
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
            <tr v-for="(quiz, index) in pendingQuizzes" :key="quiz.id">
              <td>{{ index + 1 }}</td>
              <td>{{ quiz.title }}</td>
              <td>{{ quiz.creatorName }}</td>
              <td>{{ quiz.createdAt.slice(0, 10) }}</td>
              <td><span class="badge bg-warning text-dark">Chờ duyệt</span></td>
              <td>
                <button class="btn btn-sm btn-info" @click="openModal(quiz)">Xem chi tiết</button>
              </td>

            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết Quiz</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body" v-if="selectedQuiz">
            <p><strong>Tiêu đề:</strong> {{ selectedQuiz.title }}</p>
            <p><strong>Người tạo:</strong> {{ selectedQuiz.creatorName }}</p>
            <p><strong>Ngày tạo:</strong> {{ new Date(selectedQuiz.createdAt).toLocaleString() }}</p>
            <p><strong>Danh mục:</strong> {{ selectedQuiz.categoryName || 'Không rõ' }}</p>
            <p><strong>Tags:</strong> {{ selectedQuiz.tags?.length ? selectedQuiz.tags.join(', ') : 'Không có' }}</p>            <p><strong>Trạng thái:</strong> {{ selectedQuiz.isPublic ? 'Công khai' : 'Riêng tư' }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Đóng</button>
            <button class="btn btn-success" @click="approveQuiz(selectedQuiz.id)">Duyệt</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Section 3: Charts -->
    <div class="card shadow-sm">
      <div class="card-header bg-light">
        <strong>Biểu đồ hoạt động</strong>
      </div>
      <div class="container my-4">
        <DashboardChart v-if="dashboardStats" :stats="dashboardStats" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios';
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

onMounted(async () => {
  try {
    const [pendingRes, dashboardRes] = await Promise.all([
      axios.get('/api/admin/dashboard/pending-quizzes'),
      axios.get('/api/admin/dashboard'),
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
    await axios.put(`/api/admin/quizzes/${quizId}/approve`);

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
      borderClass: 'border-primary',
      icon: 'bi bi-people-fill'
    },
    {
      label: 'Quiz đã tạo',
      value: dashboardStats.value.totalQuizzes,
      subLabel: 'Bao gồm cả public/private',
      borderClass: 'border-success',
      icon: 'bi bi-journal-code'
    },
    {
      label: 'Lượt làm Quiz',
      value: dashboardStats.value.totalAttempts,
      subLabel: 'Tất cả attempts',
      borderClass: 'border-warning',
      icon: 'bi bi-play-circle'
    },
    {
      label: 'Báo cáo vi phạm',
      value: dashboardStats.value.totalReports,
      subLabel: 'Chưa xử lý',
      borderClass: 'border-danger',
      icon: 'bi bi-flag-fill'
    },
    {
      label: 'Quiz chờ duyệt',
      value: dashboardStats.value.pendingApproval,
      subLabel: 'Chưa xét duyệt',
      borderClass: 'border-info',
      icon: 'bi bi-hourglass-split'
    },
    {
      label: 'Danh mục',
      value: dashboardStats.value.totalCategories,
      subLabel: 'Chủ đề khác nhau',
      borderClass: 'border-secondary',
      icon: 'bi bi-tags-fill'
    }
  ];
}
</script>

<style scoped>
.hover-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.hover-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.75rem 1.25rem rgba(0, 0, 0, 0.08);
}
.modal {
  z-index: 1055;
}
</style>
