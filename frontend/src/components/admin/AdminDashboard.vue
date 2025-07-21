<template>
  <div class="container-fluid">
    <!-- Welcome Message -->
    <div class="alert alert-primary shadow-sm mb-4">
      👋 Chào mừng bạn đến với trang quản trị dành cho Admin. Kiểm soát mọi thứ tại đây!
    </div>

    <!-- Section 1: Stats cards -->
    <div class="row">
      <div
        class="col-md-4 mb-3"
        v-for="card in statsCards"
        :key="card.label"
      >
        <div class="card shadow-sm border-start border-4" :class="card.borderClass">
          <div class="card-body">
            <h6 class="text-muted">{{ card.label }}</h6>
            <h3>{{ card.value }}</h3>
            <small class="text-secondary">{{ card.subLabel }}</small>
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
            </tr>
          </thead>
          <tbody>
            <tr v-for="(quiz, index) in pendingQuizzes" :key="quiz.id">
              <td>{{ index + 1 }}</td>
              <td>{{ quiz.title }}</td>
              <td>{{ quiz.creatorName }}</td>
              <td>{{ quiz.createdAt.slice(0, 10) }}</td>
              <td><span class="badge bg-warning text-dark">Chờ duyệt</span></td>
            </tr>

          </tbody>
        </table>
      </div>
    </div>

    <!-- Section 3: Charts (placeholder) -->
    <div class="card shadow-sm">
      <div class="card-header bg-light">
        <strong>Biểu đồ hoạt động (Đang phát triển)</strong>
      </div>
      <div class="card-body text-muted">
        Biểu đồ thống kê sẽ được hiển thị ở đây. (Có thể dùng Chart.js hoặc ApexCharts sau)
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios';

const pendingQuizzes = ref([]);

onMounted(async () => {
  try {
    const res = await axios.get('/api/admin/dashboard/pending-quizzes');
    pendingQuizzes.value = res.data;
  } catch (err) {
    console.error("Lỗi khi lấy quiz chờ duyệt:", err);
  }
});

// Danh sách card ban đầu, giá trị value sẽ cập nhật sau (mới lấy được API của users và quizzes)
const statsCards = ref([
  { label: 'Người dùng', value: 1532, subLabel: 'Tổng cộng', borderClass: 'border-primary' },
  { label: 'Quiz đã tạo', value: 786, subLabel: 'Bao gồm cả public/private', borderClass: 'border-success' },
  { label: 'Lượt làm Quiz', value: 14230, subLabel: 'Tất cả attempts', borderClass: 'border-warning' },
  { label: 'Báo cáo vi phạm', value: 12, subLabel: 'Chưa xử lý', borderClass: 'border-danger' },
  { label: 'Quiz chờ duyệt', value: 28, subLabel: 'Chưa xét duyệt', borderClass: 'border-info' },
  { label: 'Danh mục', value: 18, subLabel: 'Chủ đề khác nhau', borderClass: 'border-secondary' }
])
</script>
