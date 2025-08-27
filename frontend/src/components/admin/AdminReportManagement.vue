<template>
  <div class="admin-reports-container">
    <div class="reports-header">
      <h2>Quản lý Báo cáo</h2>
      <div class="stats-cards">
        <div class="stat-card pending">
          <h3>{{ stats.pendingReports || 0 }}</h3>
          <p>Chờ xử lý</p>
        </div>
        <div class="stat-card resolved">
          <h3>{{ stats.resolvedReports || 0 }}</h3>
          <p>Đã xử lý</p>
        </div>
        <div class="stat-card rejected">
          <h3>{{ stats.rejectedReports || 0 }}</h3>
          <p>Đã từ chối</p>
        </div>
      </div>
    </div>

    <div class="reports-filters">
      <select v-model="selectedStatus" @change="loadReports" class="status-filter">
        <option value="">Tất cả trạng thái</option>
        <option value="PENDING">Chờ xử lý</option>
        <option value="APPROVED">Đã chấp nhận</option>
        <option value="REJECTED">Đã từ chối</option>
      </select>
    </div>

    <div class="reports-list">
      <!-- Loading state -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải danh sách báo cáo...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-container">
        <p class="error-message">{{ error }}</p>
        <button @click="loadReports" class="retry-btn">Thử lại</button>
      </div>

      <!-- Reports list -->
      <div v-else class="reports-grid">
        <div v-for="report in reports" :key="report.id" class="report-card">
          <div class="report-header">
            <h4>Report #{{ report.id }}</h4>
            <span :class="['status-badge', report.status.toLowerCase()]">
              {{ getStatusLabel(report.status) }}
            </span>
          </div>

          <div class="report-content">
            <div class="report-info">
              <p><strong>Quiz:</strong> {{ report.quizTitle }}</p>
              <p><strong>Người báo cáo:</strong> {{ report.userFullName }}</p>
              <p><strong>Lý do:</strong> {{ report.reason }}</p>
              <p><strong>Thời gian:</strong> {{ formatDate(report.createdAt) }}</p>
            </div>

            <div v-if="report.adminResponse" class="admin-response">
              <p><strong>Phản hồi admin:</strong> {{ report.adminResponse }}</p>
            </div>
          </div>

          <div v-if="report.status === 'PENDING' || report.status === 'pending'" class="report-actions">
            <button @click="handleReportAction(report.id, 'APPROVED')" class="btn-approve">
              Chấp nhận
            </button>
            <button @click="handleReportAction(report.id, 'REJECTED')" class="btn-reject">
              Từ chối
            </button>

            <textarea v-model="adminNotes[report.id]" placeholder="Ghi chú của admin (tùy chọn)" class="admin-note"
              rows="3"></textarea>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="reports.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <p>Không có báo cáo nào</p>
        </div>
      </div>
    </div>

    <!-- Success notification -->
    <div v-if="showSuccess" class="success-notification">
      <p>{{ successMessage }}</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';

export default {
  name: 'AdminReportManagement',
  setup() {
    const reports = ref([]);
    const stats = ref({});
    const loading = ref(false);
    const error = ref('');
    const selectedStatus = ref('');
    const showSuccess = ref(false);
    const successMessage = ref('');

    // THÊM REACTIVE DATA CHO ADMIN NOTES
    const adminNotes = ref({});

    // Load reports
    const loadReports = async () => {
      loading.value = true;
      error.value = '';

      try {
        const params = {};
        if (selectedStatus.value) {
          params.status = selectedStatus.value;
        }

        const response = await axios.get('/api/reports', { params });
        // SỬA ĐỂ LẤY ĐÚNG DATA TỪ BACKEND
        console.log('Raw response:', response.data);
        reports.value = response.data.reports || response.data.content || response.data;

        console.log('Loaded reports:', reports.value);
        console.log('Reports count:', reports.value.length);
      } catch (err) {
        console.error('Error loading reports:', err);
        error.value = 'Không thể tải danh sách báo cáo';
      } finally {
        loading.value = false;
      }
    };

    // Load stats
    const loadStats = async () => {
      try {
        const response = await axios.get('/api/reports/stats');
        stats.value = response.data;
        console.log('Loaded stats:', stats.value);
      } catch (err) {
        console.error('Error loading stats:', err);
      }
    };

    // Handle report action
    const handleReportAction = async (reportId, action) => {
      try {
        const report = reports.value.find(r => r.id === reportId);

        // SỬ DỤNG ADMIN NOTE TỪ REACTIVE DATA
        const adminNote = adminNotes.value[reportId] || '';
        const adminResponse = action === 'APPROVED'
          ? (adminNote || 'Báo cáo hợp lệ, quiz đã bị ẩn')
          : (adminNote || 'Báo cáo không hợp lệ');

        console.log('Sending report action:', {
          reportId,
          action,
          adminResponse,
          adminNote,
          adminNotesValue: adminNotes.value
        });

        const response = await axios.put(`/api/reports/${reportId}/action`, {
          reportId,
          action,
          adminResponse,
          adminNote
        });

        if (response.data.status === 'SUCCESS') {
          // Update report in list
          const updatedReport = reports.value.find(r => r.id === reportId);
          if (updatedReport) {
            updatedReport.status = action;
            updatedReport.adminResponse = adminResponse;
          }

          // Show success message
          successMessage.value = `Đã ${action === 'APPROVED' ? 'chấp nhận' : 'từ chối'} report #${reportId}`;
          showSuccess.value = true;

          // Hide success message after 3 seconds
          setTimeout(() => {
            showSuccess.value = false;
          }, 3000);

          // Reload stats
          loadStats();

          console.log('Report action successful:', response.data);
        }
      } catch (err) {
        console.error('Error handling report action:', err);
        error.value = 'Có lỗi xảy ra khi xử lý báo cáo';
      }
    };

    // Helper functions
    const getStatusLabel = (status) => {
      const labels = {
        'PENDING': 'Chờ xử lý',
        'APPROVED': 'Đã chấp nhận',
        'RESOLVED': 'Đã chấp nhận', // MAP RESOLVED THÀNH APPROVED
        'REJECTED': 'Đã từ chối'
      };
      return labels[status] || status;
    };

    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleString('vi-VN');
    };

    onMounted(() => {
      loadReports();
      loadStats();
    });

    return {
      reports,
      stats,
      loading,
      error,
      selectedStatus,
      showSuccess,
      successMessage,
      adminNotes,
      loadReports,
      handleReportAction,
      getStatusLabel,
      formatDate
    };
  }
};
</script>

<style scoped>
.admin-reports-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.reports-header {
  margin-bottom: 30px;
}

.reports-header h2 {
  color: #333;
  margin-bottom: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  color: white;
}

.stat-card.pending {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}

.stat-card.resolved {
  background: linear-gradient(135deg, #51cf66, #40c057);
}

.stat-card.rejected {
  background: linear-gradient(135deg, #868e96, #6c757d);
}

.stat-card h3 {
  font-size: 24px;
  margin: 0 0 5px 0;
}

.stat-card p {
  margin: 0;
  font-size: 14px;
}

.reports-filters {
  margin-bottom: 20px;
}

.status-filter {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.reports-grid {
  display: grid;
  gap: 20px;
}

.report-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.report-header h4 {
  margin: 0;
  color: #333;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.approved {
  background: #d4edda;
  color: #155724;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

.report-content {
  margin-bottom: 15px;
}

.report-info p {
  margin: 5px 0;
  font-size: 14px;
}

.admin-response {
  margin-top: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.admin-response p {
  margin: 0;
  font-style: italic;
}

.report-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.btn-approve,
.btn-reject {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover {
  background: #218838;
}

.btn-reject {
  background: #dc3545;
  color: white;
}

.btn-reject:hover {
  background: #c82333;
}

.admin-note {
  flex: 1;
  min-width: 200px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
}

.loading-container,
.error-container,
.empty-state {
  text-align: center;
  padding: 40px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.error-message {
  color: #dc3545;
  margin-bottom: 15px;
}

.retry-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.success-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 15px 20px;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
